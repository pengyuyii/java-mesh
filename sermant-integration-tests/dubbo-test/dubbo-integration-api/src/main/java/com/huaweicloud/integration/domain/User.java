/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huaweicloud.integration.domain;

import java.io.Serializable;

/**
 * 测试
 *
 * @author pengyuyi
 * @since 2021-10-18
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;

    private String name;

    private boolean enabled;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
