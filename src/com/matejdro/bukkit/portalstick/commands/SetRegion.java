package com.matejdro.bukkit.portalstick.commands;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.Util;

public class SetRegion extends BaseCommand {
	
	public SetRegion() {
		name = "setregion";
		argLength = 1;
		usage = "<name> <- saves your selected area as a region";
	}
	
	public boolean execute() {
		if (!Permission.adminRegions(player))
			return false;
		User user = PortalStick.players.get(player.getName());
		if (user.getPointOne() == null || user.getPointTwo() == null)
			Util.sendMessage(sender, "&cPlease select two points");
		else if (Config.getRegion(args.get(0)) != null)
			Util.sendMessage(sender, "&cRegion already exists with that name!");
		else {
			Config.createRegion(args.get(0), user.getPointOne(), user.getPointTwo());
		}
		return false;
	}
}
