package com.ericlam.mc.eld;

/**
 * ELD 插件，用於不同平台作擴展
 */
public interface ELDPlugin extends MCPlugin {

    /**
     * 綁定服務，單例與文件的地方
     *
     * @param collection 註冊器
     */
    void bindServices(ServiceCollection collection);

}
