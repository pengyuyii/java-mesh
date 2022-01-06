/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

import org.apache.dubbo.config.RegistryConfig;

import java.util.List;

/**
 * 注册配置服务
 *
 * @author provenceee
 * @date 2021/12/31
 */
public class RegistryConfigServiceImpl implements RegistryConfigService {
    private static final String SC_REGISTRY_PROTOCOL = "sc";

    private static final String DUBBO_REGISTRIES_CONFIG_PREFIX = "dubbo.registries.";

    private static final String INTERFACE_NAME = "setRegistries";

    /**
     * 多注册中心注册到sc
     *
     * @param obj 增强的类
     * @param result 方法返回值
     */
    @Override
    public void after(Object obj, Object result) {
//        if (obj instanceof AbstractInterfaceConfig) {
//            AbstractInterfaceConfig config = (AbstractInterfaceConfig) obj;
//            List<RegistryConfig> registries = config.getRegistries();
//            if (registries == null) {
//                registries = new ArrayList<>();
//            }
//            if (hasScRegistryConfig(registries)) {
//                // 如果存在sc的注册配置，就不再重复加载sc注册配置了
//                return;
//            }
//            URL url = URL.valueOf(DubboCache.INSTANCE.getAddress()).setProtocol(SC_REGISTRY_PROTOCOL);
//            RegistryConfig registryConfig = new RegistryConfig(url.toString());
//            registryConfig.setId(SC_REGISTRY_PROTOCOL);
//            registryConfig.setPrefix(DUBBO_REGISTRIES_CONFIG_PREFIX);
//            registries.add(registryConfig);
//        }
    }

    @Override
    public String getName() {
        return INTERFACE_NAME;
    }

    private boolean hasScRegistryConfig(List<RegistryConfig> registries) {
        for (RegistryConfig registry : registries) {
            if (registry != null && SC_REGISTRY_PROTOCOL.equals(registry.getId())) {
                return true;
            }
        }
        return false;
    }
}