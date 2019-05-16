package com.wjybxx.fastjgame.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wjybxx.fastjgame.component.EGWorldCoreModule;
import com.wjybxx.fastjgame.module.NetModule;
import com.wjybxx.fastjgame.mrg.GuidMrg;

import java.util.stream.IntStream;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/13 12:24
 * @github - https://github.com/hl845740757
 */
public class GuidTest {

    public static void main(String[] args) {
        Injector injector= Guice.createInjector(new NetModule(),
                new EGWorldCoreModule());

        GuidMrg guidMrg=injector.getInstance(GuidMrg.class);
        IntStream.rangeClosed(1,1000).forEach(i->{
            System.out.println("guid " + guidMrg.generateGuid());
        });
    }
}
