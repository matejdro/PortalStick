package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.util.Util;

public class HelpCommand extends BaseCommand {

	public HelpCommand() {
		name = "help";
		argLength = 0;
		usage = "<- lists all PortalStick commands";
	}
	
	public boolean execute() {
		Util.sendMessage(player, "&a----------- &7PortalStick &a-----------");
		for (BaseCommand cmd : PortalStick.commands.toArray(new BaseCommand[0]))
			if (cmd.permission(player))
				Util.sendMessage(player, "&7- /portal &c" + cmd.name + " &7" + cmd.usage);
		return true;
	}
	
	public boolean permission(Player player) {
		return true;
	}

}