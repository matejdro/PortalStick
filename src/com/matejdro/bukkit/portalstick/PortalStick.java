package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
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
import com.matejdro.bukkit.portalstick.util.BlockUtil;
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
	public final RegionManager regionManager = new RegionManager(this);
	public final UserManager userManager = new UserManager(this);

	public WorldGuardPlugin worldGuard = null;
	
	public final Util util = new Util(this);
	public final BlockUtil blockUtil = new BlockUtil();

	public void onDisable() {
		ArrayList<String> copy = new ArrayList<String>(gelManager.onRedGel.keySet());
		Player p;
		Server s = getServer();
		for(String pn: copy)
		{
		  p = s.getPlayerExact(pn);
		  gelManager.resetPlayer(p);
		}
		config.saveAll();
		config.unLoad();
	}

	public void onEnable() {
		config = new Config(this);
		
		//Register events
		Server s = getServer();
		PluginManager pm = s.getPluginManager();
		pm.registerEvents(new PortalStickPlayerListener(this), this);
		pm.registerEvents(new PortalStickBlockListener(this), this);
		pm.registerEvents(new PortalStickVehicleListener(this), this);
		pm.registerEvents(new PortalStickEntityListener(this), this);
		pm.registerEvents(new PortalStickWorldListener(this), this);
		
		worldGuard = (WorldGuardPlugin) pm.getPlugin("WorldGuard");
		
		config.load();

		//Start grill checking timer
		s.getScheduler().scheduleSyncRepeatingTask(this, grillManager, 400L, 400L);
		
		//Teleport all entities.
		s.getScheduler().scheduleSyncRepeatingTask(this, entityManager, 1L, 2L);
		
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
	
	public final String PERM_CREATE_BRIDGE	= "portalstick.createbridge";
	public final String PERM_CREATE_GRILL	= "portalstick.creategrill";
	public final String PERM_PLACE_PORTAL	= "portalstick.placeportal";
	public final String PERM_DELETE_ALL		= "portalstick.admin.deleteall";
	public final String PERM_ADMIN_REGIONS	= "portalstick.admin.regions";
	public final String PERM_DELETE_BRIDGE	= "portalstick.deletebridge";
	public final String PERM_DELETE_GRILL	= "portalstick.deletegrill";
	public final String PERM_DAMAGE_BOOTS	= "portalstick.damageboots";
	public final String PERM_TELEPORT 		= "portalstick.teleport";
    
	public boolean hasPermission(Player player, String node) {
		if(player.hasPermission(node))
			return true;
		while(node.contains("."))
		{
			node = node.substring(0, node.lastIndexOf("."));
			if(player.hasPermission(node))
				return true;
			node = node.substring(0, node.length() - 1);
			if(player.hasPermission(node))
				return true;
		}
		return player.hasPermission("*");
	}
}
		    
