package com.matejdro.bukkit.portalstick.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.User;

import de.V10lator.PortalStick.V10Location;

public abstract class BaseCommand {
	final PortalStick plugin;
	
	public CommandSender sender;
	public List<String> args = new ArrayList<String>();
	public String name;
	public int argLength;
	public String usage;
	public boolean bePlayer = true;
	public Player player;
	public Region region;
	public User user;
	public String usedCommand;
	
	public BaseCommand(PortalStick plugin)
	{
	  this.plugin = plugin;
	}
	
	public boolean run(CommandSender sender, String[] preArgs, String cmd) {
		this.sender = sender;
		args.clear();
		for (String arg : preArgs)
			args.add(arg);
		args.remove(0);
		
		if (argLength != args.size()) {
			sendUsage();
			return true;
		}
		if (bePlayer && !(sender instanceof Player))
			return false;
		player = (Player)sender;
		region = plugin.regionManager.getRegion(new V10Location(player.getLocation()));
		user = plugin.userManager.getUser(player);
		usedCommand = cmd;
		if (!permission(player))
			return false;
		return execute();
	}
	
	public abstract boolean execute();
	public abstract boolean permission(Player player);
	
	public void sendUsage() {
		plugin.util.sendMessage(sender, "&c/"+usedCommand+" " + name + " " + usage);
	}

}
