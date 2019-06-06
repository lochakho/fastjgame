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

package com.wjybxx.fastjgame.trigger;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 15:01
 * @github - https://github.com/hl845740757
 */
public class Timer {
    /**
     * 上次执行的时间戳
     */
    private long lastExecuteMillTime;
    /**
     * 执行间隔(毫秒)
     * 只有在执行回调的时候修改delayTime是安全的，
     * 其它时候修改delayTime都是不安全的，它会可能破坏存储结构,因此禁止随意的修改timer延迟时间
     * 若需要修改延迟时间，要么回调的时候修改，要么删除原timer，添加新timer
     */
    private long delayTime;
    /**
     * 剩余执行次数
     */
    private int executeNum;
    /**
     * 执行回调(策略模式的好处是，同一种类型的timer可以有多种类型的回调)
     * 当然也可以不回调，自身完成某个任务
     */
    private final TimerCallBack callBack;

    /**
     * 创建一个定时器实例
     * @param delayTime 执行间隔(毫秒)
     * @param executeNum 执行次数
     * @param callBack 回调函数
     */
    public Timer(long delayTime, int executeNum, TimerCallBack callBack) {
        this.delayTime = delayTime;
        this.executeNum = executeNum;
        this.callBack = callBack;
    }

    long getLastExecuteMillTime() {
        return lastExecuteMillTime;
    }

    void setLastExecuteMillTime(long lastExecuteMillTime) {
        this.lastExecuteMillTime = lastExecuteMillTime;
    }

    public long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(long delayTime) {
        this.delayTime = delayTime;
    }

    public int getExecuteNum() {
        return executeNum;
    }

    public void setExecuteNum(int executeNum) {
        this.executeNum = executeNum;
    }

    public TimerCallBack getCallBack() {
        return callBack;
    }

    /**
     * 获取下次执行的时间戳
     * @return
     */
    public long getNextExecuteMillTime(){
        return lastExecuteMillTime + delayTime;
    }

    /**
     * 关闭timer
     */
    public void closeTimer(){
        this.executeNum =0;
    }

    /**
     * 创建一个无限执行的timer
     * @see #Timer(long, int, TimerCallBack)
     * @return executeNum == Integer.MAX_VALUE
     */
    public static Timer newInfiniteTimer(long delayTime,TimerCallBack callBack){
        return new Timer(delayTime,Integer.MAX_VALUE,callBack);
    }
}
