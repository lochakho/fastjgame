package com.wjybxx.fastjgame.mrg;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.misc.AbstractThreadLifeCycleHelper;
import com.wjybxx.fastjgame.misc.BackoffRetryForever;
import com.wjybxx.fastjgame.misc.LockPathAction;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.HashMap;
import java.util.Map;

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
 *     <li>5.也就是说，临时节点最好只有创建操作</li>
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
     * 每个路径的锁信息
     */
    private final Map<String, InterProcessMutex> lockMap=new HashMap<>();

    @Inject
    public CuratorMrg(GameConfigMrg gameConfigMrg, ZkPathMrg zkPathMrg) {
        this.gameConfigMrg = gameConfigMrg;
        this.zkPathMrg = zkPathMrg;
        client= newStartedClient();
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
    private CuratorFramework newStartedClient() {
        CuratorFramework framework = CuratorFrameworkFactory.builder()
                .connectString(gameConfigMrg.getZkConnectString())
                .connectionTimeoutMs(gameConfigMrg.getZkConnectionTimeoutMs())
                .sessionTimeoutMs(gameConfigMrg.getZkSessionTimeoutMs())
                .retryPolicy(newForeverRetry())
                .namespace(zkPathMrg.nameSpace())
                .build();
        framework.start();
        return framework;
    }

    @Override
    protected void startImp() throws Exception {
        // 在构造方法中启动
    }

    @Override
    protected void shutdownImp() {
        client.close();
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
     * 判断路径是否存在。
     * 如果用于先检查后执行可能会导致错误。对于并发操作，如果先检查后执行逻辑，必须先持有保护该路径数据的锁。
     * @param path 路径
     * @return 如果存在则返回true，否则返回false
     * @throws Exception ZK errors, connection interruptions
     */
    public boolean isPathExist(String path) throws Exception {
        return null != client.checkExists().forPath(path);
    }

    /**
     * 获取对应节点的数据，如果是可能并发修改的节点，加锁可能会有帮助。
     * 这里即使外部加锁，也无法保证，路径存在时一定能获取到数据。
     * 主要原因是临时节点超时删除的无法控制的，但是多判断能减少错误。
     *
     * //加锁 -> 检查到临时节点存在 -> (远程自动删除节点) -> 尝试获取数据导致异常
     *
     * @param path 路径
     * @return 如果路径存在，则返回对应的数据，否则抛出异常
     * @throws Exception 节点不存在抛出异常，以及zookeeper连接断开导致的异常
     */
    public byte[] getData(String path) throws Exception {
        return client.getData().forPath(path);
    }

    /**
     * 当节点存在时返回节点的数据，否则返回null。
     * 获取对应节点的数据，如果是可能并发修改的节点，加锁可能会有帮助。
     * 这里即使外部加锁，也无法保证，路径存在时一定能获取到数据。
     * 主要原因是临时节点超时删除的无法控制的，但是多判断能减少错误。
     *
     * @param path 节点路径
     * @return 节点数据
     * @throws Exception zk errors.
     */
    @Nullable
    public byte[] getDataIfPresent(String path) throws Exception {
        try {
            if (isPathExist(path)){
                return client.getData().forPath(path);
            }
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
     * 删除一个节点，单个节点的删除不需要加锁；尽量不要手动删临时节点；
     * @param path 路径。
     * @throws Exception 节点不存在抛出异常，以及zookeeper连接断开导致的异常
     */
    public void delete(String path) throws Exception {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        }catch (KeeperException.NoNodeException e){
            logger.warn("may other process delete {} node ",path,e);
        }
    }
    // endregion
}
