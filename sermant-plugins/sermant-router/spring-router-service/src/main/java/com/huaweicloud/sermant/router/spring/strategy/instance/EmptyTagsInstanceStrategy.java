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

package com.huaweicloud.sermant.router.spring.strategy.instance;

import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.config.strategy.AbstractInstanceStrategy;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 匹配空tags的实例
 *
 * @param <I> 实例泛型
 * @author provenceee
 * @since 2022-10-24
 */
public class EmptyTagsInstanceStrategy<I> extends AbstractInstanceStrategy<I, List<String>> {
    /**
     * 匹配空tags的实例
     *
     * @param instance 实例
     * @param versions 不匹配的版本号
     * @param mapper 获取metadata的方法
     * @return 是否匹配
     */
    @Override
    public boolean isMatch(I instance, List<String> versions, Function<I, Map<String, String>> mapper) {
        Map<String, String> metadata = getMetadata(instance, mapper);
        if (CollectionUtils.isEmpty(metadata)) {
            return true;
        }

        // 剔除versions中的版本实例
        if (versions.contains(metadata.get(VERSION_KEY))) {
            return false;
        }
        Set<String> keys = metadata.keySet();
        for (String key : keys) {
            // version为默认标签，zone为az路由的标签，所以有这俩key以外不属于空tags的实例
            if (!VERSION_KEY.equals(key) && !ZONE_KEY.equals(key)) {
                return false;
            }
        }
        return true;
    }
}