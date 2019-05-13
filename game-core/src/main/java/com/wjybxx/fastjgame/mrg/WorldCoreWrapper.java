package com.wjybxx.fastjgame.mrg;

import com.google.inject.Inject;

/**
 * WorldCore依赖的所有控制器的包装类
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/12 12:41
 * @github - https://github.com/hl845740757
 */
public class WorldCoreWrapper {

    private final ZkPathMrg zkPathMrg;
    private final CuratorMrg curatorMrg;
    private final GuidMrg guidMrg;
    private final GameConfigMrg gameConfigMrg;

    @Inject
    public WorldCoreWrapper(ZkPathMrg zkPathMrg, CuratorMrg curatorMrg, GuidMrg guidMrg, GameConfigMrg gameConfigMrg) {
        this.zkPathMrg = zkPathMrg;
        this.curatorMrg = curatorMrg;
        this.guidMrg = guidMrg;
        this.gameConfigMrg = gameConfigMrg;
    }

    public ZkPathMrg getZkPathMrg() {
        return zkPathMrg;
    }

    public CuratorMrg getCuratorMrg() {
        return curatorMrg;
    }

    public GuidMrg getGuidMrg() {
        return guidMrg;
    }

    public GameConfigMrg getGameConfigMrg() {
        return gameConfigMrg;
    }
}
