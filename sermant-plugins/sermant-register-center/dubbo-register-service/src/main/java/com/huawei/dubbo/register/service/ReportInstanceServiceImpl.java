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

import org.apache.dubbo.config.MetadataReportConfig;
import org.apache.dubbo.metadata.report.MetadataReport;
import org.apache.dubbo.metadata.report.MetadataReportInstance;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author provenceee
 * @date 2022/1/13
 */
public class ReportInstanceServiceImpl implements ReportInstanceService {
    @Override
    public void before(Object obj, Object[] arguments) {
        MetadataReportInstance instance = (MetadataReportInstance) obj;
        Map<String, MetadataReport> map = instance.getMetadataReports(false);
        MetadataReportConfig config = (MetadataReportConfig) arguments[0];
        if (map.get(config.getProtocol()) != null) {
            return;
        }
        config.setRegistry(config.getProtocol());
        try {
            Field field = obj.getClass().getDeclaredField("init");
            field.setAccessible(true);
            AtomicBoolean init = (AtomicBoolean) field.get(obj);
            init.set(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
