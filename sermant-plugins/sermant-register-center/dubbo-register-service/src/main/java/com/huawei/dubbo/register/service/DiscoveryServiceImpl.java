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

package com.huawei.dubbo.register.service;

import com.huawei.sermant.core.service.ServiceManager;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.client.ServiceInstance;

import java.util.List;
import java.util.Set;

/**
 * @author provenceee
 * @date 2022/1/4
 */
public class DiscoveryServiceImpl implements DiscoveryService {
    //    private static final Logger LOGGER = LogFactory.getLogger();
//    private static final EventBus EVENT_BUS = new EventBus();
    //    private static final Map<String, Microservice> INTERFACE_MAP = new ConcurrentHashMap<>();
//    private static final CountDownLatch FIRST_REGISTRATION_WAITER = new CountDownLatch(1);
//    private static final AtomicBoolean SHUTDOWN = new AtomicBoolean();
//    private static final String FRAMEWORK_NAME = "sermant";
    //    private static final String DEFAULT_TENANT_NAME = "default";
//    private ServiceCenterClient client;
//    private Microservice microservice;
//    private MicroserviceInstance microserviceInstance;
//    private ServiceCenterRegistration serviceCenterRegistration;
    //    private ServiceCenterDiscovery serviceCenterDiscovery;
//    private boolean registrationInProgress = true;
//    private DubboConfig config;
    private URL registryUrl;
    //    private ServiceInstancesChangedListener listener;
    private RegistryService registryService;

    @Override
    public void init(URL registryUrl) {
        this.registryUrl = registryUrl;
        registryService = ServiceManager.getService(RegistryService.class);
//        EVENT_BUS.register(this);
//        client = new ServiceCenterClient(new AddressManager("default",
//                Collections.singletonList("http://127.0.0.1:30100")),
//                new SSLProperties(), new DefaultRequestAuthHeaderProvider(), DEFAULT_TENANT_NAME,
//                Collections.emptyMap());
    }

    @Override
    public void doRegister(ServiceInstance serviceInstance) {
//        ThrowableAction.execute(() -> {
//        getMicroservice(serviceInstance);
//        getMicroserviceInstance(serviceInstance);
//        getServiceCenterRegistration(serviceInstance.toURL());
//        EVENT_BUS.register(this);
//        serviceCenterRegistration.startRegistration();
//        });
    }

//    private void getMicroservice(ServiceInstance serviceInstance) {
//        microservice = new Microservice(DubboCache.INSTANCE.getServiceName());
//        microservice.setAppId("default");
////        serviceInstance.get.
//        microservice.setVersion("3.0.0");
//        microservice.setEnvironment("testing");
//        Framework framework = new Framework();
//        framework.setName(FRAMEWORK_NAME);
//        framework.setVersion("3.0.0");
//        microservice.setFramework(framework);
////        microservice.setProperties(serviceInstance.getMetadata());
////        microservice.setSchemas(Collections.singletonList(serviceInstance.toURL().toString()));
//    }

//    private void getMicroserviceInstance(ServiceInstance serviceInstance) {
//        microserviceInstance = new MicroserviceInstance();
//        microserviceInstance.setStatus(MicroserviceInstanceStatus.UP);
//        HealthCheck healthCheck = new HealthCheck();
//        healthCheck.setMode(HealthCheckMode.pull);
//        healthCheck.setInterval(15);
//        healthCheck.setTimes(3);
//        microserviceInstance.setHealthCheck(healthCheck);
//        microserviceInstance.setHostName(serviceInstance.getHost());
//        microserviceInstance.setEndpoints(Collections.singletonList(serviceInstance.getAddress()));
//        microserviceInstance.setProperties(serviceInstance.getMetadata());
//    }

//    private void getServiceCenterRegistration(InstanceAddressURL instanceUrl) {
//        ServiceCenterConfiguration serviceCenterConfiguration = new ServiceCenterConfiguration();
//        serviceCenterConfiguration.setIgnoreSwaggerDifferent(false);
//        serviceCenterRegistration = new ServiceCenterRegistration(client, serviceCenterConfiguration, EVENT_BUS);
//        serviceCenterRegistration.setMicroservice(microservice);
//        serviceCenterRegistration.setMicroserviceInstance(microserviceInstance);
//        serviceCenterRegistration.setHeartBeatInterval(microserviceInstance.getHealthCheck().getInterval());
//        serviceCenterRegistration.setSchemaInfos(Collections.singletonList(createSchemaInfo(instanceUrl)));
//    }
//
//    private SchemaInfo createSchemaInfo(InstanceAddressURL url) {
//        return new SchemaInfo(url.getInstance().getServiceName(), url.getInstance().getAddress(),
//                getSchemaSummary(url.getInstance().getAddress()));
//    }
//
//    private String getSchemaSummary(String schemaContent) {
//        return DigestUtils.sha256Hex(schemaContent.getBytes(StandardCharsets.UTF_8));
//    }

//    private List<String> getEndpoints() {
//        return Collections.singletonList(
//                new URL(registryUrl.getProtocol(), registryUrl.getHost(), registryUrl.getPort()).toString());
//    }

    @Override
    public void doUpdate(ServiceInstance serviceInstance) {
//        ServiceInstance oldInstance = this.serviceInstance;
//        this.unregister(oldInstance);
//        this.register(serviceInstance);
    }

    @Override
    public void doUnregister() {
//        ThrowableAction.execute(() -> {
//        if (client != null) {
//            client.deleteMicroserviceInstance(microservice.getServiceId(), microserviceInstance.getInstanceId());
//        }
//        });
    }

    @Override
    public void doDestroy() {
//        if (!SHUTDOWN.compareAndSet(false, true)) {
//            return;
//        }
//        if (serviceCenterRegistration != null) {
//            serviceCenterRegistration.stop();
//        }
//        if (serviceCenterDiscovery != null) {
//            serviceCenterDiscovery.stop();
//        }
    }

    @Override
    public Set<String> getServices() {
//        return ThrowableFunction.execute(client, f -> {
//        MicroservicesResponse microserviceList = client.getMicroserviceList();
//        return microserviceList.getServices().stream().map(Microservice::getServiceName)
//                .collect(Collectors.toSet());
//        });
        return null;
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceName) {
//        return ThrowableFunction.execute(client, f -> {
//        MicroserviceInstancesResponse response = client.getMicroserviceInstanceList(microservice.getServiceId());
//        return response.getInstances().stream().map(instance -> {
//            URL url = URL.valueOf(instance.getEndpoints().get(0));
//            DefaultServiceInstance serviceInstance = new DefaultServiceInstance(instance.getServiceName(),
//                    url.getHost(), url.getPort(), ScopeModelUtil.getApplicationModel(registryUrl.getScopeModel()));
//            serviceInstance.setMetadata(instance.getProperties());
//            serviceInstance.setEnabled(instance.getStatus() == MicroserviceInstanceStatus.UP);
//            serviceInstance.setHealthy(instance.getStatus() == MicroserviceInstanceStatus.UP);
//            return serviceInstance;
//        }).collect(Collectors.toList());
//        });
        return null;
    }

//    @Override
//    public void addServiceInstancesChangedListener(ServiceInstancesChangedListener listener) {
//        this.listener = listener;
////        ThrowableAction.execute(() -> this.listener = listener);
//    }

    /**
     * 心跳事件
     *
     * @param event 心跳事件
     */
//    @Subscribe
//    public void onHeartBeatEvent(HeartBeatEvent event) {
//        if (event.isSuccess()) {
//            registrationInProgress = false;
//            processPendingEvent();
//        }
//    }

    /**
     * 注册事件
     *
     * @param event 注册事件
     */
//    @Subscribe
//    public void onMicroserviceRegistrationEvent(MicroserviceRegistrationEvent event) {
//        registrationInProgress = true;
//        if (event.isSuccess()) {
//            if (serviceCenterDiscovery == null) {
//                serviceCenterDiscovery = new ServiceCenterDiscovery(client, EVENT_BUS);
//                serviceCenterDiscovery.updateMyselfServiceId(microservice.getServiceId());
//                serviceCenterDiscovery.setPollInterval(15);
//                serviceCenterDiscovery.startDiscovery();
//            } else {
//                serviceCenterDiscovery.updateMyselfServiceId(microservice.getServiceId());
//            }
//        }
//    }

    /**
     * 注册事件
     *
     * @param event 注册事件
     */
//    @Subscribe
//    public void onMicroserviceInstanceRegistrationEvent(MicroserviceInstanceRegistrationEvent event) {
//        registrationInProgress = true;
//        if (event.isSuccess()) {
//            updateInterfaceMap();
//            FIRST_REGISTRATION_WAITER.countDown();
//        }
//    }

    /**
     * 实例变化事件
     *
     * @param event 实例变化事件
     */
//    @Subscribe
//    public void onInstanceChangedEvent(InstanceChangedEvent event) {
//        String serviceName = event.getServiceName();
//        List<ServiceInstance> serviceInstances = event.getInstances().stream().map(instance -> {
//            URL url = URL.valueOf(instance.getEndpoints().get(0));
//            DefaultServiceInstance serviceInstance = new DefaultServiceInstance(instance.getServiceName(),
//                    url.getHost(), url.getPort(), ScopeModelUtil.getApplicationModel(registryUrl.getScopeModel()));
//            serviceInstance.setMetadata(instance.getProperties());
//            serviceInstance.setEnabled(instance.getStatus() == MicroserviceInstanceStatus.UP);
//            serviceInstance.setHealthy(instance.getStatus() == MicroserviceInstanceStatus.UP);
//            return serviceInstance;
//        }).collect(Collectors.toList());
//        listener.onEvent(new ServiceInstancesChangedEvent(serviceName, serviceInstances));
//    }
}
