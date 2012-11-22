package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;

public abstract class BaseCommand {
	protected final PortalStick plugin;
	
	public final String name;
	protected final int argLength;
	protected final String usage;
	protected final boolean bePlayer;
	
	protected CommandSender sender;
	protected String[] args;
	protected Player player;
	protected String playerName;
	protected String usedCommand;
	
	public BaseCommand(PortalStick plugin, String name, int argLength, String usage, boolean bePlayer)
	{
	  this.plugin = plugin;
	  this.name = name;
	  this.argLength = argLength;
	  this.usage = usage;
	  this.bePlayer = bePlayer;
	}
	
	public boolean run(CommandSender sender, String[] preArgs, String cmd) {
		this.sender = sender;
		usedCommand = cmd;
		
		int nl = preArgs.length - 1;
		if (argLength != nl) {
			sendUsage();
			return true;
		}
		
		args = new String[nl];
		for(int i = 0; i < nl; i++)
			args[i] = preArgs[i + 1];
		
		if(!(sender instanceof Player))
		{
		  if(bePlayer)
		  {
			cleanup();
			return true;
		  }
		  playerName = "Console";
		}
		else
		{
		  player = (Player)sender;
		  if (!permission(player))
		  {
			cleanup();
			return false;
		  }
		  playerName = player.getName();
		}
		
		boolean ret = execute();
		cleanup();
		return ret;
	}
	
	public abstract boolean execute();
	public abstract boolean permission(Player player);
	
	public void sendUsage() {
		plugin.util.sendMessage(sender, "&c/"+usedCommand+" " + name + " " + usage);
	}
	
	private void cleanup()
	{
	  sender = null;
	  args = null;
	  player = null;
	  usedCommand = null;
	}
}
