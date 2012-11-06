package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.User;

public class SetRegionCommand extends BaseCommand {
	
	public SetRegionCommand(PortalStick plugin) {
		super(plugin);
		name = "setregion";
		argLength = 1;
		usage = "<name> <- saves selected region";
	}
	
	public boolean execute() {
		User user = plugin.userManager.getUser(player);
		if (user.pointOne == null || user.pointTwo == null)
			plugin.util.sendMessage(sender, "&cPlease select two points");
		else if (plugin.regionManager.getRegion(args.get(0)) != null)
			plugin.util.sendMessage(sender, "&cRegion already exists with that name!");
		else {
			plugin.util.sendMessage(sender, "&aRegion &7" + args.get(0) + " &acreated!");
			plugin.regionManager.createRegion(args.get(0), user.pointOne, user.pointTwo);
		}
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.permission.adminRegions(player);
	}
	
}
