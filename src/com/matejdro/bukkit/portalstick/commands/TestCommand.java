package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.Permission;

public class TestCommand extends BaseCommand {

	public TestCommand() {
		name = "test";
		argLength = 0;
		usage = "<- test stuff";
	}
	
	public boolean execute() {
		player.setVelocity(new Vector(8,2,0));
		return true;
	}
	
	public boolean permission(Player player) {
		return Permission.adminRegions(player);
	}

}