package com.wjybxx.fastjgame.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/14 1:02
 * @github - https://github.com/hl845740757
 */
public class ConcurrentUtils {

    private ConcurrentUtils() {
    }

    public static void awaitUninterruptibly(CountDownLatch countDownLatch){
        boolean interrupted=false;
        try {
            while (countDownLatch.getCount() != 0){
                try {
                    countDownLatch.await();
                }catch (InterruptedException e){
                    interrupted=true;
                }
            }
        }finally {
            if(interrupted){
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 带有心跳的等待(保持线程的活性，否则可能导致某些资源关闭，如socket)
     * @param countDownLatch 闭锁
     * @param heartbeat 心跳间隔
     * @param timeUnit 时间单位
     */
    public static void awaitWithHeartBeat(CountDownLatch countDownLatch, long heartbeat, TimeUnit timeUnit){
        boolean interrupted=false;
        try {
            while (countDownLatch.getCount() != 0){
                try {
                    countDownLatch.await(heartbeat,timeUnit);
                }catch (InterruptedException e){
                    interrupted=true;
                }
            }
        }finally {
            if(interrupted){
                Thread.currentThread().interrupt();
            }
        }
    }

    public static <T> void awaitWithHeartBeat(T resource, TryAwaitFun<T> awaitFun, TrySuccessFun<T> trySuccessFun){
        boolean interrupted=false;
        try {
            while (!trySuccessFun.isSuccess(resource)){
                try {
                    awaitFun.acquire(resource);
                }catch (InterruptedException e){
                    interrupted=true;
                }
            }
        }finally {
            if(interrupted){
                Thread.currentThread().interrupt();
            }
        }
    }

    @FunctionalInterface
    public interface TryAwaitFun<T>{

        void acquire(T resource) throws InterruptedException;

    }

    @FunctionalInterface
    public interface TrySuccessFun<T>{

        boolean isSuccess(T resource);
    }
}
