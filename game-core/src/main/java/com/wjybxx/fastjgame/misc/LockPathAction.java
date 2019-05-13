package com.wjybxx.fastjgame.misc;

/**
 * 在锁住路径之后,这是一个同步操作
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/13 10:07
 * @github - https://github.com/hl845740757
 */
@FunctionalInterface
public interface LockPathAction {
    /**
     * 执行操作
     * @param lockPath 锁定的节点
     */
    void doAction(String lockPath) throws Exception;
}
