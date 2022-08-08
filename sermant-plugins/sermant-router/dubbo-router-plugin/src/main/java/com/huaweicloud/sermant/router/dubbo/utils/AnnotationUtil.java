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

package com.huaweicloud.sermant.router.dubbo.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 测试
 *
 * @author provenceee
 * @since 2022-08-04
 */
public class AnnotationUtil {
    private static final Constructor<?> ANNOTATION_INVOCATION_HANDLER_CONSTRUCTOR;
    private static final Constructor<?> ANNOTATION_DATA_CONSTRUCTOR;
    private static final Method CLASS_ANNOTATION_DATA;
    private static final Field CLASS_CLASS_REDEFINED_COUNT;
    private static final Field ANNOTATION_DATA_ANNOTATIONS;
    private static final Field ANNOTATION_DATA_DECLARED_ANOTATIONS;
    private static final Method ATOMIC_CAS_ANNOTATION_DATA;
    private static final Class<?> ATOMIC_CLASS;
    private static final Field FIELD_EXCUTABLE_DECLARED_ANNOTATIONS;
    private static final Field FIELD_FIELD_DECLARED_ANNOTATIONS;

    static {
        // static initialization of necessary reflection Objects
        try {
            Class<?> annotationInvocationHandlerClass = Class
                .forName("sun.reflect.annotation.AnnotationInvocationHandler");
            ANNOTATION_INVOCATION_HANDLER_CONSTRUCTOR = annotationInvocationHandlerClass.getDeclaredConstructor(
                new Class[]{Class.class, Map.class});
            ANNOTATION_INVOCATION_HANDLER_CONSTRUCTOR.setAccessible(true);

            ATOMIC_CLASS = Class.forName("java.lang.Class$Atomic");
            Class<?> annotationDataClass = Class.forName("java.lang.Class$AnnotationData");

            ANNOTATION_DATA_CONSTRUCTOR = annotationDataClass.getDeclaredConstructor(
                new Class[]{Map.class, Map.class, int.class});
            ANNOTATION_DATA_CONSTRUCTOR.setAccessible(true);
            CLASS_ANNOTATION_DATA = Class.class.getDeclaredMethod("annotationData");
            CLASS_ANNOTATION_DATA.setAccessible(true);

            CLASS_CLASS_REDEFINED_COUNT = Class.class.getDeclaredField("classRedefinedCount");
            CLASS_CLASS_REDEFINED_COUNT.setAccessible(true);

            ANNOTATION_DATA_ANNOTATIONS = annotationDataClass.getDeclaredField("annotations");
            ANNOTATION_DATA_ANNOTATIONS.setAccessible(true);
            ANNOTATION_DATA_DECLARED_ANOTATIONS = annotationDataClass.getDeclaredField("declaredAnnotations");
            ANNOTATION_DATA_DECLARED_ANOTATIONS.setAccessible(true);

            ATOMIC_CAS_ANNOTATION_DATA = ATOMIC_CLASS.getDeclaredMethod("casAnnotationData",
                Class.class, annotationDataClass, annotationDataClass);
            ATOMIC_CAS_ANNOTATION_DATA.setAccessible(true);

            FIELD_EXCUTABLE_DECLARED_ANNOTATIONS = Executable.class.getDeclaredField("declaredAnnotations");
            FIELD_EXCUTABLE_DECLARED_ANNOTATIONS.setAccessible(true);

            FIELD_FIELD_DECLARED_ANNOTATIONS = Field.class.getDeclaredField("declaredAnnotations");
            FIELD_FIELD_DECLARED_ANNOTATIONS.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | NoSuchFieldException e) {
            throw new IllegalStateException("AnnotationUtil init fail, check your java version.", e);
        }
    }

    /**
     * Add annotation to Executable(Method or Constructor)<br> Note that you may need to give the root method.
     *
     * @param ex
     * @param annotation
     * @author XDean
     * @see Executable
     * @see #createAnnotationFromMap(Class, Map)
     * @see ReflectUtil#getRootMethods(Class)
     */
    @SuppressWarnings("unchecked")
    public static void addAnnotation(Executable ex, Annotation annotation) {
        ex.getAnnotation(Annotation.class);// prevent declaredAnnotations haven't initialized
        Map<Class<? extends Annotation>, Annotation> annos;
        try {
            annos = (Map<Class<? extends Annotation>, Annotation>) FIELD_EXCUTABLE_DECLARED_ANNOTATIONS.get(ex);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        if (annos.getClass() == Collections.EMPTY_MAP.getClass()) {
            annos = new HashMap<>();
            try {
                FIELD_EXCUTABLE_DECLARED_ANNOTATIONS.set(ex, annos);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        annos.put(annotation.annotationType(), annotation);
    }

    /**
     * Add annotation to Field<br> Note that you may need to give the root field.
     *
     * @param field
     * @param annotation
     * @author XDean
     * @see Field
     * @see #createAnnotationFromMap(Class, Map)
     * @see ReflectUtil#getRootFields(Class)
     */
    @SuppressWarnings("unchecked")
    public static void addAnnotation(Field field, Annotation annotation) {
        field.getAnnotation(Annotation.class);// prevent declaredAnnotations haven't initialized
        Map<Class<? extends Annotation>, Annotation> annos;
        try {
            annos = (Map<Class<? extends Annotation>, Annotation>) FIELD_FIELD_DECLARED_ANNOTATIONS.get(field);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        if (annos.getClass() == Collections.EMPTY_MAP.getClass()) {
            annos = new HashMap<>();
            try {
                FIELD_FIELD_DECLARED_ANNOTATIONS.set(field, annos);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        annos.put(annotation.annotationType(), annotation);
    }

    /**
     * @param c
     * @param annotation
     * @author Balder@stackoverflow
     * @see <a href="https://stackoverflow.com/a/30287201/7803527">Origin code on stackoverflow</a>
     * @see Class
     * @see #createAnnotationFromMap(Class, Map)
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> void addAnnotation(Class<?> c, T annotation) {
        try {
            while (true) { // retry loop
                int classRedefinedCount = CLASS_CLASS_REDEFINED_COUNT.getInt(c);
                Object /* AnnotationData */ annotationData = CLASS_ANNOTATION_DATA.invoke(c);
                // null or stale annotationData -> optimistically create new instance
                Object newAnnotationData = changeClassAnnotationData(c, annotationData,
                    (Class<T>) annotation.annotationType(), annotation, classRedefinedCount);
                // try to install it
                if ((boolean) ATOMIC_CAS_ANNOTATION_DATA.invoke(ATOMIC_CLASS, c, annotationData, newAnnotationData)) {
                    // successfully installed new AnnotationData
                    break;
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> Object changeClassAnnotationData(Class<?> c, Object annotationData,
        Class<T> annotationType, T annotation, int classRedefinedCount)
        throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Map<Class<? extends Annotation>, Annotation> annotations = (Map<Class<? extends Annotation>, Annotation>) ANNOTATION_DATA_ANNOTATIONS
            .get(annotationData);
        Map<Class<? extends Annotation>, Annotation> declaredAnnotations = (Map<Class<? extends Annotation>, Annotation>) ANNOTATION_DATA_DECLARED_ANOTATIONS
            .get(annotationData);

        Map<Class<? extends Annotation>, Annotation> newDeclaredAnnotations = new LinkedHashMap<>(annotations);
        newDeclaredAnnotations.put(annotationType, annotation);
        Map<Class<? extends Annotation>, Annotation> newAnnotations;
        if (declaredAnnotations == annotations) {
            newAnnotations = newDeclaredAnnotations;
        } else {
            newAnnotations = new LinkedHashMap<>(annotations);
            newAnnotations.put(annotationType, annotation);
        }
        return ANNOTATION_DATA_CONSTRUCTOR.newInstance(newAnnotations, newDeclaredAnnotations, classRedefinedCount);
    }

    /**
     * Create annotation from the given map.
     *
     * @param annotationClass
     * @param valuesMap
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T createAnnotationFromMap(Class<T> annotationClass,
        Map<String, Object> valuesMap) {
        Map<String, Object> map = getAnnotationDefaultMap(annotationClass);
        map.putAll(valuesMap);
        return AccessController
            .doPrivileged((PrivilegedAction<T>) () -> {
                try {
                    return (T) Proxy.newProxyInstance(
                        annotationClass.getClassLoader(),
                        new Class[]{annotationClass},
                        (InvocationHandler) ANNOTATION_INVOCATION_HANDLER_CONSTRUCTOR
                            .newInstance(annotationClass, map));
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                return null;
            });
    }

    public static <T extends Annotation> Map<String, Object> getAnnotationDefaultMap(Class<T> annotationClass) {
        return Stream.of(annotationClass.getDeclaredMethods())
            .filter(m -> m.getDefaultValue() != null)
            .collect(Collectors.toMap(m -> m.getName(), m -> m.getDefaultValue()));
    }
}
