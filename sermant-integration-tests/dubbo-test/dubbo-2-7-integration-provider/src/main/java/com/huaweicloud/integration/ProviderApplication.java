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

package com.huaweicloud.integration;

import com.huaweicloud.integration.controller.ConsumerController;

import org.apache.dubbo.config.RegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;

/**
 * 启动类
 *
 * @author provenceee
 * @since 2022-04-27
 */
@SpringBootApplication
@ImportResource({"classpath:dubbo/provider.xml"})
@ComponentScan(excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, value = ConsumerController.class))
//@PropertySource(value = "${registry.config:classpath:registry.properties}", ignoreResourceNotFound = true,
//    encoding = "UTF-8")
public class ProviderApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderApplication.class);

    /**
     * spring启动类
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        LOGGER.info("====================start=======================");
        SpringApplication.run(ProviderApplication.class);
        LOGGER.info("=====================end========================");
    }

    /**
     * 注册配置
     *
     * @return 注册配置
     */
    @Bean
    @ConditionalOnProperty(value = "has.default.registry", havingValue = "true", matchIfMissing = true)
    public RegistryConfig registryConfig() {
        RegistryConfig config = new RegistryConfig();

        // 模拟存量dubbo应用注册到zookeeper的情况，新开发的应用建议配置为 sc://127.0.0.1:30100
        config.setAddress("zookeeper://127.0.0.1:2181");
        config.setProtocol("zookeeper");
        return config;
    }
}