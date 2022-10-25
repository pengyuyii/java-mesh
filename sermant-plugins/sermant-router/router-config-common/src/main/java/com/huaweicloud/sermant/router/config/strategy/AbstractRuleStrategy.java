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

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.utils.RuleUtils;
import com.huaweicloud.sermant.router.config.utils.RuleUtils.RouteResult;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * 匹配策略
 *
 * @param <I> 实例泛型
 * @author provenceee
 * @since 2021-10-14
 */
public abstract class AbstractRuleStrategy<I> implements RuleStrategy<I> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final InstanceStrategy<I, Map<String, String>> matchInstanceStrategy;

    private final InstanceStrategy<I, List<Map<String, String>>> mismatchInstanceStrategy;

    private final InstanceStrategy<I, String> zoneInstanceStrategy;

    private final InstanceStrategy<I, List<String>> emptyTagsInstanceStrategy;

    private final Function<I, Map<String, String>> mapper;

    private final String source;

    /**
     * 构造方法
     *
     * @param source 来源
     * @param ruleStrategies 策略
     * @param mapper 获取metadata的方法
     */
    public AbstractRuleStrategy(String source, RuleStrategies<I> ruleStrategies,
        Function<I, Map<String, String>> mapper) {
        this.source = source;
        this.matchInstanceStrategy = ruleStrategies.getMatchInstanceStrategy();
        this.mismatchInstanceStrategy = ruleStrategies.getMismatchInstanceStrategy();
        this.zoneInstanceStrategy = ruleStrategies.getZoneInstanceStrategy();
        this.emptyTagsInstanceStrategy = ruleStrategies.getEmptyTagsInstanceStrategy();
        this.mapper = mapper;
    }

    @Override
    public List<I> getMatchInstances(String serviceName, List<I> instances, List<Route> routes) {
        RouteResult routeResult = RuleUtils.getTargetTags(routes);
        if (!routeResult.isMatch()) {
            // 没有匹配的标签，返回空标签实例
            return getEmptyTagsInstances(serviceName, instances, routeResult.getMismatchTag());
        }
        List<I> result = getInstances(matchInstanceStrategy, serviceName, instances, routeResult.getMatchTag());
        if (CollectionUtils.isEmpty(result)) {
            // 没有匹配的实例，返回空标签实例
            return getEmptyTagsInstances(serviceName, instances, routeResult.getMismatchTag());
        }
        return result;
    }

    @Override
    public List<I> getMismatchInstances(String serviceName, List<I> instances, List<Map<String, String>> tags) {
        List<I> result = getInstances(mismatchInstanceStrategy, serviceName, instances, tags);
        return CollectionUtils.isEmpty(result) ? instances : result;
    }

    /**
     * 选取同区域的实例
     *
     * @param instances 实例列表
     * @param zone 区域
     * @return 路由过滤后的实例
     */
    @Override
    public List<I> getZoneInstances(String serviceName, List<I> instances, String zone) {
        if (StringUtils.isBlank(zone)) {
            return instances;
        }
        List<I> result = getInstances(zoneInstanceStrategy, serviceName, instances, zone);
        return CollectionUtils.isEmpty(result) ? instances : result;
    }

    /**
     * 选取空标签的实例
     *
     * @param serviceName 服务名
     * @param instances 实例列表
     * @param tags 标签
     * @return 路由过滤后的实例
     */
    @Override
    public List<I> getEmptyTagsInstances(String serviceName, List<I> instances, List<Map<String, String>> tags) {
        List<String> versions = new ArrayList<>();
        for (Map<String, String> tag : tags) {
            String version = tag.get("version");
            if (StringUtils.isExist(version)) {
                versions.add(version);
            }
        }
        List<I> result = getInstances(emptyTagsInstanceStrategy, serviceName, instances, versions);

        // 没有空标签的实例，返回不匹配的标签实例
        return CollectionUtils.isEmpty(result) ? getMismatchInstances(serviceName, instances, tags) : result;
    }

    private <T> List<I> getInstances(InstanceStrategy<I, T> instanceStrategy, String serviceName, List<I> instances,
        T tags) {
        List<I> resultList = new ArrayList<>();
        for (I instance : instances) {
            if (instanceStrategy.isMatch(instance, tags, mapper)) {
                resultList.add(instance);
            }
        }
        boolean mismatch = CollectionUtils.isEmpty(resultList);
        if (mismatch) {
            LOGGER.warning(String.format(Locale.ROOT,
                "Cannot match instances, %s serviceName is %s, tags is %s.", source, serviceName,
                JSONObject.toJSONString(tags)));
        } else {
            LOGGER.fine(String.format(Locale.ROOT, "Match instances, %s serviceName is %s, tags is %s.", source,
                serviceName, JSONObject.toJSONString(tags)));
        }
        return mismatch ? Collections.emptyList() : resultList;
    }

    /**
     * 策略
     *
     * @param <I> 实例泛型
     * @since 2021-10-14
     */
    public static class RuleStrategies<I> {
        private final InstanceStrategy<I, Map<String, String>> matchInstanceStrategy;

        private final InstanceStrategy<I, List<Map<String, String>>> mismatchInstanceStrategy;

        private final InstanceStrategy<I, String> zoneInstanceStrategy;

        private final InstanceStrategy<I, List<String>> emptyTagsInstanceStrategy;

        /**
         * 构造方法
         *
         * @param matchInstanceStrategy 匹配上的策略
         * @param mismatchInstanceStrategy 匹配不上的策略
         * @param zoneInstanceStrategy 区域路由策略
         * @param emptyTagsInstanceStrategy 空标签路由策略
         */
        public RuleStrategies(
            InstanceStrategy<I, Map<String, String>> matchInstanceStrategy,
            InstanceStrategy<I, List<Map<String, String>>> mismatchInstanceStrategy,
            InstanceStrategy<I, String> zoneInstanceStrategy,
            InstanceStrategy<I, List<String>> emptyTagsInstanceStrategy) {
            this.matchInstanceStrategy = matchInstanceStrategy;
            this.mismatchInstanceStrategy = mismatchInstanceStrategy;
            this.zoneInstanceStrategy = zoneInstanceStrategy;
            this.emptyTagsInstanceStrategy = emptyTagsInstanceStrategy;
        }

        public InstanceStrategy<I, Map<String, String>> getMatchInstanceStrategy() {
            return matchInstanceStrategy;
        }

        public InstanceStrategy<I, List<Map<String, String>>> getMismatchInstanceStrategy() {
            return mismatchInstanceStrategy;
        }

        public InstanceStrategy<I, String> getZoneInstanceStrategy() {
            return zoneInstanceStrategy;
        }

        public InstanceStrategy<I, List<String>> getEmptyTagsInstanceStrategy() {
            return emptyTagsInstanceStrategy;
        }
    }
}