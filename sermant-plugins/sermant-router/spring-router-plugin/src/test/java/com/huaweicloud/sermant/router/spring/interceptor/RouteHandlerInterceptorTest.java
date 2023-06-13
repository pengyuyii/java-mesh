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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.common.request.RequestTag;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.BaseTransmitConfigTest;
import com.huaweicloud.sermant.router.spring.TestSpringConfigService;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 测试RouteHandlerInterceptor
 *
 * @author provenceee
 * @since 2022-09-07
 */
public class RouteHandlerInterceptorTest extends BaseTransmitConfigTest {
    private final RouteHandlerInterceptor interceptor;

    private static TestSpringConfigService configService;

    private static MockedStatic<ServiceManager> mockServiceManager;

    /**
     * UT执行前进行mock
     */
    @BeforeClass
    public static void before() {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        configService = new TestSpringConfigService();
        mockServiceManager.when(() -> ServiceManager.getService(SpringConfigService.class))
                .thenReturn(configService);
    }

    /**
     * UT执行后释放mock对象
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
    }

    public RouteHandlerInterceptorTest() {
        interceptor = new RouteHandlerInterceptor();
    }

    /**
     * 重置测试数据
     */
    @Before
    public void clear() {
        ThreadLocalUtils.removeRequestTag();
        ThreadLocalUtils.removeRequestData();
    }

    /**
     * 测试preHandle方法
     */
    @Test
    public void testPreHandle() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("bar", "bar1");
        request.addHeader("foo", "foo1");
        request.addHeader("foo2", "foo2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Object obj = new Object();

        // 测试keys全为空
        configService.setReturnEmptyWhenGetMatchTags(true);
        configService.setReturnEmptyWhenGetMatchKeys(true);
        interceptor.preHandle(request, response, obj);
        Assert.assertNull(ThreadLocalUtils.getRequestTag());

        // 测试preHandle方法，getMatchKeys不为空
        configService.setReturnEmptyWhenGetMatchTags(true);
        configService.setReturnEmptyWhenGetMatchKeys(false);
        interceptor.preHandle(request, response, obj);
        RequestTag requestTag = ThreadLocalUtils.getRequestTag();
        Map<String, List<String>> header = requestTag.getTag();
        Assert.assertNotNull(header);
        Assert.assertEquals(2, header.size());
        Assert.assertEquals("bar1", header.get("bar").get(0));
        Assert.assertEquals("foo1", header.get("foo").get(0));
    }

    /**
     * 测试afterCompletion,验证是否释放线程变量
     */
    @Test
    public void testAfterCompletion() {
        ThreadLocalUtils.addRequestTag(Collections.singletonMap("bar", Collections.singletonList("foo")));
        Assert.assertNotNull(ThreadLocalUtils.getRequestTag());

        // 测试afterCompletion,验证是否释放线程变量
        interceptor.afterCompletion(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), null);
        Assert.assertNull(ThreadLocalUtils.getRequestTag());
    }
}