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

import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import com.wjybxx.fastjgame.enummapper.*;
import com.wjybxx.fastjgame.ref.NettyTypeParameterFinderAdapter;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 反射工具类
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 10:30
 * @github - https://github.com/hl845740757
 */
public class ReflectionUtils {

    private static final Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);

    private ReflectionUtils(){
        // close
    }


    /**
     * 查找父类/父接口定义的且被子类声明为具体类型的泛型参数的具体类型
     *
     * @param instance superClazzOrInterface的子类实例
     * @param superClazzOrInterface 泛型参数typeParamName存在的类,class或interface
     * @param typeParamName 泛型参数名字
     * @param <T> 约束必须有继承关系或实现关系
     * @return 如果定义的泛型存在，则返回对应的泛型clazz
     */
    public static <T> Class<?> findTypeParameter(T instance, Class<? super T> superClazzOrInterface, String typeParamName) throws Exception {
        return NettyTypeParameterFinderAdapter.DEFAULT_INSTANCE.findTypeParameter(instance,superClazzOrInterface,typeParamName);
    }

    /**
     * 寻找protoBuf消息的parser对象
     * 优先尝试protoBuf 3.x版本
     * 其次尝试protoBuf 2.x版本
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Parser<T> findParser(Class<T> clazz) throws ReflectiveOperationException{
        Objects.requireNonNull(clazz);
        try {
            // protoBuf3获取parser的静态方法 parser();
            Method method = clazz.getDeclaredMethod("parser");
            if (null != method) {
                method.setAccessible(true);
                return (Parser<T>) method.invoke(null);
            }
        }catch (Exception e){
            logger.info("not protoBuf 3.x");
        }
        try {
            // proto2 静态parser域,public的
            Field field = clazz.getDeclaredField("PARSER");
            field.setAccessible(true);
            return (Parser<T>) field.get(null);
        } catch (Exception e) {
            logger.info("not protoBuf 2.x");
        }
        throw new ReflectiveOperationException("invalid protocol buffer class " + clazz.getSimpleName());
    }

    /**
     * 获取proto文件定义的文件的完整类名
     * @param javaPackageName 文件中的java_package
     * @param javaOuterClassName 文件中的java_outer_classname
     * @param messageName 消息体名字
     * @param <T>
     * @return
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends MessageLite> Class<T> findMessageClass(String javaPackageName, String javaOuterClassName, String messageName) throws ClassNotFoundException {
        String classFullName=javaPackageName+"."+javaOuterClassName+"$"+messageName;
        return (Class<T>)Class.forName(classFullName);
    }

    /**
     * 根据枚举的values建立索引；
     * 该方法的开销相对小，代码量也能省下；
     * @param values 枚举数组
     * @param <T> 枚举类型
     * @return unmodifiable
     */
    @SuppressWarnings("unchecked")
    public static <T extends NumberEnum> NumberEnumMapper<T> indexNumberEnum(T[] values){
        if (values.length == 0){
            return (NumberEnumMapper<T>) EmptyMapper.INSTANCE;
        }

        // 存在一定的浪费，判定重复用
        Int2ObjectMap<T> result = FastCollectionsUtils.newEnoughCapacityIntMap(values.length);
        int minNumber = values[0].getNumber();
        int maxNumber = values[0].getNumber();

        for (T t : values){
            FastCollectionsUtils.requireNotContains(result,t.getNumber(),"number");
            result.put(t.getNumber(),t);

            minNumber = Math.min(minNumber,t.getNumber());
            maxNumber = Math.max(maxNumber,t.getNumber());
        }

        if (ArrayBasedMapper.available(minNumber,maxNumber)){
            return new ArrayBasedMapper<>(values,minNumber,maxNumber);
        }else {
            return new MapBasedMapper<>(result);
        }
    }
}
