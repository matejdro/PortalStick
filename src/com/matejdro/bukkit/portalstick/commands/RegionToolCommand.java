package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.UserManager;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.Util;

public class RegionToolCommand extends BaseCommand {

	public RegionToolCommand() {
		name = "regiontool";
		argLength = 0;
		usage = "<- enable/disable region selection mode";
	}
	
	public boolean execute() {
		User user = UserManager.getUser(player);
		if (user.getUsingTool()) {
			user.setUsingTool(false);
			Util.sendMessage(sender, "&aPortal region tool disabled");
		}
		else {
			user.setUsingTool(true);
			Util.sendMessage(sender, "&aPortal region tool enabled.`n- Left click to set position one`n- Right click to set position two");
			if (!player.getInventory().contains(Config.RegionTool))
					player.getInventory().addItem(new ItemStack(Config.RegionTool));
		}
		return true;
	}
	
	public boolean permission(Player player) {
		return Permission.adminRegions(player);
	}

}
