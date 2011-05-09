package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.RegionManager;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.Util;

public class RegionListCommand extends BaseCommand {

	public RegionListCommand() {
		name = "regionlist";
		argLength = 0;
		usage = "<- list all portal regions";
	}
	
	public boolean execute() {
		Util.sendMessage(player, "&a---------- &7Portal Regions &a----------");
		for (Region region : RegionManager.getRegionMap().values())
			Util.sendMessage(player, "&7- &c" + region.Name + " &7- &c" + region.PointOne.toString() + " - " + region.PointOne.toString());
		return true;
	}
	
	public boolean permission(Player player) {
		return Permission.adminRegions(player);
	}

}