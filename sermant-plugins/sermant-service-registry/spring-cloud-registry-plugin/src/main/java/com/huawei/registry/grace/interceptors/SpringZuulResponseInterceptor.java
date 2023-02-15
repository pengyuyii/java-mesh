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
 * Based on org/apache/dubbo/rpc/protocol/dubbo/DubboInvoker.java
 * from the Apache Dubbo project.
 */

package com.huawei.registry.grace.interceptors;

import com.huawei.registry.config.grace.GraceConstants;
import com.huawei.registry.config.grace.GraceContext;
import com.huawei.registry.utils.RefreshUtils;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import com.netflix.zuul.context.RequestContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截zuul请求
 *
 * @author zhouss
 * @since 2022-05-23
 */
public class SpringZuulResponseInterceptor extends GraceSwitchInterceptor {
    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        RequestContext requestContext = RequestContext.getCurrentContext();
        Map<String, List<String>> map = getGraceIpHeaders();
        map.forEach((k, v) -> requestContext.addZuulRequestHeader(k, v.get(0)));
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        final Object rawRequest = context.getArguments()[0];
        final Object rawResponse = context.getArguments()[1];
        if (!(rawResponse instanceof HttpServletResponse) || !(rawRequest instanceof HttpServletRequest)) {
            return context;
        }
        HttpServletResponse response = (HttpServletResponse) rawResponse;
        GraceContext.INSTANCE.getGraceShutDownManager()
            .addShutdownEndpoints(response.getHeaders(GraceConstants.MARK_SHUTDOWN_SERVICE_ENDPOINT));
        HttpServletRequest request = (HttpServletRequest) rawRequest;
        RefreshUtils.refreshTargetServiceInstances(request.getRemoteHost(),
            Collections.singleton(response.getHeader(GraceConstants.MARK_SHUTDOWN_SERVICE_NAME)));
        return context;
    }

    @Override
    protected boolean isEnabled() {
        return super.isEnabled() && graceConfig.isEnableGraceShutdown();
    }
}
