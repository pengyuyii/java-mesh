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

import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

import org.apache.dubbo.metadata.MappingChangedEvent;
import org.apache.dubbo.metadata.MappingListener;
import org.apache.dubbo.metadata.ServiceNameMapping;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author provenceee
 * @date 2022/1/10
 */
public class MappingDynamicConfigListener implements DynamicConfigListener {
    private static final Logger LOGGER = LogFactory.getLogger();

    private final String serviceKey;

    private final Set<MappingListener> listeners;

    /**
     * 构造方法
     *
     * @param serviceKey 缓存的服务名
     */
    public MappingDynamicConfigListener(String serviceKey) {
        this.serviceKey = serviceKey;
        this.listeners = new HashSet<>();
    }

    public void addListener(MappingListener listener) {
        listeners.add(listener);
    }

    @Override
    public void process(DynamicConfigEvent event) {
        if (DynamicConfigEventType.DELETE != event.getEventType()) {
            if (serviceKey.equals(event.getKey())) {
                Set<String> apps = ServiceNameMapping.getAppNames(event.getContent());
                MappingChangedEvent mappingChangedEvent = new MappingChangedEvent(serviceKey, apps);
                listeners.forEach(listener -> listener.onEvent(mappingChangedEvent));
            }
        }
    }
}
