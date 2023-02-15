/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 增强ContextFilter类的invoke方法
 *
 * @author provenceee
 * @since 2022-09-26
 */
public class TestDeclarer extends AbstractDeclarer {
    private static final String[] ENHANCE_CLASS = {"org.apache.dubbo.registry.zookeeper.ZookeeperRegistry"};

    private static final String INTERCEPT_CLASS
        = "com.huaweicloud.sermant.router.dubbo.interceptor.TestInterceptor";

    /**
     * 构造方法
     */
    public TestDeclarer() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, null);
    }

    /**
     * 获取方法匹配器
     *
     * @return 方法匹配器
     */
    @Override
    public MethodMatcher getMethodMatcher() {
        return MethodMatcher.isConstructor();
    }
}
