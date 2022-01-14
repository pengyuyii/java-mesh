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

package com.huawei.dubbo.register;

import com.huawei.dubbo.register.config.DubboCache;
import com.huawei.dubbo.register.service.RegistryService;
import com.huawei.sermant.core.service.ServiceManager;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.client.AbstractServiceDiscovery;
import org.apache.dubbo.registry.client.ServiceInstance;
import org.apache.dubbo.registry.client.event.listener.ServiceInstancesChangedListener;

import java.util.List;
import java.util.Set;

/**
 * @author provenceee
 * @date 2022/1/4
 */
public class ServiceCenterServiceDiscovery extends AbstractServiceDiscovery {
    private URL registryUrl;

//    private final DiscoveryService discoveryService;

    private final RegistryService registryService;

    public ServiceCenterServiceDiscovery() {
//        discoveryService = ServiceManager.getService(DiscoveryService.class);
        registryService = ServiceManager.getService(RegistryService.class);
    }

    @Override
    public void doInitialize(URL registryUrl) throws Exception {
        this.registryUrl = registryUrl;
//        discoveryService.init(registryUrl);
        registryService.init(registryUrl);
    }

    @Override
    public void doRegister(ServiceInstance serviceInstance) throws RuntimeException {
//        discoveryService.doRegister(serviceInstance);
        registryService.getDiscoveryUrls().replaceAll(url -> url = url.setAddress(serviceInstance.getAddress()));
        DubboCache.INSTANCE.setAddress(serviceInstance.getAddress());
    }

    @Override
    public void doUpdate(ServiceInstance serviceInstance) throws RuntimeException {
        ServiceInstance oldInstance = this.serviceInstance;
        this.unregister(oldInstance);
        this.register(serviceInstance);
    }

    @Override
    public void doUnregister(ServiceInstance serviceInstance) {
//        discoveryService.doUnregister();
    }

    @Override
    public void doDestroy() throws Exception {
//        discoveryService.doDestroy();
    }

    @Override
    public Set<String> getServices() {
        return registryService.getServices();
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceName) throws NullPointerException {
        return registryService.getInstances(serviceName);
    }

    @Override
    public void addServiceInstancesChangedListener(ServiceInstancesChangedListener listener)
            throws NullPointerException, IllegalArgumentException {
        registryService.addServiceInstancesChangedListener(listener, registryUrl);
    }

    @Override
    public URL getUrl() {
        return registryUrl;
    }
}
