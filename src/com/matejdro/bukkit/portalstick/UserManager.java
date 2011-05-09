package com.matejdro.bukkit.portalstick;

import java.util.HashMap;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class UserManager {
	
	private static HashMap<String, User> users = new HashMap<String, User>();
	
	public static HashMap<String, User> getUserList() {
		return users;
	}
	
	public static void createUser(Player player) {
		users.put(player.getName(), new User());
	}
	
	public static User getUser(Player player) {
		return getUser(player.getName());
	}
	
	public static User getUser(String player) {
		return users.get(player);
	}

	public static void deleteUser(Player player) {
		PortalManager.deletePortals(getUser(player));
		deleteDroppedItems(player);
		users.remove(player.getName());
	}

	public static void emancipate(Player player) {
		
		User user = getUser(player);
		Region region = RegionManager.getRegion(player.getLocation());
		PortalManager.deletePortals(user);
		
		if (region.getBoolean(RegionSetting.GRILLS_CLEAR_INVENTORY) && !user.getUsingTool())
			PortalManager.setPortalInventory(player);
		
		if (region.getBoolean(RegionSetting.GRILLS_CLEAR_ITEM_DROPS)) {
			deleteDroppedItems(player);
		}
		
	}
	
	public static void deleteDroppedItems(Player player) {
		User user = getUser(player);
		for (Item item : user.getDroppedItems())
			item.remove();
		user.resetItems();
	}

}
