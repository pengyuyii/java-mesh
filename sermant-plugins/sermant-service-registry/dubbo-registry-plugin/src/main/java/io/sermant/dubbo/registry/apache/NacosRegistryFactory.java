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

package io.sermant.dubbo.registry.apache;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.dubbo.registry.cache.DubboCache;
import io.sermant.dubbo.registry.service.nacos.NacosRegistryService;
import io.sermant.dubbo.registry.utils.ReflectUtils;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.support.AbstractRegistryFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * NACOS registered factory
 *
 * @author chengyouling
 * @since 2022-10-25
 */
public class NacosRegistryFactory extends AbstractRegistryFactory {
    private static final String APACHE_REGISTRY_CLASS_NAME = "io.sermant.dubbo.registry.apache.NacosRegistry";

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final NacosRegistryService registryService = PluginServiceManager.getPluginService(
            NacosRegistryService.class);

    @Override
    protected Registry createRegistry(URL url) {
        DubboCache.INSTANCE.setUrlClass(url.getClass());
        try {
            Optional<Class<?>> registryClass = ReflectUtils.defineClass(APACHE_REGISTRY_CLASS_NAME);
            Map<String, String> parameters = url.getParameters();
            registryService.buildNamingService(parameters);
            if (registryClass.isPresent()) {
                // Since the plugin cannot directly instantiate the host's interface implementation class,
                // it can only be instantiated manually to the host
                return (Registry) registryClass.get().getConstructor(URL.class).newInstance(url);
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            LOGGER.log(Level.WARNING, "Can not get the registry", e);
        }
        return new NacosRegistry(url);
    }
}