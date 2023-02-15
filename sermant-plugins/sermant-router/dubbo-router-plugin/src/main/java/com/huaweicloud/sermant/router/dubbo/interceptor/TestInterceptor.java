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

package com.huaweicloud.sermant.router.dubbo.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.router.common.utils.ReflectUtils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.dubbo.remoting.zookeeper.ZookeeperClient;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 增强ContextFilter类的invoke方法
 *
 * @author provenceee
 * @since 2022-09-26
 */
public class TestInterceptor extends AbstractInterceptor {
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        ZookeeperClient zkClient = (ZookeeperClient) context.getMemberFieldValue("zkClient");
        executorService.schedule(() -> {
            ZooKeeper zooKeeper = null;
            try {
                zooKeeper = ((CuratorFramework) ReflectUtils.getFieldValue(zkClient, "client")
                    .orElse(null))
                    .getZookeeperClient()
                    .getZooKeeper();

            } catch (Exception e) {
                e.printStackTrace();
            }
            zkClient.close();
            try {
                zooKeeper.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }, 30, TimeUnit.SECONDS);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        return context;
    }
}