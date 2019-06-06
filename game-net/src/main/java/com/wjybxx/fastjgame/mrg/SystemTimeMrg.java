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

package com.wjybxx.fastjgame.mrg;

import com.google.inject.Inject;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * 系统时间控制器，非线程安全。
 * 目的为了减少频繁地调用{@link System#currentTimeMillis()}
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 22:06
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public class SystemTimeMrg {
    /**
     * 获取时间策略
     */
    private Strategy strategy = new RealTimeStrategy();

    @Inject
    public SystemTimeMrg() {

    }

    public void tick(long curMillTime){
        strategy.tick(curMillTime);
    }

    /**
     * 获取系统毫秒时间戳
     * @return
     */
    public long getSystemMillTime() {
        return strategy.getSystemMillTime();
    }

    /**
     * 获取系统秒数时间戳
     * @return
     */
    public int getSystemSecTime() {
        return strategy.getSystemSecTime();
    }

    /**
     * 切换到缓存策略
     */
    public void changeToCacheStrategy(){
        this.strategy = new CacheStrategy();
    }

    /**
     * 切换到实时策略
     */
    public void changeToRealTimeStrategy(){
        this.strategy = new RealTimeStrategy();
    }


    @Override
    public String toString() {
        return "SystemTimeMrg{" +
                "strategy=" + strategy +
                '}';
    }

    private interface Strategy {

        void tick(long curMillTime);

        long getSystemMillTime();

        int getSystemSecTime();
    }

    /**
     * 缓存策略,适合游戏运行期间(tick期间)
     */
    private static class CacheStrategy implements Strategy{
        /**
         * 当前tick开始的时间，非实时时间
         */
        private long systemMillTime;
        /**
         * 当前tick开始时对应的秒数
         */
        private int systemSecTime;

        public CacheStrategy() {
            tick(System.currentTimeMillis());
        }

        @Override
        public void tick(long curMillTime) {
            this.systemMillTime=curMillTime;
            this.systemSecTime= (int) (curMillTime/1000);
        }

        @Override
        public long getSystemMillTime() {
            return systemMillTime;
        }

        @Override
        public int getSystemSecTime() {
            return systemSecTime;
        }

        @Override
        public String toString() {
            return "CacheStrategy{" +
                    "systemMillTime=" + systemMillTime +
                    ", systemSecTime=" + systemSecTime +
                    '}';
        }
    }

    /**
     * 实时策略，适合启动和关闭期间
     */
    private static class RealTimeStrategy implements Strategy{

        @Override
        public void tick(long curMillTime) {

        }

        @Override
        public long getSystemMillTime() {
            return System.currentTimeMillis();
        }

        @Override
        public int getSystemSecTime() {
            return (int) (System.currentTimeMillis()/1000);
        }
    }
}
