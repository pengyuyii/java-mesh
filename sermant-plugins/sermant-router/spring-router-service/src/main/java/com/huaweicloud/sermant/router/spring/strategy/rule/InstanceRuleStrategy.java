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

package com.huaweicloud.sermant.router.spring.strategy.rule;

import com.huaweicloud.sermant.router.config.strategy.AbstractRuleStrategy;
import com.huaweicloud.sermant.router.spring.strategy.instance.EmptyTagsInstanceStrategy;
import com.huaweicloud.sermant.router.spring.strategy.instance.MatchInstanceStrategy;
import com.huaweicloud.sermant.router.spring.strategy.instance.MismatchInstanceStrategy;
import com.huaweicloud.sermant.router.spring.strategy.instance.ZoneInstanceStrategy;
import com.huaweicloud.sermant.router.spring.strategy.mapper.AbstractMetadataMapper;

/**
 * 路由规则匹配策略
 *
 * @param <I> 实例泛型
 * @author provenceee
 * @since 2021-10-14
 */
public class InstanceRuleStrategy<I> extends AbstractRuleStrategy<I> {
    /**
     * 构造方法
     *
     * @param mapper metadata获取方法
     */
    public InstanceRuleStrategy(AbstractMetadataMapper<I> mapper) {
        super("spring", new RuleStrategies<>(new MatchInstanceStrategy<>(), new MismatchInstanceStrategy<>(),
            new ZoneInstanceStrategy<>(), new EmptyTagsInstanceStrategy<>()), mapper);
    }
}