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

import com.huawei.dubbo.register.config.DubboCache;

import org.apache.dubbo.config.ApplicationConfig;

/**
 * 应用配置服务
 *
 * @author provenceee
 * @date 2021/12/31
 */
public class ApplicationConfigServiceImpl implements ApplicationConfigService {
    private static final String INTERFACE_NAME = "getApplication";

    /**
     * 设置注册时的服务名
     *
     * @param obj 增强的类
     * @param result 方法返回值
     */
    @Override
    public void after(Object obj, Object result) {
        if (result instanceof ApplicationConfig) {
            ApplicationConfig config = (ApplicationConfig) result;
            DubboCache.INSTANCE.setServiceName(config.getName());
        }
    }

    @Override
    public String getName() {
        return INTERFACE_NAME;
    }
}