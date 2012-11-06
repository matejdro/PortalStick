package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;

public class ReloadCommand extends BaseCommand {

	public ReloadCommand(PortalStick plugin) {
		super(plugin);
		name = "reload";
		argLength = 0;
		usage = "<- reloads the PortalStick config";
	}
	
	public boolean execute() {
		plugin.config.unLoad();
		plugin.config.load();
		plugin.util.sendMessage(player, "&cPortalStick config files reloaded");
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.permission.adminRegions(player);
	}

}
