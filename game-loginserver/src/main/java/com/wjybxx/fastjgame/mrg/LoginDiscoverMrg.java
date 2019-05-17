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
import com.wjybxx.fastjgame.misc.AbstractThreadLifeCycleHelper;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.utils.ZKPathUtils;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.TreeCacheSelector;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 登录服，用于发现所有的CenterServer。
 * 调用{@link TreeCache#close()}的时候会关闭executor...
 * {@link PathChildrenCache}还能选是否关闭，{@link TreeCache}不能选择。
 * 不能随便关闭他人的线程池，所以如果要使用{@link TreeCache}必须新建线程池。
 *
 * 使用{@link TreeCache}逻辑处理起来会简单点，而且登录服没有太多的负载要处理，
 * 创建额外的线程池影响很小。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/17 20:29
 * @github - https://github.com/hl845740757
 */
public class LoginDiscoverMrg extends AbstractThreadLifeCycleHelper {

    private final CuratorMrg curatorMrg;

    private final ConcurrentLinkedQueue<TreeCacheEvent> eventQueue=new ConcurrentLinkedQueue<>();

    private TreeCache treeCache;

    @Inject
    public LoginDiscoverMrg(CuratorMrg curatorMrg) {

        this.curatorMrg = curatorMrg;
    }

    @Override
    protected void startImp() throws Exception {
        treeCache = TreeCache.newBuilder(curatorMrg.getClient(), ZKPathUtils.onlineRootPath())
                .setCreateParentNodes(true)
                .setMaxDepth(2)
                .setExecutor(new LoginWatcherThreadFactory())
                .setSelector(new LoginCacheSelector())
                .build();
        treeCache.getListenable().addListener((client, event) -> eventQueue.offer(event));
        treeCache.start();
    }

    @Override
    protected void shutdownImp() {
        treeCache.close();
    }

    public void tick(){
        TreeCacheEvent e;
        while ((e=eventQueue.poll())!=null){

        }
    }


    /**
     * loginServer专用的watcherThread
     */
    private static class LoginWatcherThreadFactory implements ThreadFactory {

        private final AtomicInteger globalIndex=new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"login_watcher_thread_"+globalIndex.getAndIncrement());
        }
    }

    /**
     * 选择需要的cache
     */
    private static class LoginCacheSelector implements TreeCacheSelector{

        /**
         * 只取回warzone下的节点
         * @param fullPath
         * @return
         */
        @Override
        public boolean traverseChildren(String fullPath) {
            return ZKPathUtils.findNodeName(fullPath).startsWith("warzone");
        }

        /**
         * 只取回CenterServer节点
         * @param fullPath
         * @return
         */
        @Override
        public boolean acceptChild(String fullPath) {
            String nodeName = ZKPathUtils.findNodeName(fullPath);
            RoleType serverType = ZKPathUtils.parseServerType(nodeName);
            return serverType == RoleType.CENTER;
        }
    }
}
