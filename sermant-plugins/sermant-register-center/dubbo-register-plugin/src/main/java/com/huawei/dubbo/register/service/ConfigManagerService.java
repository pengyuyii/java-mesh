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

package com.huawei.dubbo.register.service;

import com.huawei.sermant.core.plugin.service.PluginService;

/**
 * @author provenceee
 * @date 2022/1/13
 */
public interface ConfigManagerService extends PluginService {
    /**
     * 拦截点之后执行
     *
     * @param obj 增强的类
     * @param arguments 参数
     * @param result 方法返回值
     */
    void after(Object obj, Object[] arguments, Object result);
}
