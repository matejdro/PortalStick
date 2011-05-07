package com.matejdro.bukkit.portalstick.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.util.Util;

public abstract class BaseCommand {
	
	protected CommandSender sender;
	protected List<String> args = new ArrayList<String>();
	public String name;
	public int argLength;
	public String usage;
	public boolean bePlayer = true;
	public Player player;
	
	public boolean run(CommandSender sender, String[] preArgs) {
		this.sender = sender;
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
		return execute();
	}
	
	public abstract boolean execute();
	
	public List<String> getArgs() {
		return args;
	}
	
	public void sendUsage() {
		Util.sendMessage(sender, "&c/portal " + name + " " + usage);
	}

}
