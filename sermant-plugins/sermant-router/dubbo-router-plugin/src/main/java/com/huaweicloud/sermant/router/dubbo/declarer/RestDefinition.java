/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.dubbo.declarer;

/**
 * 增强ClusterUtils类的mergeUrl方法
 *
 * @author provenceee
 * @since 2021-06-28
 */
public class RestDefinition extends AbstractDeclarer {
    private static final String[] ENHANCE_CLASS = {"com.alibaba.dubbo.rpc.protocol.rest.RestProtocol"};

    private static final String INTERCEPT_CLASS
        = "com.huaweicloud.sermant.router.dubbo.interceptor.RestInterceptor";

    private static final String METHOD_NAME = "doExport";

    /**
     * 构造方法
     */
    public RestDefinition() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, METHOD_NAME);
    }
}