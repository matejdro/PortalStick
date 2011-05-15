package com.matejdro.bukkit.portalstick.util;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Permission {
	
	private PortalStick plugin;
	private static PermissionPlugin handler = PermissionPlugin.OP;
	private static PermissionHandler permissionPlugin;
	private static HashSet<PermissionCache> cache = new HashSet<PermissionCache>();
	
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
		return hasPermission(player, "portalstick.creategrill", true);
	}
	public static boolean deleteGrill(Player player) {
		return hasPermission(player, "portalstick.deletegrill", true);
	}
	public static boolean teleport(Player player) {
		return hasPermission(player, "portalstick.teleport", true);
	}
	public static boolean adminRegions(Player player) {
		return hasPermission(player, "portalstick.admin.regions", false);
	}
	public static boolean damageBoots(Player player) {
		return hasPermission(player, "portalstick.damageboots", true);
	}
	public static boolean deleteAll(Player player) {
		return hasPermission(player, "portalstick.admin.deleteall", false);
	}
	
	private enum PermissionPlugin {
		PERMISSIONS,
		OP
	}
	
	private class PermissionCache {
		
		public String node = null;
		public HashMap<Player, Boolean> cache = new HashMap<Player, Boolean>();
		
	}

}
