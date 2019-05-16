package com.wjybxx.fastjgame.test;

import com.wjybxx.fastjgame.mrg.CuratorMrg;
import com.wjybxx.fastjgame.mrg.GameConfigMrg;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/14 19:02
 * @github - https://github.com/hl845740757
 */
public class CuratorTest {

    public static void main(String[] args) throws Exception {
        GameConfigMrg gameConfigMrg = new GameConfigMrg();
        CuratorMrg curatorMrg = new CuratorMrg(gameConfigMrg);
        curatorMrg.start();

        List<ChildData> childrenData = curatorMrg.watchChildren("/mutex", CuratorTest::onEvent);
        childrenData.forEach(CuratorTest::printChild);
    }

    private static void onEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception{
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
