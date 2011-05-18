package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
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
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.Util;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class PortalStick extends JavaPlugin {
	public static Logger log = Logger.getLogger("Minecraft");

	private PortalStickPlayerListener playerListener;
	private PortalStickBlockListener blockListener;
	private PortalStickVehicleListener vehicleListener;
	private PortalStickEntityListener entityListener;
	private PortalStickWorldListener worldListener;
	private GrillManager grillManager;
	
	public static List<BaseCommand> commands = new ArrayList<BaseCommand>();
	public static Config config;
	public static Permission permissions;
	
	public static PortalStick instance;

	public static WorldGuardPlugin worldGuard = null;

	public void onDisable() {
		Config.saveAll();
		Config.unLoad();
		Util.info("PortalStick unloaded");
	}

	public void onEnable() {
		instance = this;
		
		playerListener = new PortalStickPlayerListener();
		blockListener = new PortalStickBlockListener(this);
		vehicleListener = new PortalStickVehicleListener();
		entityListener = new PortalStickEntityListener();
		worldListener = new PortalStickWorldListener();
		grillManager = new GrillManager(this);
		config = new Config(this);
		permissions = new Permission(this);
		
		//Register events
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BURN, blockListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_FROMTO, blockListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Low, this);	
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.CHUNK_UNLOAD, worldListener, Event.Priority.Low, this);

		worldGuard = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");

		//Start grill checking timer
		getServer().getScheduler().scheduleSyncRepeatingTask(this, grillManager, 400, 400);
		
		//Teleport all entities - uncompleted.
		//getServer().getScheduler().scheduleSyncRepeatingTask(this, entityManager, 2, 2);
		
		//Register commands
		commands.add(new RegionToolCommand());
		commands.add(new SetRegionCommand());
		commands.add(new ReloadCommand());
		commands.add(new DeleteAllCommand());
		commands.add(new DeleteCommand());
		commands.add(new HelpCommand());
		commands.add(new RegionListCommand());
		commands.add(new DeleteRegionCommand());
		commands.add(new FlagCommand());
		commands.add(new RegionInfoCommand());
		
		Util.info("PortalStick enabled");

	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[])
	{
		if (cmd.getName().equalsIgnoreCase("portalstick")) {
			if (args.length == 0)
				args = new String[]{"help"};
			for (BaseCommand command : commands.toArray(new BaseCommand[0])) {
				if (command.name.equalsIgnoreCase(args[0]))
					return command.run(sender, args, commandLabel);
			}
		}
		return false;
	}
    
}
		    
