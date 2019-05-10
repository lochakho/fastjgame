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

package com.wjybxx.fastjgame.net.async.event;

import io.netty.channel.Channel;

import java.io.Closeable;

/**
 * 网络事件对象，由{@link com.lmax.disruptor.RingBuffer} 调用
 * {@link NetEventFactory#newInstance()}创建，并一直重用。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 9:15
 */
public class NetEvent implements Closeable {

    /**
     * 产生网络事件的channel
     */
    private Channel channel=null;
    /**
     * 网路事件类型
     */
    private NetEventType eventType=null;
    /**
     * 网路事件参数（内容）
     */
    private NetEventParam netEventParam =null;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public NetEventType getEventType() {
        return eventType;
    }

    public void setEventType(NetEventType eventType) {
        this.eventType = eventType;
    }

    public NetEventParam getNetEventParam() {
        return netEventParam;
    }

    public void setNetEventParam(NetEventParam netEventParam) {
        this.netEventParam = netEventParam;
    }

    /**
     * help GC.
     * 在disruptor中，Event对象是反复重用的。我们尽可能早的释放这些对象。
     * @throws Exception
     */
    @Override
    public void close() {
        channel=null;
        eventType=null;
        netEventParam =null;
    }
}
