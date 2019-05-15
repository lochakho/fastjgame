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

package com.wjybxx.fastjgame.protobuffer;

/**
 * 消息枚举，自动生成的文件。
 * 工具来自于项目: - https://github.com/hl845740757/netty-any-protobuf
 *
 * @author wjybxx
 * @version 1.0
 * @github - https://github.com/hl845740757
 */
public enum MessageEnum {

    P_GAME_LOCAL_SCENE_HELLO(232500781,"com.wjybxx.fastjgame.protobuffer","p_game_scene","p_game_local_scene_hello"),
    P_GAME_LOCAL_SCENE_HELLO_RESULT(1855404655,"com.wjybxx.fastjgame.protobuffer","p_game_scene","p_game_local_scene_hello_result"),
    P_GAME_CROSS_SCENE_HELLO(2047790978,"com.wjybxx.fastjgame.protobuffer","p_game_scene","p_game_cross_scene_hello"),
    P_GAME_CROSS_SCENE_HELLO_RESULT(-141500550,"com.wjybxx.fastjgame.protobuffer","p_game_scene","p_game_cross_scene_hello_result"),
    P_GAME_COMMAND_LOCAL_SCENE_START(-1697102903,"com.wjybxx.fastjgame.protobuffer","p_sync_game_scene","p_game_command_local_scene_start"),
    P_GAME_COMMAND_LOCAL_SCENE_START_RESULT(-361734829,"com.wjybxx.fastjgame.protobuffer","p_sync_game_scene","p_game_command_local_scene_start_result"),
    P_GAME_COMMAND_SCENE_ACTIVE_REGIONS(2010428779,"com.wjybxx.fastjgame.protobuffer","p_sync_game_scene","p_game_command_scene_active_regions"),
    P_GAME_COMMAND_SCENE_ACTIVE_REGIONS_RESULT(-616484495,"com.wjybxx.fastjgame.protobuffer","p_sync_game_scene","p_game_command_scene_active_regions_result"),
    ;
    /**
     * 消息id，必须唯一
     */
    private final int messageId;
    /**
     * java包名
     */
    private final String javaPackageName;

    /**
     * java外部类名字
     */
    private final String javaOuterClassName;

    /**
     * 消息名(类简单名)
     * {@link Class#getSimpleName()}
     */
    private final String messageName;

    MessageEnum(int messageId, String javaPackageName, String javaOuterClassName, String messageName) {
        this.messageId = messageId;
        this.javaPackageName = javaPackageName;
        this.javaOuterClassName = javaOuterClassName;
        this.messageName = messageName;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getJavaPackageName() {
        return javaPackageName;
    }

    public String getJavaOuterClassName() {
        return javaOuterClassName;
    }

    public String getMessageName() {
        return messageName;
    }

    @Override
    public String toString() {
        return "MessageEnum{" +
                "messageId=" + messageId +
                ", javaPackageName='" + javaPackageName + '\'' +
                ", javaOuterClassName='" + javaOuterClassName + '\'' +
                ", messageName='" + messageName + '\'' +
                '}';
    }
}
