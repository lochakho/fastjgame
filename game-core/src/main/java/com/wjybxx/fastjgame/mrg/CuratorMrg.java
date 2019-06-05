/*
 * Copyright 2019 wjybxx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wjybxx.fastjgame.mrg;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.holder.ObjectHolder;
import com.wjybxx.fastjgame.misc.AbstractThreadLifeCycleHelper;
import com.wjybxx.fastjgame.misc.BackoffRetryForever;
import com.wjybxx.fastjgame.misc.LockPathAction;
import com.wjybxx.fastjgame.utils.ConcurrentUtils;
import com.wjybxx.fastjgame.utils.GameUtils;
import com.wjybxx.fastjgame.utils.ZKPathUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.utils.CloseableExecutorService;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <h3>Curator</h3>
 * 官网：http://curator.apache.org/getting-started.html<br>
 * 使用zookeeper建议配合ZKUI。
 *
 * <h3>ZKUI</h3>
 *  ZKUI是我常用的zookeeper图形化界面工具，我自己的版本修正了原始版本的中文乱码问题(配置支持中文会好很多)，
 *  和根节点属性导出之后无法导入的问题。
 *  地址： - https://github.com/hl845740757/zkui
 *
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
 *     {@link ZKPathUtils#findAppropriateLockPath(String)}可能会有帮助。
 * </p>
 *   警告：
 *   关于Curator的{@link NodeCache} 和 {@link PathChildrenCache}线程安全问题请查看笔记：
 * - http://note.youdao.com/noteshare?id=721ba3029455fac81d8ec19c813423bf&sub=D20C495A90CD4487A909EE6637A788A6
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
    public CuratorMrg(GameConfigMrg gameConfigMrg) throws InterruptedException {
        this.gameConfigMrg = gameConfigMrg;
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
     * {@link ZKPathUtils#findAppropriateLockPath(String)}可能有帮助
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
        InterProcessMutex lock = lockMap.computeIfAbsent(path,key->new InterProcessMutex(client,key));
        lock.acquire();
    }

    /**
     * 释放对应路径的锁
     * @param path 路径
     */
    public void unlock(String path) throws Exception {
        InterProcessMutex lock = lockMap.get(path);
        if (null == lock){
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
     * 对于临时节点，即使外部加锁，也无法保证检查到路径存在时一定能获取到数据。
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
        }catch (KeeperException.NoNodeException ignore){
            // ignore,may other process delete this node, it's ok
        }
        return null;
    }

    /**
     * 创建一个空节点，如果是并发创建的节点，注意加锁，
     * (可能存在检测到节点不存在，创建节点仍可能失败)，期望原子的操作请使用
     * {@link #createNodeIfAbsent(String, CreateMode)} 和
     * {@link #createNodeIfAbsent(String, CreateMode, byte[])}
     * @param path 路径
     * @param mode 模式
     * @return 返回创建的路径
     * @throws Exception 节点存在抛出异常，以及zookeeper连接断开导致的异常
     */
    public String createNode(String path, CreateMode mode) throws Exception {
        return client.create().creatingParentsIfNeeded().withMode(mode).forPath(path);
    }

    /**
     * 创建一个节点，并以指定数据初始化它，如果是并发创建的节点，注意加锁，
     * (可能存在检测到节点不存在，创建节点仍可能失败)，期望原子的操作请使用
     * {@link #createNodeIfAbsent(String, CreateMode)} 和
     * {@link #createNodeIfAbsent(String, CreateMode, byte[])}
     * @param path 路径
     * @param mode 模式
     * @param initData 初始数据
     * @return 返回创建的路径
     * @throws Exception 节点存在抛出异常，以及zookeeper连接断开导致的异常
     */
    public String createNode(String path, CreateMode mode,@Nonnull byte[] initData) throws Exception {
        return client.create().creatingParentsIfNeeded().withMode(mode).forPath(path,initData);
    }

    /**
     *
     * 如果节点不存在的话，创建它。它是一个原子操作，不是先检查后执行的操作
     * @param path 路径
     * @param mode 模式
     * @return 创建成功则返回true，否则返回false
     * @throws Exception zk errors
     */
    public boolean createNodeIfAbsent(String path, CreateMode mode) throws Exception {
        try {
            createNode(path,mode);
            return true;
        }catch (KeeperException.NodeExistsException ignore){
            // ignore
        }
        return false;
    }

    /**
     * 如果节点不存在的话创建一个节点，并以指定数据初始化它。
     * 它是一个原子操作，不是一个先检查后执行的复合操作。
     * @param path 路径
     * @param mode 模式
     * @param initData 初始数据
     * @return 成功创建则返回true，否则返回false
     * @throws Exception zk errors
     */
    public boolean createNodeIfAbsent(String path, CreateMode mode,@Nonnull byte[] initData) throws Exception {
        try {
            createNode(path,mode,initData);
            return true;
        }catch (KeeperException.NodeExistsException ignore){
            // ignore 等价于cas尝试失败
        }
        return false;
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
            // ignore 没有影响，别人帮助我完成了这件事
        }
    }

    /**
     * 获取某个节点的所有子节点属性
     * @param path 节点路径
     * @return 所有的子节点路径,如果该节点不存在或没有子节点，则返回emptyList
     * @throws Exception zk errors
     */
    public List<String> getChildren(String path) throws Exception{
        try {
            return client.getChildren().forPath(path);
        }catch (KeeperException.NoNodeException e){
            // ignore 没有影响
            return Collections.emptyList();
        }
    }

    /**
     * 获取当前节点的所有子节点数据，返回的只是个快照。
     * 你可以参考{@link PathChildrenCache#rebuild()}
     * @param path 父节点路径
     * @return childFullPath -> data
     */
    public Map<String,byte[]> getChildrenData(String path) throws Exception {
        List<String> children = getChildren(path);
        Map<String,byte[]> result=new LinkedHashMap<>();
        for (String child : children) {
            String childFullPath = ZKPaths.makePath(path, child);
            byte[] childData = getDataIfPresent(childFullPath);
            // 即使 getChildren 查询到节点存在，也不一定能获取到数据，一定要注意
            if (null != childData){
                result.put(childFullPath,childData);
            }
        }
        return result;
    }

    /**
     * 创建一个路径节点缓存，返回之前已调用{@link PathChildrenCache#start()}，
     * 你需要在使用完之后调用{@link PathChildrenCache#close()}关闭缓存节点；
     * 如果你忘记关闭，那么会在curator关闭的时候统一关闭。
     * 更多线程安全问题，请查看类文档中提到的笔记。
     * @param path 父节点
     * @param listener 缓存初始化之后的事件，listener中的处理是事件一定后于
     * @return 当前孩子节点数据
     * @throws Exception
     */
    public List<ChildData> watchChildren(String path, @Nonnull PathChildrenCacheListener listener) throws Exception {
        // CloseableExecutorService这个还是不共享的好
        CloseableExecutorService watcherService = new CloseableExecutorService(watcherExecutor, false);
        // 指定pathChildrenCache接收事件的线程，复用线程池，以节省开销。
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true, false, watcherService);
        // 捕获初始化之前的事件，并将其保存到初始化数据中
        InitCaptureListener initCaptureListener = new InitCaptureListener(listener);
        // 先添加listener以确保不会遗漏事件
        pathChildrenCache.getListenable().addListener(initCaptureListener);
        this.allocateNodeCache.add(pathChildrenCache);

        pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

        // 等待初始化数据完毕
        initCaptureListener.awaitWithRetry(1,TimeUnit.SECONDS);
        return initCaptureListener.getInitChildData();
    }

    /**
     * 等待节点出现。
     * @param path 节点路径
     * @return 节点的数据
     */
    public byte[] waitForNodeCreate(String path) throws Exception {
        // 使用NodeCache的话，写了太多代码，搞得复杂了，不利于维护，使用简单的轮询代替。
        // 轮询虽然不雅观，但是正确性易保证
        ObjectHolder<byte[]> resultHolder=new ObjectHolder<>();
        ConcurrentUtils.awaitRemoteWithSleepingRetry(path, resource->{
            resultHolder.setValue(getDataIfPresent(resource));
            return resultHolder.getValue() != null;
            },1,TimeUnit.SECONDS);
        return resultHolder.getValue();
    }

    /**
     * 等待节点删除，当节点不存在，立即返回（注意先检查后执行的原子性问题）。
     * @param path 节点路径
     * @throws Exception zk errors
     */
    public void waitForNodeDelete(String path) throws Exception {
        // 使用NodeCache的话，主要是当前节点不存在的时候，不会产生事件，无法基于事件优雅的等待
        // 轮询虽然不雅观，但是正确性易保证
        ConcurrentUtils.awaitRemoteWithSleepingRetry(path,
                resource -> !isPathExist(resource),
                1,TimeUnit.SECONDS);
    }

    /**
     * 删除指定节点，如果该节点没有子节点的话。
     * (该操作是一个复合操作，注意加锁)
     * @param path 路径
     */
    public void deleteNodeIfNoChild(String path) throws Exception {
        List<String> children = getChildren(path);
        if (children.size()==0){
            delete(path);
        }
    }

    /**
     * 初始化数据捕获监听器，运行在PathChildrenCache的线程池中，它通过事件恢复数据，以和main-EventThread产生事件时的状态一致。
     */
    private static class InitCaptureListener implements PathChildrenCacheListener{
        /**
         * pathChildrenCache的数据更新是在main-EventThread中，但是事件在我们的线程中。
         * 这不是坑爹吗....
         */
        private final PathChildrenCacheListener after;
        private boolean inited=false;

        private final CountDownLatch countDownLatch=new CountDownLatch(1);
        private List<ChildData> initChildData;

        private InitCaptureListener(PathChildrenCacheListener after) {
            this.after = after;
        }

        @Override
        public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
            if (event.getType() == PathChildrenCacheEvent.Type.INITIALIZED){
                inited=true;
                initChildData=event.getInitialData();
                countDownLatch.countDown();
                // 在这里移除自己，注册after的话会导致after也收到init事件.
                return;
            }

            if (inited){
                after.childEvent(client,event);
                return;
            }

            switch (event.getType()){
                case CHILD_ADDED:
                case CHILD_UPDATED:
                case CHILD_REMOVED:
                    break;
                    default:
                        after.childEvent(client,event);
                        break;
            }
        }

        /**
         * 等待结果期间保持心跳，以维持线程活性(避免资源socket等关闭)
         * @param heartBeat 心跳间隔
         * @param timeUnit 时间单位
         */
        void awaitWithRetry(long heartBeat, TimeUnit timeUnit) {
            ConcurrentUtils.awaitWithRetry(countDownLatch,heartBeat,timeUnit);
        }

        /**
         * 当你从{@link #awaitWithRetry(long, TimeUnit)}成功返回后，可获取初始化数据
         * @return 初始化的数据的一个快照
         */
        List<ChildData> getInitChildData() {
            return initChildData;
        }
    }

    // endregion
}
