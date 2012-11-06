package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;

public class HelpCommand extends BaseCommand {

	public HelpCommand(PortalStick plugin)
	{
	  super(plugin);
	  name = "help";
	  argLength = 0;
	  usage = "<- lists all PortalStick commands";
	}
	
	public boolean execute() {
		player.sendMessage(ChatColor.RED+"--------------------- "+ChatColor.GRAY+"PortalStick "+ChatColor.RED+"---------------------");
		for (BaseCommand cmd : plugin.commands)
			if (cmd.permission(player))
				plugin.util.sendMessage(player, "&7- /"+usedCommand+" &c" + cmd.name + " &7" + cmd.usage);
		return true;
	}
	
	public boolean permission(Player player) {
		return true;
	}

}