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

package com.wjybxx.fastjgame.net.common;

import com.wjybxx.fastjgame.mrg.SystemTimeMrg;
import com.wjybxx.fastjgame.mrg.TimerMrg;
import com.wjybxx.fastjgame.trigger.Timer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 禁用token帮助类。
 * 注意：不能是单例，否则会出现冲突
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/8 23:00
 * @github - https://github.com/hl845740757
 */
public final class ForbiddenTokenHelper {

    private static final Logger logger= LoggerFactory.getLogger(ForbiddenTokenHelper.class);

    private final SystemTimeMrg systemTimeMrg;
    private final TimerMrg timerMrg;
    private final Long2ObjectMap<ForbiddenTokenInfo> forbiddenTokenMap =new Long2ObjectOpenHashMap<>(512);
    /**
     * 禁用多久(过期时间)
     * 作为属性传入以支持不同情况
     */
    private final int forbiddenTimeout;

    public ForbiddenTokenHelper(SystemTimeMrg systemTimeMrg, TimerMrg timerMrg, int forbiddenTimeout) {
        this.systemTimeMrg = systemTimeMrg;
        this.forbiddenTimeout=forbiddenTimeout;
        this.timerMrg=timerMrg;

        // 定时检查释放被禁用的token(1/3个周期检查一次)
        Timer releaseCacheTimer = new Timer(forbiddenTimeout/3 * 1000,
                Integer.MAX_VALUE,
                this::releaseForbiddenToken);

        timerMrg.addTimer(releaseCacheTimer,systemTimeMrg.getSystemMillTime());
    }

    /**
     * 禁用该token之前的token
     */
    public void forbiddenPreToken(Token token){
        forbiddenToken(token.getClientGuid(),token.getCreateSecTime()-1);
    }

    /**
     * 禁用该token及之前的token
     */
    public void forbiddenCurToken(Token token){
        forbiddenToken(token.getClientGuid(),token.getCreateSecTime());
    }

    /**
     * 是否是被禁用的token
     * @return 旧的token则返回true
     */
    public boolean isForbiddenToken(Token token){
        ForbiddenTokenInfo forbiddenTokenInfo= forbiddenTokenMap.get(token.getClientGuid());
        return null!=forbiddenTokenInfo && token.getCreateSecTime() <= forbiddenTokenInfo.getForbiddenCreateTime();
    }

    /**
     * 禁用该时间戳及之前的token
     * @param clientGuid sessionId
     * @param tokenCreateSecTime include
     */
    private void forbiddenToken(long clientGuid, int tokenCreateSecTime){
        int releaseTime=systemTimeMrg.getSystemSecTime() + forbiddenTimeout;
        ForbiddenTokenInfo forbiddenTokenInfo = forbiddenTokenMap.get(clientGuid);
        if (null!=forbiddenTokenInfo){
            if (tokenCreateSecTime<forbiddenTokenInfo.getForbiddenCreateTime()){
                logger.warn("unexpected invoke.");
            }else {
                forbiddenTokenInfo.update(tokenCreateSecTime,releaseTime);
            }
        }else {
            forbiddenTokenMap.put(clientGuid,new ForbiddenTokenInfo(tokenCreateSecTime,releaseTime));
        }
    }

    /**
     * 释放token，不能无限期缓存
     * @param timer
     */
    private void releaseForbiddenToken(Timer timer){
        forbiddenTokenMap.values().removeIf(e -> systemTimeMrg.getSystemSecTime() > e.getReleaseTime());
    }
}
