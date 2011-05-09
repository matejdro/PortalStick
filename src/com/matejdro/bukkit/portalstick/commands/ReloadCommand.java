package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.Util;

public class ReloadCommand extends BaseCommand {

	public ReloadCommand() {
		name = "reload";
		argLength = 0;
		usage = "<- reloads the PortalStick config";
	}
	
	public boolean execute() {
		Config.unLoad();
		Config.load();
		Util.sendMessage(player, "&cPortalStick config files reloaded");
		return true;
	}
	
	public boolean permission(Player player) {
		return Permission.adminRegions(player);
	}

}
