package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import com.matejdro.bukkit.portalstick.commands.BaseCommand;
import com.matejdro.bukkit.portalstick.commands.RegionTool;
import com.matejdro.bukkit.portalstick.commands.SetRegion;
import com.matejdro.bukkit.portalstick.listeners.PortalStickBlockListener;
import com.matejdro.bukkit.portalstick.listeners.PortalStickPlayerListener;
import com.matejdro.bukkit.portalstick.listeners.PortalStickVehicleListener;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.Setting;
import com.matejdro.bukkit.portalstick.util.Util;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class PortalStick extends JavaPlugin {
	public static Logger log = Logger.getLogger("Minecraft");

	private PortalStickPlayerListener PlayerListener;
	private PortalStickBlockListener BlockListener;
	private PortalStickVehicleListener VehicleListener;
	
	public static HashMap<String, User> players = new HashMap<String, User>();
	public static HashSet<Portal> portals = new HashSet<Portal>();
	public static HashSet<Grill> grills = new HashSet<Grill>();
	
	public static PortalStick instance;
	public static List<BaseCommand> commands;
	public static Config config;
	public static Permission permissions;

	public static WorldGuardPlugin worldGuard = null;

	public void onDisable() {
		for (Object o: portals.toArray())
		{
			Portal p = (Portal) o;
			p.delete();
		}
		
		for (Object o: grills.toArray())
		{
			Grill g = (Grill) o;
			g.delete();
		}
		
		for (Map.Entry<String, User> entry : players.entrySet()) {
			Player player = getServer().getPlayer(entry.getKey());
			User user = entry.getValue();
			if (player != null)
				player.getInventory().setContents(user.getInventory().getContents());
		}
	}

	public void onEnable() {
		instance = this;
		
		PlayerListener = new PortalStickPlayerListener(this);
		BlockListener = new PortalStickBlockListener(this);
		VehicleListener = new PortalStickVehicleListener();
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
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
		if (cmd.getName().equalsIgnoreCase("portal") && args.length > 0) {
			for (BaseCommand command : commands.toArray(new BaseCommand[0])) {
				if (command.name.equalsIgnoreCase(args[0]))
					return command.run(sender, args);
			}
		}
		return false;
	}
    
    public void placePortal(Block block, Player player, Boolean orange)
    {
    	
    	float dir = (float)Math.toDegrees(Math.atan2(player.getLocation().getBlockX() - block.getX(), block.getZ() - player.getLocation().getBlockZ()));
    	dir = dir % 360;
        if(dir < 0)
        	dir += 360;
        
    	//Try WEST/EAST
    	if (dir < 90 || dir > 270)
    	{
    		if (placePortal(block, BlockFace.EAST, player, orange, false)) return;
    	}
    	else
    	{
    		if (placePortal(block, BlockFace.WEST, player, orange, false)) return;
    	}
    	
    	//Try NORTH/SOUTH
    	if (dir < 180) 
    	{
    		if (placePortal(block, BlockFace.SOUTH, player, orange, false)) return;
    	}
    	else
    	{
    		if (placePortal(block, BlockFace.NORTH, player, orange, false)) return;
    	}
    	
    	//Try UP/DOWN
    	if (player.getEyeLocation().getY() >= block.getLocation().getY() )
    	{
    		if (placePortal(block, BlockFace.UP, player, orange, false)) return;
    	}
    	else
    	{
    		if (placePortal(block, BlockFace.DOWN, player, orange, true)) return;
    	}
    
     }
    
    public Boolean placePortal(Block block,BlockFace face, Player player, Boolean orange, Boolean end)
    {   
    	//Check if player can place here
    	Region region = Config.getRegion(player.getLocation());
    	if (region.getBoolean(Setting.CHECK_WORLDGUARD) && worldGuard != null && !worldGuard.canBuild(player, block))
    		return false;
    	if (!region.getBoolean(Setting.ENABLE_PORTALS))
    	if (!Permission.placePortal(player))
    		return false;

    	Boolean vertical = false;
    	
    	PortalCoord portalc = new PortalCoord();
    	
    	User owner = players.get(player.getName());
    	
    	Portal oldportal = orange ? owner.getOrangePortal() : owner.getBluePortal();
    	if (oldportal == null) oldportal = new Portal();
    	    	
    	if (face == BlockFace.DOWN || face == BlockFace.UP)
    	{
    		vertical = true;
    		portalc = generatePortal(block, face);
    		if (!checkPortal(portalc, oldportal))
    		{
    			if (end) Util.sendMessage(player, Config.MessageCannotPlacePortal);
    			return false;
    		}
    		
    	}
    	else
    	{
    		portalc = generateHorizontalPortal(block, face, oldportal);
    		if (portalc.finished)
    		{
    			if (end) Util.sendMessage(player, Config.MessageCannotPlacePortal);
    			return false;
    		}
    	}
    	
    	portalc.destloc.setX(portalc.destloc.getX() + 0.5);
    	portalc.destloc.setZ(portalc.destloc.getZ() + 0.5);

    	
    	Portal portal = new Portal(portalc.destloc, portalc.border, portalc.inside, owner, orange, vertical, portalc.tpface);
    	
    	if (orange)
    	{
    		if (owner.getOrangePortal() != null) owner.getOrangePortal().delete();
    		owner.setOrangePortal(portal);
    		
    	}
    	else
    	{
    		if (owner.getBluePortal() != null) owner.getBluePortal().delete();
    		owner.setBluePortal(portal);
    	}
    	
    	portals.add(portal);
    	portal.create();
    	
    	return true;
    	
    }
    
    private PortalCoord generateHorizontalPortal(Block block, BlockFace face, Portal oldportal)
    {
    	PortalCoord portal = generatePortal(block, face);
    	if (checkPortal(portal, oldportal)) return portal;
    	
    	block = block.getRelative(0,0,0);
    	portal = generatePortal(block, face);
    	if (checkPortal(portal, oldportal)) return portal;
    	
    	block = block.getRelative(0,1,0);
    	portal = generatePortal(block, face);
    	if (checkPortal(portal, oldportal)) return portal;
    	
    	block = block.getRelative(0,-1,0);
    	portal = generatePortal(block, face);
    	if (checkPortal(portal, oldportal)) return portal;
    	
    	if (!checkPortal(portal, oldportal)) portal.finished = true;
			return portal;
    	
    }
    
    private PortalCoord generatePortal(Block block, BlockFace face)
    {
    	PortalCoord portal = new PortalCoord();
    	if (face == BlockFace.DOWN || face == BlockFace.UP)
    	{
    		if (!Config.CompactPortal)
    		{
    			portal.border.add(block.getRelative(1,0,0));
        		portal.border.add(block.getRelative(0,0,1));
        		portal.border.add(block.getRelative(-1,0,0));
        		portal.border.add(block.getRelative(0,0,-1));
        		portal.border.add(block.getRelative(1,0,-1));
        		portal.border.add(block.getRelative(-1,0,1));
        		portal.border.add(block.getRelative(1,0,1));
        		portal.border.add(block.getRelative(-1,0,-1));
    		}

			portal.border.add(block.getRelative(1,0,0));

    		
    		portal.inside.add(block);
        	
        	if (face == BlockFace.DOWN)
        	{
        		portal.destloc = block.getRelative(0,-2,0).getLocation();
        		portal.tpface = BlockFace.DOWN;
        	}
        	else
        	{
        		portal.destloc = block.getRelative(0,2,0).getLocation();
        		portal.tpface = BlockFace.UP;
        	}
        		
    	}
    	else
    	{
    		int x = 0;
        	int z = 0;
        	switch(face)
        	{
        	case NORTH:
        		z = -1;
        		portal.tpface = BlockFace.SOUTH;
        		break;
        	case EAST:
        		x = -1;
        		portal.tpface = BlockFace.WEST;
        		break;
        	case SOUTH:
        		z = 1;
        		portal.tpface = BlockFace.NORTH;
        		break;
        	case WEST:
        		x = 1;
        		portal.tpface = BlockFace.EAST;
        		break;
        	}
        	if (!Config.CompactPortal)
        	{
        		portal.border.add(block.getRelative(0,1,0));
            	portal.border.add(block.getRelative(x*1,0,z*1));
            	portal.border.add(block.getRelative(x*-1,0,z*-1));
            	portal.border.add(block.getRelative(x*1,1,z*1));
            	portal.border.add(block.getRelative(x*-1,1,z*-1));
            	portal.border.add(block.getRelative(x*1,-1,z*1));
            	portal.border.add(block.getRelative(x*-1,-1,z*-1));
            	portal.border.add(block.getRelative(x*1,-2,z*1));
            	portal.border.add(block.getRelative(x*-1,-2,z*-1));
        	}
        	portal.border.add(block.getRelative(0,-2,0));

        	portal.inside.add(block);
        	portal.inside.add(block.getRelative(0,-1,0));
        	
        	portal.destloc = block.getRelative(z*2,-1,x*2).getLocation();
           	}

    	return portal;
    }
    
    private Boolean checkPortal(PortalCoord portal, Portal oldportal)
    {
    	for (Block b: portal.border)
    	{
    		Region region = Config.getRegion(b.getLocation());
    		if (region.getList(Setting.TRANSPARENT_BLOCKS).contains(b.getTypeId()) || (region.getBoolean(Setting.ALL_BLOCKS_PORTAL) && !region.getList(Setting.PORTAL_BLOCKS).contains(b.getTypeId() ) && !oldportal.getInside().contains(b) && !oldportal.getBorder().contains(b)))
    		{
    			return false;
    		}
    	}
    	for (Block b: portal.inside)
    	{
    		if (b.getTypeId() == 0 && !oldportal.getInside().contains(b) && !oldportal.getBorder().contains(b) )
    		{
    			return false;
    		}
    	}
    	return true;
    }
    
    public void setPortalInventory(Player player)
    {
    	PlayerInventory inv = player.getInventory();
		for (ItemStack i : inv.getContents().clone())
		{
			if (i != null)
				inv.remove(i);
		}
		inv.setItemInHand(new ItemStack(Material.STICK, 1));
    }
    
    public void checkPlayerMove(Player player, Region regionFrom, Region regionTo)
    {
    	User user = PortalStick.players.get(player.getName());
    	if (!regionTo.Name.equals(regionFrom.Name)) {
			if (regionTo.Name == "global") {
				player.getInventory().setContents(user.getInventory().getContents());
			}
			else {
				user.setInventory(player.getInventory());
				setPortalInventory(player);
			}
			if (regionFrom.getBoolean(Setting.DELETE_ON_EXITENTRANCE) || regionTo.getBoolean(Setting.DELETE_ON_EXITENTRANCE))
				deletePortals(user);
		}
    }
    
    public void deletePortals(User user)
    {
    	if (user.getBluePortal() != null) user.getBluePortal().delete();
		if (user.getOrangePortal() != null) user.getOrangePortal().delete();
    }
        
    public Boolean placeEmancipationGrill(Block b, Player player)
    {
    	
    	Region region = Config.getRegion(b.getLocation());
    	int x = 0;
    	int z = 0;
    	if (b.getTypeId() == region.getInt(Setting.GRILL_MATERIAL))
    	{
    		if (b.getRelative(1,1,0).getTypeId() == region.getInt(Setting.GRILL_MATERIAL))
    			x = 1;
    		else if (b.getRelative(-1,1,0).getTypeId() == region.getInt(Setting.GRILL_MATERIAL))
    			x = -1;
    		else if (b.getRelative(0,1,1).getTypeId() == region.getInt(Setting.GRILL_MATERIAL))
    			z = 1;
    		else if (b.getRelative(0,1,-1).getTypeId() == region.getInt(Setting.GRILL_MATERIAL))
    			z = -1;
    		else
    			return false;
    	}
    	else
    		return false;
    	HashSet<Block> border = new HashSet<Block>();
    	HashSet<Block> inside = new HashSet<Block>();
    	
    	
    	border.add(b);
    	border.add(b.getRelative(x*1, 1, z*1));
    	border.add(b.getRelative(x*1, 2, z*1));
    	border.add(b.getRelative(0, 3, 0));
    	border.add(b.getRelative(x*-1, 3, z*-1));
    	border.add(b.getRelative(x*-2, 1, z*-2));
    	border.add(b.getRelative(x*-2, 2, z*-2));
    	border.add(b.getRelative(x*-1, 0, z*-1));
    	
    	inside.add(b.getRelative(0,1,0));
    	inside.add(b.getRelative(0,2,0));
    	inside.add(b.getRelative(x*-1,1,z*-1));
    	inside.add(b.getRelative(x*-1,2,z*-1));
    	
    	for (Block block: border)
    	{
    		if (block.getTypeId() != region.getInt(Setting.GRILL_MATERIAL))
    		{
    			return false;
    		}
    	}

    	for (Block block: inside)
    	{
    		if (block.getType() != Material.AIR)
    		{
    			return false;
    		}
    	}
    	
    	if (!Config.EnabledWorlds.contains(player.getLocation().getWorld().getName()))
		{
			Util.sendMessage(player, Config.MessageRestrictedWorld);
			return false;
		}
    	    	
    	Grill grill = new Grill(border, inside);
    	grills.add(grill);
    	grill.create();
    	
    	return true;
    	
    }
    
}
		    
