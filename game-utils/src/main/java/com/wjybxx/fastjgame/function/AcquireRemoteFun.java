package com.wjybxx.fastjgame.function;

/**
 * 远程资源申请(主要抛出的异常不同，本地资源申请只允许抛出中断异常)
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/14 20:07
 * @github - https://github.com/hl845740757
 */
@FunctionalInterface
public interface AcquireRemoteFun<T> {

    /**
     * 申请资源，直到成功或中断
     * @param resource
     * @throws Exception
     */
    void acquire(T resource) throws Exception;
}
