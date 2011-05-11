package com.matejdro.bukkit.portalstick;

import java.util.Collection;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class PortalManager {
	
	public static HashSet<Portal> portals = new HashSet<Portal>();

	public static void checkPlayerMove(Player player, Region regionFrom, Region regionTo)
	{
		User user = UserManager.getUser(player);
		if (user.getUsingTool()) return;
		if (!regionTo.Name.equals(regionFrom.Name)) {
			if (regionFrom.getBoolean(RegionSetting.DELETE_ON_EXITENTRANCE) || regionTo.getBoolean(RegionSetting.DELETE_ON_EXITENTRANCE))
				deletePortals(user);
			UserManager.deleteDroppedItems(player);

			if (regionTo.Name.equalsIgnoreCase("global"))
				user.revertInventory(player);
			else {
				user.saveInventory(player);
				setPortalInventory(player);
			}
		}
	}
	
	public static void deleteAll()
	{
		for (Portal p : portals.toArray(new Portal[0]))
			p.delete();
	}

	private static Boolean checkPortal(PortalCoord portal, Portal oldportal)
	{
		for (Block b: portal.border)
		{
			Region region = RegionManager.getRegion(b.getLocation());
			if ((!oldportal.getInside().contains(b) && !oldportal.getBorder().contains(b)) && (region.getList(RegionSetting.TRANSPARENT_BLOCKS).contains(b.getTypeId()) || (!region.getBoolean(RegionSetting.ALL_BLOCKS_PORTAL) && !region.getList(RegionSetting.PORTAL_BLOCKS).contains(b.getTypeId()))))
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
		HashSet<Integer> walkableblocks = new HashSet<Integer>();
		Region region = RegionManager.getRegion(portal.destloc);
		if (!region.getList(RegionSetting.TRANSPARENT_BLOCKS).contains(portal.destloc.getBlock().getTypeId()) && !region.getList(RegionSetting.TRANSPARENT_BLOCKS).contains(portal.destloc.getBlock().getRelative(BlockFace.UP).getTypeId())) return false;
		return true;
	}

	public static void deletePortals(User user)
	{
		if (user.getBluePortal() != null) user.getBluePortal().delete();
		if (user.getOrangePortal() != null) user.getOrangePortal().delete();
	}

	private static PortalCoord generateHorizontalPortal(Block block, BlockFace face, Portal oldportal)
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
		
		block = block.getRelative(0,-2,0);
		portal = generatePortal(block, face);
		if (checkPortal(portal, oldportal)) return portal;
		
		block = block.getRelative(0,2,0);
		portal = generatePortal(block, face);
		if (checkPortal(portal, oldportal)) return portal;

		
		if (!checkPortal(portal, oldportal)) portal.finished = true;
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
	    	
	    	portal.destloc = block.getRelative(z*1,-1,x*1).getLocation();
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

	public static void setPortalInventory(Player player)
	{
		PlayerInventory inv = player.getInventory();
		inv.clear();
		inv.clear(inv.getSize() + 1);
		inv.clear(inv.getSize() + 2);
		inv.clear(inv.getSize() + 3);
		inv.setItemInHand(new ItemStack(Config.PortalTool, 1));
	}

}
