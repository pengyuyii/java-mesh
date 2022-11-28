/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.dubbo.registry.service;

import com.huawei.dubbo.registry.cache.DubboCache;
import com.huawei.dubbo.registry.constants.Constant;
import com.huawei.dubbo.registry.entity.GovernanceCache;
import com.huawei.dubbo.registry.entity.GovernanceData;
import com.huawei.dubbo.registry.entity.InterfaceData;
import com.huawei.dubbo.registry.entity.ProviderInfo;
import com.huawei.dubbo.registry.entity.Subscription;
import com.huawei.dubbo.registry.entity.SubscriptionKey;
import com.huawei.dubbo.registry.utils.CollectionUtils;
import com.huawei.dubbo.registry.utils.ReflectUtils;
import com.huawei.registry.config.ConfigConstants;
import com.huawei.registry.config.RegisterConfig;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.common.PluginConstant;
import com.huaweicloud.sermant.core.plugin.common.PluginSchemaValidator;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.utils.JarFileUtils;
import com.huaweicloud.sermant.core.utils.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.http.client.auth.DefaultRequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.DiscoveryEvents.InstanceChangedEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.HeartBeatEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceInstanceRegistrationEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceRegistrationEvent;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery;
import org.apache.servicecomb.service.center.client.ServiceCenterRegistration;
import org.apache.servicecomb.service.center.client.model.Framework;
import org.apache.servicecomb.service.center.client.model.HealthCheck;
import org.apache.servicecomb.service.center.client.model.HealthCheckMode;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;
import org.apache.servicecomb.service.center.client.model.SchemaInfo;
import org.apache.servicecomb.service.center.client.model.ServiceCenterConfiguration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 注册服务类，代码中使用反射调用类方法是为了同时兼容alibaba和apache dubbo
 *
 * @author provenceee
 * @since 2021-12-15
 */
public class RegistryServiceImpl implements RegistryService {
    private static final Logger LOGGER = LoggerFactory.getLogger();
    private static final EventBus EVENT_BUS = new EventBus();
    private static final Map<String, List<Microservice>> INTERFACE_MAP = new ConcurrentHashMap<>();
    private static final Map<SubscriptionKey, Set<Object>> SUBSCRIPTIONS = new ConcurrentHashMap<>();
    private static final Map<Object, List<Object>> NOTIFIED_URL_MAP = new ConcurrentHashMap<>();
    private static final CountDownLatch FIRST_REGISTRATION_WAITER = new CountDownLatch(1);
    private static final int REGISTRATION_WAITE_TIME = 30;
    private static final List<Subscription> PENDING_SUBSCRIBE_EVENT = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean SHUTDOWN = new AtomicBoolean();
    private static final String DEFAULT_TENANT_NAME = "default";
    private static final String CONSUMER_PROTOCOL_PREFIX = "consumer";
    private static final String GROUP_KEY = "group";
    private static final String VERSION_KEY = "version";
    private static final String ZONE_KEY = "zone";
    private static final String SERVICE_NAME_KEY = "service.name";
    private static final String INTERFACE_KEY = "interface";
    private static final String INTERFACE_DATA_KEY = "dubbo.interface.data";
    private static final String WILDCARD = "*";
    private static final String META_DATA_PREFIX = "service.meta.parameters.";
    private static final String META_DATA_VERSION_KEY = "service.meta.version";
    private static final String META_DATA_ZONE_KEY = "service.meta.zone";
    private static final List<String> IGNORE_REGISTRY_KEYS = Arrays.asList(GROUP_KEY, VERSION_KEY, SERVICE_NAME_KEY);
    private static final List<String> DEFAULT_INTERFACE_KEYS = Collections.singletonList("dubbo.tag");

    private final List<Object> registryUrls = new ArrayList<>();
    private List<String> ignoreKeys;
    private ServiceCenterClient client;
    private Microservice microservice;
    private MicroserviceInstance microserviceInstance;
    private ServiceCenterRegistration serviceCenterRegistration;
    private ServiceCenterDiscovery serviceCenterDiscovery;
    private boolean isRegistrationInProgress = true;
    private RegisterConfig config;
    private GovernanceService governanceService;
    private ServiceMeta serviceMeta;

    @Override
    public void startRegistration() {
        if (!DubboCache.INSTANCE.isLoadSc()) {
            // 没有加载sc的注册spi就直接return
            return;
        }
        LOGGER.info("Start registration...");
        config = PluginConfigManager.getPluginConfig(RegisterConfig.class);
        serviceMeta = ConfigManager.getConfig(ServiceMeta.class);
        governanceService = ServiceManager.getService(GovernanceService.class);
        client = new ServiceCenterClient(new AddressManager(config.getProject(), config.getAddressList(), EVENT_BUS),
            createSslProperties(), new DefaultRequestAuthHeaderProvider(), DEFAULT_TENANT_NAME, Collections.emptyMap());
        ignoreKeys = new ArrayList<>();
        ignoreKeys.addAll(IGNORE_REGISTRY_KEYS);
        ignoreKeys.addAll(DEFAULT_INTERFACE_KEYS);
        Optional.ofNullable(config.getInterfaceKeys()).ifPresent(ignoreKeys::addAll);
        createMicroservice();
        createMicroserviceInstance();
        createServiceCenterRegistration();
        EVENT_BUS.register(this);
        serviceCenterRegistration.startRegistration();
        waitRegistrationDone();
    }

    /**
     * 订阅接口
     *
     * @param url 订阅地址
     * @param notifyListener 实例通知监听器
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     * @see com.alibaba.dubbo.registry.NotifyListener
     * @see org.apache.dubbo.registry.NotifyListener
     */
    @Override
    public void doSubscribe(Object url, Object notifyListener) {
        LOGGER.info("Subscribe url is " + url.toString());
        if (!CONSUMER_PROTOCOL_PREFIX.equals(ReflectUtils.getProtocol(url))) {
            return;
        }
        Subscription subscription = new Subscription(url, notifyListener);
        if (isRegistrationInProgress) {
            PENDING_SUBSCRIBE_EVENT.add(subscription);
            return;
        }
        subscribe(subscription);
    }

    @Override
    public void shutdown() {
        if (!SHUTDOWN.compareAndSet(false, true)) {
            return;
        }
        if (serviceCenterRegistration != null) {
            serviceCenterRegistration.stop();
        }
        if (serviceCenterDiscovery != null) {
            serviceCenterDiscovery.stop();
        }
        if (client != null) {
            client.deleteMicroserviceInstance(microservice.getServiceId(), microserviceInstance.getInstanceId());
        }
    }

    /**
     * 增加注册接口
     *
     * @param url 注册url
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    @Override
    public void addRegistryUrls(Object url) {
        LOGGER.info("Registry url is " + url.toString());
        if (!CONSUMER_PROTOCOL_PREFIX.equals(ReflectUtils.getProtocol(url))) {
            registryUrls.add(url);
        }
    }

    /**
     * 通知治理url
     */
    @Override
    public void notifyGovernanceUrl() {
        SUBSCRIPTIONS.values().forEach(notifyListeners -> notifyListeners.forEach(notifyListener -> {
                List<Object> urls = NOTIFIED_URL_MAP.get(notifyListener);
                if (CollectionUtils.isEmpty(urls)) {
                    return;
                }
                List<Object> governanceUrls = wrapGovernanceData(urls);
                if (governanceUrls == urls || (governanceUrls.containsAll(urls) && urls.containsAll(governanceUrls))) {
                    // 修改前后urls一致，或者修改前后的实例url一致，则不用通知
                    return;
                }
                ReflectUtils.notify(notifyListener, governanceUrls);
                NOTIFIED_URL_MAP.put(notifyListener, governanceUrls);
            }
        ));
    }

    /**
     * 心跳事件
     *
     * @param event 心跳事件
     */
    @Subscribe
    public void onHeartBeatEvent(HeartBeatEvent event) {
        if (event.isSuccess()) {
            isRegistrationInProgress = false;
            processPendingEvent();
        }
    }

    /**
     * 注册事件
     *
     * @param event 注册事件
     */
    @Subscribe
    public void onMicroserviceRegistrationEvent(MicroserviceRegistrationEvent event) {
        isRegistrationInProgress = true;
        if (event.isSuccess()) {
            initServiceCenterDiscovery();
        }
    }

    /**
     * 注册事件
     *
     * @param event 注册事件
     */
    @Subscribe
    public void onMicroserviceInstanceRegistrationEvent(MicroserviceInstanceRegistrationEvent event) {
        isRegistrationInProgress = true;
        if (event.isSuccess()) {
            initServiceCenterDiscovery();
            updateInterfaceMap();
            FIRST_REGISTRATION_WAITER.countDown();
        }
    }

    /**
     * 实例变化事件
     *
     * @param event 实例变化事件
     */
    @Subscribe
    public void onInstanceChangedEvent(InstanceChangedEvent event) {
        notify(event.getAppName(), event.getServiceName(), event.getInstances());
    }

    private void initServiceCenterDiscovery() {
        if (serviceCenterDiscovery == null) {
            serviceCenterDiscovery = new ServiceCenterDiscovery(client, EVENT_BUS);
            serviceCenterDiscovery.updateMyselfServiceId(microservice.getServiceId());
            serviceCenterDiscovery.setPollInterval(config.getPullInterval());
            serviceCenterDiscovery.startDiscovery();
        } else {
            serviceCenterDiscovery.updateMyselfServiceId(microservice.getServiceId());
        }
    }

    private SSLProperties createSslProperties() {
        SSLProperties sslProperties = new SSLProperties();
        if (config.isSslEnabled()) {
            sslProperties.setEnabled(true);
            sslProperties.setSslOption(SSLOption.DEFAULT_OPTION);
            sslProperties.setSslCustom(SSLCustom.defaultSSLCustom());
        }
        return sslProperties;
    }

    private void createMicroservice() {
        microservice = new Microservice(DubboCache.INSTANCE.getServiceName());
        microservice.setAppId(config.getApplication());
        microservice.setVersion(config.getVersion());
        microservice.setEnvironment(config.getEnvironment());
        microservice.setProperties(config.getParametersMap());
        Framework framework = new Framework();
        framework.setName(ConfigConstants.COMMON_FRAMEWORK);
        framework.setVersion(getVersion());
        microservice.setFramework(framework);
        microservice.setSchemas(getSchemas());
    }

    private String getVersion() {
        try (JarFile jarFile = new JarFile(getClass().getProtectionDomain().getCodeSource().getLocation().getPath())) {
            String pluginName = (String) JarFileUtils.getManifestAttr(jarFile, PluginConstant.PLUGIN_NAME_KEY);
            return PluginSchemaValidator.getPluginVersionMap().get(pluginName);
        } catch (IOException e) {
            LOGGER.warning("Cannot not get the version.");
            return "";
        }
    }

    private List<String> getSchemas() {
        return registryUrls.stream().map(this::getInterface).filter(StringUtils::isExist).distinct()
            .collect(Collectors.toList());
    }

    private String getInterface(Object url) {
        return ReflectUtils.getParameters(url).get(INTERFACE_KEY);
    }

    private void createMicroserviceInstance() {
        microserviceInstance = new MicroserviceInstance();
        microserviceInstance.setStatus(MicroserviceInstanceStatus.UP);
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setMode(HealthCheckMode.push);
        healthCheck.setInterval(config.getHeartbeatInterval());
        healthCheck.setTimes(config.getHeartbeatRetryTimes());
        microserviceInstance.setHealthCheck(healthCheck);
        microserviceInstance.setHostName(getHost());
        microserviceInstance.setEndpoints(getEndpoints());

        // 存入每一个接口提供的实现的group version等信息
        microserviceInstance.getProperties().putAll(getProperties());
    }

    private String getHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOGGER.warning("Cannot get the host.");
            return "";
        }
    }

    private List<String> getEndpoints() {
        return registryUrls.stream().map(this::getUrl).filter(StringUtils::isExist).distinct()
            .collect(Collectors.toList());
    }

    private Map<String, String> getProperties() {
        // 接口的协议缓存（适配多协议接口）
        Map<String, Set<String>> protocolCache = new ConcurrentHashMap<>();

        // 把registryUrls按接口名进行分组，组装成InterfaceData，并聚合到HashSet（去重）里面
        Map<String, Set<InterfaceData>> map = registryUrls.stream().collect(Collectors.groupingBy(this::getInterface,
            Collectors.mapping(url -> getInterfaceData(url, protocolCache), Collectors.toCollection(HashSet::new))));
        Map<String, String> properties = new HashMap<>();
        properties.put(INTERFACE_DATA_KEY, JSONObject.toJSONString(map));

        // 存入实例参数
        if (!CollectionUtils.isEmpty(serviceMeta.getParameters())) {
            // 由于http header的key不区分大小写，所以使用metadata做路由时，统一把key转为小写
            // 由于dubbo注册时，会把'-'替换成'.'，所以保持一致
            serviceMeta.getParameters()
                .forEach((key, value) -> properties.put(key.replace("-", ".").toLowerCase(Locale.ROOT), value));
        }
        properties.put(VERSION_KEY, config.getVersion());
        properties.put(ZONE_KEY, serviceMeta.getZone());
        return properties;
    }

    private InterfaceData getInterfaceData(Object url, Map<String, Set<String>> protocolCache) {
        Map<String, String> parameters = ReflectUtils.getParameters(url);
        Integer order = null;
        String path = ReflectUtils.getPath(url);
        String interfaceName = parameters.get(INTERFACE_KEY);
        if (!path.equals(interfaceName) && path.length() > interfaceName.length()) {
            // 2.6.x, 2.7.0-2.7.7在多实现的场景下，路径名会在接口名后拼一个序号，取出这个序号并保存
            order = Integer.valueOf(path.substring(interfaceName.length()));
        }
        List<String> keys = new ArrayList<>(DEFAULT_INTERFACE_KEYS);
        if (!CollectionUtils.isEmpty(config.getInterfaceKeys())) {
            keys.addAll(config.getInterfaceKeys());
        }
        Map<String, String> map = new HashMap<>();
        keys.forEach(key -> {
            if (parameters.containsKey(key)) {
                map.put(key, parameters.get(key));
            }
        });
        protocolCache.computeIfAbsent(interfaceName, value -> new HashSet<>()).add(ReflectUtils.getProtocol(url));
        InterfaceData interfaceData = new InterfaceData(parameters.get(GROUP_KEY), parameters.get(VERSION_KEY),
            parameters.get(SERVICE_NAME_KEY), order, map);
        interfaceData.setProtocol(protocolCache.get(interfaceName));
        return interfaceData;
    }

    private String getUrl(Object url) {
        String protocol = ReflectUtils.getProtocol(url);
        if (StringUtils.isBlank(protocol)) {
            return "";
        }
        String address = ReflectUtils.getAddress(url);
        if (StringUtils.isBlank(address)) {
            return "";
        }
        Object endpoint = ReflectUtils.valueOf(protocol + Constant.PROTOCOL_SEPARATION + address);
        return endpoint == null ? "" : endpoint.toString();
    }

    private void createServiceCenterRegistration() {
        ServiceCenterConfiguration serviceCenterConfiguration = new ServiceCenterConfiguration();
        serviceCenterConfiguration.setIgnoreSwaggerDifferent(config.isIgnoreSwaggerDifferent());
        serviceCenterRegistration = new ServiceCenterRegistration(client, serviceCenterConfiguration, EVENT_BUS);
        serviceCenterRegistration.setMicroservice(microservice);
        serviceCenterRegistration.setMicroserviceInstance(microserviceInstance);
        serviceCenterRegistration.setHeartBeatInterval(microserviceInstance.getHealthCheck().getInterval());
        serviceCenterRegistration.setSchemaInfos(getSchemaInfos());
    }

    private List<SchemaInfo> getSchemaInfos() {
        return registryUrls.stream().map(url -> createSchemaInfo(url).orElse(null)).filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Optional<SchemaInfo> createSchemaInfo(Object url) {
        Object newUrl = ReflectUtils.setHost(url, microservice.getServiceName());
        if (newUrl == null) {
            return Optional.empty();
        }
        String interfaceName = getInterface(newUrl);
        Map<String, String> parameters = ReflectUtils.getParameters(url);
        parameters.keySet().forEach(key -> {
            // 实例属性，会存到properties中，所以不需要在接口级参数中储存
            if (key.startsWith(META_DATA_PREFIX) || META_DATA_VERSION_KEY.equals(key) || META_DATA_ZONE_KEY
                .equals(key)) {
                ignoreKeys.add(key);
            }
        });

        // schema是以接口名为维度的，ignoreKeys中的参数主要跟实现相关，所以这里去掉
        // ignoreKeys中的参数会存在实例的properties中
        // 2.6.x, 2.7.0-2.7.7在多实现的场景下，路径名会在接口名后拼一个序号，所以这里把路径名统一设置为接口名
        String newUrlString = ReflectUtils.setPath(ReflectUtils.removeParameters(newUrl, ignoreKeys), interfaceName)
            .toString();
        String schema = newUrlString.substring(newUrlString.indexOf(microservice.getServiceName()));
        return Optional.of(new SchemaInfo(interfaceName, schema, DigestUtils.sha256Hex(schema)));
    }

    private void subscribe(Subscription subscription) {
        Object url = subscription.getUrl();
        String interfaceName = getInterface(url);
        List<Microservice> serviceList = INTERFACE_MAP.get(interfaceName);
        if (CollectionUtils.isEmpty(serviceList)) {
            updateInterfaceMap();
            serviceList = INTERFACE_MAP.get(interfaceName);
        }
        if (CollectionUtils.isEmpty(serviceList)) {
            LOGGER.warning(String.format(Locale.ROOT, "the subscribe url [%s] is not registered.", interfaceName));
            PENDING_SUBSCRIBE_EVENT.add(subscription);
            return;
        }

        // 相同interfaceName的serviceList的appId和serviceName相同，所以可以取第一个
        String appId = serviceList.get(0).getAppId();
        String serviceName = serviceList.get(0).getServiceName();
        Object notifyListener = subscription.getNotifyListener();
        if (notifyListener != null) {
            SUBSCRIPTIONS.computeIfAbsent(getSubscriptionKey(appId, serviceName, url), value -> new HashSet<>())
                .add(notifyListener);
        }
        List<MicroserviceInstance> instances = new ArrayList<>();
        serviceList.forEach(service -> {
            MicroserviceInstancesResponse response = client.getMicroserviceInstanceList(service.getServiceId());
            List<MicroserviceInstance> list = response.getInstances();
            if (!CollectionUtils.isEmpty(list)) {
                instances.addAll(list);
            }
        });
        notify(appId, serviceName, instances);
        serviceCenterDiscovery.registerIfNotPresent(new ServiceCenterDiscovery.SubscriptionKey(appId, serviceName));
    }

    private void waitRegistrationDone() {
        try {
            FIRST_REGISTRATION_WAITER.await(REGISTRATION_WAITE_TIME, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "registration is not finished in 30 seconds.");
        }
    }

    private void updateInterfaceMap() {
        INTERFACE_MAP.clear();
        MicroservicesResponse microservicesResponse = client.getMicroserviceList();
        microservicesResponse.getServices().forEach(this::updateInterfaceMap);
    }

    private void updateInterfaceMap(Microservice service) {
        if (microservice.getAppId().equals(service.getAppId())) {
            service.getSchemas()
                .forEach(schema -> INTERFACE_MAP.computeIfAbsent(schema, value -> new ArrayList<>()).add(service));
        }
    }

    private void processPendingEvent() {
        List<Subscription> events = new ArrayList<>(PENDING_SUBSCRIBE_EVENT);
        PENDING_SUBSCRIBE_EVENT.clear();
        events.forEach(this::subscribe);
    }

    private void notify(String appId, String serviceName, List<MicroserviceInstance> instances) {
        if (!CollectionUtils.isEmpty(instances)) {
            Map<SubscriptionKey, List<Object>> notifyUrls = instancesToUrls(appId, serviceName, instances);
            SUBSCRIPTIONS.forEach((subscriptionKey, notifyListeners) -> {
                List<Object> urls = notifyUrls.get(subscriptionKey);
                if (CollectionUtils.isEmpty(urls)) {
                    return;
                }
                notifyListeners.forEach(notifyListener -> {
                    List<Object> governanceUrls = wrapGovernanceData(urls);
                    ReflectUtils.notify(notifyListener, governanceUrls);
                    NOTIFIED_URL_MAP.put(notifyListener, governanceUrls);
                });
            });
        }
        governanceService.doStart();
    }

    private List<Object> wrapGovernanceData(List<Object> urls) {
        GovernanceData governanceData = GovernanceCache.INSTANCE.getGovernanceData();
        if (governanceData == null || CollectionUtils.isEmpty(governanceData.getProviderInfos())) {
            return urls;
        }
        Map<com.huawei.dubbo.registry.entity.SchemaInfo, Map<String, String>> map = new HashMap<>();
        List<ProviderInfo> providerInfos = governanceData.getProviderInfos();
        providerInfos.forEach(providerInfo -> {
            List<com.huawei.dubbo.registry.entity.SchemaInfo> schemaInfos = providerInfo.getSchemaInfos();
            if (CollectionUtils.isEmpty(schemaInfos)) {
                return;
            }
            schemaInfos.forEach(schemaInfo -> map.put(schemaInfo, schemaInfo.getParameters()));
        });
        return urls.stream().map(url -> {
            Map<String, String> parameters = map.get(getSchemaInfo(url));
            if (CollectionUtils.isEmpty(parameters)) {
                return url;
            }
            Map<String, String> whiteListMap = new HashMap<>();
            Map<String, String> urlParameters = ReflectUtils.getParameters(url);

            // 参数比较敏感，所以这里使用白名单管理
            config.getGovernanceParametersWhiteList().forEach(key -> {
                String value = parameters.get(key);
                if (parameters.containsKey(key) && !Objects.equals(value, urlParameters.get(key))) {
                    whiteListMap.put(key, value);
                }
            });
            if (!CollectionUtils.isEmpty(whiteListMap)) {
                url = ReflectUtils.addParameters(url, whiteListMap);
            }
            return url;
        }).collect(Collectors.toList());
    }

    private com.huawei.dubbo.registry.entity.SchemaInfo getSchemaInfo(Object url) {
        Map<String, String> parameters = ReflectUtils.getParameters(url);
        return new com.huawei.dubbo.registry.entity.SchemaInfo(parameters.get(INTERFACE_KEY), parameters.get(GROUP_KEY),
            parameters.get(VERSION_KEY));
    }

    private Map<SubscriptionKey, List<Object>> instancesToUrls(String appId, String serviceName,
        List<MicroserviceInstance> instances) {
        Map<SubscriptionKey, List<Object>> urlMap = new HashMap<>();

        // instances中的serviceId都是一样的，所以这里可以取第一个
        List<SchemaInfo> schemaInfos = client.getServiceSchemasList(instances.get(0).getServiceId(), true);
        instances.forEach(instance -> convertToUrlMap(urlMap, appId, serviceName, instance, schemaInfos));

        // 拼接*（通配符）的场景
        urlMap.putAll(getWildcardUrlMap(urlMap));
        return urlMap;
    }

    private Map<SubscriptionKey, List<Object>> getWildcardUrlMap(Map<SubscriptionKey, List<Object>> urlMap) {
        Map<SubscriptionKey, List<Object>> map = new HashMap<>();
        urlMap.forEach((key, urls) -> {
            map.computeIfAbsent(new SubscriptionKey(key.getAppId(), key.getServiceName(), key.getInterfaceName(),
                WILDCARD, key.getVersion()), value -> new ArrayList<>()).addAll(urls);
            map.computeIfAbsent(new SubscriptionKey(key.getAppId(), key.getServiceName(), key.getInterfaceName(),
                key.getGroup(), WILDCARD), value -> new ArrayList<>()).addAll(urls);
            map.computeIfAbsent(new SubscriptionKey(key.getAppId(), key.getServiceName(), key.getInterfaceName(),
                WILDCARD, WILDCARD), value -> new ArrayList<>()).addAll(urls);
        });
        return map;
    }

    private void convertToUrlMap(Map<SubscriptionKey, List<Object>> urlMap, String appId, String serviceName,
        MicroserviceInstance instance, List<SchemaInfo> schemaInfos) {
        Map<String, String> properties = instance.getProperties();
        String data = properties.get(INTERFACE_DATA_KEY);
        Map<String, String> metaData = getMetaData(properties);
        Map<String, List<InterfaceData>> dataMap = JSONObject.parseObject(data, new JsonObjectTypeReference());
        instance.getEndpoints().forEach(endpoint -> {
            Object url = ReflectUtils.valueOf(endpoint);
            if (CollectionUtils.isEmpty(schemaInfos)) {
                urlMap.computeIfAbsent(new SubscriptionKey(appId, serviceName, getInterface(url)),
                    value -> new ArrayList<>()).add(url);
                return;
            }
            String protocol = ReflectUtils.getProtocol(url);
            schemaInfos.forEach(schema -> {
                Object newUrl = ReflectUtils.valueOf(schema.getSchema());

                // 获取对应接口的所有实现的信息，并组装成InterfaceKey
                List<InterfaceData> list = getInterfaceDataList(dataMap, properties, schema.getSchemaId(), metaData);
                if (CollectionUtils.isEmpty(list)) {
                    return;
                }

                // 遍历所有的接口实现
                list.forEach(interfaceData -> {
                    if (!CollectionUtils.isEmpty(interfaceData.getProtocol())
                        && !interfaceData.getProtocol().contains(protocol)) {
                        return;
                    }
                    Map<String, String> parameters = getParameters(interfaceData);

                    // 组装所有接口实现的访问地址列表
                    urlMap.computeIfAbsent(getSubscriptionKey(appId, serviceName, newUrl, interfaceData),
                        value -> new ArrayList<>())
                        .add(getUrlOnNotifying(newUrl, url, parameters, interfaceData.getOrder(), protocol));
                });
            });
        });
    }

    private SubscriptionKey getSubscriptionKey(String appId, String serviceName, Object url) {
        Map<String, String> parameters = ReflectUtils.getParameters(url);
        return new SubscriptionKey(appId, serviceName, parameters.get(INTERFACE_KEY), parameters.get(GROUP_KEY),
            parameters.get(VERSION_KEY));
    }

    private SubscriptionKey getSubscriptionKey(String appId, String serviceName, Object url,
        InterfaceData interfaceData) {
        return new SubscriptionKey(appId, serviceName, getInterface(url), interfaceData.getGroup(),
            interfaceData.getVersion());
    }

    private Object getUrlOnNotifying(Object schemaUrl, Object addressUrl, Map<String, String> parameters,
        Integer order, String protocol) {
        Object url = ReflectUtils.setProtocol(ReflectUtils.setAddress(ReflectUtils.addParameters(schemaUrl, parameters),
            ReflectUtils.getAddress(addressUrl)), protocol);
        if (order == null) {
            return url;
        }

        // 2.6.x, 2.7.0 - 2.7.7在多实现的场景下，路径名为接口拼一个序号
        return ReflectUtils.setPath(url, ReflectUtils.getPath(schemaUrl) + order);
    }

    private Map<String, String> getMetaData(Map<String, String> properties) {
        // 没有接口数据，说明是旧版本，跳过
        if (StringUtils.isBlank(properties.get(INTERFACE_DATA_KEY))) {
            return Collections.emptyMap();
        }
        Map<String, String> metaData = new HashMap<>();
        for (Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();

            // 接口数据不是元数据，跳过
            if (INTERFACE_DATA_KEY.equals(key)) {
                continue;
            }
            if (VERSION_KEY.equals(key)) {
                metaData.put(META_DATA_VERSION_KEY, entry.getValue());
                continue;
            }
            if (ZONE_KEY.equals(key)) {
                metaData.put(META_DATA_ZONE_KEY, entry.getValue());
                continue;
            }
            metaData.put(META_DATA_PREFIX + key, entry.getValue());
        }
        return metaData;
    }

    private List<InterfaceData> getInterfaceDataList(Map<String, List<InterfaceData>> dataMap,
        Map<String, String> properties, String schemaId, Map<String, String> metaData) {
        if (CollectionUtils.isEmpty(dataMap)) {
            // 旧版本获取方式
            return JSONArray.parseArray(properties.get(schemaId), InterfaceData.class);
        }

        // 新版本获取方式
        List<InterfaceData> list = dataMap.get(schemaId);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        list.forEach(interfaceData -> {
            Map<String, String> parameters = interfaceData.getParameters();
            if (parameters == null) {
                parameters = new HashMap<>();
                interfaceData.setParameters(parameters);
            }
            parameters.putAll(metaData);
        });
        return list;
    }

    private Map<String, String> getParameters(InterfaceData interfaceData) {
        Map<String, String> parameters = new HashMap<>();
        String group = interfaceData.getGroup();
        if (StringUtils.isExist(group)) {
            parameters.put(GROUP_KEY, group);
        }
        String version = interfaceData.getVersion();
        if (StringUtils.isExist(version)) {
            parameters.put(VERSION_KEY, version);
        }
        String dubboServiceName = interfaceData.getServiceName();
        if (StringUtils.isExist(dubboServiceName)) {
            parameters.put(SERVICE_NAME_KEY, dubboServiceName);
        }
        Map<String, String> interfaceParameters = interfaceData.getParameters();
        if (!CollectionUtils.isEmpty(interfaceParameters)) {
            parameters.putAll(interfaceParameters);
        }
        return parameters;
    }

    /**
     * JSONObject序列化类
     *
     * @since 2022-02-18
     */
    private static class JsonObjectTypeReference extends TypeReference<Map<String, List<InterfaceData>>> {
    }
}
