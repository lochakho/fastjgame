package com.wjybxx.fastjgame.mrg;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.configwrapper.ConfigWrapper;
import com.wjybxx.fastjgame.utils.ConfigLoader;

import java.io.IOException;

/**
 * 游戏配置控制器，除网络层配置以外的游戏配置都在这
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/12 12:13
 * @github - https://github.com/hl845740757
 */
public class GameConfigMrg {

    private final ConfigWrapper configWrapper;
    /**
     * zookeeper集群地址
     */
    private final String zkConnectString;
    /**
     * zookeeper建立连接超时时间
     */
    private final int zkConnectionTimeoutMs;
    /**
     * zookeeper会话超时时间
     */
    private final int zkSessionTimeoutMs;
    /**
     * zookeeper命名空间
     */
    private final String zkNameSpace;

    @Inject
    public GameConfigMrg() throws IOException {
        configWrapper= ConfigLoader.loadConfig(GameConfigMrg.class.getClassLoader(),"game_config.properties");
        zkConnectString = configWrapper.getAsString("zkConnectString");
        zkConnectionTimeoutMs=configWrapper.getAsInt("zkConnectionTimeoutMs");
        zkSessionTimeoutMs=configWrapper.getAsInt("zkSessionTimeoutMs");
        zkNameSpace=configWrapper.getAsString("zkNameSpace");
    }

    public ConfigWrapper getConfigWrapper() {
        return configWrapper;
    }

    public String getZkConnectString() {
        return zkConnectString;
    }

    public int getZkConnectionTimeoutMs() {
        return zkConnectionTimeoutMs;
    }

    public int getZkSessionTimeoutMs() {
        return zkSessionTimeoutMs;
    }

    public String getZkNameSpace() {
        return zkNameSpace;
    }
}
