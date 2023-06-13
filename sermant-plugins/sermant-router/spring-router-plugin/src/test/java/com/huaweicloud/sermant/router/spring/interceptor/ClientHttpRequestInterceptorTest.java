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
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.BaseTransmitConfigTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.client.MockClientHttpRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试ClientHttpRequestInterceptor
 *
 * @author provenceee
 * @since 2022-09-08
 */
public class ClientHttpRequestInterceptorTest extends BaseTransmitConfigTest {
    private final ClientHttpRequestInterceptor interceptor;

    private final ExecuteContext context;

    public ClientHttpRequestInterceptorTest() {
        interceptor = new ClientHttpRequestInterceptor();
        MockClientHttpRequest request = new MockClientHttpRequest();
        request.getHeaders().add("bar", "bar2");
        request.getHeaders().add("bar3", "bar3");
        context = ExecuteContext.forMemberMethod(request, null, null, null, null);
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
     * 测试before方法
     */
    @Test
    public void testBefore() {
        Map<String, List<String>> header = new HashMap<>();
        header.put("bar", Collections.singletonList("bar1"));
        header.put("foo", Collections.singletonList("foo1"));
        ThreadLocalUtils.addRequestTag(header);
        interceptor.before(context);
        RequestData requestData = ThreadLocalUtils.getRequestData();
        Assert.assertNotNull(requestData);
        Assert.assertEquals(HttpMethod.GET.name(), requestData.getHttpMethod());
        Assert.assertEquals("/", requestData.getPath());
        Map<String, List<String>> headerData = requestData.getTag();
        Assert.assertEquals(3, headerData.size());
        Assert.assertEquals("bar2", headerData.get("bar").get(0));
        Assert.assertEquals("foo1", headerData.get("foo").get(0));
        Assert.assertEquals("bar3", headerData.get("bar3").get(0));
    }

    /**
     * 测试after方法
     */
    @Test
    public void testAfter() {
        ThreadLocalUtils.setRequestData(new RequestData(Collections.emptyMap(), "", ""));
        interceptor.after(context);
        Assert.assertNull(ThreadLocalUtils.getRequestData());
    }

    /**
     * 测试onThrow方法
     */
    @Test
    public void testOnThrow() {
        ThreadLocalUtils.setRequestData(new RequestData(Collections.emptyMap(), "", ""));
        interceptor.onThrow(context);
        Assert.assertNull(ThreadLocalUtils.getRequestData());
    }
}