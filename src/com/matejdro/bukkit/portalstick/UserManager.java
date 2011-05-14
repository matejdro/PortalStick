package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;


public class UserManager {
	
	public static HashMap<Player, Boolean> teleportPermissionCache  = new HashMap<Player, Boolean>();
	private static ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();
	
	public static ConcurrentHashMap<String, User> getUserList() {
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
		deleteUser(getUser(player));
	}
	
	public static void deleteUser(User user) {
		PortalManager.deletePortals(user);
		deleteDroppedItems(user);
		String key = "";
		for (Map.Entry<String, User> entry : users.entrySet())
			if (entry.getValue() == user)
				key = entry.getKey();
		users.remove(key);
	}

	public static void deleteDroppedItems(Player player) {
		deleteDroppedItems(getUser(player));
	}
	
	public static void deleteDroppedItems(User user) {
		if (user.getDroppedItems() != null) {
			for (Item item : user.getDroppedItems())
				item.remove();
			user.resetItems();
		}
	}

}
