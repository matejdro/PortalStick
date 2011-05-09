package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalManager;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.Util;

public class DeleteAllCommand extends BaseCommand {

	public DeleteAllCommand() {
		name = "deleteall";
		argLength = 0;
		usage = "<- deletes all portals";
	}
	
	public boolean execute() {
		PortalManager.deleteAll();
		Util.sendMessage(player, "&cAll portals deleted");
		return true;
	}
	
	public boolean permission(Player player) {
		return Permission.adminRegions(player);
	}

}