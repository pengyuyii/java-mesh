/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.dubbo.register.interceptor;

import com.huawei.dubbo.register.service.ApplicationConfigService;
import com.huawei.dubbo.register.service.ConfigService;
import com.huawei.dubbo.register.service.RegistryConfigService;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.service.ServiceManager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 增强AbstractInterfaceConfig类的getApplication方法
 *
 * @author provenceee
 * @since 2021年11月8日
 */
public class InterfaceConfigInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final Map<String, ConfigService> SERVICE_MAP = new HashMap<>();

    /**
     * 构造方法
     */
    public InterfaceConfigInterceptor() {
        ApplicationConfigService applicationConfigService = ServiceManager.getService(ApplicationConfigService.class);
        SERVICE_MAP.put(applicationConfigService.getName(), applicationConfigService);
        RegistryConfigService registryConfigService = ServiceManager.getService(RegistryConfigService.class);
        SERVICE_MAP.put(registryConfigService.getName(), registryConfigService);
    }

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        ConfigService configService = SERVICE_MAP.get(method.getName());
        if (configService != null) {
            configService.after(obj, result);
        }
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "AbstractInterfaceConfig is error!", throwable);
    }
}