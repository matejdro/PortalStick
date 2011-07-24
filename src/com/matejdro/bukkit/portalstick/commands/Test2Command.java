package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.GlassBridge;
import com.matejdro.bukkit.portalstick.GlassBridgeManager;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.Util;

public class Test2Command extends BaseCommand {

	public Test2Command() {
		name = "test2";
		argLength = 0;
		usage = "<- remove bridge";
	}
	
	public boolean execute() {
		GlassBridge bridge = GlassBridgeManager.bridges.get(player.getTargetBlock(null, 50));
		if (bridge != null) bridge.deactivate();
		return true;
	}
	
	public boolean permission(Player player) {
		return Permission.adminRegions(player);
	}

}
