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

package com.huawei.dubbo.registry.interceptor;

import com.huawei.dubbo.registry.utils.AnnotationUtil;
import com.huawei.dubbo.registry.utils.ReflectUtils;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;

import com.alibaba.dubbo.rpc.StaticContext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * 增强ClusterUtils类的mergeUrl方法
 *
 * @author provenceee
 * @since 2021-06-28
 */
public class RestInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        Class<?> clazz =
            (Class<?>) StaticContext.getContext("service.classimpl")
                .get(ReflectUtils.getServiceKey(context.getArguments()[2]));
        Map<String, Object> map = new HashMap<>();
        map.put("value", clazz.getSimpleName());
        Path path = AnnotationUtil.createAnnotationFromMap(Path.class, map);
        AnnotationUtil.addAnnotation(clazz, path);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("value", new String[]{MediaType.APPLICATION_JSON + ";charset=UTF-8"});
        Consumes consumes = AnnotationUtil.createAnnotationFromMap(Consumes.class, map2);
        AnnotationUtil.addAnnotation(clazz, consumes);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("value", new String[]{MediaType.APPLICATION_JSON + ";charset=UTF-8"});
        Produces produces = AnnotationUtil.createAnnotationFromMap(Produces.class, map3);
        AnnotationUtil.addAnnotation(clazz, produces);

        Method[] methods = (Method[]) com.huaweicloud.sermant.core.utils.ReflectUtils
            .invokeMethod(clazz, "privateGetDeclaredMethods", new Class[]{boolean.class}, new Object[]{true})
            .orElse(null);
        if (methods == null) {
            return context;
        }
        for (Method method : methods) {
            if (!Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            Map<String, Object> map1 = new HashMap<>();
            map1.put("value", method.getName());
            Path path1 = AnnotationUtil.createAnnotationFromMap(Path.class, map1);
            AnnotationUtil.addAnnotation(method, path1);

            POST post = AnnotationUtil.createAnnotationFromMap(POST.class, Collections.emptyMap());
            AnnotationUtil.addAnnotation(method, post);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}