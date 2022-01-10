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

import com.huawei.dubbo.register.ServiceCenterRegistry;
import com.huawei.dubbo.register.config.DubboConfig;
import com.huawei.sermant.core.plugin.service.PluginService;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.client.ServiceInstance;
import org.apache.dubbo.registry.client.event.listener.ServiceInstancesChangedListener;

import java.util.List;
import java.util.Set;

/**
 * 注册服务
 *
 * @author provenceee
 * @date 2021/12/15
 */
public interface RegistryService extends PluginService {
    void startRegistration(DubboConfig config);

    void doSubscribe(URL url, NotifyListener notifyListener);

    void shutdown();

    void setServiceCenterRegistry(ServiceCenterRegistry registry);

    void addServiceInstancesChangedListener(ServiceInstancesChangedListener listener, URL registryUrl);

    Set<String> getServices();

    List<ServiceInstance> getInstances(String serviceName);

    void init(URL registryUrl);
}
