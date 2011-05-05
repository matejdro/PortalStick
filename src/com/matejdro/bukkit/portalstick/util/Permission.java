package com.matejdro.bukkit.portalstick.util;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Permission {
	
	private PortalStick plugin;
	private static PermissionPlugin handler = PermissionPlugin.OP;
	private static PermissionHandler permissionPlugin;
	
	public Permission(PortalStick instance) {
		plugin = instance;
        Plugin permissions = plugin.getServer().getPluginManager().getPlugin("Permissions");
        
        if (permissions != null) {
        	permissionPlugin = ((Permissions)permissions).getHandler();
        	handler = PermissionPlugin.PERMISSIONS;
        	Util.info("Using Permissions for user permissions");
        }
        else {
        	Util.info("No permission handler detected, only ops can use commands");
        }
	}
	
	private static boolean hasPermission(Player player, String node, boolean def) {
		switch (handler) {
			case PERMISSIONS:
				return permissionPlugin.has(player, node);
			case OP:
				return player.isOp();
		}
		return def;
	}
	
	public static boolean placePortal(Player player) {
		return hasPermission(player, "portalstick.placeportal", true);
	}
	public static boolean createGrill(Player player) {
		return hasPermission(player, "portalstick.creategrill", false);
	}
	public static boolean deleteGrill(Player player) {
		return hasPermission(player, "portalstick.deletegrill", false);
	}
	public static boolean teleport(Player player) {
		return hasPermission(player, "portalstick.teleport", true);
	}
	public static boolean adminRegions(Player player) {
		return hasPermission(player, "portalstick.admin.regions", true);
	}
	
	private enum PermissionPlugin {
		PERMISSIONS,
		OP
	}

}
