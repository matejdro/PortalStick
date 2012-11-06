package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;

public class DeleteRegionCommand extends BaseCommand {

	public DeleteRegionCommand(PortalStick plugin) {
		super(plugin);
		name = "deleteregion";
		argLength = 1;
		usage = "<name> <- deletes specified region";
	}
	
	public boolean execute() {
		if (args.get(0).equalsIgnoreCase("global"))
			plugin.util.sendMessage(player, "&cYou cannot delete the global config!");
		else if (plugin.regionManager.getRegion(args.get(0)) != null) {
			plugin.regionManager.deleteRegion(args.get(0));
			plugin.config.reLoad();
			plugin.util.sendMessage(player, "&cRegion &7" + args.get(0) + " &cdeleted");
		}
		else plugin.util.sendMessage(player, "&cRegion &7" + args.get(0) + " &cdoes not exist!");
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_ADMIN_REGIONS);
	}

}