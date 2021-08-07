package com.ericlam.mc.eld;

/**
 * Addon 專用的繼承器
 */
public abstract class ELDBukkitAddon extends ELDBukkit {

    private AddonManager addonManager;

    @Override
    public final void onLoad() {
        final var provider = ELDependenci.getApi().register(this, serviceCollection -> {
            this.addonManager = (AddonManager) serviceCollection;
            this.bindServices(serviceCollection);
        });
        this.preAddonInstall(provider, addonManager);
    }


    /**
     * 用於生命週期之前的操作，擴充插件安裝前的管理操作
     *
     * @param provider 管理器提供
     * @param installer 擴充管理器
     */
    protected abstract void preAddonInstall(ManagerProvider provider, AddonManager installer);

}
