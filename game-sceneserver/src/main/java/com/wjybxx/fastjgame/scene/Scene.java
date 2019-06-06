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
import com.wjybxx.fastjgame.scene.gameobject.*;
import com.wjybxx.fastjgame.trigger.TriggerSystem;
import com.wjybxx.fastjgame.utils.GameConstant;
import com.wjybxx.fastjgame.utils.MathUtils;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.List;

import static com.wjybxx.fastjgame.scene.gameobject.GameObjectType.*;

/**
 * 场景基类，所有的场景都继承该类
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/31 21:17
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public abstract class Scene extends TriggerSystem {

    private static final Logger logger = LoggerFactory.getLogger(Scene.class);

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
     * 游戏对象进出场景handler
     * (名字有点长)
     */
    private final GameObjectInOutHandlerMapper gameObjectInOutHandlerMapper = new GameObjectInOutHandlerMapper();
    /**
     * 场景对象刷帧handler
     */
    private final GameObjectTickHandlerMapper gameObjectTickHandlerMapper = new GameObjectTickHandlerMapper();

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

    private void registerHandlers(){
        registerNotifyHandlers();
        registerInOutHandlers();
        registerTickHandlers();
    }

    private void registerNotifyHandlers(){
        notifyHandlerMapper.registerHandler(PLAYER, new PlayerNotifyHandler());
        notifyHandlerMapper.registerHandler(PET, new DefaultNotifyHandler<>());
        notifyHandlerMapper.registerHandler(NPC, new DefaultNotifyHandler<>());
    }

    private void registerInOutHandlers(){
        gameObjectInOutHandlerMapper.registerHandler(PLAYER, new PlayerInOutHandler());
        gameObjectInOutHandlerMapper.registerHandler(PET, new PetInOutHandler());
        gameObjectInOutHandlerMapper.registerHandler(NPC, new NpcInOutHandler());
    }

    private void registerTickHandlers(){
        gameObjectTickHandlerMapper.registerHandler(PLAYER, new PlayerTickHandler(30));
        gameObjectTickHandlerMapper.registerHandler(PET, new PetTickHandler(30));
        gameObjectTickHandlerMapper.registerHandler(NPC, new NpcTickHandler(10));
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

        // 场景对象刷帧
        for (GameObjectType gameObjectType : GameObjectType.values()){
            tryTickGameObjects(curMillTime, gameObjectType);
        }

        // 检测视野格子刷新
        tryUpdateViewableGrid(curMillTime);
    }

    @SuppressWarnings("unchecked")
    private <T extends GameObject> void tryTickGameObjects(long curMillTime, GameObjectType gameObjectType){
        GameObjectTickHandler<T> handler = (GameObjectTickHandler<T>) gameObjectTickHandlerMapper.getHandler(gameObjectType);
        if (curMillTime < handler.getNextTickTimeMills()){
            return;
        }
        handler.setNextTickTimeMills(curMillTime + handler.getTickInterval());

        ObjectCollection<T> gameObjectSet = (ObjectCollection<T>) sceneGameObjectManager.getGameObjectSet(gameObjectType);
        if (gameObjectSet.size() == 0){
            return;
        }

        for (T gameObject : gameObjectSet){
            try {
                handler.tick(gameObject);
            }catch (Exception e){
                logger.error("tick {}-{} caught exception", gameObjectType, gameObject.getGuid(),e);
            }
        }
    }

    // endregion


    // region 视野管理

    /**
     * 刷新所有对象的视野格子
     */
    private void tryUpdateViewableGrid(long curMillTime){
        if (curMillTime < nextUpdateViewGridTime){
            return;
        }
        nextUpdateViewGridTime = curMillTime + DELTA_UPDATE_VIEW_GRIDS;

        // 视野刷新最好不要有依赖，我们之前项目某些实现导致视野刷新之间有依赖(跟随对象的特殊跟随策略)
        for (GameObjectType gameObjectType : GameObjectType.values()){
            ObjectCollection<? extends GameObject> gameObjectSet = sceneGameObjectManager.getGameObjectSet(gameObjectType);
            if (gameObjectSet.size() == 0){
                continue;
            }
            for (GameObject gameObject : gameObjectSet){
                try {
                    updateViewableGrid(gameObject);
                }catch (Exception e){
                    logger.error("update {}-{} viewableGrids caught exception", gameObjectType, gameObject.getGuid());
                }
            }
        }
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

    private class DefaultNotifyHandler<T extends GameObject> implements NotifyHandler<T>{

        @Override
        public void notifyGameObjectOthersIn(T t, List<ViewGrid> newVisibleGrids) {

        }

        @Override
        public void notifyGameObjectOthersOut(T t, List<ViewGrid> range) {

        }

        @Override
        public void notifyOthersGameObjectIn(List<ViewGrid> range, T t) {
            if (t.getObjectType() != PLAYER){
                return;
            }
            // TODO serialize range objects
//            Object msg
//            sendMrg.broadcastPlayerExcept(range, msg,(Player) gameObject);
        }

        @Override
        public void notifyOthersGameObjectOut(List<ViewGrid> range, T gameObject) {
            if (gameObject.getObjectType() != PLAYER){
                return;
            }
            // TODO serialize gameObject
//            Object msg
//            sendMrg.broadcastPlayerExcept(range, msg,(Player) gameObject);
        }
    }

    private class PlayerNotifyHandler implements NotifyHandler<Player>{

        @Override
        public void notifyGameObjectOthersIn(Player player, List<ViewGrid> newVisibleGrids) {

        }

        @Override
        public void notifyGameObjectOthersOut(Player player, List<ViewGrid> range) {

        }

        @Override
        public void notifyOthersGameObjectIn(List<ViewGrid> range, Player player) {

        }

        @Override
        public void notifyOthersGameObjectOut(List<ViewGrid> range, Player player) {

        }
    }


    // endregion

    // region 进出场景

    /**
     * 游戏对象进出场景模板实现
     * @param <T>
     */
    protected abstract class AbstractGameObjectInOutHandler<T extends GameObject> implements GameObjectInOutHandler<T> {

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
        protected abstract void afterLeaveScene(T gameObject);
    }

    protected class PlayerInOutHandler extends AbstractGameObjectInOutHandler<Player>{

        @Override
        protected void beforeEnterScene(Player player) {

        }

        @Override
        protected void afterEnterScene(Player player) {

        }

        @Override
        protected void beforeLeaveScene(Player player) {

        }

        @Override
        protected void afterLeaveScene(Player player) {

        }
    }

    protected class PetInOutHandler extends AbstractGameObjectInOutHandler<Pet>{
        @Override
        protected void beforeEnterScene(Pet pet) {

        }

        @Override
        protected void afterEnterScene(Pet pet) {

        }

        @Override
        protected void beforeLeaveScene(Pet pet) {

        }

        @Override
        protected void afterLeaveScene(Pet pet) {

        }
    }

    protected class NpcInOutHandler extends AbstractGameObjectInOutHandler<Npc>{

        @Override
        protected void beforeEnterScene(Npc npc) {

        }

        @Override
        protected void afterEnterScene(Npc npc) {

        }

        @Override
        protected void beforeLeaveScene(Npc npc) {

        }

        @Override
        protected void afterLeaveScene(Npc npc) {

        }
    }

    // endregion

    // region 刷帧
    /**
     * 游戏对象刷帧模板实现
     * @param <T>
     */
    protected abstract class AbstractGameObjectTickHandler<T extends GameObject> extends GameObjectTickHandler<T>{

        protected AbstractGameObjectTickHandler(int framePerSecond) {
            super(framePerSecond);
        }

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
    }


    protected class PlayerTickHandler extends AbstractGameObjectTickHandler<Player>{

        public PlayerTickHandler(int framePerSecond) {
            super(framePerSecond);
        }

        @Override
        protected void tickHook(Player player) {
            super.tickHook(player);
        }
    }

    protected class PetTickHandler extends AbstractGameObjectTickHandler<Pet>{

        public PetTickHandler(int framePerSecond) {
            super(framePerSecond);
        }

        @Override
        protected void tickHook(Pet pet) {
            super.tickHook(pet);
        }
    }

    protected class NpcTickHandler extends AbstractGameObjectTickHandler<Npc>{

        public NpcTickHandler(int framePerSecond) {
            super(framePerSecond);
        }

        @Override
        protected void tickHook(Npc npc) {
            super.tickHook(npc);
        }
    }
    // endregion
}


