package com.matejdro.bukkit.portalstick;

import java.util.HashMap;

import org.bukkit.entity.Player;

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
		users.remove(player.getName());
	}

}
