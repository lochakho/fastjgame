package com.wjybxx.fastjgame.mrg;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.utils.MathUtils;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.charset.StandardCharsets;

/**
 * 基于zookeeper实现的guid生成器。
 *
 * guid生成器控制器，使用redis是最简单方便的。
 * 但是现在好像还没有必要引入redis，而zookeeper是必须引入的，因此暂时还是使用zookeeper实现；
 * 尝试过{@link DistributedAtomicLong}，但是确实有点复杂，最后还是使用了分布式锁{@link InterProcessMutex}。
 *
 * 实现方式和我们项目中的一致，缓存的是整个int正整数区间，它的缺点是资源浪费(但是也狗眼)，
 * 优点是，基本上进程运行期间只需要缓存一次，会减小出现错误的几率。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/12 11:59
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public class ZkGuidMrg implements GuidMrg {

    private static final Logger logger= LoggerFactory.getLogger(ZkGuidMrg.class);

    private final CuratorMrg curatorMrg;
    private final ZkPathMrg zkPathMrg;

    /**
     * guid区间索引
     */
    private int guidIndex=0;
    /**
     * guid本地缓存
     */
    private int guidSequence=0;

    @Inject
    public ZkGuidMrg(CuratorMrg curatorMrg, ZkPathMrg zkPathMrg) {
        this.curatorMrg = curatorMrg;
        this.zkPathMrg = zkPathMrg;
    }

    @Override
    public long generateGuid() {
        try {
            checkCache();
            // 这种方式的guid不是很方便查看规律(如果是乘以10的N次方可能方便查看使用情况)
            long high = ((long)guidIndex) << 32;
            long low = guidSequence++;
            return high + low;
        } catch (Exception e) {
            throw new IllegalStateException("may lose zk connect",e);
        }
    }

    private void checkCache() throws Exception{
        if (guidIndex == 0){
            init();
            return;
        }
        if (guidSequence == Integer.MAX_VALUE){
            String guidIndexPath = zkPathMrg.guidIndexPath();
            String lockPath=zkPathMrg.findAppropriateLockPath(guidIndexPath);
            curatorMrg.actionWhitLock(lockPath,lockPath1 -> incGuidIndex());
        }
    }

    /**
     * 完成guid缓存区间的初始化
     * @throws Exception zk errors
     */
    private void init() throws Exception {
        String guidIndexPath = zkPathMrg.guidIndexPath();
        String lockPath=zkPathMrg.findAppropriateLockPath(guidIndexPath);

        curatorMrg.actionWhitLock(lockPath,lockPath1 -> {
            if (!curatorMrg.isPathExist(guidIndexPath)){
                // 初始化为1 并据为己有
                byte[] initData = "1".getBytes(StandardCharsets.UTF_8);
                curatorMrg.createNode(guidIndexPath, CreateMode.PERSISTENT,initData);

                this.guidIndex=1;
                this.guidSequence=1;
            }else {
                incGuidIndex();
            }
        });
    }

    /**
     * 更新本地guid缓存，需要运行在锁保护下
     * @throws Exception zk errors
     */
    private void incGuidIndex() throws Exception{
        byte[] oldData=curatorMrg.getData(zkPathMrg.guidIndexPath());
        int zkGuidIndex=Integer.parseInt(new String(oldData,StandardCharsets.UTF_8));

        int nextGuidIndex = zkGuidIndex+1;
        byte[] newData = String.valueOf(nextGuidIndex).getBytes(StandardCharsets.UTF_8);
        curatorMrg.setData(zkPathMrg.guidIndexPath(), newData);

        this.guidIndex = nextGuidIndex;
        this.guidSequence = 1;
    }
}
