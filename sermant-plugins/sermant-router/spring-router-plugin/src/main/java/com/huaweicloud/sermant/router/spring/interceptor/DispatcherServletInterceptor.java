/*
 * Copyright (C) 2022-2024 Huawei Technologies Co., Ltd. All rights reserved.
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
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.handler.Handler;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.handler.AbstractHandler;
import com.huaweicloud.sermant.router.spring.handler.AbstractHandler.Keys;
import com.huaweicloud.sermant.router.spring.handler.LaneHandler;
import com.huaweicloud.sermant.router.spring.handler.RouteHandler;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;
import com.huaweicloud.sermant.router.spring.utils.SpringRouterUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

/**
 * 获取http请求数据
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class DispatcherServletInterceptor extends AbstractInterceptor {
    private final List<AbstractHandler> handlers;

    private final SpringConfigService configService;

    private Function<Object, String> getQueryString;

    private Function<Object, String> getRequestUri;

    private Function<Object, String> getMethod;

    private Function<Object, Enumeration<?>> getHeaderNames;

    private BiFunction<Object, String, Enumeration<?>> getHeaders;

    /**
     * 构造方法
     */
    public DispatcherServletInterceptor() {
        configService = PluginServiceManager.getPluginService(SpringConfigService.class);
        handlers = new ArrayList<>();
        handlers.add(new LaneHandler());
        handlers.add(new RouteHandler());
        handlers.sort(Comparator.comparingInt(Handler::getOrder));
        initFunction();
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
        String queryString = getQueryString.apply(request);
        String decode = Optional.ofNullable(queryString).map(this::decode).orElse(StringUtils.EMPTY);
        Map<String, List<String>> queryParams = SpringRouterUtils.getParametersByQuery(decode);
        String path = getRequestUri.apply(request);
        String method = getMethod.apply(request);
        handlers.forEach(handler -> ThreadLocalUtils.addRequestTag(
                handler.getRequestTag(path, method, headers, queryParams, new Keys(matchKeys, injectTags))));
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

    private String decode(String str) {
        try {
            return URLDecoder.decode(str, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return StringUtils.EMPTY;
        }
    }

    private Map<String, List<String>> getHeaders(Object request) {
        Map<String, List<String>> headers = new HashMap<>();
        Enumeration<?> enumeration = getHeaderNames.apply(request);
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            headers.put(key, enumeration2List(getHeaders.apply(request, key)));
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

    private void initFunction() {
        boolean canLoadLowVersion = canLoadLowVersion();
        if (canLoadLowVersion) {
            getQueryString = obj -> ((HttpServletRequest) obj).getQueryString();
            getRequestUri = obj -> ((HttpServletRequest) obj).getRequestURI();
            getMethod = obj -> ((HttpServletRequest) obj).getMethod();
            getHeaderNames = obj -> ((HttpServletRequest) obj).getHeaderNames();
            getHeaders = (obj, key) -> ((HttpServletRequest) obj).getHeaders(key);
        } else {
            getQueryString = SpringRouterUtils::getQueryString;
            getRequestUri = SpringRouterUtils::getRequestUri;
            getMethod = SpringRouterUtils::getMethod;
            getHeaderNames = SpringRouterUtils::getHeaderNames;
            getHeaders = SpringRouterUtils::getHeaders;
        }
    }

    private boolean canLoadLowVersion() {
        try {
            Class.forName(HttpServletRequest.class.getCanonicalName());
        } catch (NoClassDefFoundError | ClassNotFoundException error) {
            return false;
        }
        return true;
    }
}