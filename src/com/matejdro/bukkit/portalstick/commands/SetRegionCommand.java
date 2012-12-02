package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.User;

public class SetRegionCommand extends BaseCommand {
	
	public SetRegionCommand(PortalStick plugin) {
		super(plugin, "setregion", 1, "<name> <- saves selected region", true);
	}
	
	public boolean execute() {
		User user = plugin.userManager.getUser(player);
		if (user.pointOne == null || user.pointTwo == null)
			plugin.util.sendMessage(sender, plugin.i18n.getString("RegionToolNoPointsSelected", playerName, args[0]));
		else if (plugin.regionManager.getRegion(args[0]) != null)
			plugin.util.sendMessage(sender, plugin.i18n.getString("RegionExists", playerName, args[0]));
		else {
			plugin.util.sendMessage(sender, plugin.i18n.getString("RegionCreated", playerName, args[0]));
			plugin.regionManager.createRegion(args[0], user.pointOne, user.pointTwo);
		}
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_ADMIN_REGIONS);
	}
	
}
