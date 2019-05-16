package com.wjybxx.fastjgame.component;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.configwrapper.ConfigWrapper;
import com.wjybxx.fastjgame.mrg.GuidMrg;
import com.wjybxx.fastjgame.mrg.WorldCoreInfoMrg;
import com.wjybxx.fastjgame.net.common.RoleType;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/13 10:48
 * @github - https://github.com/hl845740757
 */
public class EGWorldCoreInfoMrg extends WorldCoreInfoMrg {

    @Inject
    public EGWorldCoreInfoMrg(GuidMrg guidMrg) {
        super(guidMrg);
    }

    @Override
    protected void initImp(ConfigWrapper startArgs) throws Exception {

    }

    @Override
    public RoleType processType() {
        return RoleType.TEST;
    }
}
