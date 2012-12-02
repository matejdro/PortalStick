package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;

import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.BlockHolder;
import de.V10lator.PortalStick.V10Location;

public class PortalManager {
	private final PortalStick plugin;
	
	PortalManager(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public final HashSet<Portal> portals = new HashSet<Portal>();
	public final HashMap<V10Location, Portal> borderBlocks = new HashMap<V10Location, Portal>();
	public final HashMap<V10Location, Portal> behindBlocks = new HashMap<V10Location, Portal>();
	public final HashMap<V10Location, Portal> insideBlocks = new HashMap<V10Location, Portal>();
	final HashMap<V10Location, Portal> awayBlocks = new HashMap<V10Location, Portal>();
	final HashMap<V10Location, Portal> awayBlocksY = new HashMap<V10Location, Portal>();
	public final HashMap<V10Location, BlockHolder> oldBlocks = new HashMap<V10Location, BlockHolder>();

	public void checkEntityMove(Entity e, Region regionFrom, Region regionTo)
	{
	  if(!(e instanceof InventoryHolder))
		return;
	  
	  InventoryHolder ih = (InventoryHolder)e;
	  User user = plugin.userManager.getUser(e);
	  
	  if (user == null || user.usingTool)
		return;
	  if (!regionTo.name.equals(regionFrom.name)) {
		if(ih instanceof Player && (regionFrom.getBoolean(RegionSetting.DELETE_ON_EXITENTRANCE) || regionTo.getBoolean(RegionSetting.DELETE_ON_EXITENTRANCE)))
		  deletePortals(user);
		
		if (regionFrom.getBoolean(RegionSetting.UNIQUE_INVENTORY) || regionTo.getBoolean(RegionSetting.UNIQUE_INVENTORY))
		{
		  if (regionTo.name.equalsIgnoreCase("global"))
			user.revertInventory(ih);
		  else
		  {
			user.saveInventory(ih);
			setPortalInventory(ih, regionTo);
		  }
		}
	  }
	}
	
	private boolean checkPortal(PortalCoord portal)
	{
		Region region;
		int id;
		ArrayList<Portal> overlap = new ArrayList<Portal>();
		boolean ol;
		BlockHolder bh;
		Block block;
		for (V10Location loc: portal.border)
		{
			if(borderBlocks.containsKey(loc))
			{
			  overlap.add(borderBlocks.get(loc));
			  ol = true;
			}
			else if(insideBlocks.containsKey(loc))
			{
			  overlap.add(insideBlocks.get(loc));
			  ol = true;
			}
			else if(behindBlocks.containsKey(loc))
			{
			  overlap.add(behindBlocks.get(loc));
			  ol = false;
			}
			else
			  ol = false;
			
			if(!ol)
			{
			  block = loc.getHandle().getBlock();
			  id = block.getTypeId();
			  region = plugin.regionManager.getRegion(loc);
			  if(!region.getBoolean(RegionSetting.ALL_BLOCKS_PORTAL))
			  {
				bh = new BlockHolder(block);
				if(plugin.gelManager.gelMap.containsKey(bh))
				{
				  bh = plugin.gelManager.gelMap.get(bh);
				  id = bh.id;
				}
				if(!region.getList(RegionSetting.PORTAL_BLOCKS).contains(id))
				  return false;
			  }
			}
		}
		for (V10Location loc: portal.inside)
		{
			if(loc == null)
			  continue;
			if(borderBlocks.containsKey(loc))
			{
			  overlap.add(borderBlocks.get(loc));
			  ol = true;
			}
			else if(insideBlocks.containsKey(loc))
			{
			  overlap.add(insideBlocks.get(loc));
			  ol = true;
			}
			else if(behindBlocks.containsKey(loc))
			{
			  overlap.add(behindBlocks.get(loc));
			  ol = false;
			}
			else
			  ol = false;
			
			if(!ol)
			{
			  block = loc.getHandle().getBlock();
			  id = block.getTypeId();
			  region = plugin.regionManager.getRegion(loc);
			  if(!region.getBoolean(RegionSetting.ALL_BLOCKS_PORTAL))
			  {
				bh = new BlockHolder(block);
				if(plugin.gelManager.gelMap.containsKey(bh))
				{
				  bh = plugin.gelManager.gelMap.get(bh);
				  id = bh.id;
				}
				if(!region.getList(RegionSetting.PORTAL_BLOCKS).contains(id))
				  return false;
			  }
			}
		}
		for(Portal p: overlap)
		  p.delete();
		return true;
	}

	public void deletePortals(User user)
	{
		if (user.bluePortal != null) user.bluePortal.delete();
		if (user.orangePortal != null) user.orangePortal.delete();
	}

	private PortalCoord generateHorizontalPortal(V10Location block, BlockFace face)
	{
		PortalCoord portal = generatePortal(block, face); // 0
		if(!checkPortal(portal))
		{
		  block = new V10Location(block.getHandle().getBlock().getRelative(BlockFace.DOWN)); // -1
		  portal = generatePortal(block, face);
		  if(!checkPortal(portal))
		  {
			block = new V10Location(block.getHandle().getBlock().getRelative(BlockFace.DOWN)); // -2 TODO: Doesn't work
			portal = generatePortal(block, face);
			if(!checkPortal(portal))
			{
			  block = new V10Location(block.getHandle().getBlock().getRelative(BlockFace.UP, 3)); // 1 (-2 + 3)
			  portal = generatePortal(block, face);
			  if(!checkPortal(portal))
			  {
				block = new V10Location(block.getHandle().getBlock().getRelative(BlockFace.UP)); // 2
				portal = generatePortal(block, face);
				if(!checkPortal(portal))
				  portal.finished = true;
			  }
			}
		  }
		}
		return portal;
	}

	private PortalCoord generatePortal(V10Location block, BlockFace face)
	{
		PortalCoord portal = new PortalCoord();
		portal.block = block;
		Block rb = block.getHandle().getBlock();
		
		switch(face)
		{
		  case DOWN:
		  case UP:
			if (!plugin.config.CompactPortal || plugin.config.FillPortalBack < 0)
			{
				portal.border.add(new V10Location(rb.getRelative(BlockFace.NORTH)));
				if(!plugin.config.CompactPortal)
				{
				  portal.border.add(new V10Location(rb.getRelative(BlockFace.NORTH_WEST))); 
				  portal.border.add(new V10Location(rb.getRelative(BlockFace.WEST)));
				  portal.border.add(new V10Location(rb.getRelative(BlockFace.SOUTH_WEST)));
				  portal.border.add(new V10Location(rb.getRelative(BlockFace.SOUTH)));
				  portal.border.add(new V10Location(rb.getRelative(BlockFace.SOUTH_EAST)));
				  portal.border.add(new V10Location(rb.getRelative(BlockFace.EAST)));
				  portal.border.add(new V10Location(rb.getRelative(BlockFace.NORTH_EAST)));
				}
			}
			
			portal.inside[0] = new V10Location(rb);
	    	
	    	portal.destLoc[0] = new V10Location(rb.getRelative(face));
	    	face = face.getOppositeFace();
			portal.behind[0] = new V10Location(rb.getRelative(face));
	    	portal.tpFace = face;
	    	portal.vertical = true;
	    	return portal;
		  case NORTH:
		  case NORTH_EAST:
			face = BlockFace.SOUTH;
			break;
		  case EAST:
		  case SOUTH_EAST:
		    face = BlockFace.WEST;
		    break;
		  case SOUTH:
		  case SOUTH_WEST:
	    	face = BlockFace.NORTH;
	    	break;
		  default:
	    	face = BlockFace.EAST;
	    	break;
		}
	    
	    portal.tpFace = face;
	    
	    switch(face)
	    {
	      case NORTH:
	      case SOUTH:
	    	face = BlockFace.EAST;
	    	break;
	      default:
	    	face = BlockFace.NORTH;
	    }
	    
	    if (!plugin.config.CompactPortal || plugin.config.FillPortalBack < 0)
	    {
	      Block block2 = rb.getRelative(BlockFace.DOWN, 2);
	      portal.border.add(new V10Location(block2));
	      
	      if(!plugin.config.CompactPortal)
	      {
	    	block2 = block2.getRelative(face);
	    	portal.border.add(new V10Location(block2));
	    	for(int i = 0; i < 3; i++)
		    {
	    	  block2 = block2.getRelative(BlockFace.UP);
	    	  portal.border.add(new V10Location(block2));
		    }
	    	face = face.getOppositeFace();
	    	for(int i = 0; i < 2; i++)
	    	{
	    	  block2 = block2.getRelative(face);
	    	  portal.border.add(new V10Location(block2));
	    	}
	    	for(int i = 0; i < 3; i++)
	    	{
	    	  block2 = block2.getRelative(BlockFace.DOWN);
	    	  portal.border.add(new V10Location(block2));
	    	}
	      }
	    }
	    
	    portal.inside[1] = block;
	    Block block2 = rb.getRelative(BlockFace.DOWN);
	    portal.inside[0] = new V10Location(block2);
	    
	    Block block3 = block2.getRelative(portal.tpFace.getOppositeFace());
	    portal.destLoc[0] = new V10Location(block3);
	    portal.destLoc[1] = new V10Location(block3.getRelative(BlockFace.UP));
	    
	    portal.vertical = false;
	    
	    block2 = block2.getRelative(portal.tpFace);
	    portal.behind[0] = new V10Location(block2);
	    portal.behind[1] = new V10Location(block2.getRelative(BlockFace.UP));
	    
		return portal;
	}

	public boolean placePortal(V10Location block, BlockFace face, Player player, boolean orange, boolean end)
	{
		//Check if player can place here
		Location loc = block.getHandle();
		Region region = plugin.regionManager.getRegion(block);
		if (region.getBoolean(RegionSetting.CHECK_WORLDGUARD) && plugin.worldGuard != null && !plugin.worldGuard.canBuild(player, loc))
			return false;
		if (!region.getBoolean(RegionSetting.ENABLE_PORTALS) || !plugin.hasPermission(player, plugin.PERM_PLACE_PORTAL))
			return false;
		
		boolean vertical = false;
		
		PortalCoord portalc;
		
		User owner = plugin.userManager.getUser(player);
		
		if (face == BlockFace.DOWN || face == BlockFace.UP)
		{
			vertical = true;
			portalc = generatePortal(block, face);
			if (!checkPortal(portalc))
			{
				if (end) plugin.util.sendMessage(player, plugin.i18n.getString("CannotPlacePortal", player.getName()));
				plugin.util.playSound(Sound.PORTAL_CANNOT_CREATE, block);
				return false;
			}
		}
		else
		{
			portalc = generateHorizontalPortal(block, face);
			if (portalc.finished)
			{
				if (end) plugin.util.sendMessage(player, plugin.i18n.getString("CannotPlacePortal", player.getName()));
				plugin.util.playSound(Sound.PORTAL_CANNOT_CREATE, block);
				return false;
			}
		}
		
		Portal portal = new Portal(plugin, portalc.destLoc, portalc.block, portalc.border, portalc.inside, portalc.behind, owner, orange, vertical, portalc.tpFace);
		
		
		if (orange)
		{
			if (owner.orangePortal != null)
			  owner.orangePortal.delete();
			owner.orangePortal = portal;
			plugin.util.playSound(Sound.PORTAL_CREATE_ORANGE, block);
			
		}
		else
		{
			if (owner.bluePortal != null)
			  owner.bluePortal.delete();
			owner.bluePortal = portal;
			plugin.util.playSound(Sound.PORTAL_CREATE_BLUE, block);
		}
		
		portals.add(portal);
		region.portals.add(portal);
		portal.create();
		
		return true;
		
	}

	public void placePortal(V10Location block, Player player, boolean orange)
	{
		
		float dir = (float)Math.toDegrees(Math.atan2(player.getLocation().getBlockX() - block.x, block.z - player.getLocation().getBlockZ()));
		dir = dir % 360;
	    if(dir < 0)
	    	dir += 360;
	    
		//Try WEST/EAST
		if (dir < 90 || dir > 270)
		{
			if (placePortal(block, BlockFace.EAST, player, orange, false))
			  return;
		}
		else if (placePortal(block, BlockFace.WEST, player, orange, false))
		  return;
		
		//Try NORTH/SOUTH
		if (dir < 180) 
		{
			if (placePortal(block, BlockFace.SOUTH, player, orange, false))
			  return;
		}
		else if (placePortal(block, BlockFace.NORTH, player, orange, false))
		  return;
		
		//Try UP/DOWN
		if (player.getEyeLocation().getY() >= block.y )
		{
			if (placePortal(block, BlockFace.UP, player, orange, false))
			  return;
		}
		else if (placePortal(block, BlockFace.DOWN, player, orange, true))
		  return;
	
	 }

	public void setPortalInventory(InventoryHolder ih, Region region)
	{
		if(region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
		{
		  ItemStack item;
		  Inventory inv = ih.getInventory();
		  for (Object is : region.getList(RegionSetting.UNIQUE_INVENTORY_ITEMS))
		  {
			item = plugin.util.getItemData((String)is);
			inv.addItem(item);
		  }
		}
	}
	
	public void tryPlacingAutomatedPortal(Block rb)
	{
		//Check if wool is correct
		Wool wool = (Wool) Material.WOOL.getNewData(rb.getData());
		if (wool.getColor() != DyeColor.BLACK && wool.getColor() != DyeColor.LIGHT_BLUE && wool.getColor() != DyeColor.ORANGE) return;
		//Check for first iron bar
		Block firstIronBar = null;
		for (int i = 0; i < 6; i++)
		 {
			 if (rb.getRelative(BlockFace.values()[i], 2).getType() == Material.IRON_FENCE)
			 {
				firstIronBar = rb.getRelative(BlockFace.values()[i], 2);
				break;
			 }
			 else if (rb.getRelative(BlockFace.values()[i]).getType() == Material.IRON_FENCE)
			 {
				firstIronBar = rb.getRelative(BlockFace.values()[i]);
				break;
			 }
		 }
		if (firstIronBar == null)
		  return;
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
		int size = plugin.config.CompactPortal ? 2 : 4; // How far is another side of portal generator
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
		if (otherSide == null)
		  return;
		//Search for iron bars on other side of portal generator
		for (Block ironBar : ironBars.toArray(new Block[0]))
		{
			if (ironBar.getRelative(otherSide, size).getType() == Material.IRON_FENCE)
				//ironBar.setType(Material.WOOD);
				ironBars.add(ironBar.getRelative(otherSide, size));
		}
				
		BlockFace portalFace = null;
		Portal oldPortal = null;
		Region region = plugin.regionManager.getRegion(new V10Location(rb));
		//Find, where portal surface is
		for (int i = 0; i < 6; i++)
		{
			BlockFace face2 = BlockFace.values()[i];
			if (face2 == otherSide || face2.getOppositeFace() == otherSide)
				continue;
			Block firstPortalBlock = firstIronBar.getRelative(otherSide).getRelative(face2);
			if (region.getBoolean(RegionSetting.ALL_BLOCKS_PORTAL) && region.getList(RegionSetting.PORTAL_BLOCKS).contains(firstPortalBlock.getTypeId()))
			{
				portalFace = face2;
				break;
			}
			else
			{
				V10Location loc = new V10Location(firstPortalBlock);
				if(borderBlocks.containsKey(loc))
					oldPortal = borderBlocks.get(loc);
				else
					oldPortal = insideBlocks.get(loc);
				portalFace = face2;
				break;

			}
		}
		
		if (portalFace == null)
		return;
		//Is portal generator right size?
		if ((!plugin.config.CompactPortal &&
		(((portalFace == BlockFace.UP || portalFace == BlockFace.DOWN) && ironBars.size() != 6 ) ||
		(portalFace != BlockFace.UP && portalFace != BlockFace.DOWN && ironBars.size() != 8 ))) ||
		(plugin.config.CompactPortal && 
		(((portalFace == BlockFace.UP || portalFace == BlockFace.DOWN) && ironBars.size() != 2 ) ||
		(portalFace != BlockFace.UP && portalFace != BlockFace.DOWN && ironBars.size() != 4 ))))
			return;
		
		if (wool.getColor() == DyeColor.BLACK)
		{
			if (oldPortal != null)
			  oldPortal.delete();
			return;
		}
				
		//Check if portal is big enough and start making a portal
		PortalCoord portalc = new PortalCoord();
		int c = 0;
		for (int i = 0; i < ironBars.size() / 2; i++)
		{
			portalc.border.add(new V10Location(ironBars.get(i).getRelative(portalFace).getRelative(otherSide, 1)));
			portalc.border.add(new V10Location(ironBars.get(i).getRelative(portalFace).getRelative(otherSide, 3)));
			
			if (i == 0 || i == (ironBars.size() / 2) - 1)
				portalc.border.add(new V10Location(ironBars.get(i).getRelative(portalFace).getRelative(otherSide, 2)));
			else
				portalc.inside[c++] = new V10Location(ironBars.get(i).getRelative(portalFace).getRelative(otherSide, 2));
		}
		
		portalc.vertical = portalFace == BlockFace.UP || portalFace == BlockFace.DOWN;
		portalc.block = portalc.inside[0];
		
		if (oldPortal != null)
		  oldPortal.delete();
		
		if (portalc.border.size() == 0 || portalc.inside[0] == null)
		  return;
		for (V10Location tb : portalc.border)
			if ((!region.getBoolean(RegionSetting.ALL_BLOCKS_PORTAL) && !region.getList(RegionSetting.PORTAL_BLOCKS).contains(tb.getHandle().getBlock().getTypeId())) || borderBlocks.containsKey(tb) || insideBlocks.containsKey(tb)) return;
		for (V10Location tb : portalc.inside)
		  if(tb != null)
			if ((!region.getBoolean(RegionSetting.ALL_BLOCKS_PORTAL) && !region.getList(RegionSetting.PORTAL_BLOCKS).contains(tb.getHandle().getBlock().getTypeId())) || borderBlocks.containsKey(tb) || insideBlocks.containsKey(tb)) return;
		
		if (portalc.vertical)
		  portalc.destLoc[0] = new V10Location(portalc.inside[0].getHandle().getBlock().getRelative(portalFace.getOppositeFace()));
		else
		{
		  Block block = portalc.inside[0].getHandle().getBlock().getRelative(portalFace.getOppositeFace());
		  portalc.destLoc[0] = new V10Location(block);
		  block = block.getRelative(BlockFace.UP);
		  portalc.destLoc[1] = new V10Location(block);
		}
		
		portalc.tpFace = portalFace;
		
		boolean orange = wool.getColor() == DyeColor.ORANGE;
		
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
