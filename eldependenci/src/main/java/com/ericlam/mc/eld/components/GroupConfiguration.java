package com.ericlam.mc.eld.components;

/**
 * 用於定義文件池
 */
public abstract class GroupConfiguration extends Configuration {

    private String id;

    /**
     * 獲取文件 id
     * @return 標識 id
     */
    public String getId() {
        return id;
    }

}
