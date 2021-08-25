package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.GenericPoolService;
import com.ericlam.mc.eld.components.GroupConfiguration;

import javax.annotation.Nullable;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class ELDConfigPoolService extends GenericPoolService<GroupConfiguration> implements ConfigPoolService {

    @Deprecated
    @Override
    public <C extends GroupConfiguration> ScheduleService.BukkitPromise<Map<String, C>> getPoolAsync(Class<C> config) {
        return scheduleService.callAsync(plugin, () -> {
            if (groupPoolMap.containsKey(config)) return (Map<String, C>) groupPoolMap.get(config);
            throw new UnsupportedOperationException("getPoolAsync is deprecated");
        });
    }

    @Deprecated
    @Nullable
    @Override
    public <C extends GroupConfiguration> Map<String, C> getPool(Class<C> config) {
        return (Map<String, C>) groupPoolMap.get(config);
    }

    @Deprecated
    @Override
    public <C extends GroupConfiguration> boolean isPoolCached(Class<C> config) {
        return groupPoolMap.containsKey(config);
    }
}
