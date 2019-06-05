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

package com.wjybxx.fastjgame.scene;

import com.wjybxx.fastjgame.config.TemplateSceneConfig;
import com.wjybxx.fastjgame.core.SceneRegion;
import com.wjybxx.fastjgame.misc.*;
import com.wjybxx.fastjgame.mrg.MapDataLoadMrg;
import com.wjybxx.fastjgame.mrg.SceneSendMrg;
import com.wjybxx.fastjgame.mrg.SceneWrapper;
import com.wjybxx.fastjgame.scene.gameobject.GameObject;
import com.wjybxx.fastjgame.utils.GameConstant;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.List;

/**
 * 场景基类，所有的场景都继承该类
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/31 21:17
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public abstract class Scene {

    // 更新单个对象时，缓存对象，避免大量的创建list
    private static final List<ViewGrid> invisibleGridsCache = new ArrayList<>(GameConstant.VIEWABLE_GRID_NUM);
    private static final List<ViewGrid> visibleGridsCache = new ArrayList<>(GameConstant.VIEWABLE_GRID_NUM);

    /**
     * 刷新视野格子的间隔
     */
    private static final long DELTA_UPDATE_VIEW_GRIDS = 1000;

    private final SceneSendMrg sendMrg;
    private final MapDataLoadMrg mapDataLoadMrg;

    /**
     * 下次刷新视野的时间戳；
     */
    private long nextUpdateViewGridTime = 0;

    /**
     * 每一个场景都有一个唯一的id
     */
    private final long guid;
    /**
     * 每个场景都有一个对应的配置文件
     */
    private final TemplateSceneConfig sceneConfig;
    /**
     * 场景对象容器,对外提供访问对象的接口
     */
    private final SceneGameObjectManager sceneGameObjectManager;

    private final ViewGridSet viewGridSet;

    /**
     * 场景视野通知策略
     */
    private final NotifyHandlerMapper notifyHandlerMapper = new NotifyHandlerMapper();

    /**
     * 游戏对象在场景中的生命周期管理
     * (名字有点长)
     */
    private final GameObjectInSceneHandlerMapper gameObjectInSceneHandlerMapper = new GameObjectInSceneHandlerMapper();

    public Scene(long guid, TemplateSceneConfig sceneConfig, SceneWrapper sceneWrapper) {
        this.guid = guid;
        this.sceneConfig = sceneConfig;
        this.sendMrg = sceneWrapper.getSendMrg();
        this.mapDataLoadMrg = sceneWrapper.getMapDataLoadMrg();

        // 以后再考虑是否需要重用
        MapData mapData = mapDataLoadMrg.loadMapData(sceneConfig.mapId);
        this.viewGridSet = new ViewGridSet(mapData.getMapWidth(),
                mapData.getMapHeight(),
                sceneConfig.viewableRange,
                getViewGridInitCapacityHolder());

        // 创建管理该场景对象的控制器
        this.sceneGameObjectManager = new SceneGameObjectManager(getGameObjectManagerInitCapacityHolder());
    }

    /**
     * 获取创建视野格子的默认容量信息，子类在需要的时候可以覆盖它;
     * @return empty
     */
    protected InitCapacityHolder getViewGridInitCapacityHolder(){
        return InitCapacityHolder.EMPTY;
    }

    /**
     * 获取SceneGameObjectManager创建时的初始容量信息，子类在需要的时候可以覆盖它;
     * 名字是长了点...
     * @return empty
     */
    protected InitCapacityHolder getGameObjectManagerInitCapacityHolder(){
        return InitCapacityHolder.EMPTY;
    }

    public SceneGameObjectManager getSceneGameObjectManager() {
        return sceneGameObjectManager;
    }

    public long getGuid() {
        return guid;
    }

    public TemplateSceneConfig getSceneConfig() {
        return sceneConfig;
    }

    public SceneRegion region(){
        return sceneConfig.sceneRegion;
    }

    public final int mapId(){
        return sceneConfig.mapId;
    }

    public abstract SceneType sceneType();

    // ----------------------------------------核心逻辑开始------------------------------
    // region tick

    /**
     * scene刷帧
     * @param curMillTime 当前系统时间戳
     */
    public void tick(long curMillTime) throws Exception{

        // 检测视野格子刷新
        if (curMillTime >= nextUpdateViewGridTime){
            nextUpdateViewGridTime = curMillTime + DELTA_UPDATE_VIEW_GRIDS;
            updateViewableGrid();
        }
    }

    // endregion


    // region 视野管理

    /**
     * 刷新所有对象的视野格子
     */
    protected final void updateViewableGrid(){

    }

    /**
     * 刷新单个对象的视野格子
     * @param gameObject 指定对象
     * @param <T> 对象类型
     */
    protected final <T extends GameObject> void updateViewableGrid(T gameObject){
        final ViewGrid preViewGrid = gameObject.getViewGrid();
        final ViewGrid curViewGrid = viewGridSet.findViewGrid(gameObject.getPosition());

        // 视野范围未发生改变
        if (preViewGrid == curViewGrid){
            return;
        }

        // 视野格子发生改变(需要找到前后格子差异)
        List<ViewGrid> preViewableGrids = preViewGrid.getViewableGrids();
        List<ViewGrid> curViewableGrids = curViewGrid.getViewableGrids();

        // 视野更新会频繁的执行，不可以大量创建list，因此使用缓存
        List<ViewGrid> invisibleGrids = invisibleGridsCache;
        List<ViewGrid> visibleGrids = visibleGridsCache;
        try{
            // 旧的哪些看不见了
            for (ViewGrid preViewableGrid : preViewableGrids){
                if (!viewGridSet.visible(curViewGrid,preViewableGrid)){
                    invisibleGrids.add(preViewableGrid);
                }
            }
            // 新看见的格子有哪些
            for (ViewGrid curViewableGrid : curViewableGrids){
                if (!viewGridSet.visible(preViewGrid,curViewableGrid)){
                    visibleGrids.add(curViewableGrid);
                }
            }

            // 更新所在的视野格子
            gameObject.setViewGrid(curViewGrid);
            preViewGrid.removeObject(gameObject);
            curViewGrid.addGameObject(gameObject);

            NotifyHandler<T> notifyHandler = notifyHandlerMapper.getHandler(gameObject);
            // 视野，进入和退出都是相互的，他们离开了我的视野，我也离开了他们的视野
            // 通知该对象，这些对象离开了我的视野
            notifyHandler.notifyGameObjectOthersOut(gameObject,invisibleGrids);
            // 通知旧格子里面的对象，该对象离开了它们的视野
            notifyHandler.notifyOthersGameObjectOut(invisibleGrids,gameObject);

            // 通知该对象，这些格子的对象进入了他的视野
            notifyHandler.notifyGameObjectOthersIn(gameObject,visibleGrids);
            // 通知这些格子的对象，该对象进入了他们的视野
            notifyHandler.notifyOthersGameObjectIn(visibleGrids,gameObject);
        }finally {
            invisibleGrids.clear();
            visibleGrids.clear();
        }
    }
    // endregion

    /**
     * 游戏对象在场景中的模板实现
     * @param <T>
     */
    public abstract class AbstractGameObjectInSceneHandler<T extends GameObject> implements GameObjectInSceneHandler<T> {

        @Override
        public final void processEnterScene(T gameObject) {
            beforeEnterScene(gameObject);
            enterSceneCore(gameObject);
            afterEnterScene(gameObject);
        }

        /**
         * 进入场景的核心逻辑
         * @param gameObject 场景对象
         */
        private void enterSceneCore(T gameObject){

        }

        /**
         * 在进入场景之前需要进行必要的初始化
         * @param gameObject 场景对象
         */
        protected abstract void beforeEnterScene(T gameObject);

        /**
         * 在成功进入场景之后，可能有额外逻辑
         * @param gameObject 场景对象
         */
        protected abstract void afterEnterScene(T gameObject);

        @Override
        public final void tick(T gameObject) {
            tickCore(gameObject);
            tickHook(gameObject);
        }

        /**
         * tick公共逻辑(核心逻辑)
         * @param gameObject
         */
        private void tickCore(T gameObject){

        }

        /**
         * 子类独有逻辑
         * @param gameObject
         */
        protected void tickHook(T gameObject){

        }

        @Override
        public final void processLeaveScene(T gameObject) {
            beforeLeaveScene(gameObject);
            leaveSceneCore(gameObject);
            afterLeaveScene(gameObject);
        }

        /**
         * 离开场景的核心逻辑(公共逻辑)
         * @param gameObject 场景对象
         */
        private void leaveSceneCore(T gameObject){

        }

        /**
         * 在离开场景之前可能有额外逻辑
         * @param gameObject 场景对象
         */
        protected abstract void beforeLeaveScene(T gameObject);

        /**
         * 在成功离开场景之后可能有额外逻辑
         * @param gameObject 场景对象
         */
        protected abstract  void afterLeaveScene(T gameObject);
    }
}
