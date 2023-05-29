/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.plugin.agent.template;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.adviser.AdviserInterface;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;

import java.util.ListIterator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 通用的方法Adviser
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public class CommonMethodAdviser implements AdviserInterface {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    public CommonMethodAdviser() {
    }

    /**
     * 输出错误日志
     *
     * @param scene 场景
     * @param context 执行上下文
     * @param interceptor 拦截器对象
     * @param throwable 错误对象
     */
    private void logError(String scene, ExecuteContext context, Interceptor interceptor, Throwable throwable) {
        LOGGER.log(Level.SEVERE, String.format(Locale.ROOT, "An error occurred %s [%s] in interceptor [%s]: ",
                        scene, MethodKeyCreator.getMethodKey(context.getMethod()), interceptor.getClass().getName()),
                throwable);
    }

    /**
     * 调用方法的前置触发点
     *
     * @param context 执行上下文
     * @param interceptorItr 拦截器双向迭代器
     * @return 执行上下文
     * @throws Throwable 抛给宿主的异常
     */
    public ExecuteContext onMethodEnter(ExecuteContext context, ListIterator<Interceptor> interceptorItr)
            throws Throwable {
        return CommonBaseAdviser.onMethodEnter(context, interceptorItr,
                new CommonBaseAdviser.ExceptionHandler() {
                    @Override
                    public void handle(ExecuteContext context, Interceptor interceptor, Throwable throwable) {
                        logError("before executing", context, interceptor, throwable);
                    }
                });
    }

    /**
     * 调用方法的后置触发点
     *
     * @param context 执行上下文
     * @param interceptorItr 拦截器双向迭代器
     * @return 执行上下文
     * @throws Throwable 抛给宿主的异常
     */
    public ExecuteContext onMethodExit(ExecuteContext context, ListIterator<Interceptor> interceptorItr)
            throws Throwable {
        return CommonBaseAdviser.onMethodExit(context, interceptorItr,
                new CommonBaseAdviser.ExceptionHandler() {
                    @Override
                    public void handle(ExecuteContext context, Interceptor interceptor, Throwable throwable) {
                        logError("while handling error from", context, interceptor, throwable);
                    }
                },
                new CommonBaseAdviser.ExceptionHandler() {
                    @Override
                    public void handle(ExecuteContext context, Interceptor interceptor, Throwable throwable) {
                        logError("after executing", context, interceptor, throwable);
                    }
                });
    }
}
