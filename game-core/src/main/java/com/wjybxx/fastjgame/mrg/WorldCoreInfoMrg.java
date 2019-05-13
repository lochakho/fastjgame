package com.wjybxx.fastjgame.mrg;

import com.google.inject.Inject;

/**
 * WorldCore的worldGuid通过guidMrg生成
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/13 10:30
 * @github - https://github.com/hl845740757
 */
public abstract class WorldCoreInfoMrg extends WorldInfoMrg{

    private long worldGuid;

    @Inject
    public WorldCoreInfoMrg(GuidMrg guidMrg) {
        worldGuid=guidMrg.generateGuid();
    }

    @Override
    public final long getWorldGuid() {
        return worldGuid;
    }
}
