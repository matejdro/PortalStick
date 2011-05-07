package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import com.matejdro.bukkit.portalstick.commands.BaseCommand;
import com.matejdro.bukkit.portalstick.commands.RegionTool;
import com.matejdro.bukkit.portalstick.commands.SetRegion;
import com.matejdro.bukkit.portalstick.listeners.PortalStickBlockListener;
import com.matejdro.bukkit.portalstick.listeners.PortalStickPlayerListener;
import com.matejdro.bukkit.portalstick.listeners.PortalStickVehicleListener;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class PortalStick extends JavaPlugin {
	public static Logger log = Logger.getLogger("Minecraft");

	private PortalStickPlayerListener PlayerListener;
	private PortalStickBlockListener BlockListener;
	private PortalStickVehicleListener VehicleListener;
	
	public static HashMap<String, User> players = new HashMap<String, User>();
	
	public static List<BaseCommand> commands = new ArrayList<BaseCommand>();
	public static Config config;
	public static Permission permissions;

	public static WorldGuardPlugin worldGuard = null;

	public void onDisable() {
		for (Object o: PortalManager.portals.toArray())
		{
			Portal p = (Portal) o;
			p.delete();
		}
		
		for (Map.Entry<String, User> entry : players.entrySet()) {
			Player player = getServer().getPlayer(entry.getKey());
			User user = entry.getValue();
			if (player != null)
				player.getInventory().setContents(user.getInventory().getContents());
		}
	}

	public void onEnable() {
		
		PlayerListener = new PortalStickPlayerListener(this);
		BlockListener = new PortalStickBlockListener();
		VehicleListener = new PortalStickVehicleListener();
		new GrillManager(this);
		config = new Config(this);
		permissions = new Permission(this);
		
		//Register events
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, BlockListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BURN, BlockListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, BlockListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, PlayerListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, PlayerListener, Event.Priority.Low, this);	
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, PlayerListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, PlayerListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TELEPORT, PlayerListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_MOVE, VehicleListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_DROP_ITEM, PlayerListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PHYSICS, BlockListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_IGNITE, BlockListener, Event.Priority.Low, this);

		worldGuard = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
		
		//Register commands
		commands.add(new RegionTool());
		commands.add(new SetRegion());

	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[])
	{
		if (cmd.getName().equalsIgnoreCase("portal") && args.length > 0) {
			for (BaseCommand command : commands.toArray(new BaseCommand[0])) {
				if (command.name.equalsIgnoreCase(args[0]))
					return command.run(sender, args);
			}
		}
		return false;
	}
    
}
		    
