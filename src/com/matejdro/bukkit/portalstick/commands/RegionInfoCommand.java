package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.Util;

public class RegionInfoCommand extends BaseCommand {

	public RegionInfoCommand() {
		name = "regioninfo";
		argLength = 0;
		usage = "<- says the region you are in";
	}
	
	public boolean execute() {
		Util.sendMessage(player, "&7- &c" + region.Name + " &7- &c" + region.Min.toString() + " &7-&c " + region.Max.toString());
		return true;
	}
	
	public boolean permission(Player player) {
		return Permission.adminRegions(player);
	}

}