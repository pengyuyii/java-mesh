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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author provenceee
 * @date 2022/1/10
 */
public class MappingDynamicConfigListener implements DynamicConfigListener {
    private static final Logger LOGGER = LogFactory.getLogger();

    private final Map<String, Set<MappingListener>> listeners;

    public MappingDynamicConfigListener() {
        listeners = new ConcurrentHashMap<>();
    }

    public void addListener(String serviceKey, MappingListener listener) {
        listeners.computeIfAbsent(serviceKey, k -> new HashSet<>()).add(listener);
    }

    @Override
    public void process(DynamicConfigEvent event) {
        if (DynamicConfigEventType.DELETE != event.getEventType()) {
            if (listeners.get(event.getKey()) != null) {
                Set<String> apps = ServiceNameMapping.getAppNames(event.getContent());
                MappingChangedEvent mappingChangedEvent = new MappingChangedEvent(event.getKey(), apps);
                listeners.get(event.getKey()).forEach(listener -> listener.onEvent(mappingChangedEvent));
            }
        }
    }
}
