package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.User;

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
			plugin.util.sendMessage(sender, "&aPortal region tool disabled");
		}
		else {
			plugin.util.sendMessage(sender, "&aPortal region tool enabled.`n- Left click to set position one`n- Right click to set position two");
			if (!player.getInventory().contains(plugin.config.RegionTool))
					player.getInventory().addItem(new ItemStack(plugin.config.RegionTool, 1));
		}
		user.usingTool = !user.usingTool;
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.permission.adminRegions(player);
	}

}
