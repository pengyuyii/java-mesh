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

package com.huaweicloud.sermant.router.transmit.wrapper;

import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.request.RequestTag;
import com.huaweicloud.sermant.router.transmit.utils.ThreadWrapperUtils;

import java.util.concurrent.Callable;

/**
 * Runnable&Callable包装类，例如reactor.core.scheduler.WorkerTask
 *
 * @param <T> 泛型
 * @author provenceee
 * @since 2023-04-21
 */
public class RunnableAndCallableWrapper<T> implements Runnable, Callable<T> {
    private final Runnable runnable;

    private final Callable<T> callable;

    private final RequestTag requestTag;

    private final RequestData requestData;

    /**
     * 构造方法
     *
     * @param runnable runnable
     * @param callable callable
     * @param requestTag 请求标记
     * @param requestData 请求数据
     */
    public RunnableAndCallableWrapper(Runnable runnable, Callable<T> callable, RequestTag requestTag,
            RequestData requestData) {
        this.runnable = runnable;
        this.callable = callable;
        this.requestTag = requestTag;
        this.requestData = requestData;
    }

    @Override
    public void run() {
        ThreadWrapperUtils.run(runnable, requestTag, requestData);
    }

    @Override
    public T call() throws Exception {
        return ThreadWrapperUtils.call(callable, requestTag, requestData);
    }
}