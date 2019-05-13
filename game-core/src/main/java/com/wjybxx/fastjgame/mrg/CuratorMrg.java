package com.wjybxx.fastjgame.mrg;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.misc.AbstractThreadLifeCycleHelper;
import com.wjybxx.fastjgame.misc.BackoffRetryForever;
import com.wjybxx.fastjgame.misc.LockPathAction;
import com.wjybxx.fastjgame.utils.ConcurrentUtils;
import com.wjybxx.fastjgame.utils.GameUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.utils.CloseableExecutorService;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <h3>Curator</h3>
 * 官网：http://curator.apache.org/getting-started.html<br>
 * 使用zookeeper建议配合ZKUI。
 * <P>
 *     Curator控制器，管理Curator客户端和提供一些简便方法。
 *     设计给游戏逻辑线程使用，因此不是线程安全的。
 * </P>
 *
 * <h3>注意事项</h3>
 * <P>
 *     <li>1.如果操作的是私有数据，则先检查后执行是安全的。</li>
 *     <li>2.如果操作的是共享数据，则可能导致异常，需要加锁。</li>
 *     <li>3.对永久节点的操作，加锁可保证先检查后执行的原子性(防止并发修改)。</li>
 *     <li>4.对于临时节点，即使加锁也不能保证绝对的安全性，因为临时节点的删除是自动的，即使检查到(不)存在，下一步操作仍然可能失败。</li>
 *     <li>5.对于临时节点的操作一定要小心，建议使用watcher获取数据</li>
 * </P>
 * <p>
 *     加锁时注意：不可以对临时节点加锁(临时节点不能创建子节点，你需要使用另外一个节点加锁，来保护它)。
 *     {@link ZkPathMrg#findAppropriateLockPath(String)}可能会有帮助。
 * </p>
 *
 * <p>
 *     如果提供的简单方法不能满足需要，可以调用{@link #getClient()}获取client。
 * </p>
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/12 12:05
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public class CuratorMrg extends AbstractThreadLifeCycleHelper {

    private static final Logger logger= LoggerFactory.getLogger(CuratorMrg.class);

    private final GameConfigMrg gameConfigMrg;
    private final ZkPathMrg zkPathMrg;
    /**
     * CuratorFramework instances are fully thread-safe.
     * You should share one CuratorFramework per ZooKeeper cluster in your application.
     */
    private final CuratorFramework client;
    /**
     * curator事件监听线程池,减少创建的线程数;
     * 不可共享，因为它必须是单线程的，以保持事件顺序。
     * 如果共享，如果有人修改线程数将出现问题。
     */
    private final ThreadPoolExecutor watcherExecutor;
    /**
     * 每个路径的锁信息
     */
    private final Map<String, InterProcessMutex> lockMap=new HashMap<>();
    /**
     * 已分配的节点缓存，以方便统一关闭
     */
    private final List<PathChildrenCache> allocateNodeCache=new ArrayList<>(10);

    @Inject
    public CuratorMrg(GameConfigMrg gameConfigMrg, ZkPathMrg zkPathMrg) throws InterruptedException {
        this.gameConfigMrg = gameConfigMrg;
        this.zkPathMrg = zkPathMrg;
        client= newStartedClient();

        // 该线程池不要共享的好，它必须是单线程的，如果放在外部容易出问题
        watcherExecutor =new ThreadPoolExecutor(1,1,
                5, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new WatcherThreadFactory());
    }

    private static class WatcherThreadFactory implements ThreadFactory{

        private final AtomicInteger globalIndex=new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"watcher_thread_"+globalIndex.getAndIncrement());
        }
    }

    /**
     * 当你有更加复杂的需求时，获取客户端可能有帮助
     */
    public CuratorFramework getClient() {
        return client;
    }

    /**
     * 为外部提供创建client的工厂方法。
     * 注意：创建的client在使用完之后必须调用{@link CuratorFramework#close()}关闭
     * @return 已调用启动方法的客户端
     */
    private CuratorFramework newStartedClient() throws InterruptedException {
        CuratorFramework framework = CuratorFrameworkFactory.builder()
                .namespace(gameConfigMrg.getZkNameSpace())
                .connectString(gameConfigMrg.getZkConnectString())
                .connectionTimeoutMs(gameConfigMrg.getZkConnectionTimeoutMs())
                .sessionTimeoutMs(gameConfigMrg.getZkSessionTimeoutMs())
                .retryPolicy(newForeverRetry())
                .build();
        framework.start();
        framework.blockUntilConnected();
        return framework;
    }

    @Override
    protected void startImp() throws Exception {
        // 在构造方法中启动
    }

    @Override
    protected void shutdownImp() {
        client.close();
        allocateNodeCache.forEach(GameUtils::closeQuietly);
        watcherExecutor.shutdownNow();
    }

    /**
     * 使用默认的参数创建一个带退避算法的永久尝试策略。
     * 默认最小等待时间50ms;
     * 默认最大等待时间3s;
     * @return
     */
    public BackoffRetryForever newForeverRetry(){
        // 50ms - 3s 默认时间是很难调整和确定的
        return newForeverRetry(50,3000);
    }

    /**
     * 使用指定参数创建一个带退避算法的永久尝试策略
     * @param baseSleepTimeMs 起始等待时间(最小等待时间)(毫秒)
     * @param maxSleepTimeMs 最大等待时间(毫秒)
     * @return
     */
    public BackoffRetryForever newForeverRetry(int baseSleepTimeMs, int maxSleepTimeMs){
        return new BackoffRetryForever(baseSleepTimeMs,maxSleepTimeMs);
    }

    /**
     * 对某个永久类型节点加锁，不可以直接对临时节点加锁(临时节点无法创建子节点)，可锁其父节点或其它永久节点；
     * {@link ZkPathMrg#findAppropriateLockPath(String)}可能有帮助
     *
     * 实现：阻塞直到锁可用，锁为可重入锁。每一次lock调用都必须有一次对应的{@link #unlock(String)}}调用。
     * 食用方式，就向使用jdk的显式锁一样：
     *
     * <pre>
     *     {@code
     *         curatorMrg.lock(path);
     *         try{
     *             // do something
     *         } finally{
     *             curatorMrg.unlock(path);
     *         }
     *     }
     * </pre>
     * 也可以使用{@link #actionWhitLock(String, LockPathAction)}
     *
     * @param path 请求加锁的路径，再次提醒：该节点不可以是临时节点。
     * @throws Exception ZK errors, connection interruptions
     */
    public void lock(String path) throws Exception {
        // 需要保留下来才能重入
        InterProcessMutex lock=lockMap.computeIfAbsent(path,key->new InterProcessMutex(client,key));
        lock.acquire();
    }

    /**
     * 释放对应路径的锁
     * @param path 路径
     */
    public void unlock(String path) throws Exception {
        InterProcessMutex lock=lockMap.get(path);
        if (null==lock){
            throw new IllegalStateException("path " + path + " lock state is wrong.");
        }
        lock.release();
    }

    /**
     * 如果加锁成功，则执行后续逻辑，采用这种方式可以简化外部代码结构，且更加安全。
     * @param lockPath 要锁住的节点
     * @param action 执行什么操作
     */
    public void actionWhitLock(String lockPath, LockPathAction action) throws Exception {
        lock(lockPath);
        try {
            action.doAction(lockPath);
        }finally {
            unlock(lockPath);
        }
    }

    // region 其它辅助方法

    /**
     * 判断路径是否存在，建议只使用在永久类型节点上。
     * 对于分布式下的并发操作，如果先检查后执行逻辑，必须先持有保护该路径数据的锁。
     *
     * 如果仅仅是期望节点存在时获取数据，使用原子的{@link #getDataIfPresent(String)}更加安全。
     *
     * @param path 路径
     * @return 如果存在则返回true，否则返回false
     * @throws Exception ZK errors, connection interruptions
     */
    public boolean isPathExist(String path) throws Exception {
        return null != client.checkExists().forPath(path);
    }

    /**
     * 尝试获取对应节点的数据，建议使用在永久节点上，如果是可能并发修改的节点，必须加锁。
     * 对于永久节点，加锁可保证先检查后执行复合操作的原子性。
     * 对于临时节点，即使外部加锁，也无法保证，路径存在时一定能获取到数据。
     * 主要原因是临时节点超时删除的无法控制的。
     * eg.
     * 加锁 -> 检查到临时节点存在 -> (远程自动删除节点) -> 获取数据失败
     *
     * 因此该方法适用于永久节点，对于临时节点千万不要先检查后执行，临时节点请使用
     * {@link #getDataIfPresent(String)}，根据获取的结果判断。
     *
     * @param path 路径
     * @return 如果路径存在，则返回对应的数据，否则抛出异常
     * @throws Exception 节点不存在抛出异常，以及zookeeper连接断开导致的异常
     */
    public byte[] getData(String path) throws Exception {
        return client.getData().forPath(path);
    }

    /**
     * 当节点存在时返回节点的数据，否则返回null，它是一个原子操作。
     * 它不是一个先检查后执行的复合操作。
     * <b>获取临时节点数据必须使用该方法。</b>
     *
     * @param path 节点路径
     * @return 节点数据
     * @throws Exception zk errors.
     */
    @Nullable
    public byte[] getDataIfPresent(String path) throws Exception {
        try {
            return client.getData().forPath(path);
        }catch (KeeperException.NoNodeException e){
            logger.warn("may other process delete {} node.",path,e);
        }
        return null;
    }

    /**
     * 创建一个空节点，如果是并发创建的节点，注意加锁。
     * @param path 路径
     * @param mode 模式
     * @return 返回创建的路径
     * @throws Exception 节点存在抛出异常，以及zookeeper连接断开导致的异常
     */
    public String createNode(String path, CreateMode mode) throws Exception {
        return client.create().creatingParentsIfNeeded().withMode(mode).forPath(path);
    }
    /**
     * 创建一个节点，并以指定数据初始化它，如果是并发创建的节点，注意加锁。
     * @param path 路径
     * @param mode 模式
     * @param initData 初始数据
     * @return 返回创建的路径
     * @throws Exception 节点存在抛出异常，以及zookeeper连接断开导致的异常
     */
    public String createNode(String path, CreateMode mode,byte[] initData) throws Exception {
        return client.create().creatingParentsIfNeeded().withMode(mode).forPath(path,initData);
    }

    /**
     * 设置某个节点的数据，如果是并发更新的节点，一定要注意加锁！
     * @param path 路径
     * @param data 数据
     * @return 节点的最新状态
     * @throws Exception 节点不存在抛出异常，以及zookeeper连接断开导致的异常
     */
    public Stat setData(String path, byte[] data) throws Exception {
        return client.setData().forPath(path,data);
    }

    /**
     * 删除一个节点，节点不存在时什么也不做;
     * 单个节点的删除不需要加锁；尽量不要手动删临时节点；
     * @param path 路径。
     * @throws Exception zookeeper连接断开导致的异常
     */
    public void delete(String path) throws Exception {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        }catch (KeeperException.NoNodeException e){
            // 没有影响
            logger.warn("may other process delete {} node ",path,e);
        }
    }

    /**
     * 获取某个节点的所有子节点属性
     * @param path 父节点路径
     * @return 所有的子节点路径,如果父节点不存在或没有子节点，则返回emptyList
     * @throws Exception zk errors
     */
    public List<String> getChildren(String path) throws Exception{
        try {
            return client.getChildren().forPath(path);
        }catch (KeeperException.NoNodeException e){
            // 不影响
            logger.warn("may other process delete {} node ",path,e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取当前节点的所有子节点数据
     * @param path 父节点路径
     * @return childFullPath -> data
     */
    public Map<String,byte[]> getChildData(String path) throws Exception {
        // 这里的没有必要留到后面关闭
        try (PathChildrenCache pathChildrenCache = newPathChildrenCache(path)){
            pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            List<ChildData> currentData = pathChildrenCache.getCurrentData();
            Map<String,byte[]> result=new HashMap<>(currentData.size()+1,1);
            for (ChildData childData:currentData){
                result.put(childData.getPath(),childData.getData());
            }
            return result;
        }
    }

    /**
     * 创建一个路径节点缓存，返回之前已调用{@link PathChildrenCache#start()}，
     * 你需要在使用完之后调用{@link PathChildrenCache#close()}关闭缓存节点；
     * 如果你忘记关闭，那么会在curator关闭的时候统一关闭。
     *
     * @param path 父节点
     * @param listener 事件监听，注意事件分发后于数据更新。{@link PathChildrenCache} 更新数据之后才会派发事件，因此
     *                 {@link PathChildrenCache#getCurrentData()}的数据可能比事件产生的时候更加新。
     * @return 已启动的节点缓存
     * @throws Exception
     */
    public PathChildrenCache newChildrenCache(String path, @Nonnull PathChildrenCacheListener listener) throws Exception {
        // CloseableExecutorService这个还是不共享的好
        PathChildrenCache pathChildrenCache = newPathChildrenCache(path);
        pathChildrenCache.getListenable().addListener(listener);
        this.allocateNodeCache.add(pathChildrenCache);

        pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        return pathChildrenCache;
    }

    /**
     * 为指定节点创建一个缓存节点。
     * (该封装的还是得封装，减少重复代码，降低出错几率，要习惯)
     */
    private PathChildrenCache newPathChildrenCache(String path) {
        CloseableExecutorService watcherService = new CloseableExecutorService(watcherExecutor, false);
        return new PathChildrenCache(client, path, true,false, watcherService);
    }

    /**
     * 等待节点出现
     * @param path 节点一定不能是根节点
     * @return  节点的数据。不要使用{@link #getData(String)}，
     * {@link #getDataIfPresent(String)}去获取数据，因为不一定能获取到。
     */
    public byte[] waitForNodeCreate(String path) throws Exception {
        String parentPath = zkPathMrg.findParentPath(path);
        final CountDownLatch countDownLatch=new CountDownLatch(1);
        // 这里使用AtomicReference是因为lambda表达式必须使用final类型，而且不能一直睡眠，还需要保持线程活性
        final AtomicReference<byte[]> resultHolder=new AtomicReference<>();
        // 这里不能使用try with resources,还没启动就关了。
        PathChildrenCache pathChildrenCache = newPathChildrenCache(parentPath);
        pathChildrenCache.getListenable().addListener((client1, event) -> {
            if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED){
                ChildData childData = event.getData();
                if (childData.getPath().equals(path)){
                    resultHolder.compareAndSet(null,childData.getData());
                    countDownLatch.countDown();
                }
            }
        });
        boolean interrupted=false;
        try {
            pathChildrenCache.start(PathChildrenCache.StartMode.NORMAL);
            // 不能一直睡眠，需要保持活性
            while (true){
                try {
                    countDownLatch.await(1,TimeUnit.SECONDS);
                    if (countDownLatch.getCount()==0){
                        break;
                    }
                }catch (InterruptedException e){
                    interrupted=true;
                }
            }
            return resultHolder.get();
        }finally {
            GameUtils.closeQuietly(pathChildrenCache);
            if (interrupted){
                Thread.currentThread().interrupt();
            }
        }
    }

    // endregion

    public static void main(String[] args) throws Exception {
        ZkPathMrg zkPathMrg = new ZkPathMrg();
        GameConfigMrg gameConfigMrg = new GameConfigMrg();
        CuratorMrg curatorMrg = new CuratorMrg(gameConfigMrg, zkPathMrg);
        curatorMrg.start();

        byte[] bytes = curatorMrg.waitForNodeCreate("/mutex/newChild");
        System.out.println(new String(bytes,StandardCharsets.UTF_8));

        curatorMrg.shutdown();
    }

    private static void onCacheEvent(CuratorFramework client, PathChildrenCacheEvent event){
        switch (event.getType()){
            case CHILD_ADDED:
                printEventData(event);
                break;
            case CHILD_UPDATED:
                printEventData(event);
                break;
            case CHILD_REMOVED:
                printEventData(event);
                break;
                default:
                    break;
        }
    }

    private static void printEventData(PathChildrenCacheEvent event){
        ChildData childData = event.getData();
        System.out.println(String.format("thread%s eventType=%s path=%s data=%s",
                Thread.currentThread().getName(),
                event.getType(), childData.getPath(),
                new String(childData.getData(), StandardCharsets.UTF_8)));
    }

    private static void printChildData(ChildData childData){
        System.out.println(String.format("path=%s data=%s",
                childData.getPath(),
                new String(childData.getData(), StandardCharsets.UTF_8)));
    }


    private static class WaitChildEventListener implements PathChildrenCacheListener{

        private final PathChildrenCacheEvent.Type waitType;
        private final String fullPath;

        private final CountDownLatch countDownLatch = new CountDownLatch(1);
        /**
         * 结果变量，非volatile，由countdownLatch的await和countdown保护
         * non-volatile, protected by countDownLatch countdown and await
          */
        private byte[] result=null;

        private WaitChildEventListener(PathChildrenCacheEvent.Type waitType, String fullPath) {
            this.waitType = waitType;
            this.fullPath = fullPath;
        }

        @Override
        public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
            if (event.getType() == waitType && null == result){
                ChildData childData = event.getData();
                if (childData.getPath().equals(fullPath)){
                    result = childData.getData();
                    countDownLatch.countDown();
                }
            }
        }

        public void awaitUninterruptibly(){
            ConcurrentUtils.awaitUninterruptibly(countDownLatch);
        }

        public void awaitWithHearBeat(long heartBeat,TimeUnit timeUnit){
            ConcurrentUtils.awaitWithHeartBeat(countDownLatch,heartBeat,timeUnit);
        }

        /**
         * 是否已经完成
         * @return 已完成时返回true
         */
        public boolean isDone(){
            return countDownLatch.getCount() == 0;
        }

        /**
         * 当{@link #isDone()}返回true时，或你从{@link #awaitUninterruptibly()}成功返回时,
         * 或你从{@link #awaitWithHearBeat(long, TimeUnit)}成功返回时可获取结果
         * @return
         */
        public byte[] getResult(){
            return result;
        }
    }
}
