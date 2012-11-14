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

import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

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
	final HashMap<V10Location, Portal> awayBlocksGeneral = new HashMap<V10Location, Portal>();
	final HashMap<V10Location, Portal> awayBlocksX = new HashMap<V10Location, Portal>();
	final HashMap<V10Location, Portal> awayBlocksY = new HashMap<V10Location, Portal>();
	final HashMap<V10Location, Portal> awayBlocksZ = new HashMap<V10Location, Portal>();

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
	//TODO: Remove one liner:
/*	public void deleteAll()
	{
		portals.clear();
	}
*/
	private boolean checkPortal(PortalCoord portal)
	{
		Region region;
		int id;
		for (V10Location loc: portal.border)
		{
			region = plugin.regionManager.getRegion(loc);
			if (!borderBlocks.containsKey(loc) && !insideBlocks.containsKey(loc) && !behindBlocks.containsKey(loc))
			{
			  id = loc.getHandle().getBlock().getTypeId();
			  if(region.getList(RegionSetting.TRANSPARENT_BLOCKS).contains(id) || (!region.getBoolean(RegionSetting.ALL_BLOCKS_PORTAL) && !region.getList(RegionSetting.PORTAL_BLOCKS).contains(id)))
				return false;
			}
		}
		for (V10Location loc: portal.inside)
		{
			region = plugin.regionManager.getRegion(loc);
			if (!borderBlocks.containsKey(loc) && !insideBlocks.containsKey(loc) && !behindBlocks.containsKey(loc))
			{
			  id = loc.getHandle().getBlock().getTypeId();
			  if(region.getList(RegionSetting.TRANSPARENT_BLOCKS).contains(id) || (!region.getBoolean(RegionSetting.ALL_BLOCKS_PORTAL) && !region.getList(RegionSetting.PORTAL_BLOCKS).contains(id)))
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

	private PortalCoord generateHorizontalPortal(V10Location block, BlockFace face)
	{
		PortalCoord portal = generatePortal(block, face); // 0
		if (checkPortal(portal)) return portal;
	//TODO Remove this useless check (if really useless)	
/*		block = block.getRelative(0,0,0);
		portal = generatePortal(block, face);
		if (checkPortal(portal)) return portal;
*/
		block = new V10Location(block.getHandle().getBlock().getRelative(BlockFace.DOWN)); // -1
		portal = generatePortal(block, face);
		if (checkPortal(portal)) return portal;
		
		block = new V10Location(block.getHandle().getBlock().getRelative(BlockFace.DOWN)); // -2
		portal = generatePortal(block, face);
		if (checkPortal(portal)) return portal;
		
		block = new V10Location(block.getHandle().getBlock().getRelative(BlockFace.UP, 3)); // 1 (-2 + 3)
		portal = generatePortal(block, face);
		if (checkPortal(portal)) return portal;
		
		block = new V10Location(block.getHandle().getBlock().getRelative(BlockFace.UP)); // 2
		portal = generatePortal(block, face);
		if (checkPortal(portal)) return portal;

		
		if (!checkPortal(portal)) portal.finished = true;
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
			
			portal.inside.add(new V10Location(rb));
	    	
	    	portal.destLoc = new V10Location(rb.getRelative(face));
	    	face = face.getOppositeFace();
			portal.behind.add(new V10Location(rb.getRelative(face)));
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
	    	face = BlockFace.NORTH;
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
	      portal.border.add(new V10Location(rb.getRelative(BlockFace.DOWN, 2)));
	      if(!plugin.config.CompactPortal)
	      {
	    	portal.border.add(new V10Location(rb.getRelative(BlockFace.UP)));
	    	
	    	Block block2 = rb.getRelative(face);
	    	for(int i = 0; i < 3; i++)
	    	  portal.border.add(new V10Location(block2.getRelative(BlockFace.DOWN, i)));
	    	block2 = rb.getRelative(face.getOppositeFace());
	    	for(int i = 0; i < 3; i++)
		      portal.border.add(new V10Location(block2.getRelative(BlockFace.DOWN, i)));
	      }
	    }
	    
	    portal.inside.add(block);
	    Block block2 = rb.getRelative(BlockFace.DOWN);
	    portal.inside.add(new V10Location(block2));
	    
	    portal.destLoc = new V10Location(block2.getRelative(portal.tpFace));
	    portal.vertical = false;
	    
	    block2.getRelative(portal.tpFace.getOppositeFace());
	    portal.behind.add(new V10Location(block2));
	    portal.behind.add(new V10Location(block2.getRelative(BlockFace.UP)));
	    
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
		
		PortalCoord portalc = new PortalCoord();
		
		User owner = plugin.userManager.getUser(player);
		
		if (face == BlockFace.DOWN || face == BlockFace.UP)
		{
			vertical = true;
			portalc = generatePortal(block, face);
			if (!checkPortal(portalc))
			{
				if (end) plugin.util.sendMessage(player, plugin.config.MessageCannotPlacePortal);
				plugin.util.PlaySound(Sound.PORTAL_CANNOT_CREATE, player, block);
				return false;
			}
		}
		else
		{
			portalc = generateHorizontalPortal(block, face);
			if (portalc.finished)
			{
				if (end) plugin.util.sendMessage(player, plugin.config.MessageCannotPlacePortal);
				plugin.util.PlaySound(Sound.PORTAL_CANNOT_CREATE, player, block);
				return false;
			}
		}
		
		//TODO : ALAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARM:
//		portalc.destLoc.setX(portalc.destLoc.x + 0.5);
//		portalc.destLoc.setZ(portalc.destLoc.z + 0.5);
	
		
		Portal portal = new Portal(plugin, portalc.destLoc, portalc.block, portalc.border, portalc.inside, portalc.behind, owner, orange, vertical, portalc.tpFace);
		
		
		if (orange)
		{
			if (owner.orangePortal != null) owner.orangePortal.delete();
			owner.orangePortal = portal;
			plugin.util.PlaySound(Sound.PORTAL_CREATE_ORANGE, player, block);
			
		}
		else
		{
			if (owner.bluePortal != null) owner.bluePortal.delete();
			owner.bluePortal = portal;
			plugin.util.PlaySound(Sound.PORTAL_CREATE_BLUE, player, block);
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
		if (player.getEyeLocation().getY() >= block.y )
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
				ItemStack itemcheck = plugin.util.getItemData((String) is);
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
			ItemStack item = plugin.util.getItemData((String) is);
			if (item.getTypeId() == plugin.config.PortalTool)
				inv.setItemInHand(item);
			else
				inv.addItem(item);
		}
	}
	
	public void tryPlacingAutomatedPortal(V10Location b)
	{
		Block rb = b.getHandle().getBlock();
		//Check if wool is correct
		Wool wool = (Wool) Material.WOOL.getNewData(rb.getData());
		if (wool.getColor() != DyeColor.BLACK && wool.getColor() != DyeColor.BLUE && wool.getColor() != DyeColor.ORANGE) return;
		
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
			else
			{
				
				if (borderBlocks.containsKey(firstPortalBlock.getLocation()) || insideBlocks.containsKey(firstPortalBlock.getLocation()))
			
				oldPortal = borderBlocks.get(firstPortalBlock.getLocation());
				if (oldPortal == null) oldPortal = insideBlocks.get(firstPortalBlock.getLocation());
				portalFace = face2;
				break;

			}
		}
		
		if (portalFace == null) return;
				
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
			if (oldPortal != null) oldPortal.delete();
			return;
		}
			
				
		//Check if portal is big enough and start making a portal
		PortalCoord portalc = new PortalCoord();
		for (int i = 0; i < ironBars.size() / 2; i++)
		{
			portalc.border.add(new V10Location(ironBars.get(i).getRelative(portalFace).getRelative(otherSide, 1)));
			portalc.border.add(new V10Location(ironBars.get(i).getRelative(portalFace).getRelative(otherSide, 3)));
			
			if (i == 0 || i == (ironBars.size() / 2) - 1)
				portalc.border.add(new V10Location(ironBars.get(i).getRelative(portalFace).getRelative(otherSide, 2)));
			else
				portalc.inside.add(new V10Location(ironBars.get(i).getRelative(portalFace).getRelative(otherSide, 2)));
		}
		
		portalc.vertical = portalFace == BlockFace.UP || portalFace == BlockFace.DOWN;
		portalc.block = portalc.inside.toArray(new V10Location[0])[0];
		
		if (portalc.border.size() == 0 || portalc.inside.size() == 0) return;
		for (V10Location tb : portalc.border)
			if (!(tb.getHandle().getBlock().getType() == Material.STONE || borderBlocks.containsKey(tb) || insideBlocks.containsKey(tb))) return;
		for (V10Location tb : portalc.inside)
			if (!(tb.getHandle().getBlock().getType() == Material.STONE || borderBlocks.containsKey(tb) || insideBlocks.containsKey(tb))) return;
				
		
		if (portalc.vertical) portalc.destLoc = new V10Location(portalc.inside.toArray(new V10Location[0])[0].getHandle().getBlock().getRelative(portalFace.getOppositeFace()));
		else
		{
			//Find lowest block inside horizontal portal
			int y = 200;
			V10Location lBlock = null;
			for (V10Location lb : portalc.inside)
				if (lBlock == null || lBlock.y < y) lBlock = lb;
					
			portalc.destLoc = new V10Location(lBlock.getHandle().getBlock().getRelative(portalFace.getOppositeFace()));
		}
		//TODO: AAAALLLLLAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARM:
//		portalc.destLoc.setX(portalc.destLoc.x + 0.5);
//		portalc.destLoc.setZ(portalc.destLoc.z + 0.5);
		
		portalc.tpFace = portalFace;
		
		if (oldPortal != null) oldPortal.delete();
		
		Boolean orange = wool.getColor() == DyeColor.ORANGE;
		Region region = plugin.regionManager.getRegion(b);
		
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
