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

package com.huaweicloud.sermant.router.transmit.utils;

import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.request.RequestTag;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;

import org.junit.Assert;
import org.junit.Test;

/**
 * 测试ThreadWrapperUtils
 *
 * @author provenceee
 * @since 2023-05-26
 */
public class ThreadWrapperUtilsTest {
    @Test
    public void testCall() throws Exception {
        Assert.assertNull(ThreadLocalUtils.getRequestData());
        Assert.assertNull(ThreadLocalUtils.getRequestTag());

        Object obj = new Object();
        Object result = ThreadWrapperUtils.call(() -> {
            Assert.assertNotNull(ThreadLocalUtils.getRequestData());
            Assert.assertNotNull(ThreadLocalUtils.getRequestTag());
            return obj;
        }, new RequestTag(null), new RequestData(null, null, null));
        Assert.assertEquals(obj, result);

        Assert.assertNull(ThreadLocalUtils.getRequestData());
        Assert.assertNull(ThreadLocalUtils.getRequestTag());
    }

    @Test
    public void testRun() {
        Assert.assertNull(ThreadLocalUtils.getRequestData());
        Assert.assertNull(ThreadLocalUtils.getRequestTag());

        ThreadWrapperUtils.run(() -> {
            Assert.assertNotNull(ThreadLocalUtils.getRequestData());
            Assert.assertNotNull(ThreadLocalUtils.getRequestTag());
        }, new RequestTag(null), new RequestData(null, null, null));

        Assert.assertNull(ThreadLocalUtils.getRequestData());
        Assert.assertNull(ThreadLocalUtils.getRequestTag());
    }
}