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

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.request.RequestTag;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CallableWrapper/RunnableWrapper/RunnableAndCallableWrapper工具类
 *
 * @author provenceee
 * @since 2023-05-25
 */
public class ThreadWrapperUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private ThreadWrapperUtils() {
    }

    /**
     * call方法
     *
     * @param callable callable
     * @param requestTag 请求标记
     * @param requestData 请求数据
     * @param <T> 泛型
     * @return 调用结果
     * @throws Exception 调用异常
     */
    public static <T> T call(Callable<T> callable, RequestTag requestTag, RequestData requestData) throws Exception {
        try {
            if (requestTag != null) {
                ThreadLocalUtils.setRequestTag(requestTag);
            }
            if (requestData != null) {
                ThreadLocalUtils.setRequestData(requestData);
            }
            log(callable, requestTag, requestData);
            return callable.call();
        } finally {
            ThreadLocalUtils.removeRequestTag();
            ThreadLocalUtils.removeRequestData();
        }
    }

    /**
     * run方法
     *
     * @param runnable runnable
     * @param requestTag 请求标记
     * @param requestData 请求数据
     */
    public static void run(Runnable runnable, RequestTag requestTag, RequestData requestData) {
        try {
            if (requestTag != null) {
                ThreadLocalUtils.setRequestTag(requestTag);
            }
            if (requestData != null) {
                ThreadLocalUtils.setRequestData(requestData);
            }
            log(runnable, requestTag, requestData);
            runnable.run();
        } finally {
            ThreadLocalUtils.removeRequestTag();
            ThreadLocalUtils.removeRequestData();
        }
    }

    private static void log(Object obj, RequestTag requestTag, RequestData requestData) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Current thread is {0}, class name is {1}, hash code is {2}, requestTag is {3}, "
                            + "requestData is {4}, will be executed.",
                    new Object[]{Thread.currentThread().getName(), obj.getClass().getName(),
                            Integer.toHexString(obj.hashCode()), requestTag, requestData});
        }
    }
}