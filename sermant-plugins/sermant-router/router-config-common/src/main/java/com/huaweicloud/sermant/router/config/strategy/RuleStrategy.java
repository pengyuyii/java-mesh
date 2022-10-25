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

package com.huaweicloud.sermant.router.config.strategy;

import com.huaweicloud.sermant.router.config.entity.Route;

import java.util.List;
import java.util.Map;

/**
 * 路由策略
 *
 * @param <T> 泛型
 * @author provenceee
 * @since 2021-10-14
 */
public interface RuleStrategy<T> {
    /**
     * 选取路由的实例
     *
     * @param serviceName 服务名
     * @param instances 实例列表
     * @param routes 路由规则
     * @return 路由过滤后的实例
     */
    List<T> getMatchInstances(String serviceName, List<T> instances, List<Route> routes);

    /**
     * 选取不匹配标签的实例
     *
     * @param serviceName 服务名
     * @param instances 实例列表
     * @param tags 标签
     * @return 路由过滤后的实例
     */
    List<T> getMismatchInstances(String serviceName, List<T> instances, List<Map<String, String>> tags);

    /**
     * 选取同区域的实例
     *
     * @param serviceName 服务名
     * @param instances 实例列表
     * @param zone 区域
     * @return 路由过滤后的实例
     */
    List<T> getZoneInstances(String serviceName, List<T> instances, String zone);

    /**
     * 选取空标签的实例
     *
     * @param serviceName 服务名
     * @param instances 实例列表
     * @param tags 标签
     * @return 路由过滤后的实例
     */
    List<T> getEmptyTagsInstances(String serviceName, List<T> instances, List<Map<String, String>> tags);
}