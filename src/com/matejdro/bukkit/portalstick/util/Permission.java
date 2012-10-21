package com.matejdro.bukkit.portalstick.util;

import org.bukkit.entity.Player;

public class Permission {
	
	private static boolean hasPermission(Player player, String node, boolean def) {
		if(player.hasPermission(node))
			return true;
		while(node.contains("."))
		{
			node = node.substring(0, node.lastIndexOf("."));
			if(player.hasPermission(node))
				return true;
			node = node.substring(0, node.length() - 1);
			if(player.hasPermission(node))
				return true;
		}
		return player.hasPermission("*");
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
	public static boolean createBridge(Player player) {
		return hasPermission(player, "portalstick.createbridge", false);
	}public static boolean deleteBridge(Player player) {
		return hasPermission(player, "portalstick.deletebridge", false);
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
}
