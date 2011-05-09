package com.matejdro.bukkit.portalstick.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.RegionManager;
import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.UserManager;
import com.matejdro.bukkit.portalstick.util.Util;

public abstract class BaseCommand {
	
	public CommandSender sender;
	public List<String> args = new ArrayList<String>();
	public String name;
	public int argLength;
	public String usage;
	public boolean bePlayer = true;
	public Player player;
	public Region region;
	public User user;
	
	public boolean run(CommandSender sender, String[] preArgs) {
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
		region = RegionManager.getRegion(player.getLocation());
		user = UserManager.getUser(player);
		if (!permission(player))
			return false;
		return execute();
	}
	
	public abstract boolean execute();
	public abstract boolean permission(Player player);
	
	public void sendUsage() {
		Util.sendMessage(sender, "&c/portal " + name + " " + usage);
	}

}
