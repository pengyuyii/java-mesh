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

import com.huawei.dubbo.register.service.MetadataReportService;
import com.huawei.sermant.core.service.ServiceManager;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.config.configcenter.ConfigItem;
import org.apache.dubbo.metadata.MappingListener;
import org.apache.dubbo.metadata.MetadataInfo;
import org.apache.dubbo.metadata.report.identifier.MetadataIdentifier;
import org.apache.dubbo.metadata.report.identifier.ServiceMetadataIdentifier;
import org.apache.dubbo.metadata.report.identifier.SubscriberMetadataIdentifier;
import org.apache.dubbo.metadata.report.support.AbstractMetadataReport;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author provenceee
 * @date 2022/1/6
 */
public class ServiceCenterMetadataReport extends AbstractMetadataReport {
    private final MetadataReportService metadataReportService;

    public ServiceCenterMetadataReport(URL url) {
        super(url);
        metadataReportService = ServiceManager.getService(MetadataReportService.class);
    }

    @Override
    protected void doStoreProviderMetadata(MetadataIdentifier identifier, String serviceDefinitions) {
        metadataReportService.doStoreProviderMetadata(identifier, serviceDefinitions);
    }

    @Override
    protected void doStoreConsumerMetadata(MetadataIdentifier identifier, String serviceParameterString) {
        metadataReportService.doStoreConsumerMetadata(identifier, serviceParameterString);
    }

    @Override
    protected void doSaveMetadata(ServiceMetadataIdentifier identifier, URL url) {
        metadataReportService.doSaveMetadata(identifier, url);
    }

    @Override
    protected void doRemoveMetadata(ServiceMetadataIdentifier identifier) {
        metadataReportService.doRemoveMetadata(identifier);
    }

    @Override
    protected List<String> doGetExportedURLs(ServiceMetadataIdentifier identifier) {
        return metadataReportService.doGetExportedURLs(identifier);
    }

    @Override
    protected void doSaveSubscriberData(SubscriberMetadataIdentifier identifier, String urlListStr) {
        metadataReportService.doSaveSubscriberData(identifier, urlListStr);
    }

    @Override
    protected String doGetSubscribedURLs(SubscriberMetadataIdentifier identifier) {
        return metadataReportService.doGetSubscribedURLs(identifier);
    }

    @Override
    public String getServiceDefinition(MetadataIdentifier identifier) {
        return metadataReportService.getServiceDefinition(identifier);
    }

    @Override
    public void publishAppMetadata(SubscriberMetadataIdentifier identifier, MetadataInfo metadataInfo) {
        metadataReportService.publishAppMetadata(identifier, metadataInfo);
    }

    @Override
    public MetadataInfo getAppMetadata(SubscriberMetadataIdentifier identifier, Map<String, String> instanceMetadata) {
        return metadataReportService.getAppMetadata(identifier, instanceMetadata);
    }

    @Override
    public Set<String> getServiceAppMapping(String serviceKey, MappingListener listener, URL url) {
        return metadataReportService.getServiceAppMapping(serviceKey, listener, url);
    }

    @Override
    public Set<String> getServiceAppMapping(String serviceKey, URL url) {
        return metadataReportService.getServiceAppMapping(serviceKey, url);
    }

    @Override
    public ConfigItem getConfigItem(String serviceKey, String group) {
        return metadataReportService.getConfigItem(serviceKey, group);
    }

    @Override
    public boolean registerServiceAppMapping(String key, String group, String content, Object ticket) {
        return metadataReportService.registerServiceAppMapping(key, group, content, ticket);
    }
}
