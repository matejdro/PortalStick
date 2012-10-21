package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Wool;

import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class PortalManager {
	private final PortalStick plugin;
	
	PortalManager(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public final HashSet<Portal> portals = new HashSet<Portal>();
	//public static HashMap<Chunk, HashMap<Location, String>> oldportals = new HashMap<Chunk, HashMap<Location, String> >(); //Some preparation for unloaded chunk fix
	public final HashMap<Location, Portal> borderBlocks = new HashMap<Location, Portal>();
	public final HashMap<Location, Portal> behindBlocks = new HashMap<Location, Portal>();
	public final HashMap<Location, Portal> insideBlocks = new HashMap<Location, Portal>();
	final HashMap<Location, Portal> awayBlocksGeneral = new HashMap<Location, Portal>();
	final HashMap<Location, Portal> awayBlocksX = new HashMap<Location, Portal>();
	final HashMap<Location, Portal> awayBlocksY = new HashMap<Location, Portal>();
	final HashMap<Location, Portal> awayBlocksZ = new HashMap<Location, Portal>();

	public void checkPlayerMove(Player player, Region regionFrom, Region regionTo)
	{
		User user = plugin.userManager.getUser(player);
		if (user == null) return;
		if (user.usingTool) return;
		if (!regionTo.name.equals(regionFrom.name)) {
			if (regionFrom.getBoolean(RegionSetting.DELETE_ON_EXITENTRANCE) || regionTo.getBoolean(RegionSetting.DELETE_ON_EXITENTRANCE))
				deletePortals(user);
			plugin.userManager.deleteDroppedItems(player);

			if (regionFrom.getBoolean(RegionSetting.UNIQUE_INVENTORY) || regionTo.getBoolean(RegionSetting.UNIQUE_INVENTORY))
			{
				if (regionTo.name.equalsIgnoreCase("global"))
					user.revertInventory(player);
				else {
					user.saveInventory(player);
					setPortalInventory(player, regionTo);
				}
			}
			
		}
	}
	
	public void deleteAll()
	{
		for (Portal p : portals.toArray(new Portal[0]))
			p.delete();
	}

	private boolean checkPortal(PortalCoord portal)
	{
		for (Block b: portal.border)
		{
			Region region = plugin.regionManager.getRegion(b.getLocation());
			if ((!borderBlocks.containsKey(b.getLocation()) && !insideBlocks.containsKey(b.getLocation()) && !behindBlocks.containsKey(b.getLocation())) && (region.getList(RegionSetting.TRANSPARENT_BLOCKS).contains(b.getTypeId()) || (!region.getBoolean(RegionSetting.ALL_BLOCKS_PORTAL) && !region.getList(RegionSetting.PORTAL_BLOCKS).contains(b.getTypeId()))))
			{
				return false;
			}
		}
		for (Block b: portal.inside)
		{
			Region region = plugin.regionManager.getRegion(b.getLocation());
			if ((!borderBlocks.containsKey(b.getLocation()) && !insideBlocks.containsKey(b.getLocation()) && !behindBlocks.containsKey(b.getLocation())) && (region.getList(RegionSetting.TRANSPARENT_BLOCKS).contains(b.getTypeId()) || (!region.getBoolean(RegionSetting.ALL_BLOCKS_PORTAL) && !region.getList(RegionSetting.PORTAL_BLOCKS).contains(b.getTypeId()))))
			{
				return false;
			}
		}
		return true;
	}

	public void deletePortals(User user)
	{
		if (user.bluePortal != null) user.bluePortal.delete();
		if (user.orangePortal != null) user.orangePortal.delete();
	}

	private PortalCoord generateHorizontalPortal(Block block, BlockFace face)
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

	private PortalCoord generatePortal(Block block, BlockFace face)
	{
		PortalCoord portal = new PortalCoord();
		portal.block = block;
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
	    		portal.tpFace = BlockFace.UP;
	    	}
	    	else
	    	{
	    		portal.destLoc = block.getRelative(0,1,0).getLocation();
				portal.behind.add(block.getRelative(0,-1,0));

	    		portal.tpFace = BlockFace.DOWN;
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

	public boolean placePortal(Block block, BlockFace face, Player player, Boolean orange, Boolean end)
	{   
		//Check if player can place here
		Region region = plugin.regionManager.getRegion(block.getLocation());
		if (region.getBoolean(RegionSetting.CHECK_WORLDGUARD) && plugin.worldGuard != null && !plugin.worldGuard.canBuild(player, block))
			return false;
		if (!region.getBoolean(RegionSetting.ENABLE_PORTALS))
			return false;
		if (!Permission.placePortal(player))
			return false;
	
		Boolean vertical = false;
		
		PortalCoord portalc = new PortalCoord();
		
		User owner = plugin.userManager.getUser(player);
				    	
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
	
		
		Portal portal = new Portal(plugin, portalc.destLoc, portalc.block, portalc.border, portalc.inside, portalc.behind, owner, orange, vertical, portalc.tpFace);
		
		
		if (orange)
		{
			if (owner.orangePortal != null) owner.orangePortal.delete();
			owner.orangePortal = portal;
			Util.PlaySound(Sound.PORTAL_CREATE_ORANGE, player, block.getLocation());
			
		}
		else
		{
			if (owner.bluePortal != null) owner.bluePortal.delete();
			owner.bluePortal = portal;
			Util.PlaySound(Sound.PORTAL_CREATE_BLUE, player, block.getLocation());
		}
		
		portals.add(portal);
		region.portals.add(portal);
		portal.create();
		
		return true;
		
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

	public void setPortalInventory(Player player, Region region)
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
	
	public void tryPlacingAutomatedPortal(Block b)
	{
		//Check if wool is correct
		Wool wool = (Wool) Material.WOOL.getNewData(b.getData());
		if (wool.getColor() != DyeColor.BLACK && wool.getColor() != DyeColor.BLUE && wool.getColor() != DyeColor.ORANGE) return;
		
		//Check for first iron bar
		Block firstIronBar = null;
		for (int i = 0; i < 6; i++)
		 {
			 if (b.getRelative(BlockFace.values()[i], 2).getType() == Material.IRON_FENCE)
			 {
				firstIronBar = b.getRelative(BlockFace.values()[i], 2);
				break;
			 }
			 else if (b.getRelative(BlockFace.values()[i]).getType() == Material.IRON_FENCE)
			 {
				firstIronBar = b.getRelative(BlockFace.values()[i]);
				break;
			 }
		 }
		if (firstIronBar == null) return;
		
		//Find other iron bars at same side of portal generator
		ArrayList<Block> ironBars = new ArrayList<Block>();
		
		for (int i = 0; i < 6; i++)
		 {
			BlockFace face = BlockFace.values()[i];
			 if (firstIronBar.getRelative(face).getType() == Material.IRON_FENCE)
			 {
				 while (firstIronBar.getRelative(face).getType() == Material.IRON_FENCE)
				 {
					 firstIronBar = firstIronBar.getRelative(face);
				 }
				 
				//firstIronBar.setType(Material.WOOD);
				 ironBars.add(firstIronBar);
				 
				 int counter = 1;
				 while (firstIronBar.getRelative(face.getOppositeFace(), counter).getType() == Material.IRON_FENCE)
				 {
					 ironBars.add(firstIronBar.getRelative(face.getOppositeFace(), counter));
					 counter++;
				 }

				 
				 break;
			 }
		 }

		//Find, in which direction is other side of portal generator
		int size = Config.CompactPortal ? 2 : 4; // How far is another side of portal generator
		BlockFace otherSide = null;
		for (int i = 0; i < 6; i++)
		 {
			BlockFace face = BlockFace.values()[i];
			if (firstIronBar.getRelative(face, size).getType() == Material.IRON_FENCE)
			{
				otherSide = face;
				break;
			}
		 }
		if (otherSide == null) return;
		
		//Search for iron bars on other side of portal generator
		for (Block ironBar : ironBars.toArray(new Block[0]))
		{
			if (ironBar.getRelative(otherSide, size).getType() == Material.IRON_FENCE)
				//ironBar.setType(Material.WOOD);
				ironBars.add(ironBar.getRelative(otherSide, size));
		}
				
		BlockFace portalFace = null;
		Portal oldPortal = null;
		//Find, where portal surface is
		for (int i = 0; i < 6; i++)
		{
			BlockFace face2 = BlockFace.values()[i];
			if (face2 == otherSide || face2.getOppositeFace() == otherSide) continue;
			Block firstPortalBlock = firstIronBar.getRelative(otherSide).getRelative(face2);
			if (firstPortalBlock.getType() == Material.STONE)
			{
				portalFace = face2;
				break;
			}
			else if (borderBlocks.containsKey(firstPortalBlock.getLocation()) || insideBlocks.containsKey(firstPortalBlock.getLocation()))
			{
				oldPortal = borderBlocks.get(firstPortalBlock.getLocation());
				if (oldPortal == null) oldPortal = insideBlocks.get(firstPortalBlock.getLocation());
				portalFace = face2;
				break;

			}
		}
		
		if (portalFace == null) return;
				
		//Is portal generator right size?
		if ((!Config.CompactPortal &&
		(((portalFace == BlockFace.UP || portalFace == BlockFace.DOWN) && ironBars.size() != 6 ) ||
		(portalFace != BlockFace.UP && portalFace != BlockFace.DOWN && ironBars.size() != 8 ))) ||
		(Config.CompactPortal && 
		(((portalFace == BlockFace.UP || portalFace == BlockFace.DOWN) && ironBars.size() != 2 ) ||
		(portalFace != BlockFace.UP && portalFace != BlockFace.DOWN && ironBars.size() != 4 ))))
			return;
		if (wool.getColor() == DyeColor.BLACK)
		{
			if (oldPortal != null) oldPortal.delete();
			return;
		}
			
				
		//Check if portal is big enough and start making a portal
		PortalCoord portalc = new PortalCoord();
		for (int i = 0; i < ironBars.size() / 2; i++)
		{
			portalc.border.add(ironBars.get(i).getRelative(portalFace).getRelative(otherSide, 1));
			portalc.border.add(ironBars.get(i).getRelative(portalFace).getRelative(otherSide, 3));
			
			if (i == 0 || i == (ironBars.size() / 2) - 1)
				portalc.border.add(ironBars.get(i).getRelative(portalFace).getRelative(otherSide, 2));
			else
				portalc.inside.add(ironBars.get(i).getRelative(portalFace).getRelative(otherSide, 2));
		}
		
		portalc.vertical = portalFace == BlockFace.UP || portalFace == BlockFace.DOWN;
		portalc.block = portalc.inside.toArray(new Block[0])[0];
		
		if (portalc.border.size() == 0 || portalc.inside.size() == 0) return;
		for (Block tb : portalc.border)
			if (!(tb.getType() == Material.STONE || borderBlocks.containsKey(tb.getLocation()) || insideBlocks.containsKey(tb.getLocation()))) return;
		for (Block tb : portalc.inside)
			if (!(tb.getType() == Material.STONE || borderBlocks.containsKey(tb.getLocation()) || insideBlocks.containsKey(tb.getLocation()))) return;
				
		
		if (portalc.vertical) portalc.destLoc = portalc.inside.toArray(new Block[0])[0].getRelative(portalFace.getOppositeFace()).getLocation();
		else
		{
			//Find lowest block inside horizontal portal
			int y = 200;
			Block lBlock = null;
			for (Block lb : portalc.inside)
				if (lBlock == null || lBlock.getY() < y) lBlock = lb;
					
			portalc.destLoc = lBlock.getRelative(portalFace.getOppositeFace()).getLocation();
		}
		portalc.destLoc.setX(portalc.destLoc.getX() + 0.5);
		portalc.destLoc.setZ(portalc.destLoc.getZ() + 0.5);
		
		portalc.tpFace = portalFace;
		
		if (oldPortal != null) oldPortal.delete();
		
		Boolean orange = wool.getColor() == DyeColor.ORANGE;
		Region region = plugin.regionManager.getRegion(b.getLocation());
		
		Portal portal = new Portal(plugin, portalc.destLoc, portalc.block, portalc.border, portalc.inside, portalc.behind, region, orange, portalc.vertical, portalc.tpFace);
		
		if (orange)
		{
			if (region.orangePortal != null) region.orangePortal.delete();
			region.orangePortal = portal;
		}
		else
		{
			if (region.bluePortal != null) region.bluePortal.delete();
			region.bluePortal = portal;
		}
		portals.add(portal);
		region.portals.add(portal);

		portal.create();
	}

}
