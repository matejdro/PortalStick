package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.RegionManager;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.Util;

public class DeleteRegionCommand extends BaseCommand {

	public DeleteRegionCommand() {
		name = "deleteregion";
		argLength = 1;
		usage = "<name> <- deletes specified region";
	}
	
	public boolean execute() {
		if (args.get(0).equalsIgnoreCase("global"))
			Util.sendMessage(player, "&cYou cannot delete the global config!");
		else if (RegionManager.getRegion(args.get(0)) != null) {
			RegionManager.deleteRegion(args.get(0));
			Config.reLoad();
			Util.sendMessage(player, "&cRegion &7" + args.get(0) + " &cdeleted");
		}
		else Util.sendMessage(player, "&cRegion &7" + args.get(0) + " &cdoes not exist!");
		return true;
	}
	
	public boolean permission(Player player) {
		return Permission.adminRegions(player);
	}

}