package com.wjybxx.fastjgame.test;

import com.wjybxx.fastjgame.mrg.CuratorMrg;
import com.wjybxx.fastjgame.mrg.GameConfigMrg;
import com.wjybxx.fastjgame.mrg.ZkPathMrg;
import com.wjybxx.fastjgame.utils.ConcurrentUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/14 20:23
 * @github - https://github.com/hl845740757
 */
public class ZKLockTest {

    public static void main(String[] args) throws Exception {
        ZkPathMrg zkPathMrg = new ZkPathMrg();
        GameConfigMrg gameConfigMrg = new GameConfigMrg();
        CuratorMrg curatorMrg = new CuratorMrg(gameConfigMrg, zkPathMrg);
        curatorMrg.start();

        curatorMrg.lock("/mutex/guid");

        // 始终占用锁
        ConcurrentUtils.awaitRemoteWithHeartBeat(null,
                (resource, timeout, timeUnit) -> false,
                500, TimeUnit.MILLISECONDS);

    }
}
