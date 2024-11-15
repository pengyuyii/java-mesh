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

package io.sermant.tag.transmission.apachedubbov2.interceptors;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import io.sermant.tag.transmission.interceptors.AbstractServerInterceptor;

import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcInvocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dubbo traffic tag transparent transmission provider side interceptor, supports dubbo2.7.x
 *
 * @author daizhenyu
 * @since 2023-08-02
 **/
public class ApacheDubboProviderInterceptor extends AbstractServerInterceptor<RpcInvocation> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * parameter subscript of invoker
     */
    private static final int ARGUMENT_INVOKER_INDEX = 0;

    /**
     * parameter subscript of invocation
     */
    private static final int ARGUMENT_INVOCATION_INDEX = 1;

    /**
     * dubbo consumer
     */
    private static final String DUBBO_CONSUMER = "consumer";

    /**
     * dubbo provider
     */
    private static final String DUBBO_PROVIDER = "provider";

    /**
     * distinguish between dubbo callers: provider or consumer
     */
    private static final String DUBBO_SIDE = "side";

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        if (isConsumer(context)) {
            return context;
        }

        Object invocationArgument = context.getArguments()[ARGUMENT_INVOCATION_INDEX];
        if (!(invocationArgument instanceof RpcInvocation)) {
            return context;
        }

        TrafficUtils.updateTrafficTag(extractTrafficTagFromCarrier((RpcInvocation) invocationArgument));
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        if (isConsumer(context)) {
            return context;
        }
        TrafficUtils.removeTrafficTag();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        if (isConsumer(context)) {
            return context;
        }
        TrafficUtils.removeTrafficTag();
        return context;
    }

    /**
     * Parse traffic tag from RpcInvocation
     *
     * @param invocation Apache Dubbo carrier of the traffic tag on provider
     * @return traffic tag
     */
    @Override
    protected Map<String, List<String>> extractTrafficTagFromCarrier(RpcInvocation invocation) {
        Map<String, List<String>> tag = new HashMap<>();

        for (Map.Entry<String, String> entry : invocation.getAttachments().entrySet()) {
            String key = entry.getKey();
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }
            String value = invocation.getAttachment(key);

            // When the value of the traffic label is null, it also needs to be stored in a local variable to overwrite
            // the original value to prevent misuse of the old traffic label.
            if (value == null) {
                tag.put(key, null);
                LOGGER.log(Level.FINE, "Traffic tag {0} have been extracted from dubbo.", entry);
                continue;
            }
            tag.put(key, Collections.singletonList(value));
            LOGGER.log(Level.FINE, "Traffic tag {0} have been extracted from dubbo.", entry);
        }
        return tag;
    }

    private boolean isConsumer(ExecuteContext context) {
        Object invokerArgument = context.getArguments()[ARGUMENT_INVOKER_INDEX];
        if (!(invokerArgument instanceof Invoker<?>)) {
            return false;
        }
        Invoker<?> invoker = (Invoker<?>) invokerArgument;
        return isConsumer(invoker);
    }

    private boolean isConsumer(Invoker<?> invoker) {
        return DUBBO_CONSUMER.equals(invoker.getUrl().getParameter(DUBBO_SIDE, DUBBO_PROVIDER));
    }
}