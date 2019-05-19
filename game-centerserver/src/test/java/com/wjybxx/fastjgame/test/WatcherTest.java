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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wjybxx.fastjgame.module.CenterModule;
import com.wjybxx.fastjgame.module.NetModule;
import com.wjybxx.fastjgame.mrg.CuratorMrg;
import com.wjybxx.fastjgame.utils.GameUtils;
import com.wjybxx.fastjgame.utils.ZKPathUtils;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 注意：
 * 客户端无法直接从事件对象{@link org.apache.zookeeper.WatchedEvent}中获取获取到对应数据节点的
 * 原始数据以及变更后的数据，而是需要客户端再次去重新获取数据！！！
 *
 * 中间是有可能出现不一致的情况的，zookeeper只能保证最终一致性，并不能保证捕获到所有事件。
 * 遇见的一次输出：
 * Search "type=" (2 hits in 1 file)
 *   new 1 (2 hits)
 * 	Line 1: type=CHILD_ADDED ,data=592
 * 	Line 34: type=CHILD_REMOVED ,data=1002
 *
 * <pre>
 * A： create 592 -------->  delete 592 ---------> create 1002 -------------> delete 1002
 * B:  ---> notify NodeChildrenChanged ---->  getDataWithUsingWatcher ----> get 1002 trigger create-> notify remove -> trigger remove 1002
 * C:  ---> notify NodeChildrenChanged ---->  getDataWithUsingWatcher --------> get null
 * </pre>
 * 事件是基于返回的数据与本地数据比较产生的：
 * <li>null -> data -> childAdded</li>
 * <li>data -> null -> childRemove</li>
 * <li>data -> newData -> childUpdate</li>
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/19 10:56
 * @github - https://github.com/hl845740757
 */
public class WatcherTest {

    private static final ConcurrentLinkedQueue<PathChildrenCacheEvent> eventQueue=new ConcurrentLinkedQueue<>();

    public static void main(String[] args) throws Exception {
        String logDir=new File("").getAbsolutePath() + File.separator + "log";
        String logFilePath = logDir + File.separator + "watcher.log";
        System.setProperty("logFilePath",logFilePath);

        Injector injector= Guice.createInjector(new NetModule(),new CenterModule());
        CuratorMrg curatorMrg=injector.getInstance(CuratorMrg.class);
        curatorMrg.start();

        String watchPath = ZKPathUtils.onlineParentPath(1);
        List<ChildData> childrenData = curatorMrg.watchChildren(watchPath, (client, event) -> eventQueue.offer(event));

        // 初始监听
        childrenData.forEach(e->onEvent(Type.CHILD_ADDED,e));

        while (true){
            Thread.sleep(50);
            tick();
        }
    }

    private static void tick(){
        PathChildrenCacheEvent event;
        while ((event=eventQueue.poll()) != null){
            onEvent(event.getType(),event.getData());
        }
    }

    private static void onEvent(Type type,ChildData childData){
        if (type == Type.CHILD_ADDED || type==Type.CHILD_REMOVED || type==Type.CHILD_UPDATED){
            System.out.println("type="+type + " ,data=" + GameUtils.parseIntFromStringBytes(childData.getData()));
        }

    }
}
