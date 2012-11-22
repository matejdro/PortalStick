package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;

public class DeleteCommand extends BaseCommand {

	public DeleteCommand(PortalStick plugin) {
		super(plugin, "delete", 0, "<- deletes your portals", true);
	}
	
	public boolean execute() {
		plugin.portalManager.deletePortals(plugin.userManager.getUser(player));
		plugin.util.sendMessage(sender, plugin.i18n.getString("OwnPortalsDeleted", playerName));
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_PLACE_PORTAL);
	}

}