package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.GlassBridgeManager;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.Util;

public class TestCommand extends BaseCommand {

	public TestCommand() {
		name = "test";
		argLength = 0;
		usage = "<- place new light bridge.";
	}
	
	public boolean execute() {
		GlassBridgeManager.placeGlassBridge(player.getTargetBlock(null, 50));
		return true;
	}
	
	public boolean permission(Player player) {
		return Permission.adminRegions(player);
	}

}
