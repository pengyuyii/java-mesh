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
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;

import java.util.ListIterator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 通用的基础Adviser
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public class CommonBaseAdviser {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private CommonBaseAdviser() {
    }

    /**
     * 前置触发点
     *
     * @param context 执行上下文
     * @param interceptorItr 拦截器双向迭代器
     * @param beforeHandler before的异常处理器
     * @return 执行上下文
     * @throws Throwable 抛给宿主的异常
     */
    public static ExecuteContext onMethodEnter(ExecuteContext context, ListIterator<Interceptor> interceptorItr,
            ExceptionHandler beforeHandler) throws Throwable {
        ExecuteContext newContext = context;
        while (interceptorItr.hasNext()) {
            try {
                final Interceptor interceptor = interceptorItr.next();
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE,
                            String.format(Locale.ROOT, "Method[%s] had been entered, interceptor is [%s].",
                                    MethodKeyCreator.getMethodKey(context.getMethod()),
                                    interceptor.getClass().getName()));
                }
                try {
                    final ExecuteContext tempContext = interceptor.before(newContext);
                    if (tempContext != null) {
                        newContext = tempContext;
                    }
                    if (newContext.isSkip()) {
                        return newContext;
                    }
                } catch (Throwable t) {
                    beforeHandler.handle(context, interceptor, t);
                }
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "A exception occur when method enter.", exception);
                return newContext;
            }

            // 指定向宿主应用抛出异常
            if (newContext.getThrowableOut() != null) {
                throw newContext.getThrowableOut();
            }
        }
        return newContext;
    }

    /**
     * 后置触发点
     *
     * @param context 执行上下文
     * @param interceptorItr 拦截器双向迭代器
     * @param onThrowHandler onThrow的异常处理器
     * @param afterHandler after的的异常处理器
     * @return 执行上下文
     * @throws Throwable 抛给宿主的异常
     */
    public static ExecuteContext onMethodExit(ExecuteContext context, ListIterator<Interceptor> interceptorItr,
            ExceptionHandler onThrowHandler, ExceptionHandler afterHandler) throws Throwable {
        ExecuteContext newContext = context;
        while (interceptorItr.hasPrevious()) {
            try {
                final Interceptor interceptor = interceptorItr.previous();
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE,
                            String.format(Locale.ROOT, "Method[%s] had been exited, interceptor is [%s].",
                                    MethodKeyCreator.getMethodKey(context.getMethod()),
                                    interceptor.getClass().getName()));
                }
                if (newContext.getThrowable() != null && onThrowHandler != null) {
                    try {
                        final ExecuteContext tempContext = interceptor.onThrow(newContext);
                        if (tempContext != null) {
                            newContext = tempContext;
                        }
                    } catch (Throwable t) {
                        onThrowHandler.handle(newContext, interceptor, t);
                    }
                    if (newContext.getThrowableOut() != null) {
                        throw newContext.getThrowableOut();
                    }
                }
                try {
                    final ExecuteContext tempContext = interceptor.after(newContext);
                    if (tempContext != null) {
                        newContext = tempContext;
                    }
                } catch (Throwable t) {
                    afterHandler.handle(newContext, interceptor, t);
                }
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "A exception occur when method exit.", exception);
                return newContext;
            }
            if (newContext.getThrowableOut() != null) {
                throw newContext.getThrowableOut();
            }
        }
        return newContext;
    }

    /**
     * 异常处理器
     */
    public interface ExceptionHandler {
        void handle(ExecuteContext context, Interceptor interceptor, Throwable throwable);
    }
}
