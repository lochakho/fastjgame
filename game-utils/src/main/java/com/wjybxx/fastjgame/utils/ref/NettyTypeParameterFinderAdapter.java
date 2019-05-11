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

package com.wjybxx.fastjgame.utils.ref;

import io.netty.util.internal.TypeParameterMatcher;

import java.lang.reflect.*;
import java.util.Objects;

/**
 * 对Netty的泛型参数查找增强(适配)。
 * Netty自带的查找只支持超类中查找，这里进行适配增强，以支持查找接口中声明的泛型参数。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 10:51
 * @github - https://github.com/hl845740757
 */
public class NettyTypeParameterFinderAdapter implements TypeParameterFinder{

    public static final NettyTypeParameterFinderAdapter DEFAULT_INSTANCE=new NettyTypeParameterFinderAdapter();

    private NettyTypeParameterFinderAdapter() {
    }

    @Override
    public <T> Class<?> findTypeParameter(T instance, Class<? super T> superClazzOrInterface, String typeParamName) throws Exception {
        Objects.requireNonNull(instance,"instance");
        Objects.requireNonNull(superClazzOrInterface,"superClazzOrInterface");
        Objects.requireNonNull(typeParamName,"typeParamName");

        if (instance.getClass()==superClazzOrInterface){
            // 仅仅支持查找父类/父接口定义的泛型且被子类声明为具体类型的泛型参数
            throw new IllegalArgumentException("typeParam " + typeParamName + " is declared in self class"
                    + ", instance class " + instance.getClass().getSimpleName()
                    + "\n only support find superClassOrInterface typeParam.");
        }

        if (superClazzOrInterface.isInterface()){
            // 自己实现的在接口中查找泛型参数的具体类型，待严格的测试
            return findInterfaceTypeParameter(instance,superClazzOrInterface,typeParamName);
        }else {
            // netty实现的，保证正确性
            Method method = TypeParameterMatcher.class.getDeclaredMethod("find0", Object.class, Class.class, String.class);
            method.setAccessible(true);//设置为可访问，跳过校验
            return (Class<?>) method.invoke(null, instance, superClazzOrInterface, typeParamName);
        }
    }

    /**
     * 从instance所属的类开始，寻找最近通向泛型接口的通路。
     *
     * @param instance
     * @param parametrizedSuperInterface
     * @param typeParamName
     * @return
     * @throws Exception
     */
    private <T> Class<?> findInterfaceTypeParameter(T instance, Class<? super T> parametrizedSuperInterface, String typeParamName) throws Exception {
        ensureTypeParameterExist(parametrizedSuperInterface,typeParamName);

        @SuppressWarnings("unchecked")
        Class<? super T> currentClass = findInterfaceDirectChildClass(instance,(Class<T>)instance.getClass(),parametrizedSuperInterface);
        return parseTypeParameter(instance,currentClass,parametrizedSuperInterface,typeParamName);
    }

    /**
     * 确保泛型参数在该类型中进行了定义
     * @param superClazz 超类Class对象
     * @param typeParamName 泛型参数名
     */
    private void ensureTypeParameterExist(Class<?> superClazz, String typeParamName){
        TypeVariable<? extends Class<?>>[] typeParameters = superClazz.getTypeParameters();
        for (TypeVariable<? extends Class<?>> typeVariable:typeParameters){
            if (typeVariable.getName().equals(typeParamName)){
                return;
            }
        }
        throw new IllegalArgumentException("typeParamName "  + typeParamName + " is not declared in superClazz " + superClazz.getSimpleName());
    }

    /**
     * 查找接口的任意直接子节点
     *
     * 为什么任意子节点都是正确的呢？
     * 当子类和父类实现相同的接口时，或实现多个接口时，对同一个泛型变量进行约束时，子类的泛型参数约束必定是所有约束的子集。
     * 当指定具体类型以后，任意一条通路结果都是正确的。
     *
     * @param currentClazzOrInterface 递归到的当前类或接口
     * @param parametrizedSuperInterface 起始class继承的接口或实现的接口
     * @return
     */
    private <T> Class<? super T> findInterfaceDirectChildClass(T instance, Class<? super T> currentClazzOrInterface, Class<? super T> parametrizedSuperInterface){
        if (!parametrizedSuperInterface.isAssignableFrom(currentClazzOrInterface)){
            throw new IllegalArgumentException("currentClazzOrInterface=" + currentClazzOrInterface.getSimpleName()
                    + " ,parametrizedSuperInterface=" + parametrizedSuperInterface.getSimpleName());
        }

        // 查询直接实现/继承的接口
        Class<?>[] implementationInterfaces = currentClazzOrInterface.getInterfaces();
        for (Class<?> clazz:implementationInterfaces){
            if (clazz==parametrizedSuperInterface){
                return currentClazzOrInterface;
            }
        }

        // 因为指定了泛型的具体类型，那么任意一个路径达到目标类都能获取到相同结果
        // 如果超类 是 目标类的子类或实现类，就在超类体系中查找，更快更简单
        Class<? super T> superclass = currentClazzOrInterface.getSuperclass();
        if (null != superclass && parametrizedSuperInterface.isAssignableFrom(superclass)){
            return findInterfaceDirectChildClass(instance, superclass,parametrizedSuperInterface);
        }

        // 这里，currentClazzOrInterface继承或实现的接口中必定存在目标接口的子接口
        assert parametrizedSuperInterface.isInterface():"currentClazzOrInterface " + currentClazzOrInterface.getSimpleName() + " i";
        for (Class<?> clazz:implementationInterfaces){
            if (parametrizedSuperInterface.isAssignableFrom(clazz)){
                // 任意一个通路上去
                @SuppressWarnings({"unchecked"})
                Class<? super T> superInterface = (Class<? super T>) clazz;
                return findInterfaceDirectChildClass(instance, superInterface,parametrizedSuperInterface);
            }
        }

        // 这里走不到
        throw new IllegalStateException();
    }

    /**
     * 通过找到的类/接口的声明信息和类对象解析出具体的泛型类型,使用了netty的解析代码，保持尽量少的改动
     * {@link TypeParameterMatcher#find0(Object, Class, String)}
     * @param instance 查找的对象，可能需要递归重新查找
     * @param currentClass 直接孩子(子接口或实现类)
     * @param parametrizedSuperInterface 显示声明指定泛型参数typeParamName的接口
     * @param typeParamName 泛型名字
     * @return
     */
    private <T> Class<?> parseTypeParameter(final T instance,Class<? super T> currentClass,final Class<? super T> parametrizedSuperInterface,final String typeParamName) throws Exception {
        int typeParamIndex = -1;
        // 获取的是声明的泛型变量 类名/接口名之后的<>
        TypeVariable<?>[] typeParams = parametrizedSuperInterface.getTypeParameters();
        for (int i = 0; i < typeParams.length; i ++) {
            if (typeParamName.equals(typeParams[i].getName())) {
                typeParamIndex = i;
                break;
            }
        }

        // 这里其实不可达，因为上面有检查，这是netty源码，保持最少改动
        if (typeParamIndex < 0) {
            throw new IllegalStateException(
                    "unknown type parameter '" + typeParamName + "': " + parametrizedSuperInterface);
        }

        // 这里的实现是在接口中查找，而netty的实现是在超类中查找
        Type genericSuperType = null;
        Class<?>[] extendsInterfaces = currentClass.getInterfaces();
        for (int index = 0; index< extendsInterfaces.length;index++){
            if (extendsInterfaces[index]==parametrizedSuperInterface){
                genericSuperType=currentClass.getGenericInterfaces()[index];
                break;
            }
        }

        assert null!=genericSuperType:"genericSuperType";

        if (!(genericSuperType instanceof ParameterizedType)) {
            // 1.currentClass忽略了该接口中所有泛型参数，会导致获取到不是 ParameterizedType，而是一个普通的class对象
            // 因为忽略了泛型参数，那么就是Object
            return Object.class;
        }

        // 2.currentClass对父接口中至少一个泛型参数进行了保留或指定了具体类型，会导致获取到的是ParameterizedType
        // 获取到的信息是一个Type类型，可以进行嵌套，所有需要对其具体类型进行判断
        Type[] actualTypeParams = ((ParameterizedType) genericSuperType).getActualTypeArguments();
        Type actualTypeParam = actualTypeParams[typeParamIndex];

        if (actualTypeParam instanceof ParameterizedType) {
            // 3.真实类型也是个泛型接口，获取其原始类型 rawType
            actualTypeParam = ((ParameterizedType) actualTypeParam).getRawType();
        }

        if (actualTypeParam instanceof Class) {
            // 4.成功找到
            return (Class<?>) actualTypeParam;
        }

        if (actualTypeParam instanceof GenericArrayType) {
            // 5.泛型参数的真实类型是个数组，获取数组的元素类型
            Type componentType = ((GenericArrayType) actualTypeParam).getGenericComponentType();
            if (componentType instanceof ParameterizedType) {
                componentType = ((ParameterizedType) componentType).getRawType();
            }
            // 这里好像没做完全的处理，但是考虑全面会很复杂
            if (componentType instanceof Class) {
                return Array.newInstance((Class<?>) componentType, 0).getClass();
            }
        }

        if (actualTypeParam instanceof TypeVariable) {
            // 6.真实类型是另一个泛型参数，即子接口仍然用泛型参数表示父接口中的泛型参数，可能原封不动的保留了，也可能添加了边界，也可能换了个名
            // Resolved type parameter points to another type parameter.
            TypeVariable<?> v = (TypeVariable<?>) actualTypeParam;
            if (!(v.getGenericDeclaration() instanceof Class)) {
                // 7.声明新泛型参数(换名后的参数名)的对象如果不是class返回object。 好像不会出现？ 暂时保留，这里还想不到情况
                // 科普：可以声明(定义)泛型变量的有：类/接口 方法 构造器
                return Object.class;
            }

            Class<?> genericDeclarationClass= (Class<?>) v.getGenericDeclaration();
            if (parametrizedSuperInterface.isAssignableFrom(instance.getClass())) {
                // 8.实例对象的某个超类或接口仍然用泛型参数表示目标泛型参数，则需要重新查找被重新定义的泛型参数
                @SuppressWarnings("unchecked")
                Class<? super T> newSuperClazzOrInerface = (Class<? super T>) genericDeclarationClass;
                return findTypeParameter(instance, newSuperClazzOrInerface,v.getName());
            } else {
                // 9.泛型参数来自另一个继承体系？没搞太明白，保留
                return Object.class;
            }
        }

        return fail(instance.getClass(), typeParamName);
    }

    private Class<?> fail(Class<?> type, String typeParamName) {
        throw new IllegalStateException(
                "cannot determine the type of the type parameter '" + typeParamName + "': " + type);
    }
}
