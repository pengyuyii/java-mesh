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

import com.huawei.dubbo.register.MappingDynamicConfigListener;
import com.huawei.dubbo.register.config.DubboCache;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.dynamicconfig.DynamicConfigService;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.config.configcenter.ConfigItem;
import org.apache.dubbo.metadata.MappingListener;
import org.apache.dubbo.metadata.MetadataInfo;
import org.apache.dubbo.metadata.MetadataService;
import org.apache.dubbo.metadata.ServiceNameMapping;
import org.apache.dubbo.metadata.report.identifier.BaseMetadataIdentifier;
import org.apache.dubbo.metadata.report.identifier.MetadataIdentifier;
import org.apache.dubbo.metadata.report.identifier.ServiceMetadataIdentifier;
import org.apache.dubbo.metadata.report.identifier.SubscriberMetadataIdentifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author provenceee
 * @date 2022/1/7
 */
public class MetadataReportServiceImpl implements MetadataReportService {
    private static final String REGISTRY_GROUP = "metadataType=registry";

    private static final String MAPPING_GROUP = "metadataType=mapping";

    private DynamicConfigService configService;

    private RegistryService registryService;

    private MappingDynamicConfigListener listener;

    @Override
    public void start() {
        configService = ServiceManager.getService(DynamicConfigService.class);
        listener = new MappingDynamicConfigListener();
        configService.addGroupListener(MAPPING_GROUP, listener, true);
    }

    @Override
    public void doStoreProviderMetadata(MetadataIdentifier identifier, String serviceDefinitions) {
        String serviceInterface = identifier.getServiceInterface();
        if (!MetadataService.SERVICE_INTERFACE_NAME.equals(serviceInterface)) {
            JSONObject json = JSONObject.parseObject(serviceDefinitions);
            Map<String, String> parameters = json.getObject("parameters", new TypeReference<HashMap<String, String>>() {
            });
            URL url;
            if (DubboCache.INSTANCE.getAddress() == null) {
                url = new URL("dubbo", "127.0.0.1", 0, parameters);
            } else {
                url = URL.valueOf(DubboCache.INSTANCE.getAddress()).setProtocol("dubbo").addParameters(parameters);
            }
            if (registryService == null) {
                registryService = ServiceManager.getService(RegistryService.class);
            }
            registryService.getDiscoveryUrls().add(url.setPath(serviceInterface));
        }
        configService.removeConfig(getIdentifierKey(identifier), REGISTRY_GROUP);
        configService.publishConfig(getIdentifierKey(identifier), REGISTRY_GROUP, serviceDefinitions);
    }

    @Override
    public void doStoreConsumerMetadata(MetadataIdentifier identifier, String serviceParameterString) {
        configService.removeConfig(getIdentifierKey(identifier), REGISTRY_GROUP);
        configService.publishConfig(getIdentifierKey(identifier), REGISTRY_GROUP, serviceParameterString);
    }

    @Override
    public void doSaveMetadata(ServiceMetadataIdentifier identifier, URL url) {
        configService.removeConfig(getIdentifierKey(identifier), REGISTRY_GROUP);
        configService.publishConfig(getIdentifierKey(identifier), REGISTRY_GROUP, url.toFullString());
    }

    @Override
    public void doRemoveMetadata(ServiceMetadataIdentifier identifier) {
        configService.removeConfig(getIdentifierKey(identifier), REGISTRY_GROUP);
    }

    @Override
    public List<String> doGetExportedURLs(ServiceMetadataIdentifier identifier) {
        String content = configService.getConfig(getIdentifierKey(identifier), REGISTRY_GROUP);
        return StringUtils.isBlank(content) ? Collections.emptyList() : Collections.singletonList(content);
    }

    @Override
    public void doSaveSubscriberData(SubscriberMetadataIdentifier identifier, String urlListStr) {
        configService.removeConfig(getIdentifierKey(identifier), REGISTRY_GROUP);
        configService.publishConfig(getIdentifierKey(identifier), REGISTRY_GROUP, urlListStr);
    }

    @Override
    public String doGetSubscribedURLs(SubscriberMetadataIdentifier identifier) {
        return configService.getConfig(getIdentifierKey(identifier), REGISTRY_GROUP);
    }

    @Override
    public String getServiceDefinition(MetadataIdentifier identifier) {
        return configService.getConfig(getIdentifierKey(identifier), REGISTRY_GROUP);
    }

    @Override
    public void publishAppMetadata(SubscriberMetadataIdentifier identifier, MetadataInfo metadataInfo) {
        configService.removeConfig(getIdentifierKey(identifier), REGISTRY_GROUP);
        configService.publishConfig(getIdentifierKey(identifier), REGISTRY_GROUP,
                JSONObject.toJSONString(metadataInfo));
    }

    @Override
    public MetadataInfo getAppMetadata(SubscriberMetadataIdentifier identifier, Map<String, String> instanceMetadata) {
        return JSONObject.parseObject(configService.getConfig(getIdentifierKey(identifier), REGISTRY_GROUP),
                MetadataInfo.class);
    }

    @Override
    public Set<String> getServiceAppMapping(String serviceKey, MappingListener listener, URL url) {
        this.listener.addListener(serviceKey, listener);
        return getServiceAppMapping(serviceKey, url);
    }

    @Override
    public Set<String> getServiceAppMapping(String serviceKey, URL url) {
        return ServiceNameMapping.getAppNames(configService.getConfig(serviceKey, MAPPING_GROUP));
    }

    @Override
    public ConfigItem getConfigItem(String serviceKey, String group) {
        String info = configService.getConfig(serviceKey, MAPPING_GROUP);
        return new ConfigItem(info, StringUtils.isBlank(info) ? "" : DigestUtils.sha256Hex(info));
    }

    @Override
    public boolean registerServiceAppMapping(String key, String group, String content, Object ticket) {
        // 这里需要cas
        configService.removeConfig(key, MAPPING_GROUP);
        return configService.publishConfig(key, MAPPING_GROUP, content);
    }

    private String getIdentifierKey(BaseMetadataIdentifier identifier) {
        //kie配置的key值不支持冒号（:），所以替换成点（.）
        return identifier.getIdentifierKey().replace(":", ".");
    }
}
