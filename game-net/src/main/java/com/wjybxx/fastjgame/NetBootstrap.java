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

package com.wjybxx.fastjgame;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wjybxx.fastjgame.misc.AbstractThreadLifeCycleHelper;
import com.wjybxx.fastjgame.module.NetModule;
import com.wjybxx.fastjgame.mrg.DisruptorMrg;
import com.wjybxx.fastjgame.mrg.WorldInfoMrg;
import com.wjybxx.fastjgame.net.async.event.NetEventHandlerImp;
import com.wjybxx.fastjgame.configwrapper.ArrayConfigWrapper;
import com.wjybxx.fastjgame.configwrapper.ConfigWrapper;
import com.wjybxx.fastjgame.world.World;

import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 网络层引导程序，可以继承它。
 *
 * 注意：在main方法中需要先初始化日志文件路径，见 logback.xml中的 logFilePath属性。
 *
 * @param <T> the type of this
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/9 20:36
 * @github - https://github.com/hl845740757
 */
public class NetBootstrap<T extends NetBootstrap<T>> extends AbstractThreadLifeCycleHelper {
    /**
     * 启动参数
     */
    private ConfigWrapper args;
    /**
     * 帧率
     */
    private int framesPerSecond;
    /**
     * 创建游戏世界线程的工厂
     */
    private ThreadFactory threadFactory=new DefaultThreadFactory();
    /**
     * 游戏世界
     */
    private World world;

    private List<AbstractModule> modules=new ArrayList<>();

    protected void verifyArgs(){
        if (null==args){
            throw new IllegalArgumentException("args is missing");
        }
        if (framesPerSecond<1){
            throw new IllegalArgumentException("framesPerSecond must greater 0");
        }
    }

    @Override
    protected final void startImp() throws Exception {
        verifyArgs();

        modules.add(new NetModule());
        modules.addAll(childModules());

        Injector injector = Guice.createInjector(modules);
        // worldInfo初始化
        WorldInfoMrg worldInfoMrg=injector.getInstance(WorldInfoMrg.class);
        worldInfoMrg.init(args,framesPerSecond);

        // 启动之前的钩子
        beforeStart(injector);

        // 尽可能的延迟创建world
        World world=injector.getInstance(World.class);
        this.world=world;
        // 启动
        DisruptorMrg disruptorMrg=injector.getInstance(DisruptorMrg.class);
        disruptorMrg.start(threadFactory,new NetEventHandlerImp(world, framesPerSecond));
        // 没有留启动完成钩子是因为启动完成后设置属性可能存在线程安全问题
    }

    /**
     * 留给子类的启动钩子，主要完成一些初始化工作
     * 最好不要在这里面启动需要游戏线程数据的线程，可以启动连接池这种不依赖游戏数据的线程。
     * @param injector 用于获取需要的对象，最好不要获取world对象，world依赖太大，触发的逻辑太多,很难保证安全性
     */
    protected void beforeStart(Injector injector){

    }

    @Override
    protected final void shutdownImp() {
        if (null!=world){
            world.requestShutdown();
        }
    }

    /**
     * 返回子类自己的modules
     * @return 应该总是返回相同的结果
     */
    protected List<AbstractModule> childModules(){
        return Collections.emptyList();
    }

    /**
     * 添加启动的依赖注入的对象module
     * @param module
     * @return
     */
    public T addModule(AbstractModule module){
        this.modules.add(module);
        return castThis();
    }

    /**
     * 设置启动命令参数
     * @param args 原始数组
     * @return
     */
    public T setArgs(String[] args){
        this.args= new ArrayConfigWrapper(args);
        return castThis();
    }

    /**
     * 设置启动参数
     * @param configWrapper 已封装的参数
     * @return
     */
    public T setArgs(ConfigWrapper configWrapper){
        this.args=configWrapper;
        return castThis();
    }

    /**
     * 设置游戏世界帧率
     * @param framesPerSecond
     * @return
     */
    public T setFramesPerSecond(int framesPerSecond) {
        this.framesPerSecond = framesPerSecond;
        return castThis();
    }

    public T setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory=Objects.requireNonNull(threadFactory);
        return castThis();
    }

    // region getter
    public ConfigWrapper getArgs() {
        return args;
    }

    public int getFramesPerSecond() {
        return framesPerSecond;
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    // endregion

    // region utils
    @SuppressWarnings("unchecked")
    protected final T castThis(){
        return (T) this;
    }

    /**
     * 默认线程工厂
     */
    private static class DefaultThreadFactory implements ThreadFactory {

        private final AtomicLong threadId=new AtomicLong(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"logic_thread_"+threadId.getAndIncrement());
        }
    }
    // endregion
}
