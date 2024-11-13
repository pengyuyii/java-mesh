/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/dubbo/remoting/exchange/support/header/HeaderExchangeServer.java
 * from the Apache Dubbo project.
 */

package com.huawei.registry.grace.interceptors;

import com.huawei.registry.config.GraceConfig;
import com.huawei.registry.config.grace.GraceConstants;
import com.huawei.registry.config.grace.GraceContext;
import com.huawei.registry.config.grace.GraceShutDownManager;
import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.context.RegisterContext.ClientInfo;
import com.huawei.registry.services.GraceService;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.huaweicloud.sermant.core.utils.StringUtils;

/**
 * Spring Web请求拦截器
 *
 * @author zhouss
 * @since 2022-05-23
 */
public class DispatcherServletInterceptor extends GraceSwitchInterceptor {
    private final GraceService graceService;

    private final GraceConfig graceConfig;

    /**
     * 构造方法
     */
    public DispatcherServletInterceptor() {
        graceService = PluginServiceManager.getPluginService(GraceService.class);
        graceConfig = PluginConfigManager.getPluginConfig(GraceConfig.class);
    }

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (GraceContext.INSTANCE.getStartWarmUpTime() == 0) {
            GraceContext.INSTANCE.setStartWarmUpTime(System.currentTimeMillis());
        }
        Object[] arguments = context.getArguments();
        Object request = arguments[0];
        Object response = arguments[1];

        addGraceAddress(request);
        final GraceShutDownManager graceShutDownManager = GraceContext.INSTANCE.getGraceShutDownManager();
        graceShutDownManager.increaseRequestCount();
        if (graceShutDownManager.isShutDown() && graceConfig.isEnableGraceShutdown()) {
            // 已被标记为关闭状态, 开始统计进入的请求数
            final ClientInfo clientInfo = RegisterContext.INSTANCE.getClientInfo();
            addHeader(response, GraceConstants.MARK_SHUTDOWN_SERVICE_ENDPOINT,
                buildEndpoint(clientInfo.getIp(), clientInfo.getPort()));
            addHeader(response, GraceConstants.MARK_SHUTDOWN_SERVICE_ENDPOINT,
                buildEndpoint(clientInfo.getHost(), clientInfo.getPort()));
            addHeader(response, GraceConstants.MARK_SHUTDOWN_SERVICE_NAME, clientInfo.getServiceName());
        }
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        GraceContext.INSTANCE.getGraceShutDownManager().decreaseRequestCount();
        return context;
    }

    @Override
    public ExecuteContext doThrow(ExecuteContext context) {
        GraceContext.INSTANCE.getGraceShutDownManager().decreaseRequestCount();
        return context;
    }

    private void addGraceAddress(Object request) {
        if (graceConfig.isEnableSpring() && graceConfig.isEnableGraceShutdown() && graceConfig.isEnableOfflineNotify()
            && GraceConstants.GRACE_OFFLINE_SOURCE_VALUE
                .equals(getHeader(request, GraceConstants.GRACE_OFFLINE_SOURCE_KEY))) {
            String address = getHeader(request, GraceConstants.SERMANT_GRACE_ADDRESS);
            if (StringUtils.isBlank(address)) {
                address = getRemoteAddr(request) + ":" + getServerPort(request);
            }
            graceService.addAddress(address);
        }
    }

    private void addHeader(Object httpServletRequest, String key, String value) {
        ReflectUtils.invokeMethod(httpServletRequest, "addHeader", new Class[]{String.class, String.class},
                new Object[]{key, value});
    }

    private int getServerPort(Object httpServletRequest) {
        return (int) ReflectUtils.invokeMethodWithNoneParameter(httpServletRequest, "getServerPort").orElse(0);
    }

    private String getRemoteAddr(Object httpServletRequest) {
        return getString(httpServletRequest, "getRemoteAddr");
    }

    private String getHeader(Object httpServletRequest, String key) {
        return (String) ReflectUtils.invokeMethod(httpServletRequest, "getHeader", new Class[]{String.class},
                new Object[]{key}).orElse(null);
    }

    private String getString(Object object, String method) {
        return (String) ReflectUtils.invokeMethodWithNoneParameter(object, method).orElse(null);
    }
}
