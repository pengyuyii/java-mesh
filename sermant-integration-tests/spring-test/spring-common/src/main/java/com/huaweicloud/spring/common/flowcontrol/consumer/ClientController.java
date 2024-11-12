/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.spring.common.flowcontrol.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;
import java.util.UUID;

/**
 * 基于Client端的治理测试
 *
 * @author zhouss
 * @since 2022-07-28
 */
@Controller
@ResponseBody
@RequestMapping("/flowcontrol")
public class ClientController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientController.class);

    @Value("${down.serviceName}")
    private String downServiceName;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 实例隔离接口测试
     *
     * @return 实例隔离
     */
    @RequestMapping("instanceIsolation")
    public String instanceIsolation() {
        return restTemplate.getForObject(buildUrl("instanceIsolation"), String.class);
    }

    /**
     * 实例隔离接口测试
     *
     * @return 实例隔离
     */
    @RequestMapping("retry")
    public int retry() {
        String tryCount = null;
        try {
            tryCount = restTemplate.getForObject(buildUrl("retry") + "?invocationId=" + UUID.randomUUID(),
                    String.class);
        } catch (Exception ex) {
            LOGGER.error("Retry {} times", tryCount);
            LOGGER.error(ex.getMessage(), ex);
        }
        return tryCount == null ? 0 : Integer.parseInt(tryCount);
    }

    /**
     * 错误注入测试-返回空
     *
     * @return 返回空-由agent实现
     */
    @RequestMapping("faultNull")
    public String faultNull() {
        return restTemplate.getForObject(buildUrl("faultNull"), String.class);
    }

    /**
     * 错误注入测试-抛异常
     *
     * @return 抛异常-由agent实现
     */
    @RequestMapping("faultThrow")
    public String faultThrow() {
        return restTemplate.getForObject(buildUrl("faultThrow"), String.class);
    }

    /**
     * 错误注入测试-请求延迟
     *
     * @return 请求延迟-由agent实现
     */
    @RequestMapping("faultDelay")
    public String faultDelay() {
        return restTemplate.getForObject(buildUrl("faultDelay"), String.class);
    }

    private String buildUrl(String api) {
        return String.format(Locale.ENGLISH, "http://%s/%s", downServiceName, api);
    }
}
