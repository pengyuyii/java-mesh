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

package com.huaweicloud.sermant.router.spring.handler;

import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.spring.TestSpringConfigService;
import com.huaweicloud.sermant.router.spring.handler.AbstractHandler.Keys;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试RouteMappingHandler
 *
 * @author provenceee
 * @since 2023-02-28
 */
public class RouteMappingHandlerTest {
    private static MockedStatic<ServiceManager> mockServiceManager;

    private static TestSpringConfigService configService;

    private final RouteHandler handler;

    /**
     * UT执行前进行mock
     */
    @BeforeClass
    public static void before() {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        configService = new TestSpringConfigService();
    }

    /**
     * UT执行后释放mock对象
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
    }

    public RouteMappingHandlerTest() {
        handler = new RouteHandler();
    }

    /**
     * 测试getRequestTag方法
     */
    @Test
    public void testGetRequestTag() {
        // 正常情况
        configService.setReturnEmptyWhenGetMatchKeys(false);
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("bar", Collections.singletonList("bar1"));
        headers.put("foo", Collections.singletonList("foo1"));
        Map<String, List<String>> requestTag = handler.getRequestTag("", "", headers, null,
                new Keys(configService.getMatchKeys(), configService.getInjectTags()));
        Assert.assertNotNull(requestTag);
        Assert.assertEquals(2, requestTag.size());
        Assert.assertEquals("bar1", requestTag.get("bar").get(0));
        Assert.assertEquals("foo1", requestTag.get("foo").get(0));

        // 测试getMatchKeys返回空
        configService.setReturnEmptyWhenGetMatchKeys(true);
        requestTag = handler.getRequestTag("", "", null, null,
                new Keys(configService.getMatchKeys(), configService.getInjectTags()));
        Assert.assertEquals(Collections.emptyMap(), requestTag);
    }
}