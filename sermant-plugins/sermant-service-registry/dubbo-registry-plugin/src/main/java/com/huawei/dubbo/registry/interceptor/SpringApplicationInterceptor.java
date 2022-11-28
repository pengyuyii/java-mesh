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

package com.huawei.dubbo.registry.interceptor;

import com.huawei.dubbo.registry.service.RegistryService;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.service.ServiceManager;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextStartedEvent;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 增强SpringApplication类的run方法
 *
 * @author provenceee
 * @since 2022-01-24
 */
public class SpringApplicationInterceptor extends AbstractInterceptor {
    private static final AtomicBoolean INIT = new AtomicBoolean();

    private final RegistryService registryService;

    /**
     * 构造方法
     */
    public SpringApplicationInterceptor() {
        registryService = ServiceManager.getService(RegistryService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        Object obj = context.getArguments()[0];
        if (obj == null) {
            return context;
        }
        String name = obj.getClass().getCanonicalName();
        if ("org.springframework.context.event.ContextStartedEvent".equals(name)) {
            start(((ContextStartedEvent) obj).getApplicationContext());
        }
        if ("org.springframework.boot.context.event.ApplicationStartedEvent".equals(name)) {
            start(((ApplicationStartedEvent) obj).getApplicationContext());
        }
        return context;
    }

    private void start(ApplicationContext applicationContext) {
        if (applicationContext.getParent() != null && INIT.compareAndSet(false, true)) {
            registryService.startRegistration();
        }
    }
}