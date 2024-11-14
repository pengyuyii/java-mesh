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

package com.huaweicloud.sermant.router.spring.utils;

import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.config.entity.Match;
import com.huaweicloud.sermant.router.config.entity.MatchRule;
import com.huaweicloud.sermant.router.config.entity.MatchStrategy;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.config.entity.ValueMatch;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 路由插件工具类
 *
 * @author provenceee
 * @since 2021-06-21
 */
public class RouteUtils {
    private RouteUtils() {
    }

    /**
     * 获取匹配的泳道
     *
     * @param list 有效的规则
     * @param headers header
     * @param parameters parameters
     * @return 匹配的泳道标记
     */
    public static List<Route> getLaneRoutesByParameterList(List<Rule> list, Map<String, List<String>> headers,
            Map<String, List<String>> parameters) {
        for (Rule rule : list) {
            Match match = rule.getMatch();
            if (match == null) {
                return rule.getRoute();
            }
            if (isMatchByHeaders(match.getHeaders(), headers) && isMatchByParameterList(match.getParameters(),
                    parameters)) {
                return rule.getRoute();
            }
        }
        return Collections.emptyList();
    }

    private static boolean isMatchByHeaders(Map<String, List<MatchRule>> matchHeaders,
            Map<String, List<String>> headers) {
        if (CollectionUtils.isEmpty(matchHeaders)) {
            return true;
        }
        for (Entry<String, List<MatchRule>> entry : matchHeaders.entrySet()) {
            String key = entry.getKey();
            List<MatchRule> matchRuleList = entry.getValue();
            for (MatchRule matchRule : matchRuleList) {
                ValueMatch valueMatch = matchRule.getValueMatch();
                List<String> values = valueMatch.getValues();
                MatchStrategy matchStrategy = valueMatch.getMatchStrategy();
                List<String> list = headers.get(key);
                String arg = list == null ? null : list.get(0);
                if (!matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 只要一个匹配不上，那就是不匹配
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isMatchByParameterList(Map<String, List<MatchRule>> matchParameters,
            Map<String, List<String>> parameters) {
        if (CollectionUtils.isEmpty(matchParameters)) {
            return true;
        }
        for (Entry<String, List<MatchRule>> entry : matchParameters.entrySet()) {
            String key = entry.getKey();
            List<MatchRule> matchRuleList = entry.getValue();
            for (MatchRule matchRule : matchRuleList) {
                ValueMatch valueMatch = matchRule.getValueMatch();
                List<String> values = valueMatch.getValues();
                MatchStrategy matchStrategy = valueMatch.getMatchStrategy();
                List<String> list = parameters.get(key);
                String arg = CollectionUtils.isEmpty(list) ? null : list.get(0);
                if (!matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 只要一个匹配不上，那就是不匹配
                    return false;
                }
            }
        }
        return true;
    }
}