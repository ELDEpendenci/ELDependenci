package com.ericlam.mc.eld;

/**
 * 用於主類繼承
 */
public abstract class ELDBukkitPlugin extends ELDBukkit {

    @Override
    public final void onLoad() {
        final var provider = ELDependenci.getApi().register(this, this::bindServices);
        this.manageProvider(provider);
    }


    /**
     * 用於生命週期之前的操作
     *
     * @param provider 管理器提供
     */
    protected abstract void manageProvider(ManagerProvider provider);

}
