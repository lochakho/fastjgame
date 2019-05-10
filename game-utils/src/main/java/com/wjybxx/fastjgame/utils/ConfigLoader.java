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

package com.wjybxx.fastjgame.utils;

import com.wjybxx.fastjgame.utils.configwrapper.ConfigWrapper;
import com.wjybxx.fastjgame.utils.configwrapper.PropertiesWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 配置文件加载器(properties文件)
 * 会在 本地文件夹(GameConfigDir) 和 classPath(resources文件夹)尝试加载配置文件。
 * 当一个参数在两个配置文件都存在时，本地文件夹中的参数生效。
 * => 旨在可以使用外部配置文件代替jar包内配置。
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/26 22:36
 */
public final class ConfigLoader {

    private static final Logger logger=LoggerFactory.getLogger(ConfigLoader.class);
    /**
     * 游戏文件夹(加载文件时，优先从该文件夹中加载文件，其次从resources下寻找)
     * 默认文件夹路径在jar包所在文件夹的config文件夹下
     * ./config
     */
    public static final String GAME_CONFIG_DIR;

    static {
        final String defaultGameConfigDir=new File("").getAbsolutePath() + File.separator + "config";
        GAME_CONFIG_DIR =System.getProperty("gameConfigDir",defaultGameConfigDir);
    }

    private ConfigLoader() {

    }

    /**
     * 优先在当前运行环境目录下寻找，如果当前运行环境不存在，则在jar环境下寻找。
     * 该方法仅仅是一个简便方法，使用ConfigLoader的classLoader来加载文件。
     * @param fileName
     * @return
     * @throws IOException
     */
    public static ConfigWrapper loadConfig(String fileName) throws IOException {
        return loadConfig(ConfigLoader.class.getClassLoader(),fileName);
    }

    /**
     * 会在 本地文件夹(GameConfigDir) 和 classPath(resources文件夹)尝试加载配置文件。
     * 当一个参数在两个配置文件都存在时，本地文件夹中的参数生效。
     *
     * @param classLoader 指定classLoader
     * @param fileName 配置文件名字
     * @return 如果成功加载，则返回其包装对象
     * @throws IOException 找不到文件或加载文件错误
     */
    public static ConfigWrapper loadConfig(ClassLoader classLoader, String fileName) throws IOException {
        // 当前运行环境目录下寻找
        ConfigWrapper cfgFromGameConfigDir=null;
        try{
            cfgFromGameConfigDir = loadCfgFromGameConfigDir(fileName);
        }catch (IOException e){
            // ignore e
            logger.info("load {} from gameConfigDir failed",fileName);
        }
        // jar包环境下寻找
        ConfigWrapper cfgFromJarResources=null;
        try {
            cfgFromJarResources=loadCfgFromJarResources(classLoader,fileName);
        }catch (IOException e){
            logger.info("load {} from jarResources failed",fileName);
        }
        // 两个配置文件都不存在
        if (cfgFromGameConfigDir == null && cfgFromJarResources == null){
            throw new FileNotFoundException(fileName);
        }
        // 两个都存在，需要合并(gameConfig下的替换jar包中的)
        if (cfgFromGameConfigDir != null && cfgFromJarResources != null){
            return cfgFromJarResources.convert2MapWrapper().replaceAll(cfgFromGameConfigDir.convert2MapWrapper());
        }
        // 哪个存在返回哪个
        if (cfgFromGameConfigDir != null){
            return cfgFromGameConfigDir;
        }else {
            return cfgFromJarResources;
        }
    }

    /**
     * 优先在游戏配置文件夹中寻找
     * @return nullable
     * @param fileName
     * @throws IOException if file not found
     */
    public static ConfigWrapper loadCfgFromGameConfigDir(String fileName) throws IOException {
        String path= GAME_CONFIG_DIR + File.separator + fileName;
        logger.info("loadCfgFromGameConfigDir {}",path);

        File file=new File(path);
        if (file.exists() && file.isFile()){
            try(FileInputStream fileInputStream=new FileInputStream(file);
                InputStreamReader inputStreamReader=new InputStreamReader(fileInputStream, StandardCharsets.UTF_8)){
                Properties properties=new Properties();
                properties.load(inputStreamReader);
                return new PropertiesWrapper(properties);
            }
        }
        throw new FileNotFoundException(fileName);
    }

    /**
     * 在jar包resources下环境中寻找
     * @return nullable
     * @param classLoader 运行环境根路径
     * @param fileName
     * @throws IOException if file not found
     */
    public static ConfigWrapper loadCfgFromJarResources(ClassLoader classLoader, String fileName) throws IOException {
        logger.info("loadCfgFromJarResources {}",fileName);
        URL resource = classLoader.getResource(fileName);
        if (resource==null){
            throw new FileNotFoundException(fileName);
        }
        try (InputStream inputStream=resource.openStream()){
            Properties properties=new Properties();
            properties.load(inputStream);
            return new PropertiesWrapper(properties);
        }
    }
}
