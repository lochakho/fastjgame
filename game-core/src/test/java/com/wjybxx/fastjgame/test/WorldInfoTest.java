package com.wjybxx.fastjgame.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wjybxx.fastjgame.component.EGWorldCoreModule;
import com.wjybxx.fastjgame.module.CoreModule;
import com.wjybxx.fastjgame.module.NetModule;
import com.wjybxx.fastjgame.mrg.WorldInfoMrg;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/13 12:28
 * @github - https://github.com/hl845740757
 */
public class WorldInfoTest {

    public static void main(String[] args) {
        Injector injector= Guice.createInjector(new NetModule(),
                new EGWorldCoreModule());

        WorldInfoMrg worldInfoMrg = injector.getInstance(WorldInfoMrg.class);
        System.out.println(worldInfoMrg.getProcessGuid());
        System.out.println(worldInfoMrg.getProcessType());
        System.out.println(worldInfoMrg.getFramesPerSecond());
        System.out.println(worldInfoMrg.getStartArgs());
    }
}
