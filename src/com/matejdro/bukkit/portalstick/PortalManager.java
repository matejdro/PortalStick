package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class PortalManager {
	
	public static HashSet<Portal> portals = new HashSet<Portal>();
	//public static HashMap<Chunk, HashMap<Location, String>> oldportals = new HashMap<Chunk, HashMap<Location, String> >(); //Some preparation for unloaded chunk fix
	public static HashMap<Location, Portal> borderBlocks = new HashMap<Location, Portal>();
	public static HashMap<Location, Portal> behindBlocks = new HashMap<Location, Portal>();
	public static HashMap<Location, Portal> insideBlocks = new HashMap<Location, Portal>();
	public static HashMap<Location, Portal> awayBlocksGeneral = new HashMap<Location, Portal>();
	public static HashMap<Location, Portal> awayBlocksX = new HashMap<Location, Portal>();
	public static HashMap<Location, Portal> awayBlocksY = new HashMap<Location, Portal>();
	public static HashMap<Location, Portal> awayBlocksZ = new HashMap<Location, Portal>();
	public static HashMap<Location, Vector> vectors = new HashMap<Location, Vector>();

	public static void checkPlayerMove(Player player, Region regionFrom, Region regionTo)
	{
		User user = UserManager.getUser(player);
		if (user == null) return;
		if (user.getUsingTool()) return;
		if (!regionTo.Name.equals(regionFrom.Name)) {
			if (regionFrom.getBoolean(RegionSetting.DELETE_ON_EXITENTRANCE) || regionTo.getBoolean(RegionSetting.DELETE_ON_EXITENTRANCE))
				deletePortals(user);
			UserManager.deleteDroppedItems(player);

			if (regionFrom.getBoolean(RegionSetting.UNIQUE_INVENTORY) || regionTo.getBoolean(RegionSetting.UNIQUE_INVENTORY))
			{
				if (regionTo.Name.equalsIgnoreCase("global"))
					user.revertInventory(player);
				else {
					user.saveInventory(player);
					setPortalInventory(player, regionTo);
				}
			}
			
		}
	}
	
	public static void deleteAll()
	{
		for (Portal p : portals.toArray(new Portal[0]))
			p.delete();
	}

	private static Boolean checkPortal(PortalCoord portal)
	{
		for (Block b: portal.border)
		{
			Region region = RegionManager.getRegion(b.getLocation());
			if ((!borderBlocks.containsKey(b.getLocation()) && !insideBlocks.containsKey(b.getLocation()) && !behindBlocks.containsKey(b.getLocation())) && (region.getList(RegionSetting.TRANSPARENT_BLOCKS).contains(b.getTypeId()) || (!region.getBoolean(RegionSetting.ALL_BLOCKS_PORTAL) && !region.getList(RegionSetting.PORTAL_BLOCKS).contains(b.getTypeId()))))
			{
				return false;
			}
		}
		for (Block b: portal.inside)
		{
			Region region = RegionManager.getRegion(b.getLocation());
			if ((!borderBlocks.containsKey(b.getLocation()) && !insideBlocks.containsKey(b.getLocation()) && !behindBlocks.containsKey(b.getLocation())) && (region.getList(RegionSetting.TRANSPARENT_BLOCKS).contains(b.getTypeId()) || (!region.getBoolean(RegionSetting.ALL_BLOCKS_PORTAL) && !region.getList(RegionSetting.PORTAL_BLOCKS).contains(b.getTypeId()))))
			{
				return false;
			}
		}
		return true;
	}

	public static void deletePortals(User user)
	{
		if (user.getBluePortal() != null) user.getBluePortal().delete();
		if (user.getOrangePortal() != null) user.getOrangePortal().delete();
	}

	private static PortalCoord generateHorizontalPortal(Block block, BlockFace face)
	{
		PortalCoord portal = generatePortal(block, face);
		if (checkPortal(portal)) return portal;
		
		block = block.getRelative(0,0,0);
		portal = generatePortal(block, face);
		if (checkPortal(portal)) return portal;
		
		block = block.getRelative(0,1,0);
		portal = generatePortal(block, face);
		if (checkPortal(portal)) return portal;
		
		block = block.getRelative(0,-1,0);
		portal = generatePortal(block, face);
		if (checkPortal(portal)) return portal;
		
		block = block.getRelative(0,-2,0);
		portal = generatePortal(block, face);
		if (checkPortal(portal)) return portal;
		
		block = block.getRelative(0,2,0);
		portal = generatePortal(block, face);
		if (checkPortal(portal)) return portal;

		
		if (!checkPortal(portal)) portal.finished = true;
			return portal;
		
	}

	private static PortalCoord generatePortal(Block block, BlockFace face)
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
			
	
			if (Config.FillPortalBack < 0 || !Config.CompactPortal) portal.border.add(block.getRelative(1,0,0));
	
			
			portal.inside.add(block);
	    	
	    	if (face == BlockFace.DOWN)
	    	{
	    		portal.destLoc = block.getRelative(0,-1,0).getLocation();
				portal.behind.add(block.getRelative(0,1,0));
	    		portal.tpFace = BlockFace.DOWN;
	    	}
	    	else
	    	{
	    		portal.destLoc = block.getRelative(0,2,0).getLocation();
				portal.behind.add(block.getRelative(0,-1,0));

	    		portal.tpFace = BlockFace.UP;
	    	}
	    portal.vertical = true;		
		}
		else
		{
			int x = 0;
	    	int z = 0;
	    	switch(face)
	    	{
	    	case NORTH:
	    		z = -1;
	    		portal.tpFace = BlockFace.SOUTH;
	    		break;
	    	case EAST:
	    		x = -1;
	    		portal.tpFace = BlockFace.WEST;
	    		break;
	    	case SOUTH:
	    		z = 1;
	    		portal.tpFace = BlockFace.NORTH;
	    		break;
	    	case WEST:
	    		x = 1;
	    		portal.tpFace = BlockFace.EAST;
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
	    	if (Config.FillPortalBack < 0 || !Config.CompactPortal) portal.border.add(block.getRelative(0,-2,0));
	
	    	portal.inside.add(block);
	    	portal.inside.add(block.getRelative(0,-1,0));
	    	
	    	portal.destLoc = block.getRelative(z*1,-1,x*1).getLocation();
	    	portal.vertical = false;
	    	
	    	portal.behind.add(block.getRelative(z*-1,-1,x*-1));
	    	portal.behind.add(block.getRelative(z*-1,0,x*-1));
	       	}
	
		return portal;
	}

	public static Boolean placePortal(Block block, BlockFace face, Player player, Boolean orange, Boolean end)
	{   
		//Check if player can place here
		Region region = RegionManager.getRegion(block.getLocation());
		if (region.getBoolean(RegionSetting.CHECK_WORLDGUARD) && PortalStick.worldGuard != null && !PortalStick.worldGuard.canBuild(player, block))
			return false;
		if (!region.getBoolean(RegionSetting.ENABLE_PORTALS))
			return false;
		if (!Permission.placePortal(player))
			return false;
	
		Boolean vertical = false;
		
		PortalCoord portalc = new PortalCoord();
		
		User owner = UserManager.getUser(player);
				    	
		if (face == BlockFace.DOWN || face == BlockFace.UP)
		{
			vertical = true;
			portalc = generatePortal(block, face);
			if (!checkPortal(portalc))
			{
				if (end) Util.sendMessage(player, Config.MessageCannotPlacePortal);
				Util.PlaySound(Sound.PORTAL_CANNOT_CREATE, player, block.getLocation());
				return false;
			}
		}
		else
		{
			portalc = generateHorizontalPortal(block, face);
			if (portalc.finished)
			{
				if (end) Util.sendMessage(player, Config.MessageCannotPlacePortal);
				Util.PlaySound(Sound.PORTAL_CANNOT_CREATE, player, block.getLocation());
				return false;
			}
		}
		
		portalc.destLoc.setX(portalc.destLoc.getX() + 0.5);
		portalc.destLoc.setZ(portalc.destLoc.getZ() + 0.5);
	
		
		Portal portal = new Portal(portalc.destLoc, portalc.border, portalc.inside, portalc.behind, owner, orange, vertical, portalc.tpFace);
		
		
		if (orange)
		{
			if (owner.getOrangePortal() != null) owner.getOrangePortal().delete();
			owner.setOrangePortal(portal);
			Util.PlaySound(Sound.PORTAL_CREATE_ORANGE, player, block.getLocation());
			
		}
		else
		{
			if (owner.getBluePortal() != null) owner.getBluePortal().delete();
			owner.setBluePortal(portal);
			Util.PlaySound(Sound.PORTAL_CREATE_BLUE, player, block.getLocation());
		}
		
		portals.add(portal);
		portal.create();
		
		return true;
		
	}

	public static void placePortal(Block block, Player player, Boolean orange)
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

	public static void setPortalInventory(Player player, Region region)
	{
		PlayerInventory inv = player.getInventory();
		for (int i = 0; i < 40; i++)
		{
			
			ItemStack item = inv.getItem(i);
			if (item == null || item.getTypeId() == 0) continue;
			
			Boolean keep = false;
			for (Object is : region.getList(RegionSetting.GRILL_INVENTORY_CLEAR_EXCEPTIONS))
			{
				ItemStack itemcheck = Util.getItemData((String) is);
				if (item.getTypeId() == itemcheck.getTypeId())
				{
					keep = true;
					break;
				}
			}
			if (!keep) inv.clear(i);
		}
		
		
		for (Object is : region.getList(RegionSetting.UNIQUE_INVENTORY_ITEMS))
		{
			ItemStack item = Util.getItemData((String) is);
			if (item.getTypeId() == Config.PortalTool)
				inv.setItemInHand(item);
			else
				inv.addItem(item);
		}
	}

}
