package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.Util;

public class RegionToolCommand extends BaseCommand {

	public RegionToolCommand(PortalStick plugin) {
		super(plugin);
		name = "regiontool";
		argLength = 0;
		usage = "<- enable/disable region selection mode";
	}
	
	public boolean execute() {
		User user = plugin.userManager.getUser(player);
		if (user.usingTool) {
			Util.sendMessage(sender, "&aPortal region tool disabled");
		}
		else {
			Util.sendMessage(sender, "&aPortal region tool enabled.`n- Left click to set position one`n- Right click to set position two");
			if (!player.getInventory().contains(Config.RegionTool))
					player.getInventory().addItem(new ItemStack(Config.RegionTool, 1));
		}
		user.usingTool = !user.usingTool;
		return true;
	}
	
	public boolean permission(Player player) {
		return Permission.adminRegions(player);
	}

}
