/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.dubbo.register.config;

/**
 * dubbo应用名缓存
 *
 * @author provenceee
 * @date 2021/12/23
 */
public enum DubboCache {
    INSTANCE;

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 注册地址
     */
//    private String address;

    private DubboConfig dubboConfig;

    /**
     * 是否加载了sc的注册spi的标志
     */
    private boolean loadSc;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

//    public String getAddress() {
//        return address;
//    }
//
//    public void setAddress(String address) {
//        this.address = address;
//    }

    public DubboConfig getDubboConfig() {
        return dubboConfig;
    }

    public void setDubboConfig(DubboConfig dubboConfig) {
        this.dubboConfig = dubboConfig;
    }

    /**
     * 加载sc spi
     */
    public void loadSc() {
        loadSc = true;
    }

    public boolean isLoadSc() {
        return loadSc;
    }
}