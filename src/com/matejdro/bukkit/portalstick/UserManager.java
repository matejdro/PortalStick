package com.matejdro.bukkit.portalstick;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;


public class UserManager {
	private final PortalStick plugin;
	
	UserManager(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();
	
	public void createUser(Player player) {
		users.put(player.getName(), new User(player.getName()));
	}
	
	public User getUser(Player player) {
		return getUser(player.getName());
	}
	
	public User getUser(String player) {
		return users.get(player);
	}

	public void deleteUser(Player player) {
		deleteUser(getUser(player));
	}
	
	public void deleteUser(User user) {
		plugin.portalManager.deletePortals(user);
		deleteDroppedItems(user);
		users.values().remove(user);
	}

	public void deleteDroppedItems(Player player) {
		deleteDroppedItems(getUser(player));
	}
	
	public void deleteDroppedItems(User user) {
		if (user != null && user.droppedItems != null) {
			for (Item item : user.droppedItems)
				if (item != null)
					item.remove();
			user.droppedItems.clear();
		}
	}

}
