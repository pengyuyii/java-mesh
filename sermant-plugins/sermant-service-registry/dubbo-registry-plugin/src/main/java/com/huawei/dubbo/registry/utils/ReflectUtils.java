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

package com.huawei.dubbo.registry.utils;

import com.huawei.dubbo.registry.cache.DubboCache;
import com.huawei.dubbo.registry.constants.Constant;

import com.huaweicloud.sermant.core.classloader.ClassLoaderManager;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.utils.ClassLoaderUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 反射工具类，为了同时兼容alibaba和apache dubbo，所以需要用反射的方法进行类的操作
 *
 * @author provenceee
 * @since 2022-02-07
 */
public class ReflectUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();
    private static final String GET_PROTOCOL_METHOD_NAME = "getProtocol";
    private static final String GET_ADDRESS_METHOD_NAME = "getAddress";
    private static final String GET_PATH_METHOD_NAME = "getPath";
    private static final String GET_ID_METHOD_NAME = "getId";
    private static final String GET_NAME_METHOD_NAME = "getName";
    private static final String GET_PARAMETERS_METHOD_NAME = "getParameters";
    private static final String GET_REGISTRIES_METHOD_NAME = "getRegistries";
    private static final String GET_EXTENSION_CLASSES_METHOD_NAME = "getExtensionClasses";
    private static final String IS_VALID_METHOD_NAME = "isValid";
    private static final String SET_HOST_METHOD_NAME = "setHost";
    private static final String SET_ADDRESS_METHOD_NAME = "setAddress";
    private static final String SET_PATH_METHOD_NAME = "setPath";
    private static final String SET_ID_METHOD_NAME = "setId";
    private static final String SET_PREFIX_METHOD_NAME = "setPrefix";
    private static final String SET_PROTOCOL_METHOD_NAME = "setProtocol";
    private static final String NOTIFY_METHOD_NAME = "notify";
    private static final String VALUE_OF_METHOD_NAME = "valueOf";
    private static final String REMOVE_PARAMETERS_METHOD_NAME = "removeParameters";
    private static final String ADD_PARAMETERS_METHOD_NAME = "addParameters";
    private static final String GET_PARAMETER_METHOD_NAME = "getParameter";
    private static final String GET_HOST_METHOD_NAME = "getHost";
    private static final String GET_PORT_METHOD_NAME = "getPort";
    private static final String GET_SERVICE_INTERFACE_METHOD_NAME = "getServiceInterface";

    private ReflectUtils() {
    }

    /**
     * 加载宿主类
     *
     * @param className 宿主全限定类名
     * @return 宿主类
     */
    public static Optional<Class<?>> defineClass(String className) {
        ClassLoader contextClassLoader = ClassLoaderManager.getContextClassLoaderOrUserClassLoader();
        try {
            return Optional.of(ClassLoaderUtils.defineClass(className, contextClassLoader,
                ClassLoaderUtils.getClassResource(ReflectUtils.class.getClassLoader(), className)));
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | IOException e) {
            // 有可能已经加载过了，直接用contextClassLoader.loadClass加载
            try {
                return Optional.of(contextClassLoader.loadClass(className));
            } catch (ClassNotFoundException ex) {
                return Optional.empty();
            }
        }
    }

    /**
     * 新建注册配置
     *
     * @param clazz RegistryConfig(apache/alibaba)
     * @param <T> RegistryConfig(apache/alibaba)
     * @return 注册配置
     * @see com.alibaba.dubbo.config.RegistryConfig
     * @see org.apache.dubbo.config.RegistryConfig
     */
    public static <T> Optional<T> newRegistryConfig(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getConstructor(String.class);

            // 这个url不重要，重要的是protocol，所以设置成localhost:30100就行
            return Optional.of(constructor.newInstance(Constant.SC_REGISTRY_ADDRESS));
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
            | InvocationTargetException e) {
            LOGGER.log(Level.SEVERE, "Cannot new the registryConfig.", e);
            return Optional.empty();
        }
    }

    /**
     * 获取协议
     *
     * @param obj RegistryConfig | URL
     * @return 协议
     * @see com.alibaba.dubbo.config.RegistryConfig
     * @see org.apache.dubbo.config.RegistryConfig
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static String getProtocol(Object obj) {
        return invokeWithNoneParameterAndReturnString(obj, GET_PROTOCOL_METHOD_NAME);
    }

    /**
     * 获取应用地址
     *
     * @param obj URL
     * @return 应用地址
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static String getAddress(Object obj) {
        return invokeWithNoneParameterAndReturnString(obj, GET_ADDRESS_METHOD_NAME);
    }

    /**
     * 获取路径名，多数情况下与接口名相同，2.6.x, 2.7.0-2.7.7在多实现的场景下，会在接口名后拼一个序号
     *
     * @param obj URL
     * @return 接口
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static String getPath(Object obj) {
        return invokeWithNoneParameterAndReturnString(obj, GET_PATH_METHOD_NAME);
    }

    /**
     * 获取注册id
     *
     * @param obj RegistryConfig
     * @return 注册id
     * @see com.alibaba.dubbo.config.RegistryConfig
     * @see org.apache.dubbo.config.RegistryConfig
     */
    public static String getId(Object obj) {
        return invokeWithNoneParameterAndReturnString(obj, GET_ID_METHOD_NAME);
    }

    /**
     * 获取dubbo应用名
     *
     * @param obj ApplicationConfig
     * @return 应用名
     * @see com.alibaba.dubbo.config.ApplicationConfig
     * @see org.apache.dubbo.config.ApplicationConfig
     */
    public static String getName(Object obj) {
        return invokeWithNoneParameterAndReturnString(obj, GET_NAME_METHOD_NAME);
    }

    /**
     * 获取应用接口名
     *
     * @param obj URL
     * @return 应用接口名
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static String getServiceInterface(Object obj) {
        return invokeWithNoneParameterAndReturnString(obj, GET_SERVICE_INTERFACE_METHOD_NAME);
    }

    /**
     * 获取应用主机
     *
     * @param obj URL
     * @return 应用主机
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static String getHost(Object obj) {
        return invokeWithNoneParameterAndReturnString(obj, GET_HOST_METHOD_NAME);
    }

    /**
     * 获取应用端口
     *
     * @param obj URL
     * @return 应用端口
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static int getPort(Object obj) {
        return invokeWithNoneParameterAndReturnInteger(obj, GET_PORT_METHOD_NAME);
    }

    /**
     * 获取url参数
     *
     * @param obj ApplicationConfig
     * @param key key
     * @return 应用名
     * @see com.alibaba.dubbo.config.ApplicationConfig
     * @see org.apache.dubbo.config.ApplicationConfig
     */
    public static String getParameter(Object obj, String key) {
        return invokeWithStringParameter(obj, GET_PARAMETER_METHOD_NAME, key, String.class);
    }

    /**
     * 获取url参数
     *
     * @param obj URL
     * @return url参数
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static Map<String, String> getParameters(Object obj) {
        return invokeWithNoneParameter(obj, GET_PARAMETERS_METHOD_NAME, Map.class, true);
    }

    /**
     * 获取注册信息列表
     *
     * @param obj AbstractInterfaceConfig
     * @return 注册信息列表
     * @see com.alibaba.dubbo.config.AbstractInterfaceConfig
     * @see org.apache.dubbo.config.AbstractInterfaceConfig
     */
    public static List<Object> getRegistries(Object obj) {
        return invokeWithNoneParameter(obj, GET_REGISTRIES_METHOD_NAME, List.class, true);
    }

    /**
     * 获取dubbo spi缓存类
     *
     * @param obj ExtensionLoader
     * @return 缓存列表
     * @see com.alibaba.dubbo.common.extension.ExtensionLoader
     * @see org.apache.dubbo.common.extension.ExtensionLoader
     */
    public static Map<String, Class<?>> getExtensionClasses(Object obj) {
        return invokeWithNoneParameter(obj, GET_EXTENSION_CLASSES_METHOD_NAME, Map.class, false);
    }

    /**
     * 判断注册信息是否有效
     *
     * @param obj RegistryConfig
     * @return 是否有效
     * @see com.alibaba.dubbo.config.RegistryConfig
     * @see org.apache.dubbo.config.RegistryConfig
     */
    public static boolean isValid(Object obj) {
        Boolean isValid = invokeWithNoneParameter(obj, IS_VALID_METHOD_NAME, Boolean.class, true);
        if (isValid == null) {
            // 为null代表没有这个方法，返回true
            return true;
        }
        return isValid;
    }

    /**
     * 设置host
     *
     * @param obj URL
     * @param host host
     * @return URL
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static Object setHost(Object obj, String host) {
        return invokeWithStringParameter(obj, SET_HOST_METHOD_NAME, host);
    }

    /**
     * 设置地址
     *
     * @param obj URL
     * @param address 地址
     * @return URL
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static Object setAddress(Object obj, String address) {
        return invokeWithStringParameter(obj, SET_ADDRESS_METHOD_NAME, address);
    }

    /**
     * 设置路径
     *
     * @param obj URL
     * @param path 路径
     * @return URL
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static Object setPath(Object obj, String path) {
        return invokeWithStringParameter(obj, SET_PATH_METHOD_NAME, path);
    }

    /**
     * 设置id
     *
     * @param obj RegistryConfig
     * @param id id
     * @see com.alibaba.dubbo.config.RegistryConfig
     * @see org.apache.dubbo.config.RegistryConfig
     */
    public static void setId(Object obj, String id) {
        invokeWithStringParameter(obj, SET_ID_METHOD_NAME, id);
    }

    /**
     * 设置前缀
     *
     * @param obj RegistryConfig
     * @param prefix 前缀
     * @see com.alibaba.dubbo.config.RegistryConfig
     * @see org.apache.dubbo.config.RegistryConfig
     */
    public static void setPrefix(Object obj, String prefix) {
        invokeWithStringParameter(obj, SET_PREFIX_METHOD_NAME, prefix);
    }

    /**
     * 设置协议
     *
     * @param obj URL
     * @param protocol 协议
     * @return URL
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static Object setProtocol(Object obj, String protocol) {
        return invokeWithStringParameter(obj, SET_PROTOCOL_METHOD_NAME, protocol);
    }

    /**
     * 通知dubbo下游接口的URL
     *
     * @param notifyListener 通知监听器
     * @param urls 下游接口的URL
     * @see com.alibaba.dubbo.registry.NotifyListener
     * @see org.apache.dubbo.registry.NotifyListener
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static void notify(Object notifyListener, List<Object> urls) {
        invokeWithParameter(notifyListener, NOTIFY_METHOD_NAME, urls, List.class);
    }

    /**
     * 根据address新建URL
     *
     * @param address 地址
     * @return URL
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static Object valueOf(String address) {
        return invoke(new InvokeParameter(DubboCache.INSTANCE.getUrlClass(), null, VALUE_OF_METHOD_NAME, address,
            String.class)).orElse(null);
    }

    /**
     * 删除url中的参数
     *
     * @param url URL
     * @param keys 需要删除的参数的key
     * @return url
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static Object removeParameters(Object url, Collection<String> keys) {
        return invokeWithParameter(url, REMOVE_PARAMETERS_METHOD_NAME, keys, Collection.class);
    }

    /**
     * 增加url中的参数
     *
     * @param url URL
     * @param parameters 需要增加的参数
     * @return url
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static Object addParameters(Object url, Map<String, String> parameters) {
        return invokeWithParameter(url, ADD_PARAMETERS_METHOD_NAME, parameters, Map.class);
    }

    private static String invokeWithNoneParameterAndReturnString(Object obj, String name) {
        return invokeWithNoneParameter(obj, name, String.class, true);
    }

    private static int invokeWithNoneParameterAndReturnInteger(Object obj, String name) {
        return invokeWithNoneParameter(obj, name, Integer.class, true);
    }

    private static <T> T invokeWithNoneParameter(Object obj, String name, Class<T> returnClass, boolean isPublic) {
        InvokeParameter invokeParameter = new InvokeParameter(obj.getClass(), obj, name, null, null);
        invokeParameter.isPublic = isPublic;
        return returnClass.cast(invoke(invokeParameter).orElse(null));
    }

    private static <T> T invokeWithStringParameter(Object obj, String name, String parameter, Class<T> returnClass) {
        InvokeParameter invokeParameter = new InvokeParameter(obj.getClass(), obj, name, parameter, String.class);
        invokeParameter.isPublic = true;
        return returnClass.cast(invoke(invokeParameter).orElse(null));
    }

    private static Object invokeWithStringParameter(Object obj, String name, String parameter) {
        return invokeWithParameter(obj, name, parameter, String.class);
    }

    private static Object invokeWithParameter(Object obj, String name, Object parameter, Class<?> parameterClass) {
        return invoke(new InvokeParameter(obj.getClass(), obj, name, parameter, parameterClass)).orElse(null);
    }

    private static Optional<Object> invoke(InvokeParameter parameter) {
        try {
            Method method = getMethod(parameter.invokeClass, parameter.name, parameter.parameterClass,
                parameter.isPublic);
            if (parameter.parameter == null) {
                return Optional.ofNullable(method.invoke(parameter.obj));
            }
            return Optional.ofNullable(method.invoke(parameter.obj, parameter.parameter));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // 因版本的原因，有可能会找不到方法，所以可以忽略这些错误
            return Optional.empty();
        }
    }

    private static Method getMethod(Class<?> invokeClass, String name, Class<?> parameterClass, boolean isPublic)
        throws NoSuchMethodException {
        boolean hasParameter = parameterClass != null;
        if (hasParameter && isPublic) {
            // 有参公共方法
            return invokeClass.getMethod(name, parameterClass);
        }
        if (hasParameter) {
            // 有参非公共方法
            return setAccessible(invokeClass.getDeclaredMethod(name, parameterClass));
        }
        if (isPublic) {
            // 无参公共方法
            return invokeClass.getMethod(name);
        }

        // 无参非公共方法
        return setAccessible(invokeClass.getDeclaredMethod(name));
    }

    private static Method setAccessible(Method method) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            method.setAccessible(true);
            return method;
        });
        return method;
    }

    /**
     * 反射参数
     *
     * @since 2022-02-07
     */
    private static class InvokeParameter {
        Class<?> invokeClass;
        Object obj;
        String name;
        Object parameter;
        Class<?> parameterClass;
        boolean isPublic;

        InvokeParameter(Class<?> invokeClass, Object obj, String name, Object parameter, Class<?> parameterClass) {
            this.invokeClass = invokeClass;
            this.obj = obj;
            this.name = name;
            this.parameter = parameter;
            this.parameterClass = parameterClass;
            this.isPublic = true;
        }
    }
}