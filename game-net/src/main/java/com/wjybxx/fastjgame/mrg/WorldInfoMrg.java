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
import com.wjybxx.fastjgame.configwrapper.ConfigWrapper;
import com.wjybxx.fastjgame.net.common.RoleType;

/**
 * 游戏世界基本信息控制器，用于逻辑线程(游戏世界线程)，它会在游戏世界线程启动之前初始化。
 * 子类可以存储更多的信息。
 * {@link #init(ConfigWrapper, int)} happens-before world thread invoke other public methods
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/6 10:23
 * @github - https://github.com/hl845740757
 */
public abstract class WorldInfoMrg {
    /**
     * 启动参数
     */
    private ConfigWrapper startArgs;
    /**
     * 游戏世界帧率
     */
    private int framesPerSecond;

    @Inject
    public WorldInfoMrg() {

    }

    /**
     * 在启动world线程之前会调用初始化方法。
     * 子类还可以新增自定义的初始化方法，只要在启动world之前执行就是ok的。
     * (保证逻辑线程及后续线程的可见性)
     * @throws Exception 允许抛出异常
     */
    public void init(ConfigWrapper startArgs,int framesPerSecond) throws Exception {
        this.startArgs=startArgs;
        this.framesPerSecond=framesPerSecond;
        this.initImp(startArgs);
    }

    /**
     * 子类自身的初始化工作
     * @param startArgs 启动参数，可能需要
     * @throws Exception 允许启动前的初始化抛出异常
     */
    protected abstract void initImp(ConfigWrapper startArgs) throws Exception;

    /**
     * 获取启动参数
     */
    public final ConfigWrapper getStartArgs() {
        return startArgs;
    }

    /**
     * 获取游戏世界全局唯一id
     */
    public abstract long getWorldGuid();

    /**
     * 获取游戏世界的角色类型
     */
    public abstract RoleType getWorldType();

    /**
     * 获取游戏世界帧率
     */
    public final int getFramesPerSecond(){
        return framesPerSecond;
    }

}
