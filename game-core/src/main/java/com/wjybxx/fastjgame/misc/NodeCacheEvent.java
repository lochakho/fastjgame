package com.wjybxx.fastjgame.misc;

import org.apache.curator.framework.recipes.cache.ChildData;

import javax.annotation.Nullable;

/**
 * NodeCache事件
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/14 14:10
 * @github - https://github.com/hl845740757
 */
public class NodeCacheEvent {
    /**
     * 事件类型
     */
    private final NodeCacheEventType eventType;
    /**
     * 产生事件时节点的最新数据，may null
     */
    private final ChildData nodeData;

    public NodeCacheEvent(NodeCacheEventType eventType, ChildData nodeData) {
        this.eventType = eventType;
        this.nodeData = nodeData;
    }

    public NodeCacheEventType getEventType() {
        return eventType;
    }

    /**
     * 获取产生事件时的节点数据，当事件为{@link NodeCacheEventType#REMOVE}时可能为null
     * @return 当前节点数据
     */
    @Nullable
    public ChildData getNodeData() {
        return nodeData;
    }

    /**
     * NodeCache事件类型
     */
    public enum NodeCacheEventType {
        /**
         * 创建
         */
        CREATE,
        /**
         * 更新
         */
        UPDATE,
        /**
         * 删除
         */
        REMOVE,
    }
}
