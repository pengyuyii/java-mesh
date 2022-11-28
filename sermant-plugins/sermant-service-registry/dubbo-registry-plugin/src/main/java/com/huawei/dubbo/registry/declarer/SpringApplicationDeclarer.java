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

package com.huawei.dubbo.registry.declarer;

import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 增强SpringApplication类的run方法
 *
 * @author provenceee
 * @since 2022-01-24
 */
public class SpringApplicationDeclarer extends AbstractDeclarer {
    private static final String[] ENHANCE_CLASS = {"org.springframework.context.support.AbstractApplicationContext"};

    private static final String INTERCEPT_CLASS = "com.huawei.dubbo.registry.interceptor.SpringApplicationInterceptor";

    private static final String METHOD_NAME = "publishEvent";

    /**
     * 构造方法
     */
    public SpringApplicationDeclarer() {
        super(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
            InterceptDeclarer.build(MethodMatcher.nameEquals(METHOD_NAME)
                    .and(MethodMatcher.paramTypesEqual("java.lang.Object", "org.springframework.core.ResolvableType")),
                INTERCEPT_CLASS)
        };
    }
}