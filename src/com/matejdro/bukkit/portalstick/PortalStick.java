package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class PortalStick extends JavaPlugin {
	public static Logger log = Logger.getLogger("Minecraft");

	private PortalStickPlayer PlayerListener;
	private PortalStickBlock BlockListener;
	private PortalStickVehicle VehicleListener;
	private InputOutput IO;
	
	public static HashMap<String, PortalStickUser> players = new HashMap<String, PortalStickUser>();
	public static HashSet<PortalStickPortal> portals = new HashSet<PortalStickPortal>();
	public static HashSet<PortalStickGrill> grills = new HashSet<PortalStickGrill>();
	
	public static PortalStick instance;
		
	public static Plugin permissions = null;

	public static WorldGuardPlugin worldGuard = null;

	@Override
	public void onDisable() {
		for (Object o: portals.toArray())
		{
			PortalStickPortal p = (PortalStickPortal) o;
			p.delete();
		}
		
		for (Object o: grills.toArray())
		{
			PortalStickGrill g = (PortalStickGrill) o;
			g.delete();
		}

	}

	@Override
	public void onEnable() {
		instance = this;
		
		PlayerListener = new PortalStickPlayer(this);
		BlockListener = new PortalStickBlock(this);
		VehicleListener = new PortalStickVehicle(this);
		IO = new InputOutput(this);
		
		IO.LoadSettings();
				
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, BlockListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BURN, BlockListener, Event.Priority.Low, this);
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, BlockListener, Event.Priority.Low, this);

		
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, PlayerListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, PlayerListener, Event.Priority.Low, this);	
		if (Settings.DeletePortalsOnLeave)
			getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, PlayerListener, Event.Priority.Low, this);		

		
		if (Settings.TeleportVehicles)
			getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_MOVE, VehicleListener, Event.Priority.Low, this);
		
		if (Settings.EnableMaterialEmancipationGrill)
		{
			getServer().getPluginManager().registerEvent(Event.Type.PLAYER_DROP_ITEM, PlayerListener, Event.Priority.Low, this);
			getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PHYSICS, BlockListener, Event.Priority.Low, this);
			getServer().getPluginManager().registerEvent(Event.Type.BLOCK_IGNITE, BlockListener, Event.Priority.Low, this);
		}

		permissions = this.getServer().getPluginManager().getPlugin("Permissions");
		worldGuard = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");

	}
			
    public static Boolean permission(Player player, String line, Boolean def)
    {
    	
    	    if(permissions != null && player != null) {
    	    	return (((Permissions) permissions).getHandler()).has(player, line);
    	    } else {
    	    	return def;
    	    }
    }
    
    public void PlacePortal(Block block, Player player, Boolean orange)
    {
    	if (Settings.CheckWorldGuard && worldGuard != null && !worldGuard.canBuild(player, block)) return;
		 if (!permission(player, "portalstick.placeportal", true)) return;

    	
    	float dir = (float)Math.toDegrees(Math.atan2(player.getLocation().getBlockX() - block.getX(), block.getZ() - player.getLocation().getBlockZ()));
    	dir = dir % 360;
        if(dir < 0)
        	dir += 360;
        
    	//Try WEST/EAST
    	if (dir < 90 || dir > 270)
    	{
    		if (PlacePortal(block, BlockFace.EAST, player, orange, false)) return;
    	}
    	else
    	{
    		if (PlacePortal(block, BlockFace.WEST, player, orange, false)) return;
    	}
    	
    	//Try NORTH/SOUTH
    	if (dir < 180) 
    	{
    		if (PlacePortal(block, BlockFace.SOUTH, player, orange, false)) return;
    	}
    	else
    	{
    		if (PlacePortal(block, BlockFace.NORTH, player, orange, false)) return;
    	}
    	
    	//Try UP/DOWN
    	if (player.getEyeLocation().getY() >= block.getLocation().getY() )
    	{
    		if (PlacePortal(block, BlockFace.UP, player, orange, false)) return;
    	}
    	else
    	{
    		if (PlacePortal(block, BlockFace.DOWN, player, orange, true)) return;
    	}
    
     }
    
    public Boolean PlacePortal(Block block,BlockFace face, Player player, Boolean orange, Boolean end)
    {    	
    	if (Settings.CheckWorldGuard && worldGuard != null && !worldGuard.canBuild(player, block)) return false;
		 if (!permission(player, "portalstick.placeportal", true)) return false;

    	Boolean vertical = false;;
    	
    	if (!players.containsKey(player.getName())) 
    		{
    		PortalStickUser user = new PortalStickUser();
			players.put(player.getName(), user);
    		}
    	
    	PortalCoord portalc = new PortalCoord();
    	
    	PortalStickUser owner = players.get(player.getName());
    	
    	PortalStickPortal oldportal = orange ? owner.getOrangePortal() : owner.getBluePortal();
    	if (oldportal == null) oldportal = new PortalStickPortal();
    	    	

    	    
    	if (face == BlockFace.DOWN || face == BlockFace.UP)
    	{
    		vertical = true;
    		portalc = generatePortal(block, face);
    		if (!checkportal(portalc, oldportal))
    		{
    			if (end) player.sendMessage(Settings.MessagePortalCannotCreate);
    			return false;
    		}
    		
    	}
    	else
    	{
    		portalc = generateHorizontalPortal(block, face, oldportal);
    		if (portalc.finished)
    		{
    			if (end) player.sendMessage(Settings.MessagePortalCannotCreate);
    			return false;
    		}
    	}
    	
    	portalc.destloc.setX(portalc.destloc.getX() + 0.5);
    	portalc.destloc.setZ(portalc.destloc.getZ() + 0.5);

    	
    	PortalStickPortal portal = new PortalStickPortal(portalc.destloc, portalc.border, portalc.inside, owner, orange, vertical, portalc.tpface);
    	
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
    
    private PortalCoord generateHorizontalPortal(Block block, BlockFace face, PortalStickPortal oldportal)
    {
    	PortalCoord portal = generatePortal(block, face);
    	if (checkportal(portal, oldportal)) return portal;
    	
    	block = block.getRelative(0,0,0);
    	portal = generatePortal(block, face);
    	if (checkportal(portal, oldportal)) return portal;
    	
    	block = block.getRelative(0,1,0);
    	portal = generatePortal(block, face);
    	if (checkportal(portal, oldportal)) return portal;
    	
    	block = block.getRelative(0,-1,0);
    	portal = generatePortal(block, face);
    	if (checkportal(portal, oldportal)) return portal;
    	
    	if (!checkportal(portal, oldportal)) portal.finished = true;
			return portal;
 
    	
    }
    
    private PortalCoord generatePortal(Block block, BlockFace face)
    {
    	PortalCoord portal = new PortalCoord();
    	if (face == BlockFace.DOWN || face == BlockFace.UP)
    	{
    		if (!Settings.CompactPortal)
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
        	if (!Settings.CompactPortal)
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
    
    private Boolean checkportal(PortalCoord portal, PortalStickPortal oldportal)
    {
    	for (Block b: portal.border)
    	{
    		if (Settings.TransparentBlocks.contains(b.getTypeId()) || (!Settings.AnyBlockIsPortallable && !Settings.PortallableBlocks.contains(b.getTypeId() ) && !oldportal.getInside().contains(b) && !oldportal.getBorder().contains(b)))
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
        
    public Boolean PlaceEmancipationGrill(Block b, Player player)
    {
    	int x = 0;
    	int z = 0;
    	if (b.getTypeId() == Settings.MaterialEmancipationGrillFrameBlock)
    	{
    		if (b.getRelative(1,1,0).getTypeId() == Settings.MaterialEmancipationGrillFrameBlock)
    			x = 1;
    		else if (b.getRelative(-1,1,0).getTypeId() == Settings.MaterialEmancipationGrillFrameBlock)
    			x = -1;
    		else if (b.getRelative(0,1,1).getTypeId() == Settings.MaterialEmancipationGrillFrameBlock)
    			z = 1;
    		else if (b.getRelative(0,1,-1).getTypeId() == Settings.MaterialEmancipationGrillFrameBlock)
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
    		if (block.getTypeId() != Settings.MaterialEmancipationGrillFrameBlock)
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
    	
    	if (!Settings.EnabledWorlds.contains(player.getLocation().getWorld().getName()))
		{
			player.sendMessage(Settings.MessageRestrictedWorld);
			return false;
		}
    	    	
    	PortalStickGrill grill = new PortalStickGrill(border, inside);
    	grills.add(grill);
    	grill.create();
    	
    	return true;
    		
    	
    }
    
}
		    
