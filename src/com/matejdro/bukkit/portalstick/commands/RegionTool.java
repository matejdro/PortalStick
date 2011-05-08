package com.matejdro.bukkit.portalstick.commands;

import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.UserManager;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.Util;

public class RegionTool extends BaseCommand {

	public RegionTool() {
		name = "regiontool";
		argLength = 0;
		usage = "<- enables or disables region selection mode";
	}
	
	public boolean execute() {
		if (!Permission.adminRegions(player))
			return false;
		User user = UserManager.getUser(player);
		if (user.getUsingTool()) {
			user.setUsingTool(false);
			Util.sendMessage(sender, "&aPortal region tool disabled");
		}
		else {
			user.setUsingTool(true);
			Util.sendMessage(sender, "&aPortal region tool enabled.`nLeft click to set position one`nRight click to set position two");
		}
		return true;
	}

}
