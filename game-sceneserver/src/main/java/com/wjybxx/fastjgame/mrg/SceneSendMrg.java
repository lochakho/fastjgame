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
import com.wjybxx.fastjgame.constants.NetConstants;
import com.wjybxx.fastjgame.misc.PlatformType;
import com.wjybxx.fastjgame.mrg.async.S2CSessionMrg;
import com.wjybxx.fastjgame.scene.ViewGrid;
import com.wjybxx.fastjgame.scene.gameobject.GameObject;
import com.wjybxx.fastjgame.scene.gameobject.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

/**
 * 对会话信息的封装，提供良好的接口，方便使用；
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/4 18:22
 * @github - https://github.com/hl845740757
 */
public class SceneSendMrg {

    private static final Logger logger = LoggerFactory.getLogger(SceneSendMrg.class);

    /**
     * CenterServer 和 Player 连接Scene，而scene不主动发起连接，因此作为服务器方
     */
    private final S2CSessionMrg s2CSessionMrg;
    /**
     * center服在scene服中的信息
     */
    private final CenterInSceneInfoMrg centerInSceneInfoMrg;

    @Inject
    public SceneSendMrg(S2CSessionMrg s2CSessionMrg, CenterInSceneInfoMrg centerInSceneInfoMrg) {
        this.s2CSessionMrg = s2CSessionMrg;
        this.centerInSceneInfoMrg = centerInSceneInfoMrg;
    }

    /**
     * 发送消息给玩家
     * @param player 玩家
     * @param msg 消息
     */
    public void sendToPlayer(Player player,Object msg){
        s2CSessionMrg.send(player.getGuid(),msg);
    }

    /**
     * 发送到玩家所在的center服
     * @param player 玩家
     * @param msg 消息
     */
    public void sendToCenter(Player player,Object msg){
        sendToCenter(player.getPlatformType(),player.getActualServerId(),msg);
    }

    /**
     * 发送到指定中心服
     * @param platformType 中心服所在的平台
     * @param serverId 中心服的id
     * @param msg 消息
     */
    public void sendToCenter(PlatformType platformType,int serverId,Object msg){
        long centerGuid = centerInSceneInfoMrg.getCenterGuid(platformType, serverId);
        if (NetConstants.isInvalid(centerGuid)){
            logger.warn("send to disconnected center {}-{}",platformType,serverId);
            return;
        }
        s2CSessionMrg.send(centerGuid,msg);
    }

    /**
     * 广播指定对象视野内的所有玩家，如果gameobject为玩家，包括自己
     * @param gameObject 广播中心对象
     * @param msg 广播消息
     */
    public void broadcastPlayer(GameObject gameObject,Object msg){
        ViewGrid centerViewGrid = gameObject.getViewGrid();
        broadcastPlayer(centerViewGrid.getViewableGrids(), msg);
    }

    /**
     * 广播指定视野格子的玩家
     * @param viewGrids 视野格子
     * @param msg 消息
     */
    public void broadcastPlayer(List<ViewGrid> viewGrids, Object msg){
        for (ViewGrid  viewGrid: viewGrids){
            if (viewGrid.getPlayerNum() <= 0){
                continue;
            }
            for (Player player : viewGrid.getPlayerSet()){
                sendToPlayer(player,msg);
            }
        }
    }

    /**
     * 广播指定对象视野内的所有玩家，去除掉指定玩家
     * @param gameObject 广播中心对象
     * @param msg 消息对象
     * @param exceptPlayer 不广播该玩家
     */
    public void broadcastPlayerExcept(GameObject gameObject,Object msg,Player exceptPlayer){
        ViewGrid centerViewGrid = gameObject.getViewGrid();
        broadcastPlayerExcept(centerViewGrid.getViewableGrids(), msg, exceptPlayer);
    }

    /**
     * 广播指定视野格子内的玩家，去除指定玩家
     * @param viewGrids 视野格子
     * @param msg 消息
     * @param exceptPlayer 去除的玩家
     */
    public void broadcastPlayerExcept(List<ViewGrid> viewGrids,Object msg,Player exceptPlayer){
        for (ViewGrid  viewGrid: viewGrids){
            if (viewGrid.getPlayerNum() <= 0){
                continue;
            }
            for (Player player : viewGrid.getPlayerSet()){
                // 去除指定玩家
                if (player == exceptPlayer){
                    continue;
                }
                sendToPlayer(player,msg);
            }
        }
    }

    /**
     * 广播指定对象视野内的所有玩家，去除掉指定条件的玩家
     * @param gameObject 广播中心对象
     * @param msg 广播消息
     * @param except 排除条件，true的不广播
     * {@link com.wjybxx.fastjgame.misc.SceneBroadcastFilters}可能会有帮助
     */
    public void broadcastPlayerExcept(GameObject gameObject, Object msg, Predicate<Player> except){
        ViewGrid centerViewGrid = gameObject.getViewGrid();
        broadcastPlayerExcept(centerViewGrid.getViewableGrids(), msg, except);
    }

    /**
     * 广播指定视野格子的玩家，去除掉指定条件的玩家
     * @param viewGrids 指定的视野格子
     * @param msg 消息
     * @param except 排除条件，true的不广播
     * {@link com.wjybxx.fastjgame.misc.SceneBroadcastFilters}可能会有帮助
     */
    public void broadcastPlayerExcept(List<ViewGrid> viewGrids, Object msg, Predicate<Player> except){
        for (ViewGrid  viewGrid: viewGrids){
            if (viewGrid.getPlayerNum() <= 0){
                continue;
            }
            for (Player player : viewGrid.getPlayerSet()){
                if (!except.test(player)){
                    sendToPlayer(player,msg);
                }
            }
        }
    }
}
