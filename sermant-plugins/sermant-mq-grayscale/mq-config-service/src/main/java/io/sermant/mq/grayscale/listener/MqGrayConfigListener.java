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

package io.sermant.mq.grayscale.listener;

import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

/**
 * grayscale dynamic config listener
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class MqGrayConfigListener implements DynamicConfigListener {
    private final MqGrayConfigHandler handler;

    /**
     * construction
     */
    public MqGrayConfigListener() {
        handler = new MqGrayConfigHandler();
    }

    @Override
    public void process(DynamicConfigEvent event) {
        handler.handle(event);
    }
}
