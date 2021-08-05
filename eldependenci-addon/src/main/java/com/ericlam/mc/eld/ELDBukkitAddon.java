package com.ericlam.mc.eld;

/**
 * Addon 專用的繼承器
 */
public abstract class ELDBukkitAddon extends ELDBukkit {

    private ModuleInstaller moduleInstaller;

    @Override
    public final void onLoad() {
        final var provider = ELDependenci.getApi().register(this, serviceCollection -> {
            this.moduleInstaller = (ModuleInstaller) serviceCollection;
            this.bindServices(serviceCollection);
        });
        this.manageProviderAndInstallModule(provider, moduleInstaller);
    }


    /**
     * 用於生命週期之前的操作，可以安裝 guice module
     *
     * @param provider 管理器提供
     */
    protected abstract void manageProviderAndInstallModule(ManagerProvider provider, ModuleInstaller installer);

}
