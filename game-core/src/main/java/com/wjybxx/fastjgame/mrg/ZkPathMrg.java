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
import org.apache.curator.utils.PathUtils;

/**
 * zookeeper中使用的节点路径
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/12 14:21
 * @github - https://github.com/hl845740757
 */
public class ZkPathMrg {

    @Inject
    public ZkPathMrg() {
    }

    /**
     * 获取全局锁路径
     * @return
     */
    private String globalLockPath() {
        return "/globalLock";
    }

    /**
     * 返回全局guidIndex所在路径
     * @return
     */
    public String guidIndexPath(){
        return "/mutex/guid/guidIndex";
    }

    /**
     * 找到一个合适的锁节点，锁的范围越小越好。
     * 对于非根节点来说，使用它的父节点路径。
     * 对应根节点来说，使用全局锁位置。
     * @param path 查询的路径
     * @return 一个合适的加锁路径
     */
    public String findAppropriateLockPath(String path){
        PathUtils.validatePath(path);
        int delimiterIndex = path.lastIndexOf("/");
        if (delimiterIndex == 0){
            return globalLockPath();
        }
        return path.substring(0,delimiterIndex);
    }

    /**
     *
     * @param path 路径参数，不可以是根节点("/")
     * @return
     */
    public String findParentPath(String path){
        PathUtils.validatePath(path);
        int delimiterIndex = path.lastIndexOf("/");
        // root(nameSpace)
        if (delimiterIndex == 0){
            throw new IllegalArgumentException("path " + path + " is root parent");
        }
        return path.substring(0,delimiterIndex);
    }

}
