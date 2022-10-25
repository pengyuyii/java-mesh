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

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.spring.cache.AppCache;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试DiscoveryManagerInterceptor
 *
 * @author provenceee
 * @since 2022-10-13
 */
public class DiscoveryManagerInterceptorTest {
    private final DiscoveryManagerInterceptor interceptor;

    private final ExecuteContext context;

    private static TestSpringConfigService configService;

    private static MockedStatic<ServiceManager> mockServiceManager;

    private final RouterConfig routerConfig;

    /**
     * UT执行前进行mock
     */
    @BeforeClass
    public static void before() {
        configService = new TestSpringConfigService();
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(SpringConfigService.class)).thenReturn(configService);
    }

    /**
     * UT执行后释放mock对象
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
    }

    public DiscoveryManagerInterceptorTest() throws NoSuchFieldException, IllegalAccessException {
        interceptor = new DiscoveryManagerInterceptor();
        routerConfig = new RouterConfig();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("foo", "foo1");
        parameters.put("bar", "bar1");
        routerConfig.setParameters(parameters);
        Field field = interceptor.getClass().getDeclaredField("routerConfig");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(interceptor, routerConfig);
        Object[] arguments = new Object[1];
        arguments[0] = new TestObject("foo");
        context = ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }

    /**
     * 测试before方法
     */
    @Test
    public void testBefore() {
        interceptor.before(context);
        Assert.assertEquals("foo", AppCache.INSTANCE.getAppName());
        Map<String, String> metadata = ((TestObject) context.getArguments()[0]).getMetadata();
        Assert.assertEquals("bar1", metadata.get("bar"));
        Assert.assertEquals("foo1", metadata.get("foo"));
        Assert.assertEquals(routerConfig.getRouterVersion(), metadata.get("version"));

        context.getArguments()[0] = new TestObject(null);
        interceptor.before(context);
        Assert.assertEquals("foo", AppCache.INSTANCE.getAppName());
    }

    /**
     * 测试after方法
     */
    @Test
    public void testAfter() {
        AppCache.INSTANCE.setAppName("foo");
        interceptor.after(context);
        Assert.assertEquals(RouterConstant.SPRING_CACHE_NAME, configService.getCacheName());
        Assert.assertEquals("foo", configService.getServiceName());
    }

    public static class TestObject {
        private final String serviceName;

        private final Map<String, String> metadata;

        /**
         * 构造方法
         *
         * @param serviceName 服务名
         */
        public TestObject(String serviceName) {
            this.serviceName = serviceName;
            this.metadata = new HashMap<>();
        }

        public String getServiceName() {
            return serviceName;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }
    }
}