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

package com.wjybxx.fastjgame.misc;

import org.bson.Document;

/**
 * 索引文档，帮助方便的创建索引(比较器)，屏蔽mongo内部实现，避免外部使用大量的1和-1。
 * 使用流式语法会更加方便。
 * <pre>
 * {@code
 *     IndexDocument indexDocument=new IndexDocument("name",true)
 *                                  .thenComparing("age",true);
 * }
 * </pre>
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/22 11:47
 * @github - https://github.com/hl845740757
 */
public class IndexDocument {
    /**
     * 在mongo中升序使用1表示
     */
    private static final int ASCENDING_INDEX=1;
    /**
     * 在mongo中降序使用-1表示
     */
    private static final int DESCENDING_INDEX=-1;

    private final Document document=new Document();

    public IndexDocument() {

    }

    /**
     * 创建一个索引文档
     * @param firstFiled 第一个比较字段
     * @param ascending 是否升序
     */
    public IndexDocument(String firstFiled,boolean ascending) {
        thenComparing(firstFiled,ascending);
    }

    /**
     * 接下来比较什么字段
     * @param filed 然后比较什么字段
     * @param ascending 是否升序
     * @return this
     */
    public IndexDocument thenComparing(String filed, boolean ascending){
        if (document.containsKey(filed)){
            throw new IllegalArgumentException(filed);
        }
        document.append(filed,ascending?ASCENDING_INDEX:DESCENDING_INDEX);
        return this;
    }

    /**
     * 内部使用
     */
    public Document getDocument() {
        return document;
    }
}
