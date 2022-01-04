/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

/**
 * 接口配置服务
 *
 * @author provenceee
 * @date 2021/12/15
 */
public interface ConfigService {
    /**
     * 拦截点之后执行
     *
     * @param obj 增强的类
     * @param result 方法返回值
     */
    void after(Object obj, Object result);

    /**
     * 获取实现类的名字
     *
     * @return 实现类的名字
     */
    String getName();
}