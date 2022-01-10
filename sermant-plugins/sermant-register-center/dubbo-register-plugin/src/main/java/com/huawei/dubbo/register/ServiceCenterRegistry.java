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

import com.huawei.dubbo.register.service.RegistryService;
import com.huawei.sermant.core.service.ServiceManager;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * sc注册
 *
 * @author provenceee
 * @date 2021/12/15
 */
public class ServiceCenterRegistry extends FailbackRegistry {
    private static final String CONSUMER_PROTOCOL_PREFIX = "consumer";

    private final List<URL> registryUrls;

    private final RegistryService registryService;

    /**
     * 构造方法
     *
     * @param url url
     */
    public ServiceCenterRegistry(URL url) {
        super(url);
        registryUrls = new ArrayList<>();
        registryService = ServiceManager.getService(RegistryService.class);
        registryService.setServiceCenterRegistry(this);
    }

    @Override
    public void doRegister(URL url) {
        if (!CONSUMER_PROTOCOL_PREFIX.equals(url.getProtocol())) {
            registryUrls.add(url);
        }
    }

    @Override
    public void doUnregister(URL url) {
        registryService.shutdown();
    }

    @Override
    public void doSubscribe(URL url, NotifyListener notifyListener) {
        registryService.doSubscribe(url, notifyListener);
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener notifyListener) {
        registryService.shutdown();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    public List<URL> getRegistryUrls() {
        return registryUrls;
    }
}