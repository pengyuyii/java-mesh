/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/skywalking/apm/agent/core/boot/ServiceManager.java
 * from the Apache Skywalking project.
 */

package io.sermant.core.service;

import io.sermant.core.classloader.ClassLoaderManager;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.ConfigManager;
import io.sermant.core.event.EventManager;
import io.sermant.core.event.collector.FrameworkEventCollector;
import io.sermant.core.exception.DupServiceException;
import io.sermant.core.utils.KeyGenerateUtils;
import io.sermant.core.utils.SpiLoadUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ServiceManager
 *
 * @author justforstudy-A, beetle-man, HapThorin
 * @version 1.0.0
 * @since 2021-10-26
 */
public class ServiceManager {
    /**
     * Class name of dynamic config service
     */
    public static final String BUFFERED_DYNAMIC_CONFIG_SERVICE =
            "io.sermant.implement.service.dynamicconfig.BufferedDynamicConfigService";

    /**
     * Class name of heartbeat service
     */
    public static final String HEARTBEAT_SERVICE_IMPL =
            "io.sermant.implement.service.heartbeat.HeartbeatServiceImpl";

    /**
     * Class name of inject service
     */
    public static final String INJECT_SERVICE_IMPL =
            "io.sermant.implement.service.inject.InjectServiceImpl";

    /**
     * Class name of netty gateway service
     */
    public static final String NETTY_GATEWAY_CLIENT =
            "io.sermant.implement.service.send.netty.NettyGatewayClient";

    /**
     * Class name of tracing service
     */
    public static final String TRACING_SERVICE_IMPL =
            "io.sermant.implement.service.tracing.TracingServiceImpl";

    /**
     * HttpServer service name
     */
    public static final String HTTP_SERVER_SERVICE_IMPL =
            "io.sermant.implement.service.httpserver.HttpServerServiceImpl";

    /**
     * xDS Service Discover
     */
    public static final String XDS_CORE_SERVICE_IMPL =
            "io.sermant.implement.service.xds.XdsCoreServiceImpl";

    /**
     * Hot plugging service
     */
    public static final String HOT_PLUGGING_SERVICE_IMPL =
            "io.sermant.implement.service.hotplugging.HotPluggingServiceImpl";

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * service map
     */
    private static final Map<String, BaseService> SERVICES = new HashMap<>();

    /**
     * Constructor.
     */
    protected ServiceManager() {
    }

    /**
     * Initialize all services
     */
    public static void initServices() {
        ServiceConfig serviceConfig = ConfigManager.getConfig(ServiceConfig.class);
        List<String> startServiceArray = new ArrayList<>();
        for (final BaseService service : ServiceLoader.load(BaseService.class,
                ClassLoaderManager.getFrameworkClassLoader())) {
            String serviceName = service.getClass().getName();
            if (serviceConfig.checkServiceEnable(serviceName) && loadService(service,
                    service.getClass(), BaseService.class)) {
                service.start();
                startServiceArray.add(serviceName);
            }
        }
        FrameworkEventCollector.getInstance().collectServiceStartEvent(startServiceArray.toString());
        addStopHook(); // Start after all services are loaded
    }

    /**
     * Get service object
     *
     * @param serviceClass service class
     * @param <T> service generic type
     * @return service object
     * @throws IllegalArgumentException The corresponding service could not be found
     */
    public static <T extends BaseService> T getService(Class<T> serviceClass) {
        String serviceKey = KeyGenerateUtils.generateClassKeyWithClassLoader(serviceClass);
        final BaseService baseService = SERVICES.get(serviceKey);
        if (baseService != null && serviceClass.isAssignableFrom(baseService.getClass())) {
            return (T) baseService;
        }
        throw new IllegalArgumentException("Service instance of [" + serviceClass + "] is not found. ");
    }

    /**
     * Load the service object to the service map
     *
     * @param service service object
     * @param serviceCls service class
     * @param baseCls base class of service，used for spi
     * @return load result
     */
    protected static boolean loadService(BaseService service, Class<?> serviceCls,
            Class<? extends BaseService> baseCls) {
        if (serviceCls == null || serviceCls == baseCls || !baseCls.isAssignableFrom(serviceCls)) {
            return false;
        }
        final String serviceKey = KeyGenerateUtils.generateClassKeyWithClassLoader(serviceCls);
        final BaseService oldService = SERVICES.get(serviceKey);
        if (oldService != null && oldService.getClass() == service.getClass()) {
            return false;
        }
        boolean isLoadSucceed = false;
        final BaseService betterService =
                SpiLoadUtils.getBetter(oldService, service, new SpiLoadUtils.WeightEqualHandler<BaseService>() {
                    @Override
                    public BaseService handle(BaseService source, BaseService target) {
                        throw new DupServiceException(serviceKey);
                    }
                });
        if (betterService != oldService) {
            SERVICES.put(serviceKey, service);
            isLoadSucceed = true;
        }
        isLoadSucceed |= loadService(service, serviceCls.getSuperclass(), baseCls);
        for (Class<?> interfaceCls : serviceCls.getInterfaces()) {
            isLoadSucceed |= loadService(service, interfaceCls, baseCls);
        }
        return isLoadSucceed;
    }

    /**
     * stop service
     *
     * @param serviceName service name
     */
    protected static void stopService(String serviceName) {
        SERVICES.remove(serviceName).stop();
    }

    /**
     * Add a hook for stop service
     */
    private static void addStopHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            offerEvent();
            for (Map.Entry<String, BaseService> entry : SERVICES.entrySet()) {
                try {
                    entry.getValue().stop();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error occurs while stopping service: " + entry.getKey(), ex);
                }
            }
        }));
    }

    private static void offerEvent() {
        // Report service stop event
        List<String> stopServiceArray = new ArrayList<>();
        for (BaseService baseService : new HashSet<>(SERVICES.values())) {
            stopServiceArray.add(baseService.getClass().getName());
        }
        FrameworkEventCollector.getInstance().collectServiceStopEvent(stopServiceArray.toString());

        // Report Sermant stop event
        FrameworkEventCollector.getInstance().collectAgentStopEvent();
        EventManager.shutdown();
    }

    /**
     * Close ServiceManager
     */
    public static void shutdown() {
        Set<BaseService> services = new HashSet<>(SERVICES.values());
        for (BaseService baseService : services) {
            baseService.stop();
        }
        SERVICES.clear();
    }
}
