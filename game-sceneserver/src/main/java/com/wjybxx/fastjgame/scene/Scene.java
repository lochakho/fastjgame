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

import com.wjybxx.fastjgame.config.TemplateMapConfig;
import com.wjybxx.fastjgame.core.SceneRegion;
import com.wjybxx.fastjgame.misc.NotifyHandler;
import com.wjybxx.fastjgame.misc.SceneGameObjectManager;
import com.wjybxx.fastjgame.mrg.SceneSendMrg;
import com.wjybxx.fastjgame.mrg.SceneWrapper;
import com.wjybxx.fastjgame.scene.gameobject.GameObject;
import com.wjybxx.fastjgame.scene.gameobject.GameObjectType;
import com.wjybxx.fastjgame.utils.CollectionUtils;
import com.wjybxx.fastjgame.utils.GameConstant;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.EnumMap;
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
    /**
     * 当前累积的用于刷新视野的时间增量；
     */
    private long deltaUpdateViewGrids=0;

    private final SceneSendMrg sendMrg;

    /**
     * 每一个场景都有一个唯一的id
     */
    private final long guid;
    /**
     * 每个场景都有一个对应的配置文件
     */
    private final TemplateMapConfig mapConfig;
    /**
     * 场景对象容器,对外提供访问对象的接口
     */
    private final SceneGameObjectManager sceneGameObjectManager;

    private final ViewGridSet viewGridSet;

    /**
     * 场景视野通知策略
     */
    private final EnumMap<GameObjectType, NotifyHandler<?>> notifyHandlerMap = new EnumMap<>(GameObjectType.class);

    public Scene(long guid, TemplateMapConfig mapConfig, SceneWrapper sceneWrapper) {
        this.guid = guid;
        this.mapConfig = mapConfig;
        this.sendMrg = sceneWrapper.getSendMrg();

        // 以后再考虑是否需要重用
        this.viewGridSet = new ViewGridSet(mapConfig.mapData.getMapWidth(),
                mapConfig.mapData.getMapHeight(),
                mapConfig.viewableRange,
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

    /**
     * scene刷帧
     * @param elapsedTime 上一帧到当前帧逝去的时间
     */
    public void tick(long elapsedTime) throws Exception{

        deltaUpdateViewGrids += elapsedTime;

        if (deltaUpdateViewGrids > DELTA_UPDATE_VIEW_GRIDS){
            deltaUpdateViewGrids = 0;
            updateViewableGrid();
        }

    }

    public long getGuid() {
        return guid;
    }

    public TemplateMapConfig getMapConfig() {
        return mapConfig;
    }

    public SceneRegion region(){
        return mapConfig.sceneRegion;
    }

    public final int mapId(){
        return mapConfig.mapData.getMapId();
    }

    /**
     * 注册视野通知策略，不可以重复注册；
     * 如果想要替换，请使用{@link #replaceNotifyHandler(GameObjectType, NotifyHandler)}
     * @param gameObjectType 游戏对象类型
     * @param notifyHandler 通知策略
     */
    protected final void registerNotifyHandler(GameObjectType gameObjectType, NotifyHandler<?> notifyHandler){
        CollectionUtils.requireNotContains(notifyHandlerMap,gameObjectType,"gameObjectType");
        notifyHandlerMap.put(gameObjectType,notifyHandler);
    }

    /**
     * 获取游戏对象对应的使用通知处理器
     * @param gameObject 游戏对象
     * @param <T> 对象类型
     * @return notify handler
     */
    @SuppressWarnings("unchecked")
    protected final <T extends GameObject> NotifyHandler<T> getNotifyHandler(T gameObject){
        return (NotifyHandler<T>) notifyHandlerMap.get(gameObject.getObjectType());
    }

    /**
     * 如果某些场景想替换通知策略，请使用该方法
     * @param gameObjectType 游戏对象类型
     * @param notifyHandler 通知策略
     */
    protected final void replaceNotifyHandler(GameObjectType gameObjectType,NotifyHandler<?> notifyHandler){
        notifyHandlerMap.put(gameObjectType,notifyHandler);
    }

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

            NotifyHandler<T> notifyHandler = getNotifyHandler(gameObject);
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

}
