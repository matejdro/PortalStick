package com.matejdro.bukkit.portalstick.listeners;

import java.lang.reflect.Field;
import java.util.HashSet;

import net.minecraft.server.EntityFallingBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.craftbukkit.entity.CraftFallingSand;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.Bridge;
import com.matejdro.bukkit.portalstick.Funnel;
import com.matejdro.bukkit.portalstick.Grill;
import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.BlockHolder;
import de.V10lator.PortalStick.V10Location;

public class PortalStickBlockListener implements Listener
{
	private PortalStick plugin;
	private HashSet<Block> blockedPistonBlocks = new HashSet<Block>();	
	private boolean fakeBBE;
	private final Field f;
	
	public PortalStickBlockListener(PortalStick instance)
	{
		plugin = instance;
		
		Field f;
		try
		{
		  f = EntityFallingBlock.class.getDeclaredField("e");
		  f.setAccessible(true);
		}
		catch(Exception e)
		{
		  e.printStackTrace();
		  f = null;
		}
		this.f = f;
	}

	@EventHandler()
	public void onBlockBreak(BlockBreakEvent event)
	{
	  Block block = event.getBlock();
	  V10Location loc = new V10Location(block);
	  if(plugin.config.DisabledWorlds.contains(loc.world))
		return;
	  
	  //Delete from gel maps
	  BlockHolder bh = new BlockHolder(block);
	  if(plugin.gelManager.gelMap.containsKey(bh))
	    plugin.gelManager.removeGel(bh);
	  
	  //Update bridge if destroyed block made space.
	  //We call this as early as possible to not be suppressed by one of the returns.
	  //At the end it will be scheduled by one tick anyway.
	  plugin.funnelBridgeManager.updateBridge(loc);
	  
	  Portal portal = null;
	  if(plugin.portalManager.borderBlocks.containsKey(loc))
		portal = plugin.portalManager.borderBlocks.get(loc);
	  else if(plugin.portalManager.behindBlocks.containsKey(loc))
		portal = plugin.portalManager.behindBlocks.get(loc);
	  else if (plugin.portalManager.insideBlocks.containsKey(loc))
	  {
		portal = plugin.portalManager.insideBlocks.get(loc);
		if(portal.transmitter && block.getType() == Material.REDSTONE_TORCH_ON)
		{
		  event.setCancelled(true);
		  fakeBBE = false;
		  return;
		}
		if(portal.open)
		  return;
	  }
	  if (portal != null)
	  {
		portal.delete();
		event.setCancelled(true);
		return;
	  }
	  
	  // Don't destroy inner grill blocks or bridges
	  if(plugin.grillManager.insideBlocks.containsKey(loc) ||
			  plugin.funnelBridgeManager.bridgeBlocks.containsKey(loc))
	  {
		event.setCancelled(true);
		fakeBBE = false;
		return;
	  }
	  
	  //Delete bridge
	  if(plugin.funnelBridgeManager.bridgeMachineBlocks.containsKey(loc))
	  {
		if(event.getPlayer() == null || plugin.hasPermission(event.getPlayer(), plugin.PERM_DELETE_BRIDGE))
		  plugin.funnelBridgeManager.bridgeMachineBlocks.get(loc).delete();
		else
		{
		  event.setCancelled(true);
		  fakeBBE = false;
		}
		return;
	  }
	  
	  //Delete grill
	  if (plugin.grillManager.borderBlocks.containsKey(loc))
	  {
		if(event.getPlayer() == null || plugin.hasPermission(event.getPlayer(), plugin.PERM_DELETE_GRILL))
		  plugin.grillManager.borderBlocks.get(loc).delete();
		else
		{
		  event.setCancelled(true);
		  fakeBBE = false;
		}
		return;
	  }
	  
	  Material type = block.getType();
	  Region region = plugin.regionManager.getRegion(loc);
	  if(type == Material.REDSTONE_WIRE && region.getBoolean(RegionSetting.ENABLE_REDSTONE_TRANSFER))
	  {
		Location l = block.getLocation();
		
		for (int i = 0; i < 4; i++)
		{
		  BlockFace face = BlockFace.values()[i];
		  loc = new V10Location(new Location(l.getWorld(), l.getX() + face.getModX(), l.getY() + face.getModY(), l.getZ() + face.getModZ()));
		  if (plugin.portalManager.insideBlocks.containsKey(loc)) 
		  {
			portal = plugin.portalManager.insideBlocks.get(loc);
			if (!portal.open)
			  continue;
			
			Portal destination = portal.getDestination();
			if (destination == null || destination.transmitter)
			  continue;
			
			for (V10Location b: destination.inside)
			  if(b != null)
				b.getHandle().getBlock().setType(Material.AIR);
			portal.transmitter = false;
		  }
		}
	  }
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockBurn(BlockIgniteEvent event)
	{
	  Block block = event.getBlock();
	  if(plugin.config.DisabledWorlds.contains(block.getLocation().getWorld().getName()))
		return;
	  V10Location loc;
	  Region region;
	  for(BlockFace face: new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP})
	  {
		loc = new V10Location(block.getRelative(face));
		if (plugin.portalManager.borderBlocks.containsKey(loc) ||
				plugin.portalManager.behindBlocks.containsKey(loc))
		{
		  event.setCancelled(true);
		  return;
		}
		if(plugin.portalManager.insideBlocks.containsKey(loc))
		{
		  event.setCancelled(true);
		  Portal portal = plugin.portalManager.insideBlocks.get(loc);
		  if(!portal.open)
			return;
		  Portal dest = portal.getDestination();
		  
		  V10Location destl;
		  if(dest.horizontal || portal.inside[0].equals(loc))
			destl = dest.teleport[0];
		  else
			destl = dest.teleport[1];
		  block = destl.getHandle().getBlock();
		  if(block.getType() == Material.AIR)
			block.setType(Material.FIRE);
		  return;
		}
		region = plugin.regionManager.getRegion(loc);
		if(plugin.blockUtil.compareBlockToString(loc, (String)region.settings.get(RegionSetting.BLUE_GEL_BLOCK)) ||
				plugin.blockUtil.compareBlockToString(loc, (String)region.settings.get(RegionSetting.RED_GEL_BLOCK)))
		{
		  event.setCancelled(true);
		  return;
		}
	  }
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockBurn2(BlockBurnEvent event) {	
		V10Location loc = new V10Location(event.getBlock());
		if(plugin.config.DisabledWorlds.contains(loc.world))
		  return;
		if (plugin.portalManager.borderBlocks.containsKey(loc) ||
				plugin.portalManager.insideBlocks.containsKey(loc) ||
				plugin.portalManager.behindBlocks.containsKey(loc))
		{
			event.setCancelled(true);
			return;
		}
		Region region = plugin.regionManager.getRegion(loc);
		if(plugin.blockUtil.compareBlockToString(loc, (String)region.settings.get(RegionSetting.BLUE_GEL_BLOCK)) ||
				plugin.blockUtil.compareBlockToString(loc, (String)region.settings.get(RegionSetting.RED_GEL_BLOCK)))
		  event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(plugin.config.DisabledWorlds.contains(event.getBlock().getLocation().getWorld().getName()))
		  return;
		Material block = event.getBlock().getType();
		
		//Prevent obstructing funnel
		if (plugin.funnelBridgeManager.bridgeBlocks.containsKey(new V10Location(event.getBlock())))
		{
			event.setCancelled(true);
			return;
		}
		
		if (block == Material.RAILS || block == Material.POWERED_RAIL || block == Material.DETECTOR_RAIL)
		  return;
		
		if (plugin.portalManager.insideBlocks.containsKey(new V10Location(event.getBlockPlaced())))
		  event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event)
	{
		Block block = event.getBlock();
		if(block.getType() != Material.SUGAR_CANE_BLOCK || plugin.config.DisabledWorlds.contains(block.getLocation().getWorld().getName()))
		  return;
		if(plugin.grillManager.insideBlocks.containsKey(new V10Location(block)))
		  event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void noGrowingGrills(BlockGrowEvent event)
	{
		if(plugin.config.DisabledWorlds.contains(event.getBlock().getLocation().getWorld().getName()))
		  return;
		if(plugin.grillManager.insideBlocks.containsKey(new V10Location(event.getBlock().getRelative(BlockFace.DOWN))))
		  event.setCancelled(true);
	}
	
	@EventHandler()
	public void onBlockFromTo(BlockFromToEvent event) {
		Block from = event.getBlock();
		V10Location loc = new V10Location(from);
		if(plugin.config.DisabledWorlds.contains(loc.world))
		  return;
		Block to = event.getToBlock();
		V10Location tb = new V10Location(to);
		 Region region = plugin.regionManager.getRegion(loc);
		 //Liquid teleporting
			if (region. //TODO: region is null! - Seems to be solved.
					getBoolean(
							RegionSetting.
							TELEPORT_LIQUIDS)
							&& 
							!plugin.
							funnelBridgeManager.
							bridgeBlocks.
							containsKey(loc))
			{
				Portal portal = plugin.portalManager.insideBlocks.get(tb);
				if (portal != null && portal.open)
				{
					int blockt = Material.AIR.getId();
					int blockt2 = blockt;
					switch (from.getType())
					{
						case WATER:
						case STATIONARY_WATER:
							blockt = Material.WATER.getId();
							blockt2 = Material.STATIONARY_WATER.getId();
							break;
						default:
							blockt = Material.LAVA.getId();
							blockt2 = Material.STATIONARY_LAVA.getId();
					}
					
					V10Location dest;
					Portal destination = portal.getDestination();
					if(destination.horizontal || portal.inside[0].equals(tb))
					  dest = destination.teleport[0];
					else
					  dest = destination.teleport[1];
					
					Block destb = dest.getHandle().getBlock();
					if (destb.getType() == Material.AIR)
					{
					  destb.setTypeId(blockt);
					  LiquidCheck lc = new LiquidCheck(loc, dest, destination, blockt2, blockt);
					  lc.setPid(plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, lc, 10L, 10L));  
					}
					event.setCancelled(true);
				}
			}
				//Funnel
				if (plugin.funnelBridgeManager.bridgeBlocks.containsKey(loc) && plugin.funnelBridgeManager.bridgeBlocks.containsKey(tb)) 
				{
					if (!(plugin.funnelBridgeManager.bridgeBlocks.get(loc) instanceof Funnel && plugin.funnelBridgeManager.bridgeBlocks.get(tb) instanceof Funnel))
					{
						event.setCancelled(true);
						return;
					}
					
					Funnel funnel1 = (Funnel) plugin.funnelBridgeManager.bridgeBlocks.get(loc);
					Funnel funnel2 = (Funnel) plugin.funnelBridgeManager.bridgeBlocks.get(tb);
					if (!funnel1.equals(funnel2))
					{
						event.setCancelled(true);
						return;
					}
					
					int numfrom = funnel1.getCounter(loc);
					int numto = funnel1.getCounter(tb);
					
					if (numfrom < numto || numfrom < 0 || numto < 0)
					{
						event.setCancelled(true);
						return;
					}
					
				
				}
				else if (plugin.funnelBridgeManager.bridgeBlocks.containsKey(loc) || plugin.funnelBridgeManager.bridgeBlocks.containsKey(tb))
				{
					event.setCancelled(true);
					return;
				}
			}
	
	@EventHandler
	public void infiniteDispenser(BlockDispenseEvent event)
	{
	  if(plugin.config.DisabledWorlds.contains(event.getBlock().getLocation().getWorld().getName()))
		return;
	  BlockState bs = event.getBlock().getState();
	  if(!(bs instanceof Dispenser))
		return;
	  Dispenser d = (Dispenser)bs;
	  ItemStack is = d.getInventory().getItem(4);
	  if(is == null)
		return;
	  Material mat = is.getType();
	  Region region = plugin.regionManager.getRegion(new V10Location(bs.getLocation()));
	  if(region.getBoolean(RegionSetting.GEL_TUBE))
	  {
		ItemStack gel = plugin.util.getItemData(region.getString(RegionSetting.RED_GEL_BLOCK));
		if(mat == gel.getType() && is.getDurability() == gel.getDurability())
		{
		  event.setCancelled(true);
		  Block to = d.getBlock();
		  V10Location from = new V10Location(to);
		  if(plugin.gelManager.activeGelTubes.contains(from))
			return;
		  BlockFace direction;
		  switch(d.getData().getData())
		  {
		  	case 2:
		  	  direction = BlockFace.EAST;
		  	  break;
		  	case 3:
		  	  direction = BlockFace.WEST;
		  	  break;
		  	case 4:
			  direction = BlockFace.NORTH;
			  break;
		  	default:
			  direction = BlockFace.SOUTH;
		  }
		  plugin.gelManager.tubePids.put(from, plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new GelTube(from, direction, mat.getId(), is.getData().getData()), 0L, 5L));
		  plugin.gelManager.activeGelTubes.add(from);
		  return;
		}
		else
		{
		  gel = plugin.util.getItemData(region.getString(RegionSetting.BLUE_GEL_BLOCK));
		  if(mat == gel.getType() && is.getDurability() == gel.getDurability())
		  {
			event.setCancelled(true);
			Block to = d.getBlock();
			V10Location from = new V10Location(to);
			if(plugin.gelManager.activeGelTubes.contains(from))
			  return;
			BlockFace direction;
			switch(d.getData().getData())
			{
			  case 2:
			    direction = BlockFace.EAST;
			  	break;
			  case 3:
			    direction = BlockFace.WEST;
			    break;
			  case 4:
				direction = BlockFace.NORTH;
				break;
			  default:
				direction = BlockFace.SOUTH;
			}
			  plugin.gelManager.tubePids.put(from, plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new GelTube(from, direction, mat.getId(), is.getData().getData()), 0L, 5L));
			  plugin.gelManager.activeGelTubes.add(from);
			  return;
		  }
		}
	  }
	  if(region.getBoolean(RegionSetting.INFINITE_DISPENSERS))
	  {
		if(is != null && is.getType() != Material.AIR)
		  is.setAmount(is.getAmount() + 1);
	  }
	}
	
	private class GelTube implements Runnable
	{
	  private final V10Location loc;
	  private final BlockFace direction;
	  private final int mat;
	  private final byte data;
	  
	  private GelTube(V10Location loc, BlockFace direction, int mat, byte data)
	  {
		this.loc = loc;
		this.direction = direction;
		this.mat = mat;
		this.data = data;
	  }
	  
	  public void run()
	  {
		Block to = loc.getHandle().getBlock();
		if(to.getType() != Material.DISPENSER || to.getBlockPower() == 0)
		{
		  plugin.gelManager.stopGelTube(loc);
		  return;
		}
		to = to.getRelative(direction);
		if(to.getType() != Material.AIR)
		  return;
		Location loc2 = to.getLocation();
		to = to.getRelative(direction);
		if(to.isLiquid())
		  return;
		Vector vector = new Vector();
		double v = plugin.rand.nextDouble();
		if(to.getType() != Material.AIR)
		  vector.setY(-v);
		else
		{
		  switch(direction)
		  {
		    case NORTH:
		      vector.setX(-v);
		      break;
		  	case EAST:
		  	  vector.setZ(-v);
		  	  break;
		  	case SOUTH:
		  	  vector.setX(v);
		  	  break;
		  	default:
		  	  vector.setZ(v);
		  }
		}
		loc2.setX(loc2.getX()+0.5D);
		loc2.setZ(loc2.getZ()+0.5D);
		FallingBlock fb = loc2.getWorld().spawnFallingBlock(loc2, mat, data);
		fb.setDropItem(false);
		fb.setVelocity(vector);
		EntityFallingBlock nmsFB = ((CraftFallingSand)fb).getHandle();
		try
		{
		  f.setBoolean(nmsFB, true);
		}
		catch(Exception e)
		{
		  e.printStackTrace();
		}
		plugin.gelManager.flyingGels.put(fb.getUniqueId(), loc);
	  }
	}
	
	@EventHandler()
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		if(event.getOldCurrent() == event.getNewCurrent())
		  return;
		 Block block = event.getBlock();
		 V10Location loc = new V10Location(block);
		 if(plugin.config.DisabledWorlds.contains(loc.world))
			 return;
		 
		 Region region = plugin.regionManager.getRegion(loc);
		 
		 //Redstone teleportation
		 if (region.getBoolean(RegionSetting.ENABLE_REDSTONE_TRANSFER))
		 {			 
			 Location l = block.getLocation();
			 BlockFace face;
			 Block block2;
			 for (int i = 0; i < 5; i++)
			 {
				 face = BlockFace.values()[i];
				 loc = new V10Location(new Location(l.getWorld(), l.getX() + face.getModX(), l.getY() + face.getModY(), l.getZ() + face.getModZ()));
				 if (plugin.portalManager.insideBlocks.containsKey(loc)) 
					 {
					 	Portal portal = plugin.portalManager.insideBlocks.get(loc);
					 	if (!portal.open) continue;
					 
					 	Portal destination = portal.getDestination();
					 	if (destination == null || destination.transmitter) continue;
					 	
					 	Material mat1, mat2;
					 	if (event.getNewCurrent() > 0)
					 	{
					 		portal.transmitter = true;
					 		mat1 = Material.REDSTONE_TORCH_ON;
					 		mat2 = Material.AIR;
					 	}
					 	else
					 	{
					 		portal.transmitter = false;
					 		mat1 = Material.AIR;
					 		mat2 = Material.REDSTONE_TORCH_ON;
					 	}
					 	for (V10Location b: destination.inside)
					 	{
					 	  if(b != null)
					 	  {
					 		block2 = b.getHandle().getBlock();
					 		if(block2.getType() == mat2)
				 			block2.setType(mat1);
					 	  }
					 	}
					 }
			 }	 
		 }
		 
		 //Turning off grills
		 if (region.getBoolean(RegionSetting.ENABLE_GRILL_REDSTONE_DISABLING)) 
		 {
			 
			 Grill grill = null;
			 for (int i = 0; i < 5; i++)
			 { 
				grill = plugin.grillManager.borderBlocks.get(new V10Location(block.getRelative(BlockFace.values()[i])));
				if (grill != null)
				{
				  if (event.getNewCurrent() > 0)
					grill.disable();
				  else
				    grill.enable();
				}
			 }
		 }
		 
		 //Turning off bridges or reversing funnels
		 if (region.getBoolean(RegionSetting.ENABLE_BRIDGE_REDSTONE_DISABLING) && block.getType() != Material.REDSTONE_TORCH_ON && block.getType() != Material.REDSTONE_TORCH_OFF) 
		 {
			 Bridge bridge = null;
			 boolean cblock = false;
			 for (int i = 0; i < 5; i++)
			 {
				 bridge = plugin.funnelBridgeManager.bridgeMachineBlocks.get(new V10Location(block.getRelative(BlockFace.values()[i])));
				 if (bridge != null) 
				 {
					 cblock = new V10Location(block.getRelative(BlockFace.values()[i])).equals(bridge.creationBlock);
					 break;
				 }
			 }
			 
			 if (bridge != null)
			 {
				 if (bridge instanceof Funnel && cblock)
				 {
					((Funnel) bridge).setReverse(event.getNewCurrent() > 0); 
				 }
				 else
				 {
					 if (event.getNewCurrent() > 0)
						 bridge.deactivate();
				     else
				    	 bridge.activate(); 
				 }
			 }
		 }
		 
		 //Portal Generators
		 if (event.getOldCurrent()  == 0 && event.getNewCurrent() > 0)
		 {
			 Block block2;
			 for (int i = 0; i < 5; i++)
			 {
				 block2 = block.getRelative(BlockFace.values()[i]);
				 if (block2.getType() == Material.WOOL)
					 plugin.portalManager.tryPlacingAutomatedPortal(block2);
			 }
		 }
	 }
	 
	@EventHandler(ignoreCancelled = true)
	 public void onBlockPistonExtend(BlockPistonExtendEvent event) 
	 {
		if(plugin.config.DisabledWorlds.contains(event.getBlock().getLocation().getWorld().getName()))
			  return;
		 Region region = plugin.regionManager.getRegion(new V10Location(event.getBlock()));

		 BlockBreakEvent bbe;
		 V10Location loc;
		 for (final Block b : event.getBlocks())
		 {
			 fakeBBE = true;
			 bbe = new BlockBreakEvent(b, null);
			 onBlockBreak(bbe);
			 if(bbe.isCancelled())
			 {
				 if(!fakeBBE)
					 event.setCancelled(true);
				 else
					 fakeBBE = false;
				 continue;
			 }
			 else
				 fakeBBE = false;
			 if (blockedPistonBlocks.contains(b))
			 {
				 event.setCancelled(true);
				 return;
			 }
			 
			 if(!region.getBoolean(RegionSetting.ENABLE_PISTON_BLOCK_TELEPORT))
				 return;
			 
			 loc = new V10Location(b.getRelative(event.getDirection()));
			 if(!plugin.portalManager.insideBlocks.containsKey(loc))
				 continue;
			 
			 Portal portal = plugin.portalManager.insideBlocks.get(loc);
			 if(!portal.open)
				 continue;
			 
			 Portal destP = portal.getDestination();
			 V10Location dest;
			 
			 if(destP.horizontal || portal.inside[0].equals(loc))
				 dest = destP.teleport[0];
			 else
				 dest = destP.teleport[1];
			 
			 Block destB = dest.getHandle().getBlock();
			 
			 if (destB.isLiquid() || destB.getType() == Material.AIR)
			 {
				 destB.setTypeIdAndData(b.getType().getId(), b.getData(), true);
				 final Block b2 = b.getRelative(event.getDirection());
				 blockedPistonBlocks.add(b2);
				 plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				 {
					 public void run()
					 {
						 b2.setType(Material.AIR);
						 blockedPistonBlocks.remove(b2);
					 }
				 }, 2L);
			 }
			 else
				 event.setCancelled(true);
		}
	 }
	 
	@EventHandler(ignoreCancelled = true)
	 public void onBlockPistonRetract(BlockPistonRetractEvent event) 
	 {
		 if(!event.isSticky())
			 return;
		 
		 Block block = event.getRetractLocation().getBlock();
		 if(plugin.config.DisabledWorlds.contains(block.getLocation().getWorld().getName()))
			  return;
		 
		 fakeBBE = true;
		 BlockBreakEvent bbe = new BlockBreakEvent(block, null);
		 onBlockBreak(bbe);
		 if(bbe.isCancelled())
		 {
			 if(!fakeBBE)
				 event.setCancelled(true);
			 else
				 fakeBBE = false;
			 return;
		 }
		 else
			 fakeBBE = false;
		 
		 if (blockedPistonBlocks.contains(block))
		 {
			 event.setCancelled(true);
			 return;
		 }
		 
		 Region region = plugin.regionManager.getRegion(new V10Location(event.getBlock()));
		 
		 if(!region.getBoolean(RegionSetting.ENABLE_PISTON_BLOCK_TELEPORT))
			 return;

		 V10Location loc = new V10Location(event.getRetractLocation());
		 Portal portal = plugin.portalManager.insideBlocks.get(loc);
		 
		 if (portal != null)
		 {
			 Portal destP = portal.getDestination();
			 V10Location dest;
			 if(destP.horizontal || portal.inside[0].equals(loc))
				 dest = destP.teleport[0];
			 else
				 dest = destP.teleport[1];
			 Block sourceB = dest.getHandle().getBlock();
			 
			 if (!sourceB.isLiquid() && sourceB.getType() != Material.AIR)
			 {
				 Block endBlock = event.getRetractLocation().getBlock();
				 endBlock.setTypeIdAndData(sourceB.getTypeId(), sourceB.getData(), false);
				 sourceB.setType(Material.AIR);
			 }
		 }
		 else
		 {
			 if (plugin.portalManager.borderBlocks.containsKey(loc) || plugin.grillManager.borderBlocks.containsKey(loc) || plugin.grillManager.insideBlocks.containsKey(loc))
				 event.setCancelled(true);
		 }
		 
		 //Update bridge if piston made space
		 plugin.funnelBridgeManager.updateBridge(new V10Location(event.getRetractLocation()));
	 }
	 
	private class LiquidCheck implements Runnable
	{
	  private final V10Location source;
	  private final V10Location destination;
	  private final Portal exit;
	  private final int mat1, mat2;
	  private int pid;
	  
	  private LiquidCheck(V10Location source, V10Location destination, Portal exit, int mat1, int mat2)
	  {
		this.source = source;
		this.destination = destination;
		this.exit = exit;
		this.mat1 = mat1;
		this.mat2 = mat2;
	  }
	  
	  private void setPid(int pid)
	  {
		this.pid = pid;
	  }
	  
	  @Override
	  public void run()
	  {
		Location loc = source.getHandle();
		if(loc == null)
		{
		  plugin.getServer().getScheduler().cancelTask(pid);
		  return;
		}
		Block source = loc.getBlock();
		loc = destination.getHandle();
		if(loc == null)
		{
		  plugin.getServer().getScheduler().cancelTask(pid);
		  return;
		}
		Block destination = loc.getBlock();
		if(!exit.open || source.getTypeId() != mat1 || source.getTypeId() != mat2)
		{
		  if(destination.getTypeId() == mat1)
			destination.setType(Material.AIR);
		  plugin.getServer().getScheduler().cancelTask(pid);
		}
		else if(destination.getType() == Material.AIR)
		  destination.setTypeId(mat2);
	  }
	}
}
