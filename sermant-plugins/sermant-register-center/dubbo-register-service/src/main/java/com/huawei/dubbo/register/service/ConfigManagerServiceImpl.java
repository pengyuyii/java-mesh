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

package com.huawei.dubbo.register.service;

import com.huawei.dubbo.register.config.DubboCache;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.sermant.core.util.CollectionUtils;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.config.Environment;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author provenceee
 * @date 2022/1/13
 */
public class ConfigManagerServiceImpl implements ConfigManagerService {
    private static final String SC_REGISTRY_PROTOCOL = "sc";

    @Override
    public void after(Object obj, Object[] arguments, Object result) {
        /*if (arguments == null || arguments.length == 0 || !(arguments[0] instanceof Class<?>)) {
            return;
        }
        Class<?> clazz = ((Class<?>) arguments[0]);
        if (!RegistryConfig.class.getName().equals(clazz.getName())) {
            return;
        }
        List<RegistryConfig> tmpConfigs = (List<RegistryConfig>) result;
        AbstractConfigManager configManager = (AbstractConfigManager) obj;
        URL url = URL.valueOf(DubboCache.INSTANCE.getDubboConfig().getAddress().get(0))
                .setProtocol(SC_REGISTRY_PROTOCOL);
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.refresh();
        registryConfig.setId(SC_REGISTRY_PROTOCOL);
        registryConfig.setAddress(url.toString());
        registryConfig.setProtocol(SC_REGISTRY_PROTOCOL);
        registryConfig.setPort(url.getPort());
        configManager.addConfig(registryConfig);
        tmpConfigs.add(registryConfig);*/
        Map<String, String> properties = (Map<String, String>) arguments[0];
        if (CollectionUtils.isEmpty(properties) || StringUtils.isBlank(properties.get("dubbo.registry.address"))) {
            return;
        }
        String protocol = URL.valueOf(properties.get("dubbo.registry.address")).getProtocol();
        Map<String, String> registryProperties = properties.entrySet().stream()
                .filter(e -> e.getKey().startsWith("dubbo.registry."))
                .collect(Collectors.toMap(
                        e -> e.getKey().replace("dubbo.registry.", "dubbo.registries." + protocol + "."),
                        Entry::getValue));
        URL url = URL.valueOf(DubboCache.INSTANCE.getDubboConfig().getAddress().get(0))
                .setProtocol(SC_REGISTRY_PROTOCOL);
        registryProperties.put("dubbo.registries.sc.address", url.toString());
        Environment env = (Environment) obj;
        env.getPropertiesConfiguration().getProperties().putAll(registryProperties);
    }
}
