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

package com.huawei.flowcontrol;

import com.huawei.flowcontrol.common.config.ConfigConst;
import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.common.entity.HttpRequestEntity;
import com.huawei.flowcontrol.common.entity.RequestEntity.RequestType;
import com.huawei.flowcontrol.service.InterceptorSupporter;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.LogUtils;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * DispatcherServlet 的 API接口增强 埋点定义sentinel资源
 *
 * @author zhouss
 * @since 2022-02-11
 */
public class DispatcherServletInterceptor extends InterceptorSupporter {
    private final String className = DispatcherServletInterceptor.class.getName();

    /**
     * http请求数据转换 适应plugin -> service数据传递 注意，该方法不可抽出，由于宿主依赖仅可由该拦截器加载，因此抽出会导致找不到类
     *
     * @param request 请求
     * @return HttpRequestEntity
     */
    private Optional<HttpRequestEntity> convertToHttpEntity(Object request) {
        if (request == null) {
            return Optional.empty();
        }
        String uri = getRequestUri(request);
        return Optional.of(new HttpRequestEntity.Builder()
                .setRequestType(RequestType.SERVER)
                .setPathInfo(getPathInfo(request))
                .setServletPath(uri)
                .setHeaders(getHeaders(request))
                .setMethod(getMethod(request))
                .setServiceName(getHeader(request, ConfigConst.FLOW_REMOTE_SERVICE_NAME_HEADER_KEY))
                .build());
    }

    /**
     * 获取http请求头信息
     *
     * @param request 请求信息
     * @return headers
     */
    private Map<String, String> getHeaders(Object request) {
        final Enumeration<String> headerNames = getHeaderNames(request);
        final Map<String, String> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            headers.put(headerName, getHeader(request, headerName));
        }
        return Collections.unmodifiableMap(headers);
    }

    @Override
    protected final ExecuteContext doBefore(ExecuteContext context) throws Exception {
        LogUtils.printHttpRequestBeforePoint(context);
        final Object[] allArguments = context.getArguments();
        final Object request = allArguments[0];
        final FlowControlResult result = new FlowControlResult();
        final Optional<HttpRequestEntity> httpRequestEntity = convertToHttpEntity(request);
        if (!httpRequestEntity.isPresent()) {
            return context;
        }
        chooseHttpService().onBefore(className, httpRequestEntity.get(), result);
        if (result.isSkip()) {
            context.skip(null);
            final Object response = allArguments[1];
            if (response != null) {
                setStatus(response, result.getResponse().getCode());
                getWriter(response).print(result.buildResponseMsg());
            }
        }
        return context;
    }

    @Override
    protected final ExecuteContext doAfter(ExecuteContext context) {
        chooseHttpService().onAfter(className, context.getResult());
        LogUtils.printHttpRequestAfterPoint(context);
        return context;
    }

    @Override
    protected final ExecuteContext doThrow(ExecuteContext context) {
        chooseHttpService().onThrow(className, context.getThrowable());
        LogUtils.printHttpRequestOnThrowPoint(context);
        return context;
    }

    private String getRequestUri(Object httpServletRequest) {
        return getString(httpServletRequest, "getRequestURI");
    }

    private String getPathInfo(Object httpServletRequest) {
        return getString(httpServletRequest, "getPathInfo");
    }

    private String getMethod(Object httpServletRequest) {
        return getString(httpServletRequest, "getMethod");
    }

    private Enumeration<String> getHeaderNames(Object httpServletRequest) {
        return (Enumeration<String>) ReflectUtils.invokeMethodWithNoneParameter(httpServletRequest, "getHeaderNames")
                .orElse(null);
    }

    private String getHeader(Object httpServletRequest, String key) {
        return (String) ReflectUtils.invokeMethod(httpServletRequest, "getHeader", new Class[]{String.class},
                new Object[]{key}).orElse(null);
    }

    private PrintWriter getWriter(Object httpServletRequest) {
        return (PrintWriter) ReflectUtils.invokeMethodWithNoneParameter(httpServletRequest, "getWriter")
                .orElse(null);
    }

    private void setStatus(Object httpServletResponse, int code) {
        ReflectUtils.invokeMethod(httpServletResponse, "setStatus", new Class[]{int.class}, new Object[]{code});
    }

    private String getString(Object object, String method) {
        return (String) ReflectUtils.invokeMethodWithNoneParameter(object, method).orElse(null);
    }
}
