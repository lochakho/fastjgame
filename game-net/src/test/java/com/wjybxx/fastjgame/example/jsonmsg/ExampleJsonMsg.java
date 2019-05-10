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

package com.wjybxx.fastjgame.example.jsonmsg;

/**
 * json消息外部内
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/28 13:50
 * @github - https://github.com/hl845740757
 */
public final class ExampleJsonMsg {

    /**
     * 登录请求
     */
    public static class LoginRequest{

        private final String name;

        private final int accountId;

        public LoginRequest(String name, int accountId) {
            this.name = name;
            this.accountId = accountId;
        }

        public String getName() {
            return name;
        }

        public int getAccountId() {
            return accountId;
        }

        @Override
        public String toString() {
            return "LoginRequest{" +
                    "name='" + name + '\'' +
                    ", accountId=" + accountId +
                    '}';
        }
    }

    /**
     * 登录请求的响应
     */
    public static class LoginResponse{

        private final String name;

        private final int accountId;

        private final boolean success;

        public LoginResponse(String name, int accountId, boolean success) {
            this.name = name;
            this.accountId = accountId;
            this.success = success;
        }

        public String getName() {
            return name;
        }

        public int getAccountId() {
            return accountId;
        }

        public boolean isSuccess() {
            return success;
        }

        @Override
        public String toString() {
            return "LoginResponse{" +
                    "name='" + name + '\'' +
                    ", accountId=" + accountId +
                    ", success=" + success +
                    '}';
        }
    }
}
