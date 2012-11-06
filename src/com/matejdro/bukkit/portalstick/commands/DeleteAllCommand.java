package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;

public class DeleteAllCommand extends BaseCommand {
	public DeleteAllCommand(PortalStick plugin)
	{
		super(plugin);
		name = "deleteall";
		argLength = 0;
		usage = "<- deletes all portals";
	}
	
	public boolean execute() {
		plugin.portalManager.deleteAll();
		plugin.util.sendMessage(player, "&cAll portals deleted");
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_DELETE_ALL);
	}

}