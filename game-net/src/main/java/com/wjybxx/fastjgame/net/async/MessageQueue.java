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

package com.wjybxx.fastjgame.net.async;

import com.wjybxx.fastjgame.misc.LongSequencer;

import java.util.LinkedList;

/**
 * 消息队列，可与tcp的收发缓冲区比较
 * 消息视图大致如下：
 *
 * |---------------------------  nextSequence
 * | sentQueue | needSendQueue | ↓
 * | --------------------------|
 * |    0~n    |      0~n      |
 * |---------------------------
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 11:43
 * @github - https://github.com/hl845740757
 */
public final class MessageQueue {
    /**
     * 初始ACK
     */
    public static final int INIT_ACK = 0;

    /**
     * 序号分配器
     */
    private LongSequencer sequencer=new LongSequencer(INIT_ACK);
    /**
     * 接收到对方的最大消息编号
     */
    private long ack = INIT_ACK;
    /**
     * 已发送待确认的消息，只要发送过就不会再放入 {@link #needSendQueue}
     */
    private final LinkedList<Message> sentQueue=new LinkedList<>();
    /**
     * 待发送的消息,还没有尝试发送过的消息
     */
    private final LinkedList<Message> needSendQueue=new LinkedList<>();


    // -----------------对方返回的ack
    // 每次返回的Ack不小于上次返回的ack,不大于发出去的最大消息号

    /**
     * 对方发送过来的ack是否有效
     * @param ack
     * @return
     */
    public boolean isAckOK(long ack){
        return ack>= getAckLowerBound() && ack<= getAckUpperBound();
    }

    /**
     * 获取上一个已确认的消息号
     * @return
     */
    private long getAckLowerBound(){
        // 有已发送未确认的消息，那么它的上一个就是ack下界
        if (sentQueue.size()>0){
            return sentQueue.getFirst().getSequence()-1;
        }
        // 已发送的都已确认，那么待发送的上一个就是ack下界
        if (needSendQueue.size()>0){
            return needSendQueue.getFirst().getSequence()-1;
        }
        // 都已确认，且没有新消息，那么上次分配的就是ack下界
        return sequencer.get();
    }

    /**
     * 获取下一个可能的最大ackGuid，
     * @return
     */
    private long getAckUpperBound(){
        // 有已发送待确认的消息，那么它的最后一个就是可能的ack上界
        if (sentQueue.size()>0){
            return sentQueue.getLast().getSequence();
        }
        // 已发送的都已确认，那么待发送的上一个就是ack的上界
        if (needSendQueue.size()>0){
            return needSendQueue.getFirst().getSequence()-1;
        }
        // 都已确认，且没有新消息，那么上次分配的就是ack上界
        return sequencer.get();
    }


    /**
     * 根据对方发送的ack更新已发送队列
     * @param ack
     */
    public void updateSentQueue(long ack){
        if (!isAckOK(ack)){
            throw new IllegalArgumentException(generateAckErrorInfo(ack));
        }
        while (sentQueue.size()>0){
            if (sentQueue.getFirst().getSequence()>ack){
                break;
            }
            sentQueue.removeFirst();
        }
    }

    /**
     * 生成ack信息
     * @param ack 服务器发送来的ack
     * @return
     */
    public String generateAckErrorInfo(long ack) {
        return String.format("{ack=%d, lastAckGuid=%d, nextMaxAckGuid=%d}", ack, getAckLowerBound(), getAckUpperBound());
    }

    /**
     * 分配下一个消息guid
     * @return
     */
    public long nextSequence(){
        return sequencer.incAndGet();
    }


    public long getAck() {
        return ack;
    }

    public void setAck(long ack) {
        this.ack = ack;
    }

    public LinkedList<Message> getSentQueue() {
        return sentQueue;
    }

    public LinkedList<Message> getNeedSendQueue() {
        return needSendQueue;
    }

    /**
     * 获取当前缓存的消息数
     * @return
     */
    public int getCacheMessageNum(){
        return sentQueue.size() + needSendQueue.size();
    }

    @Override
    public String toString() {
        return "MessageQueue{" +
                "sequencer=" + sequencer +
                ", ack=" + ack +
                ", sentQueueSize=" + sentQueue.size() +
                ", needSendQueueSize=" + needSendQueue.size() +
                "}";
    }
}
