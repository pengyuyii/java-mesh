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

package com.huaweicloud.integration.service;

import com.huaweicloud.integration.domain.Test;

import com.alibaba.dubbo.config.RegistryConfig;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 测试接口
 *
 * @author provenceee
 * @since 2022-04-28
 */
public class FooServiceImpl implements FooService {
    @Autowired
    private RegistryConfig registryConfig;

    @Override
    public String foo(String str) {
        return "foo:" + str;
    }

    @Override
    public String foo2(String str) {
        return "foo2:" + str;
    }

    @Override
    public String getRegistryProtocol() {
        return registryConfig.getProtocol();
    }

    @Override
    public Test test(Test test) {
        return test;
    }
}