package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;

public class DeleteCommand extends BaseCommand {

	public DeleteCommand(PortalStick plugin) {
		super(plugin);
		name = "delete";
		argLength = 0;
		usage = "<- deletes your portals";
	}
	
	public boolean execute() {
		plugin.portalManager.deletePortals(user);
		player.sendMessage(ChatColor.RED+"Portals deleted");
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_PLACE_PORTAL);
	}

}