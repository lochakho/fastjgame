package com.wjybxx.fastjgame.holder;

/**
 * 引用类型实例持有者，在lambda表达式中使用
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/14 16:03
 * @github - https://github.com/hl845740757
 */
public class ObjectHolder<T> {

    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
