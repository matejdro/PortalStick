package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;

public class ReloadCommand extends BaseCommand {

	public ReloadCommand(PortalStick plugin) {
		super(plugin, "reload", 0, "<- reloads the PortalStick config", false);
	}
	
	public boolean execute() {
		plugin.config.reLoad();
		plugin.util.sendMessage(sender, plugin.i18n.getString("ConfigurationReloaded", playerName));
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_ADMIN_REGIONS);
	}
}
