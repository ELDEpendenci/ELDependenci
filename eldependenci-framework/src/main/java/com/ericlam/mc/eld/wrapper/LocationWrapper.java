package com.ericlam.mc.eld.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * 由於 Location 無法在 onload 的時候加載，因此創建此 Wrapper
 */
public final class LocationWrapper {

    @JsonProperty
    private String world;
    @JsonProperty
    private double x, y, z;
    @JsonProperty
    private float yaw, pitch;

    /**
     * 創建 wrapper
     * @param location 原本的 location
     */
    public LocationWrapper(Location location) {
        this.setLocation(location);
    }

    public LocationWrapper() {
    }

    /**
     * 設定 location
     * @param location 原本的 location
     */
    public void setLocation(Location location){
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    /**
     * 取得 location
     * @return location
     */
    public Location toLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z,  yaw, pitch);
    }
}
