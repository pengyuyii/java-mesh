/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.transmit.declarer;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.router.common.config.TransmitConfig;

/**
 * 拦截Executor
 *
 * @author provenceee
 * @since 2023-04-20
 */
public class ExecutorDeclarer extends AbstractPluginDeclarer {
    private static final String ENHANCE_CLASS = "java.util.concurrent.Executor";

    private static final String INTERCEPT_CLASS
            = "com.huaweicloud.sermant.router.transmit.interceptor.ExecutorInterceptor";

    private static final String[] METHOD_NAME = {"execute", "submit"};

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.isExtendedFrom(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameContains(METHOD_NAME).and(MethodMatcher.isPublicMethod()),
                        INTERCEPT_CLASS)
        };
    }

    @Override
    public boolean isEnabled() {
        TransmitConfig pluginConfig = PluginConfigManager.getPluginConfig(TransmitConfig.class);
        return pluginConfig.isEnabledThread() || pluginConfig.isEnabledThreadPool();
    }
}