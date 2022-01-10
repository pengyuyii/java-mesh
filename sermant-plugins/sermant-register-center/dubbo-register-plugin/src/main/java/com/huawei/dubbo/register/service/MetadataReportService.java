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

import com.huawei.sermant.core.plugin.service.PluginService;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.config.configcenter.ConfigItem;
import org.apache.dubbo.metadata.MappingListener;
import org.apache.dubbo.metadata.MetadataInfo;
import org.apache.dubbo.metadata.report.identifier.MetadataIdentifier;
import org.apache.dubbo.metadata.report.identifier.ServiceMetadataIdentifier;
import org.apache.dubbo.metadata.report.identifier.SubscriberMetadataIdentifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author provenceee
 * @date 2022/1/7
 */
public interface MetadataReportService extends PluginService {
    void doStoreProviderMetadata(MetadataIdentifier identifier, String serviceDefinitions);

    void doStoreConsumerMetadata(MetadataIdentifier identifier, String serviceParameterString);

    void doSaveMetadata(ServiceMetadataIdentifier identifier, URL url);

    void doRemoveMetadata(ServiceMetadataIdentifier identifier);

    List<String> doGetExportedURLs(ServiceMetadataIdentifier identifier);

    void doSaveSubscriberData(SubscriberMetadataIdentifier identifier, String urlListStr);

    String doGetSubscribedURLs(SubscriberMetadataIdentifier identifier);

    String getServiceDefinition(MetadataIdentifier identifier);

    void publishAppMetadata(SubscriberMetadataIdentifier identifier, MetadataInfo metadataInfo);

    MetadataInfo getAppMetadata(SubscriberMetadataIdentifier identifier, Map<String, String> instanceMetadata);

    Set<String> getServiceAppMapping(String serviceKey, MappingListener listener, URL url);

    Set<String> getServiceAppMapping(String serviceKey, URL url);

    ConfigItem getConfigItem(String serviceKey, String group);

    boolean registerServiceAppMapping(String key, String group, String content, Object ticket);
}
