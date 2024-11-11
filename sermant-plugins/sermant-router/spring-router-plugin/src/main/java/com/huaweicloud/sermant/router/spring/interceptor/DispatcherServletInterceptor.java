/*
 *
 *  * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.router.common.handler.Handler;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.handler.AbstractRequestTagHandler;
import com.huaweicloud.sermant.router.spring.handler.AbstractRequestTagHandler.Keys;
import com.huaweicloud.sermant.router.spring.handler.LaneRequestTagHandler;
import com.huaweicloud.sermant.router.spring.handler.RouteRequestTagHandler;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;
import com.huaweicloud.sermant.router.spring.utils.SpringRouterUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 获取http请求数据
 *
 * @author provenceee
 * @since 2024-11-11
 */
public class DispatcherServletInterceptor extends AbstractInterceptor {
    private final List<AbstractRequestTagHandler> handlers;

    private final SpringConfigService configService;

    /**
     * 构造方法
     */
    public DispatcherServletInterceptor() {
        configService = PluginServiceManager.getPluginService(SpringConfigService.class);
        handlers = new ArrayList<>();
        handlers.add(new LaneRequestTagHandler());
        handlers.add(new RouteRequestTagHandler());
        handlers.sort(Comparator.comparingInt(Handler::getOrder));
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Set<String> matchKeys = configService.getMatchKeys();
        Set<String> injectTags = configService.getInjectTags();
        if (CollectionUtils.isEmpty(matchKeys) && CollectionUtils.isEmpty(injectTags)) {
            // 染色标记为空，代表没有染色规则，直接return
            return context;
        }
        Object request = context.getArguments()[0];
        Map<String, List<String>> headers = getHeaders(request);
        Map<String, String[]> parameterMap = SpringRouterUtils.getParameterMap(request);
        String path = SpringRouterUtils.getRequestUri(request);
        String method = SpringRouterUtils.getMethod(request);
        handlers.forEach(handler -> ThreadLocalUtils.addRequestTag(
                handler.getRequestTag(path, method, headers, parameterMap, new Keys(matchKeys, injectTags))));
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        ThreadLocalUtils.removeRequestTag();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        ThreadLocalUtils.removeRequestTag();
        return context;
    }

    private Map<String, List<String>> getHeaders(Object request) {
        Map<String, List<String>> headers = new HashMap<>();
        Enumeration<?> enumeration = SpringRouterUtils.getHeaderNames(request);
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            headers.put(key, enumeration2List(SpringRouterUtils.getHeaders(request, key)));
        }
        return headers;
    }

    private List<String> enumeration2List(Enumeration<?> enumeration) {
        if (enumeration == null) {
            return Collections.emptyList();
        }
        List<String> collection = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            collection.add((String) enumeration.nextElement());
        }
        return collection;
    }
}
