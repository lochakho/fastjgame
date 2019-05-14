package com.wjybxx.fastjgame.function;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/14 10:58
 * @github - https://github.com/hl845740757
 */
@FunctionalInterface
public interface AcquireFun<T> {
    /**
     * 申请资源，直到成功或中断
     * @param resource
     * @throws InterruptedException
     */
    void acquire(T resource) throws InterruptedException;
}
