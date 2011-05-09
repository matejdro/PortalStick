package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.RegionManager;
import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.UserManager;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.Util;

public class SetRegionCommand extends BaseCommand {
	
	public SetRegionCommand() {
		name = "setregion";
		argLength = 1;
		usage = "<name> <- saves your selected area as a region";
	}
	
	public boolean execute() {
		User user = UserManager.getUser(player);
		if (user.getPointOne() == null || user.getPointTwo() == null)
			Util.sendMessage(sender, "&cPlease select two points");
		else if (RegionManager.getRegion(args.get(0)) != null)
			Util.sendMessage(sender, "&cRegion already exists with that name!");
		else {
			Util.sendMessage(sender, "&aRegion &7" + args.get(0) + " &acreated!");
			RegionManager.createRegion(args.get(0), user.getPointOne(), user.getPointTwo());
		}
		return true;
	}
	
	public boolean permission(Player player) {
		return Permission.adminRegions(player);
	}
	
}
