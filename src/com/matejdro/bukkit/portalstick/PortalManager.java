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
import org.bukkit.inventory.PlayerInventory;
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
				  bh.id = block.getTypeId();
				  bh.data = block.getData();
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
				  bh.id = block.getTypeId();
				  bh.data = block.getData();
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
		Inventory inv = ih.getInventory();
		List<?> ice = region.getList(RegionSetting.GRILL_REMOVE_EXCEPTIONS);
		ItemStack item, item2;
		for (int i = 0; i < inv.getSize(); i++)
		{
			item = inv.getItem(i);
			if(item == null || item.getTypeId() == 0)
			  continue;
			for (Object is: ice)
			{
				item2 = plugin.util.getItemData((String)is);
				if(item.getTypeId() != item2.getTypeId() || item.getDurability() != item2.getDurability())
				{
				  inv.clear(i);
				  break;
				}
			}
		}
		if(inv instanceof PlayerInventory)
		{
		  PlayerInventory pi = (PlayerInventory)inv;
		  ItemStack[] armor = pi.getArmorContents();
		  
		  for(int i = 0; i < armor.length; i++)
		  {
			if(armor[i] == null || armor[i].getTypeId() == 0)
			  continue;
			for (Object is: ice)
			{
				item2 = plugin.util.getItemData((String)is);
				if(armor[i].getTypeId() != item2.getTypeId() || armor[i].getDurability() != item2.getDurability())
				{
				  armor[i] = null;
				  break;
				}
			}
		  }
		  pi.setArmorContents(armor);
		}
		
		if(region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
		  for (Object is : region.getList(RegionSetting.UNIQUE_INVENTORY_ITEMS))
		  {
			item = plugin.util.getItemData((String)is);
			inv.addItem(item);
		  }
	}
	
	public final HashMap<V10Location, HashMap<V10Location, BlockHolder>> openAutoPortals = new HashMap<V10Location, HashMap<V10Location, BlockHolder>>();
	public final HashMap<V10Location, Portal> autoPortals = new HashMap<V10Location, Portal>();
	
	public void tryPlacingAutomatedPortal(Block b)
	{
	  DyeColor color = ((Wool)Material.WOOL.getNewData(b.getData())).getColor();
	  boolean orange;
	  boolean black;
	  if(color == DyeColor.ORANGE)
	  {
		orange = true;
		black = false;
	  }
	  else if(color == DyeColor.LIGHT_BLUE)
	  {
		orange = false;
		black = false;
	  }
	  else if(color == DyeColor.BLACK)
	  {
		orange = false;
		black = true;
	  }
	  else
		return;
	  Block iron = null;
	  //Search iron fence
	  BlockFace[] sides = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
	  BlockFace f = null;
	  for(BlockFace face: sides)
	  {
		iron = b.getRelative(face, 2);
		if(iron.getType() == Material.IRON_FENCE)
		{
		  f = face;
		  break;
		}
		else
		  iron = null;
	  }
	  if(iron == null)
		return;
	  
	  //get iron block at the other side
	  Block iron2 = null;
	  for(BlockFace face: sides)
	  {
		iron2 = iron.getRelative(face, 4);
		if(iron2.getType() == Material.IRON_FENCE)
		  break;
		else
		  iron2 = null;
	  }
	  if(iron2 == null)
		return;
	  //get all iron
	  ArrayList<Block> ironBlocks1 = getAllIron(iron);
	  if(ironBlocks1.size() != 4)
		return;
	  ArrayList<Block> ironBlocks2 = getAllIron(iron2);
	  if(ironBlocks2.size() != 4)
		return;
	  
	  //get min y
	  int minY = getMinY(ironBlocks1);
	  if(minY != getMinY(ironBlocks2))
		return;
	  //get max y
	  int maxY = getMaxY(ironBlocks1);
	  if(maxY != getMaxY(ironBlocks2))
		return;
	  
	  boolean x = f == BlockFace.WEST || f == BlockFace.EAST;
	  //get min/max x
	  int min, max;
	  if(x)
	  {
		if(iron.getX() < iron2.getX())
		{
		  min = iron.getX();
		  max = iron2.getX();
		}
		else
		{
		  min = iron2.getX();
		  max = iron.getX();
		}
	  }
	  else
	  {
		if(iron.getZ() < iron2.getZ())
		{
		  min = iron.getZ();
		  max = iron2.getZ();
		}
		else
		{
		  min = iron2.getZ();
		  max = iron.getZ();
		}
	  }
	  
	  //get comparable location
	  V10Location mb;
	  World world = iron.getWorld();
	  if(x)
		mb = new V10Location(new Location(world, min, minY, iron.getZ()));
	  else
		mb = new V10Location(new Location(world, iron.getX(), minY, min));
	  
	  if(black)
	  {
		if(!openAutoPortals.containsKey(mb))
		  return;
//		for(Entry<V10Location, BlockHolder> ts: openAutoPortals.get(mb).entrySet())
//		  ts.getValue().setTo(ts.getKey());
		openAutoPortals.remove(mb);
		Portal destination = autoPortals.get(mb).getDestination();
		if(destination != null)
		  destination.close();
		autoPortals.remove(mb);
		return;
	  }
	  
	  //get blocks to place portal at
	  Region region = plugin.regionManager.getRegion(mb);
	  HashMap<V10Location, BlockHolder> blocks = new HashMap<V10Location, BlockHolder>();
	  if(!region.getBoolean(RegionSetting.ALL_BLOCKS_PORTAL))
	  {
		List<?> placeable = region.getList(RegionSetting.PORTAL_BLOCKS);
		int tz;
		if(x)
		  tz = iron.getZ() - 1;
		else
		  tz = iron.getX() - 1;
		for(int ty = minY; ty <= maxY; ty++)
		{
		  for(int tx = min + 1; tx < max; tx++)
		  {
			if(x)
			  iron = world.getBlockAt(tx, ty, tz);
			else
			  iron = world.getBlockAt(tz, ty, tx);
			if(!placeable.contains(iron.getTypeId()))
			  return;
			blocks.put(new V10Location(iron), new BlockHolder(iron));
		  }
		}
	  }
	  if(!openAutoPortals.containsKey(mb))
		openAutoPortals.put(mb, blocks);
	  plugin.getServer().broadcastMessage("Structure fine!");
	  V10Location middle;
	  if(x)
		middle = new V10Location(new Location(world, min + 2, minY + 2, iron.getZ()));
	  else
		middle = new V10Location(new Location(world, iron.getX(), minY + 2, min + 2));
	  PortalCoord pc = generatePortal(middle, f);
	  Portal portal = new Portal(plugin, pc.destLoc, middle, pc.border, pc.inside, pc.behind, region, orange, false, pc.tpFace);
	  portal.recreate();
	  Portal dest;
	  if(orange)
	  {
		region.orangePortal = portal;
		dest = region.orangePortalDest;
	  }
	  else
	  {
		region.bluePortal = portal;
		dest = region.bluePortalDest;
	  }
	  if(dest != null)
		portal.open();
	  autoPortals.put(mb, portal);
	}
	
	private ArrayList<Block> getAllIron(Block start)
	{
	  ArrayList<Block> ironBlocks = new ArrayList<Block>();
	  Block iron = start;
	  boolean first = true;
	  for(BlockFace face: new BlockFace[] {BlockFace.UP, BlockFace.DOWN})
	  {
		while(iron.getType() == Material.IRON_FENCE)
		{
		  if(first)
			first = false;
		  else
			ironBlocks.add(iron);
		  iron = iron.getRelative(face);
		}
		iron = start;
	  }
	  
	  return ironBlocks;
	}
	
	private int getMinY(ArrayList<Block> blocks)
	{
	  int ret = Integer.MAX_VALUE;
	  int y;
	  for(Block b: blocks)
	  {
		y = b.getY();
		if(y < ret)
		  ret = y;
	  }
	  return ret;
	}
	
	private int getMaxY(ArrayList<Block> blocks)
	{
	  int ret = 0;
	  int y;
	  for(Block b: blocks)
	  {
		y = b.getY();
		if(y > ret)
		  ret = y;
	  }
	  return ret;
	}
}
