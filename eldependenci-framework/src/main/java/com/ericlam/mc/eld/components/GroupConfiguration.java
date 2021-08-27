package com.ericlam.mc.eld.components;

/**
 * 用於定義文件池
 */
public abstract class GroupConfiguration {

    private String id;

    /**
     * 獲取文件 id
     * @return 標識 id
     */
    public String getId() {
        return id;
    }


    /**
     * 設置文件 id
     * @param id 標識 id
     */
    public void setId(String id) {
        this.id = id;
    }

}
