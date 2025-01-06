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

package io.sermant.flowcontrol.common.xds.retry;

import io.sermant.flowcontrol.common.handler.retry.Retry;

/**
 * Retry Evaluator
 *
 * @author zhp
 * @since 2024-11-29
 */
public interface RetryCondition {
    /**
     * Determine whether a retry is necessary
     *
     * @param ex exceptions occurring during the service invocation
     * @param retry retry policy
     * @param result response result
     * @param statusCode response status code
     * @return The result of the decision
     */
    boolean needRetry(Retry retry, Throwable ex, String statusCode, Object result);
}