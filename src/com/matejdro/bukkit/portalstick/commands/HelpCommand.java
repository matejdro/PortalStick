package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;

public class HelpCommand extends BaseCommand {

	public HelpCommand(PortalStick plugin)
	{
	  super(plugin, "help", 0, "<- lists all PortalStick commands", false);
	}
	
	public boolean execute() {
		player.sendMessage(ChatColor.RED+"--------------------- "+ChatColor.GRAY+"PortalStick "+ChatColor.RED+"---------------------");
		for (BaseCommand cmd : plugin.commands)
			if ((player != null && cmd.permission(player)) || (player == null && !cmd.bePlayer))
				plugin.util.sendMessage(sender, "&7- /"+usedCommand+" &c" + cmd.name + " &7" + cmd.usage);
		return true;
	}
	
	public boolean permission(Player player) {
		return true;
	}

}