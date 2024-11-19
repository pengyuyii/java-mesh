/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.flowcontrol.common.handler.retry;

import io.sermant.core.classloader.ClassLoaderManager;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.flowcontrol.common.config.FlowControlConfig;
import io.sermant.flowcontrol.common.support.ReflectMethodCacheSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * retryAbstraction，provide a common method
 *
 * @author zhouss
 * @since 2022-02-10
 */
public abstract class AbstractRetry extends ReflectMethodCacheSupport implements Retry {
    /**
     * retry exception class
     */
    protected Class<? extends Throwable>[] classes;

    /**
     * Attempt to load based on user-defined retry exception
     *
     * @param classNames exception class name
     * @return classes
     */
    protected final Class<? extends Throwable>[] findClass(String[] classNames) {
        if (classNames == null || classNames.length == 0) {
            return new Class[0];
        }
        final List<Class<?>> result = new ArrayList<>(classNames.length);
        for (String className : classNames) {
            try {
                result.add(Class.forName(className, false,
                        ClassLoaderManager.getContextClassLoaderOrUserClassLoader()));
            } catch (ClassNotFoundException exception) {
                LoggerFactory.getLogger().info(String.format(Locale.ENGLISH,
                        "Can not find retry exception class %s", className));
            }
        }
        return result.toArray(new Class[0]);
    }

    @Override
    public boolean needRetry(Set<String> statusList, Object result) {
        if (result == null) {
            return false;
        }
        final Optional<String> code = getCode(result);
        return code.filter(statusList::contains).isPresent();
    }

    /**
     * implemented by subclasses， if subclass implement {@link #needRetry(Set, Object)}, no need to implement the get
     * code method
     *
     * @param result interface response result
     * @return response status code
     * @throws UnsupportedOperationException unsupported operation
     */
    protected Optional<String> getCode(Object result) {
        throw new UnsupportedOperationException();
    }

    /**
     * get retry exception
     *
     * @return Class<? extends Throwable>[]
     * @throws IllegalArgumentException parameter is thrown illegally
     */
    protected final Class<? extends Throwable>[] getRetryExceptions() {
        if (classes != null) {
            return classes;
        }
        synchronized (this) {
            final RetryFramework retryFramework = retryType();
            final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
            String[] retryExceptions;
            if (retryFramework == RetryFramework.SPRING_CLOUD) {
                retryExceptions = pluginConfig.getSpringRetryExceptions();
            } else if (retryFramework == RetryFramework.ALIBABA_DUBBO) {
                retryExceptions = pluginConfig.getAlibabaDubboRetryExceptions();
            } else if (retryFramework == RetryFramework.APACHE_DUBBO) {
                retryExceptions = pluginConfig.getApacheDubboRetryExceptions();
            } else {
                throw new IllegalArgumentException("Not supported retry framework! " + retryFramework);
            }
            classes = findClass(retryExceptions);
        }
        return classes;
    }
}
