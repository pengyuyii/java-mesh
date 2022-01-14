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

import com.huawei.sermant.core.util.CollectionUtils;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.config.configcenter.ConfigItem;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.metadata.MetadataService;
import org.apache.dubbo.metadata.report.MetadataReport;
import org.apache.dubbo.metadata.report.MetadataReportInstance;
import org.apache.dubbo.rpc.model.ApplicationModel;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author provenceee
 * @date 2022/1/13
 */
public class NameMappingServiceImpl implements NameMappingService {
    @Override
    public void after(Object obj, Object[] arguments, Object result) {
        ApplicationModel applicationModel;
        try {
            Field field = obj.getClass().getSuperclass().getDeclaredField("applicationModel");
            field.setAccessible(true);
            applicationModel = (ApplicationModel) field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (CollectionUtils.isEmpty(applicationModel.getApplicationConfigManager().getMetadataConfigs())) {
            return;
        }
        URL url = (URL) arguments[0];
        String serviceInterface = url.getServiceInterface();
        if (MetadataService.SERVICE_INTERFACE_NAME.equals(serviceInterface)) {
            return;
        }
        Map<String, MetadataReport> map;
        try {
            Field field = obj.getClass().getDeclaredField("metadataReportInstance");
            field.setAccessible(true);
            MetadataReportInstance instance = (MetadataReportInstance) field.get(obj);
            map = instance.getMetadataReports(false);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String appName = applicationModel.getApplicationName();
        for (MetadataReport metadataReport : map.values()) {
            String newConfigContent = appName;
            try {
                ConfigItem configItem = metadataReport.getConfigItem(serviceInterface, "mapping");
                String oldConfigContent = configItem.getContent();
                if (StringUtils.isNotEmpty(oldConfigContent)) {
                    boolean contains = StringUtils.isContains(oldConfigContent, appName);
                    if (contains) {
                        continue;
                    }
                    newConfigContent = oldConfigContent + "," + newConfigContent;
                }
                metadataReport.registerServiceAppMapping(serviceInterface, "mapping", newConfigContent,
                        configItem.getTicket());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
