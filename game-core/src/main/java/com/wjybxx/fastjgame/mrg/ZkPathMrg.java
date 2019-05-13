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
     * 名字空间
     * @return
     */
    public String nameSpace(){
        return "global";
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

}
