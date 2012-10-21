package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.matejdro.bukkit.portalstick.commands.BaseCommand;
import com.matejdro.bukkit.portalstick.commands.DeleteAllCommand;
import com.matejdro.bukkit.portalstick.commands.DeleteCommand;
import com.matejdro.bukkit.portalstick.commands.DeleteRegionCommand;
import com.matejdro.bukkit.portalstick.commands.FlagCommand;
import com.matejdro.bukkit.portalstick.commands.HelpCommand;
import com.matejdro.bukkit.portalstick.commands.RegionInfoCommand;
import com.matejdro.bukkit.portalstick.commands.RegionListCommand;
import com.matejdro.bukkit.portalstick.commands.RegionToolCommand;
import com.matejdro.bukkit.portalstick.commands.ReloadCommand;
import com.matejdro.bukkit.portalstick.commands.SetRegionCommand;
import com.matejdro.bukkit.portalstick.listeners.PortalStickBlockListener;
import com.matejdro.bukkit.portalstick.listeners.PortalStickEntityListener;
import com.matejdro.bukkit.portalstick.listeners.PortalStickPlayerListener;
import com.matejdro.bukkit.portalstick.listeners.PortalStickVehicleListener;
import com.matejdro.bukkit.portalstick.listeners.PortalStickWorldListener;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Util;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class PortalStick extends JavaPlugin {
	
	public BaseCommand[] commands;
	public Config config;
	
	public final EntityManager entityManager = new EntityManager(this);
	public final FunnelBridgeManager funnelBridgeManager = new FunnelBridgeManager(this);
	public final GelManager gelManager = new GelManager(this);
	public final GrillManager grillManager = new GrillManager(this);
	public final PortalManager portalManager = new PortalManager(this);
	public final RegionManager regionManager = new RegionManager();
	public final UserManager userManager = new UserManager(this);

	public WorldGuardPlugin worldGuard = null;

	public void onDisable() {
		Config.saveAll();
		Config.unLoad();
	}

	public void onEnable() {
		Util.setPlugin(this);
		
		//Register events		
		getServer().getPluginManager().registerEvents(new PortalStickPlayerListener(this), this);
		getServer().getPluginManager().registerEvents(new PortalStickBlockListener(this), this);
		getServer().getPluginManager().registerEvents(new PortalStickVehicleListener(this), this);
		getServer().getPluginManager().registerEvents(new PortalStickEntityListener(this), this);
		getServer().getPluginManager().registerEvents(new PortalStickWorldListener(this), this);
		config = new Config(this);
		
		worldGuard = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");

		//Start grill checking timer
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new GrillManager(this), 400, 400);
		
		//Teleport all entities.
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new EntityManager(this), 2, 2);
		
		//Register commands
		ArrayList<BaseCommand> tmpList = new ArrayList<BaseCommand>();
		tmpList.add(new RegionToolCommand(this));
		tmpList.add(new SetRegionCommand(this));
		tmpList.add(new ReloadCommand(this));
		tmpList.add(new DeleteAllCommand(this));
		tmpList.add(new DeleteCommand(this));
		tmpList.add(new HelpCommand(this));
		tmpList.add(new RegionListCommand(this));
		tmpList.add(new DeleteRegionCommand(this));
		tmpList.add(new FlagCommand(this));
		tmpList.add(new RegionInfoCommand(this));
		commands = tmpList.toArray(new BaseCommand[0]);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[])
	{
		if (args.length == 0)
			args = new String[]{"help"};
		for (BaseCommand command : commands) {
			if (command.name.equalsIgnoreCase(args[0]))
				return command.run(sender, args, commandLabel);
		}
		return false;
	}
    
}
		    
