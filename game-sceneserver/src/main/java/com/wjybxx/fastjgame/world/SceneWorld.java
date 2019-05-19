package com.wjybxx.fastjgame.world;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.core.SceneProcessType;
import com.wjybxx.fastjgame.core.onlinenode.SceneNodeData;
import com.wjybxx.fastjgame.misc.HostAndPort;
import com.wjybxx.fastjgame.mrg.*;
import com.wjybxx.fastjgame.mrg.async.S2CSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncS2CSessionMrg;
import com.wjybxx.fastjgame.net.async.S2CSession;
import com.wjybxx.fastjgame.net.async.initializer.TCPServerChannelInitializer;
import com.wjybxx.fastjgame.net.async.initializer.WsServerChannelInitializer;
import com.wjybxx.fastjgame.net.common.CodecHelper;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import com.wjybxx.fastjgame.utils.GameUtils;
import com.wjybxx.fastjgame.utils.ZKPathUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.wjybxx.fastjgame.protobuffer.p_center_scene.p_center_cross_scene_hello;
import static com.wjybxx.fastjgame.protobuffer.p_center_scene.p_center_single_scene_hello;
import static com.wjybxx.fastjgame.protobuffer.p_sync_center_scene.p_center_command_single_scene_active_regions;
import static com.wjybxx.fastjgame.protobuffer.p_sync_center_scene.p_center_command_single_scene_start;

/**
 * SceneServer
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 21:45
 * @github - https://github.com/hl845740757
 */
public class SceneWorld extends WorldCore {

    private static final Logger logger= LoggerFactory.getLogger(SceneWorld.class);

    private final CenterInSceneInfoMrg centerInSceneInfoMrg;
    private final SceneRegionMrg sceneRegionMrg;
    private final SceneWorldInfoMrg sceneWorldInfoMrg;

    @Inject
    public SceneWorld(WorldWrapper worldWrapper, WorldCoreWrapper coreWrapper, CenterInSceneInfoMrg centerInSceneInfoMrg, SceneRegionMrg sceneRegionMrg) {
        super(worldWrapper, coreWrapper);
        this.centerInSceneInfoMrg = centerInSceneInfoMrg;
        this.sceneRegionMrg = sceneRegionMrg;
        this.sceneWorldInfoMrg= (SceneWorldInfoMrg) worldWrapper.getWorldInfoMrg();
    }

    @Override
    protected void registerCodecHelper() throws Exception {
        super.registerCodecHelper();
        // 这里没有使用模板方法是因为不是都有额外的codec要注册，导致太多钩子方法也不好
        // TODO 注册与玩家交互的codec帮助类
    }

    @Override
    protected void registerMessageHandlers() {
        registerRequestMessageHandler(p_center_single_scene_hello.class,centerInSceneInfoMrg::p_center_single_scene_hello_handler);
        registerRequestMessageHandler(p_center_cross_scene_hello.class,centerInSceneInfoMrg::p_center_cross_scene_hello_handler);
    }

    @Override
    protected void registerHttpRequestHandlers() {

    }

    @Override
    protected void registerSyncRequestHandlers() {
        registerSyncRequestHandler(p_center_command_single_scene_start.class,sceneRegionMrg::p_center_command_single_scene_start_handler);
        registerSyncRequestHandler(p_center_command_single_scene_active_regions.class,sceneRegionMrg::p_center_command_scene_active_regions_handler);
    }

    @Override
    protected void registerAsyncSessionLifeAware(S2CSessionMrg s2CSessionMrg) {
        this.s2CSessionMrg.registerLifeCycleAware(RoleType.CENTER, new SessionLifecycleAware<S2CSession>() {
            @Override
            public void onSessionConnected(S2CSession session) {

            }

            @Override
            public void onSessionDisconnected(S2CSession session) {
                centerInSceneInfoMrg.onDisconnect(session.getClientGuid(),SceneWorld.this);
            }
        });
    }

    @Override
    protected void registerSyncSessionLifeAware(SyncS2CSessionMrg syncS2CSessionMrg) {

    }

    @Override
    protected void startHook() throws Exception {
        // 启动场景
        sceneRegionMrg.onWorldStart();
        // 注册到zookeeper
        bindAndRegisterToZK();

    }

    private void bindAndRegisterToZK() throws Exception {
        // 绑定3个内部交互的端口
        HostAndPort tcpHostAndPort = innerAcceptorMrg.bindInnerTcpPort(true);
        HostAndPort syncRpcHostAndPort = innerAcceptorMrg.bindInnerSyncRpcPort(true);
        HostAndPort httpHostAndPort = innerAcceptorMrg.bindInnerHttpPort();

        // 绑定与玩家交互的两个端口 TODO 这里需要和前端确定到底使用什么通信方式，暂时使用服务器之间机制
        CodecHelper codecHelper=codecHelperMrg.getCodecHelper(GameUtils.INNER_CODEC_NAME);
        TCPServerChannelInitializer tcplInitializer=new TCPServerChannelInitializer(netConfigMrg.maxFrameLength(),
                codecHelper,disruptorMrg);
        HostAndPort outerTcpHostAndPort = s2CSessionMrg.bindRange(true,GameUtils.OUTER_TCP_PORT_RANGE,tcplInitializer);

        WsServerChannelInitializer wsInitializer=new WsServerChannelInitializer("/ws",netConfigMrg.maxFrameLength(),
                codecHelper,disruptorMrg);
        HostAndPort outerWebsocketHostAndPort = s2CSessionMrg.bindRange(true,GameUtils.OUTER_WS_PORT_RANGE,wsInitializer);

        SceneNodeData sceneNodeData =new SceneNodeData(tcpHostAndPort.toString(),
                syncRpcHostAndPort.toString(), httpHostAndPort.toString(),
                sceneWorldInfoMrg.getChannelId(),
                outerTcpHostAndPort.toString(),outerWebsocketHostAndPort.toString());

        String parentPath= ZKPathUtils.onlineParentPath(sceneWorldInfoMrg.getWarzoneId());
        String nodeName;
        if (sceneWorldInfoMrg.getSceneProcessType()== SceneProcessType.SINGLE){
            nodeName= ZKPathUtils.buildSingleSceneNodeName(sceneWorldInfoMrg.getPlatformType(),sceneWorldInfoMrg.getServerId(),sceneWorldInfoMrg.getProcessGuid());
        }else {
            nodeName= ZKPathUtils.buildCrossSceneNodeName(sceneWorldInfoMrg.getProcessGuid());
        }
        curatorMrg.createNode(ZKPaths.makePath(parentPath,nodeName), CreateMode.EPHEMERAL,GameUtils.serializeToJsonBytes(sceneNodeData));
    }


    @Override
    protected void tickHook() {

    }

    @Override
    protected void shutdownHook() {

    }
}
