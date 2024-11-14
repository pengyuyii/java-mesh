/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.spring.utils;

import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.common.utils.ReflectUtils;
import com.huaweicloud.sermant.router.spring.cache.AppCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 反射工具类
 *
 * @author provenceee
 * @since 2022-07-19
 */
public class SpringRouterUtils {
    private static final String VERSION_KEY = "version";

    private static final String ZONE_KEY = "zone";

    private static final String QUERY_SEPARATOR = "&";

    private static final String KV_SEPARATOR = "=";

    private static final int KV_SPLIT_LENGTH = 2;

    private SpringRouterUtils() {
    }

    /**
     * get parameters from query sting
     *
     * @param query query sting
     * @return parameters
     */
    public static Map<String, List<String>> getParametersByQuery(String query) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyMap();
        }
        String[] queryArr = query.split(QUERY_SEPARATOR);
        Map<String, List<String>> parameters = new HashMap<>();
        for (String kv : queryArr) {
            String[] kvArr = kv.split(KV_SEPARATOR, KV_SPLIT_LENGTH);
            parameters.computeIfAbsent(kvArr[0], value -> new ArrayList<>()).add(kvArr[1]);
        }
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * 获取请求中的query
     *
     * @param obj HttpServletRequest
     * @return query
     */
    public static String getQueryString(Object obj) {
        return ReflectUtils.invokeWithNoneParameterAndReturnString(obj, "getQueryString");
    }

    /**
     * 获取请求中的uri
     *
     * @param obj HttpServletRequest
     * @return uri
     */
    public static String getRequestUri(Object obj) {
        return ReflectUtils.invokeWithNoneParameterAndReturnString(obj, "getRequestURI");
    }

    /**
     * 获取请求中的方法
     *
     * @param obj HttpServletRequest
     * @return method
     */
    public static String getMethod(Object obj) {
        return ReflectUtils.invokeWithNoneParameterAndReturnString(obj, "getMethod");
    }

    /**
     * 获取请求中的所有请求头的key
     *
     * @param obj HttpServletRequest
     * @return key
     */
    public static Enumeration<?> getHeaderNames(Object obj) {
        return (Enumeration<?>) ReflectUtils.invokeWithNoneParameter(obj, "getHeaderNames");
    }

    /**
     * 获取元数据
     *
     * @param obj HttpServletRequest
     * @param key header key
     * @return 元数据
     */
    public static Enumeration<?> getHeaders(Object obj, String key) {
        return (Enumeration<?>) ReflectUtils.invokeWithParameter(obj, "getHeaders", key, String.class);
    }

    /**
     * 获取元数据
     *
     * @param obj 对象
     * @return 元数据
     */
    public static Map<String, String> getMetadata(Object obj) {
        return (Map<String, String>) ReflectUtils.invokeWithNoneParameter(obj, "getMetadata");
    }

    /**
     * 存入元数据
     *
     * @param metadata 元数据
     * @param routerConfig 路由配置
     */
    public static void putMetaData(Map<String, String> metadata, RouterConfig routerConfig) {
        if (metadata == null) {
            return;
        }
        metadata.putIfAbsent(VERSION_KEY, routerConfig.getRouterVersion());
        if (StringUtils.isExist(routerConfig.getZone())) {
            metadata.putIfAbsent(ZONE_KEY, routerConfig.getZone());
        }
        Map<String, String> parameters = routerConfig.getParameters();
        if (!CollectionUtils.isEmpty(parameters)) {
            // 请求头在http请求中，会统一转成小写
            parameters.forEach((key, value) -> metadata.putIfAbsent(key.toLowerCase(Locale.ROOT), value));
        }
        AppCache.INSTANCE.setMetadata(metadata);
    }
}
