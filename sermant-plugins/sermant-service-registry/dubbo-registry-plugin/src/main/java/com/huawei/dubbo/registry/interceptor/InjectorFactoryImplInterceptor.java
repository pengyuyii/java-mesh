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

package com.huawei.dubbo.registry.interceptor;

import com.huawei.dubbo.registry.utils.ReflectUtils;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.metadata.ResourceLocator;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * 增强ApplicationConfig类的setName方法
 *
 * @author provenceee
 * @since 2021-11-08
 */
public class InjectorFactoryImplInterceptor extends AbstractInterceptor {
    private static final String METHOD_INJECTOR_IMPL_CLASS_NAME = "com.huawei.dubbo.registry.interceptor.MethodInjectorImpl";

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        ResourceLocator method = (ResourceLocator) arguments[0];
        ResteasyProviderFactory factory = (ResteasyProviderFactory) arguments[1];
        if (method.getParams() == null || method.getParams().length < 2) {
            return context;
        }
        try {
            Optional<Class<?>> clazz = ReflectUtils.defineClass(METHOD_INJECTOR_IMPL_CLASS_NAME);
            if (clazz.isPresent()) {
                // 由于plugin不能直接new宿主的接口实现类，所以只能手动new出来给宿主
                context.skip(clazz.get().getConstructor(ResourceLocator.class, ResteasyProviderFactory.class)
                    .newInstance(method, factory));
            }
            context.skip(new com.huawei.dubbo.registry.interceptor.MethodInjectorImpl(method, factory));
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
            | InvocationTargetException e) {
            context.skip(new com.huawei.dubbo.registry.interceptor.MethodInjectorImpl(method, factory));
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
