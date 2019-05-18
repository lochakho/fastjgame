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

package com.wjybxx.fastjgame.test;

import com.wjybxx.fastjgame.mrg.CuratorMrg;
import com.wjybxx.fastjgame.mrg.GameConfigMrg;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;

import java.nio.charset.StandardCharsets;

/**
 * 测试ZKUI的中文是否ok
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/18 11:20
 * @github - https://github.com/hl845740757
 */
public class ZKUICharsetTest {
    public static void main(String[] args) throws Exception {
        GameConfigMrg gameConfigMrg = new GameConfigMrg();
        CuratorMrg curatorMrg = new CuratorMrg(gameConfigMrg);
        curatorMrg.start();

        TreeCache treeCache = TreeCache
                .newBuilder(curatorMrg.getClient(), "/")
                .build();
        treeCache.getListenable().addListener(ZKUICharsetTest::onEvent);
        treeCache.start();

        Thread.sleep(5000);
    }

    private static void onEvent(CuratorFramework client, TreeCacheEvent event) throws Exception{
        ChildData childData = event.getData();
        if (childData==null){
            System.out.println(String.format("thread=%s, eventType=%s",
                    Thread.currentThread().getName(),
                    event.getType()));
        }else {
            System.out.println(String.format("thread=%s, eventType=%s, path=%s, data=%s",
                    Thread.currentThread().getName(),
                    event.getType(),
                    childData.getPath(),
                    new String(childData.getData(), StandardCharsets.UTF_8)));
        }
    }

    private static void printChild(ChildData childData){
        System.out.println(String.format("childData: path=%s, data=%s",childData.getPath(),
                new String(childData.getData(),StandardCharsets.UTF_8)));
    }

}
