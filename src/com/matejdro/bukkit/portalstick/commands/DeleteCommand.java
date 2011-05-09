package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalManager;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.Util;

public class DeleteCommand extends BaseCommand {

	public DeleteCommand() {
		name = "delete";
		argLength = 0;
		usage = "<- deletes your portals";
	}
	
	public boolean execute() {
		PortalManager.deletePortals(user);
		Util.sendMessage(player, "&cPortals deleted");
		return true;
	}
	
	public boolean permission(Player player) {
		return Permission.placePortal(player);
	}

}