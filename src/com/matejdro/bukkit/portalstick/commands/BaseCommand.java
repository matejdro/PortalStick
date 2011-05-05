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
	
	public boolean run(CommandSender sender, String[] args) {
		this.sender = sender;
		if (args.length > 1) {
			int i = 1;
			while (i < args.length) {
				this.args.add(args[i]);
			}
		}
		if (argLength != (args.length -1)) {
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
