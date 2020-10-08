package com.ericlam.mc.test.eld.experinment;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Set;

public class ConsoleSender implements CommandSender {

    private final List<String> permissions = List.of("console.1", "console.2", "console.3");

    @Override
    public void sendMessage(String s) {
        System.out.println("[CONSOLE] "+s);
    }

    @Override
    public void sendMessage(String[] strings) {
        for (String string : strings) {
            this.sendMessage(string);
        }
    }

    @Override
    public Server getServer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public Spigot spigot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPermissionSet(String s) {
        return permissions.contains(s);
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPermission(String s) {
        return permissions.contains(s);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttachment(PermissionAttachment permissionAttachment) {

    }

    @Override
    public void recalculatePermissions() {

    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean b) {

    }
}
