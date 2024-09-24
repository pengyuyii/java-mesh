/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.core.plugin.agent.enhance;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.agent.interceptor.Interceptor;
import io.sermant.core.service.inject.config.InjectConfig;

import java.util.Set;

public abstract class AbstractClassLoaderInterceptor implements Interceptor {
    private final Set<String> essentialPackage;

    /**
     * constructor
     */
    public AbstractClassLoaderInterceptor() {
        essentialPackage = ConfigManager.getConfig(InjectConfig.class).getEssentialPackage();
    }

    protected boolean isSermantClass(String name) {
        for (String prefix : essentialPackage) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isSermantResource(String path) {
        String name = path.replace('/', '.');
        for (String prefix : essentialPackage) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
