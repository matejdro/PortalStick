package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;

public class DeleteRegionCommand extends BaseCommand {

	public DeleteRegionCommand(PortalStick plugin) {
		super(plugin, "deleteregion", 1, "<name> <- deletes specified region", false);
	}
	
	public boolean execute() {
		if (args[0].equalsIgnoreCase("global"))
			plugin.util.sendMessage(sender, plugin.i18n.getString("CanNotDeleteGlobalRegion", playerName));
		else if (plugin.regionManager.getRegion(args[0]) != null) {
			plugin.regionManager.deleteRegion(args[0]);
			plugin.config.reLoad();
			plugin.util.sendMessage(sender, plugin.i18n.getString("RegionDeleted", playerName, args[0]));
		}
		else plugin.util.sendMessage(sender, plugin.i18n.getString("RegionNotFound", playerName, args[0]));
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_ADMIN_REGIONS);
	}

}