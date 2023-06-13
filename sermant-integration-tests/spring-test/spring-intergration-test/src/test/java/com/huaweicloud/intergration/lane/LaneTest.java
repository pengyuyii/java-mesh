/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.intergration.lane;

import com.alibaba.fastjson.JSONObject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 泳道测试
 *
 * @author provenceee
 * @since 2023-03-06
 */
@EnabledIfSystemProperty(named = "sermant.integration.test.type", matches = "LANE")
public class LaneTest {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final int ZUUL_PORT = 8000;

    private static final int GATEWAY_PORT = 8001;

    private static final String IP = "http://127.0.0.1:";

    private static final String CLOUD_BASE_PATH = "/router/cloud/getLane";

    private static final String REST_BASE_PATH = "/rest";

    private static final String FEIGN_BASE_PATH = "/feign";

    private static final String ZUUL_REST_CLOUD_BASE_PATH = IP + ZUUL_PORT + REST_BASE_PATH + CLOUD_BASE_PATH;

    private static final String ZUUL_FEIGN_CLOUD_BASE_PATH = IP + ZUUL_PORT + FEIGN_BASE_PATH + CLOUD_BASE_PATH;

    private static final String ZUUL_WEBCLIENT_CLOUD_BASE_PATH =
            IP + ZUUL_PORT + FEIGN_BASE_PATH + CLOUD_BASE_PATH + "ByWebclient";

    private static final String GATEWAY_REST_CLOUD_BASE_PATH = IP + GATEWAY_PORT + REST_BASE_PATH + CLOUD_BASE_PATH;

    private static final String GATEWAY_FEIGN_CLOUD_BASE_PATH = IP + GATEWAY_PORT + FEIGN_BASE_PATH + CLOUD_BASE_PATH;

    private static final String GATEWAY_WEBCLIENT_CLOUD_BASE_PATH =
            IP + GATEWAY_PORT + FEIGN_BASE_PATH + CLOUD_BASE_PATH + "ByWebclient";

    private static final int TIMES = 30;

    private static final String REST_PROVIDER_NAME = "rest-provider";

    private static final String REST_CONSUMER_NAME = "rest-consumer";

    private static final String FEIGN_PROVIDER_NAME = "feign-provider";

    private static final String FEIGN_CONSUMER_NAME = "feign-consumer";

    private static final String VERSION_KEY = "version";

    private static final List<String> SPRING_CLOUD_VERSIONS_FOR_ZUUL = Arrays
            .asList("Edgware.SR2", "Finchley.RELEASE", "Greenwich.RELEASE", "Hoxton.RELEASE");

    private static final List<String> SPRING_CLOUD_VERSIONS_FOR_GATEWAY = Arrays
            .asList("Finchley.RELEASE", "Greenwich.RELEASE", "Hoxton.RELEASE", "2020.0.0", "2021.0.0", "2021.0.3");

    private final String springCloudVersion;

    private final boolean shouldTestWebclient;

    /**
     * 构造方法
     */
    public LaneTest() {
        springCloudVersion = Optional.ofNullable(System.getenv("SPRING_CLOUD_VERSION")).orElse("Hoxton.RELEASE");
        String springBootVersion = Optional.ofNullable(System.getenv("SPRING_BOOT_VERSION")).orElse("2.2.0.RELEASE");
        String[] parts = springBootVersion.split("\\.");

        // spring boot版本从2.0.x才开始有webclient
        shouldTestWebclient = Integer.parseInt(parts[0]) >= 2;
    }

    /**
     * 测试标签路由
     */
    @Test
    public void testByZuul() {
        // SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (!SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            return;
        }

        testByGateway(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH, ZUUL_WEBCLIENT_CLOUD_BASE_PATH);
        testByWebInterceptor(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH, ZUUL_WEBCLIENT_CLOUD_BASE_PATH);
    }

    /**
     * 测试标签路由
     */
    @Test
    public void testBySpringCloudGateway() {
        // SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (!SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            return;
        }
        testByGateway(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH, GATEWAY_WEBCLIENT_CLOUD_BASE_PATH);
        testByWebInterceptor(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH,
                GATEWAY_WEBCLIENT_CLOUD_BASE_PATH);
    }

    /**
     * 染色路口在网关
     *
     * @param restPath rest路径
     * @param feignPath feign路径
     */
    private void testByGateway(String restPath, String feignPath, String webClientPath) {
        // 正常染色
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-user-id", "101");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE
                    .exchange(restPath + "?name=BaR&id=9&enabled=true", HttpMethod.GET, entity,
                            String.class);
            JSONObject json = JSONObject.parseObject(exchange.getBody());
            JSONObject providerMsg = json.getJSONObject(REST_PROVIDER_NAME);
            JSONObject consumerMsg = json.getJSONObject(REST_CONSUMER_NAME);
            Assertions.assertEquals("gray1", providerMsg.getString("x-sermant-flag1"));
            Assertions.assertEquals("gray2", providerMsg.getString("x-sermant-flag2"));
            Assertions.assertEquals("1.0.0", providerMsg.getString(VERSION_KEY));
            Assertions.assertEquals("1.0.1", consumerMsg.getString(VERSION_KEY));

            exchange = REST_TEMPLATE
                    .exchange(feignPath + "?name=BaR&id=9&enabled=true", HttpMethod.GET, entity,
                            String.class);
            json = JSONObject.parseObject(exchange.getBody());
            providerMsg = json.getJSONObject(FEIGN_PROVIDER_NAME);
            consumerMsg = json.getJSONObject(FEIGN_CONSUMER_NAME);
            Assertions.assertEquals("gray3", providerMsg.getString("x-sermant-flag3"));
            Assertions.assertEquals("gray4", providerMsg.getString("x-sermant-flag4"));
            Assertions.assertEquals("1.0.0", providerMsg.getString(VERSION_KEY));
            Assertions.assertEquals("1.0.1", consumerMsg.getString(VERSION_KEY));

            if (shouldTestWebclient) {
                exchange = REST_TEMPLATE
                        .exchange(webClientPath + "?name=BaR&id=9&enabled=true", HttpMethod.GET, entity,
                                String.class);
                json = JSONObject.parseObject(exchange.getBody());
                providerMsg = json.getJSONObject(FEIGN_PROVIDER_NAME);
                consumerMsg = json.getJSONObject(FEIGN_CONSUMER_NAME);
                Assertions.assertEquals("gray3", providerMsg.getString("x-sermant-flag3"));
                Assertions.assertEquals("gray4", providerMsg.getString("x-sermant-flag4"));
                Assertions.assertEquals("1.0.0", providerMsg.getString(VERSION_KEY));
                Assertions.assertEquals("1.0.1", consumerMsg.getString(VERSION_KEY));
            }
        }

        // 测试已传入泳道标记
        headers.clear();
        headers.add("x-user-id", "101");
        headers.add("x-sermant-flag2", "gray12");
        headers.add("x-sermant-flag4", "gray14");
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE
                    .exchange(restPath + "?name=BaR&id=9&enabled=true", HttpMethod.GET, entity,
                            String.class);
            JSONObject json = JSONObject.parseObject(exchange.getBody());
            JSONObject providerMsg = json.getJSONObject(REST_PROVIDER_NAME);
            JSONObject consumerMsg = json.getJSONObject(REST_CONSUMER_NAME);
            Assertions.assertEquals("gray1", providerMsg.getString("x-sermant-flag1"));
            Assertions.assertEquals("gray12", providerMsg.getString("x-sermant-flag2"));
            Assertions.assertEquals("gray14", providerMsg.getString("x-sermant-flag4"));
            Assertions.assertEquals("1.0.0", providerMsg.getString(VERSION_KEY));
            Assertions.assertEquals("1.0.1", consumerMsg.getString(VERSION_KEY));

            exchange = REST_TEMPLATE
                    .exchange(feignPath + "?name=BaR&id=9&enabled=true", HttpMethod.GET, entity,
                            String.class);
            json = JSONObject.parseObject(exchange.getBody());
            providerMsg = json.getJSONObject(FEIGN_PROVIDER_NAME);
            consumerMsg = json.getJSONObject(FEIGN_CONSUMER_NAME);
            Assertions.assertEquals("gray12", providerMsg.getString("x-sermant-flag2"));
            Assertions.assertEquals("gray3", providerMsg.getString("x-sermant-flag3"));
            Assertions.assertEquals("gray14", providerMsg.getString("x-sermant-flag4"));
            Assertions.assertEquals("1.0.0", providerMsg.getString(VERSION_KEY));
            Assertions.assertEquals("1.0.1", consumerMsg.getString(VERSION_KEY));

            if (shouldTestWebclient) {
                exchange = REST_TEMPLATE
                        .exchange(webClientPath + "?name=BaR&id=9&enabled=true", HttpMethod.GET, entity,
                                String.class);
                json = JSONObject.parseObject(exchange.getBody());
                providerMsg = json.getJSONObject(FEIGN_PROVIDER_NAME);
                consumerMsg = json.getJSONObject(FEIGN_CONSUMER_NAME);
                Assertions.assertEquals("gray12", providerMsg.getString("x-sermant-flag2"));
                Assertions.assertEquals("gray3", providerMsg.getString("x-sermant-flag3"));
                Assertions.assertEquals("gray14", providerMsg.getString("x-sermant-flag4"));
                Assertions.assertEquals("1.0.0", providerMsg.getString(VERSION_KEY));
                Assertions.assertEquals("1.0.1", consumerMsg.getString(VERSION_KEY));
            }
        }

        // 测试不满足染色条件
        headers.clear();
        headers.add("x-user-id", "100");
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE
                    .exchange(restPath + "?name=BaR&id=9&enabled=true", HttpMethod.GET, entity,
                            String.class);
            JSONObject json = JSONObject.parseObject(exchange.getBody());
            JSONObject providerMsg = json.getJSONObject(REST_PROVIDER_NAME);
            JSONObject consumerMsg = json.getJSONObject(REST_CONSUMER_NAME);
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag1"));
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag2"));
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag3"));
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag4"));
            Assertions.assertEquals("1.0.0", providerMsg.getString(VERSION_KEY));
            Assertions.assertEquals("1.0.0", consumerMsg.getString(VERSION_KEY));

            exchange = REST_TEMPLATE
                    .exchange(feignPath + "?name=BaR&id=9&enabled=true", HttpMethod.GET, entity,
                            String.class);
            json = JSONObject.parseObject(exchange.getBody());
            providerMsg = json.getJSONObject(FEIGN_PROVIDER_NAME);
            consumerMsg = json.getJSONObject(FEIGN_CONSUMER_NAME);
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag1"));
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag2"));
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag3"));
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag4"));
            Assertions.assertEquals("1.0.0", providerMsg.getString(VERSION_KEY));
            Assertions.assertEquals("1.0.0", consumerMsg.getString(VERSION_KEY));

            if (shouldTestWebclient) {
                exchange = REST_TEMPLATE
                        .exchange(webClientPath + "?name=BaR&id=9&enabled=true", HttpMethod.GET, entity,
                                String.class);
                json = JSONObject.parseObject(exchange.getBody());
                providerMsg = json.getJSONObject(FEIGN_PROVIDER_NAME);
                consumerMsg = json.getJSONObject(FEIGN_CONSUMER_NAME);
                Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag1"));
                Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag2"));
                Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag3"));
                Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag4"));
                Assertions.assertEquals("1.0.0", providerMsg.getString(VERSION_KEY));
                Assertions.assertEquals("1.0.0", consumerMsg.getString(VERSION_KEY));
            }
        }
    }

    /**
     * 染色路口在spring web拦截器
     *
     * @param restPath rest路径
     * @param feignPath feign路径
     */
    private void testByWebInterceptor(String restPath, String feignPath, String webClientPath) {
        // 正常染色
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-user-id", "99");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE
                    .exchange(restPath + "?name=BaR&id=11&enabled=true", HttpMethod.GET, entity,
                            String.class);
            JSONObject json = JSONObject.parseObject(exchange.getBody());
            JSONObject providerMsg = json.getJSONObject(REST_PROVIDER_NAME);
            JSONObject consumerMsg = json.getJSONObject(REST_CONSUMER_NAME);
            Assertions.assertEquals("gray5", providerMsg.getString("x-sermant-flag5"));
            Assertions.assertEquals("1.0.1", providerMsg.getString(VERSION_KEY));
            Assertions.assertEquals("1.0.0", consumerMsg.getString(VERSION_KEY));

            exchange = REST_TEMPLATE
                    .exchange(feignPath + "?name=FoO&id=9&enabled=true", HttpMethod.GET, entity,
                            String.class);
            json = JSONObject.parseObject(exchange.getBody());
            providerMsg = json.getJSONObject(FEIGN_PROVIDER_NAME);
            consumerMsg = json.getJSONObject(FEIGN_CONSUMER_NAME);
            Assertions.assertEquals("gray6", providerMsg.getString("x-sermant-flag6"));
            Assertions.assertEquals("1.0.1", providerMsg.getString(VERSION_KEY));
            Assertions.assertEquals("1.0.0", consumerMsg.getString(VERSION_KEY));

            if (shouldTestWebclient) {
                exchange = REST_TEMPLATE
                        .exchange(webClientPath + "?name=FoO&id=9&enabled=true", HttpMethod.GET, entity,
                                String.class);
                json = JSONObject.parseObject(exchange.getBody());
                providerMsg = json.getJSONObject(FEIGN_PROVIDER_NAME);
                consumerMsg = json.getJSONObject(FEIGN_CONSUMER_NAME);
                Assertions.assertEquals("gray6", providerMsg.getString("x-sermant-flag6"));
                Assertions.assertEquals("1.0.1", providerMsg.getString(VERSION_KEY));
                Assertions.assertEquals("1.0.0", consumerMsg.getString(VERSION_KEY));
            }
        }

        // 测试已传入泳道标记
        headers.clear();
        headers.add("x-user-id", "99");
        headers.add("x-sermant-flag1", "gray1");
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE
                    .exchange(restPath + "?name=BaR&id=11&enabled=true", HttpMethod.GET, entity,
                            String.class);
            JSONObject json = JSONObject.parseObject(exchange.getBody());
            JSONObject providerMsg = json.getJSONObject(REST_PROVIDER_NAME);
            JSONObject consumerMsg = json.getJSONObject(REST_CONSUMER_NAME);
            Assertions.assertEquals("gray1", providerMsg.getString("x-sermant-flag1"));
            Assertions.assertEquals("gray5", providerMsg.getString("x-sermant-flag5"));
            Assertions.assertEquals("1.0.1", providerMsg.getString(VERSION_KEY));
            Assertions.assertEquals("1.0.1", consumerMsg.getString(VERSION_KEY));

            exchange = REST_TEMPLATE
                    .exchange(feignPath + "?name=FoO&id=9&enabled=true", HttpMethod.GET, entity,
                            String.class);
            json = JSONObject.parseObject(exchange.getBody());
            providerMsg = json.getJSONObject(FEIGN_PROVIDER_NAME);
            consumerMsg = json.getJSONObject(FEIGN_CONSUMER_NAME);
            Assertions.assertEquals("gray1", providerMsg.getString("x-sermant-flag1"));
            Assertions.assertEquals("gray6", providerMsg.getString("x-sermant-flag6"));
            Assertions.assertEquals("1.0.1", providerMsg.getString(VERSION_KEY));
            Assertions.assertEquals("1.0.1", consumerMsg.getString(VERSION_KEY));

            if (shouldTestWebclient) {
                exchange = REST_TEMPLATE
                        .exchange(webClientPath + "?name=FoO&id=9&enabled=true", HttpMethod.GET, entity,
                                String.class);
                json = JSONObject.parseObject(exchange.getBody());
                providerMsg = json.getJSONObject(FEIGN_PROVIDER_NAME);
                consumerMsg = json.getJSONObject(FEIGN_CONSUMER_NAME);
                Assertions.assertEquals("gray1", providerMsg.getString("x-sermant-flag1"));
                Assertions.assertEquals("gray6", providerMsg.getString("x-sermant-flag6"));
                Assertions.assertEquals("1.0.1", providerMsg.getString(VERSION_KEY));
                Assertions.assertEquals("1.0.1", consumerMsg.getString(VERSION_KEY));
            }
        }

        // 测试不满足染色条件
        headers.clear();
        headers.add("x-user-id", "100");
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE
                    .exchange(restPath + "?name=BaR&id=9&enabled=true", HttpMethod.GET, entity,
                            String.class);
            JSONObject json = JSONObject.parseObject(exchange.getBody());
            JSONObject providerMsg = json.getJSONObject(REST_PROVIDER_NAME);
            JSONObject consumerMsg = json.getJSONObject(REST_CONSUMER_NAME);
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag1"));
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag2"));
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag3"));
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag4"));
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag5"));
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag6"));
            Assertions.assertEquals("1.0.0", providerMsg.getString(VERSION_KEY));
            Assertions.assertEquals("1.0.0", consumerMsg.getString(VERSION_KEY));

            exchange = REST_TEMPLATE
                    .exchange(feignPath + "?name=BaR&id=9&enabled=true", HttpMethod.GET, entity,
                            String.class);
            json = JSONObject.parseObject(exchange.getBody());
            providerMsg = json.getJSONObject(FEIGN_PROVIDER_NAME);
            consumerMsg = json.getJSONObject(FEIGN_CONSUMER_NAME);
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag1"));
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag2"));
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag3"));
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag4"));
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag5"));
            Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag6"));
            Assertions.assertEquals("1.0.0", providerMsg.getString(VERSION_KEY));
            Assertions.assertEquals("1.0.0", consumerMsg.getString(VERSION_KEY));

            if (shouldTestWebclient) {
                exchange = REST_TEMPLATE
                        .exchange(webClientPath + "?name=BaR&id=9&enabled=true", HttpMethod.GET, entity,
                                String.class);
                json = JSONObject.parseObject(exchange.getBody());
                providerMsg = json.getJSONObject(FEIGN_PROVIDER_NAME);
                consumerMsg = json.getJSONObject(FEIGN_CONSUMER_NAME);
                Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag1"));
                Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag2"));
                Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag3"));
                Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag4"));
                Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag5"));
                Assertions.assertFalse(providerMsg.containsKey("x-sermant-flag6"));
                Assertions.assertEquals("1.0.0", providerMsg.getString(VERSION_KEY));
                Assertions.assertEquals("1.0.0", consumerMsg.getString(VERSION_KEY));
            }
        }
    }
}