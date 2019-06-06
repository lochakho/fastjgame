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

package com.wjybxx.fastjgame.world;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.mrg.*;

/**
 * 名字真心不好起，超类负责一些公共的核心逻辑。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/12 12:25
 * @github - https://github.com/hl845740757
 */
public abstract class WorldCore extends World{

    protected final WorldCoreWrapper coreWrapper;
    protected final CuratorMrg curatorMrg;
    protected final GameConfigMrg gameConfigMrg;
    protected final GuidMrg guidMrg;
    protected final InnerAcceptorMrg innerAcceptorMrg;
    protected final MongoDBMrg mongoDBMrg;

    @Inject
    public WorldCore(WorldWrapper worldWrapper, WorldCoreWrapper coreWrapper) {
        super(worldWrapper);
        this.coreWrapper=coreWrapper;
        curatorMrg=coreWrapper.getCuratorMrg();
        gameConfigMrg=coreWrapper.getGameConfigMrg();
        guidMrg=coreWrapper.getGuidMrg();
        innerAcceptorMrg=coreWrapper.getInnerAcceptorMrg();
        mongoDBMrg=coreWrapper.getMongoDBMrg();
    }

    @Override
    protected void registerCodecHelper() throws Exception {
        innerAcceptorMrg.registerInnerCodecHelper();
    }

    @Override
    protected final void worldStartImp() throws Exception {
        startCore();

        startHook();
    }

    private void startCore() throws Exception {
        curatorMrg.start();
    }

    /**
     * 启动游戏服务器
     */
    protected abstract void startHook() throws Exception;

    @Override
    protected final void tickImp(long curMillTime) {
        tickCore();
        tickHook();
    }

    /**
     * 超类tick逻辑
     */
    private void tickCore(){

    }

    /**
     * 子类tick钩子
     */
    protected abstract void tickHook();


    @Override
    protected final void beforeShutdown() throws Exception {
        try {
            shutdownHook();
        }finally {
            shutdownCore();
        }
    }

    /**
     * 关闭公共服务
     */
    private void shutdownCore(){
        curatorMrg.shutdown();
    }

    /**
     * 子类自己的关闭动作
     */
    protected abstract void shutdownHook();

}
