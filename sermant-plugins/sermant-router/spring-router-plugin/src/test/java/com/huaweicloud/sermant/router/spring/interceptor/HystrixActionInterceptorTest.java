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
import com.huaweicloud.sermant.router.common.request.RequestTag;
import com.huaweicloud.sermant.router.common.utils.ReflectUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.BaseTransmitConfigTest;

import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategyDefault;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 测试HystrixActionInterceptor
 *
 * @author provenceee
 * @since 2022-09-08
 */
public class HystrixActionInterceptorTest extends BaseTransmitConfigTest {
    private final HystrixActionInterceptor interceptor;

    private final ExecuteContext context;

    public HystrixActionInterceptorTest() {
        interceptor = new HystrixActionInterceptor();
        Object[] arguments = new Object[1];
        arguments[0] = HystrixConcurrencyStrategyDefault.getInstance();
        context = ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
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
        HystrixRequestContext context = HystrixRequestContext.getContextForCurrentThread();
        Assert.assertNotNull(context);
        Map<HystrixRequestVariableDefault<?>, ?> state = ReflectUtils.getFieldValue(context, "state")
                .map(value -> (Map<HystrixRequestVariableDefault<?>, ?>) value).orElse(Collections.emptyMap());
        for (Entry<HystrixRequestVariableDefault<?>, ?> entry : state.entrySet()) {
            Object lazyInitializer = entry.getValue();
            Object obj = ReflectUtils.getFieldValue(lazyInitializer, "value").orElse(null);
            if (obj instanceof RequestTag) {
                entry.getKey().remove();
                Assert.assertEquals(header, ((RequestTag) obj).getTag());
            }
        }
    }
}