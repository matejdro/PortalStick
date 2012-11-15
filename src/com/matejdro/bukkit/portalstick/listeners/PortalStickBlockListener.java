package com.matejdro.bukkit.portalstick.listeners;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;

import com.matejdro.bukkit.portalstick.Bridge;
import com.matejdro.bukkit.portalstick.Funnel;
import com.matejdro.bukkit.portalstick.Grill;
import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.V10Location;

public class PortalStickBlockListener implements Listener {
	
	private PortalStick plugin;
	private HashSet<Block> blockedPistonBlocks = new HashSet<Block>();	
	
	public PortalStickBlockListener(PortalStick instance)
	{
		plugin = instance;
	}

	@EventHandler()
	public void onBlockBreak(BlockBreakEvent event) { //TODO: Tune
		Block block = event.getBlock();
		V10Location loc = new V10Location(block);
		Portal portal = plugin.portalManager.borderBlocks.get(loc);
		if (portal == null) portal = plugin.portalManager.behindBlocks.get(loc);
		if (portal != null)
		{
			portal.delete();
			event.setCancelled(true);
			return;
		}
		portal = plugin.portalManager.insideBlocks.get(loc);
		if (portal != null)
		{
		  if(portal.transmitter && block.getType() == Material.REDSTONE_TORCH_ON)
			event.setCancelled(true);
		  return;
		}
		Grill grill = plugin.grillManager.insideBlocks.get(loc);
		if (grill != null )
			event.setCancelled(true);
		
		//Prevent destroying bridge
		V10Location vb = new V10Location(block);
		Bridge bridge = plugin.funnelBridgeManager.bridgeBlocks.get(vb);
		if (bridge != null )
		{
				event.setCancelled(true);
				return;
		}
		//Delete bridge
		bridge = plugin.funnelBridgeManager.bridgeMachineBlocks.get(vb);
		if (bridge != null )
		{
			if (plugin.hasPermission(event.getPlayer(), plugin.PERM_DELETE_BRIDGE))
				bridge.delete();
			else
				event.setCancelled(true);
			return;
		}
		
		//Update bridge if destroyed block made space
		plugin.funnelBridgeManager.updateBridge(loc);
		
		Region region = plugin.regionManager.getRegion(loc);
		if (plugin.blockUtil.compareBlockToString(block, region.getString(RegionSetting.GRILL_MATERIAL)))
		{
			grill = plugin.grillManager.borderBlocks.get(loc);
				if (grill == null || !plugin.hasPermission(event.getPlayer(), plugin.PERM_DELETE_GRILL)) return;
				grill.delete();
		}
		
		Material type = block.getType();
		if (type == Material.REDSTONE_WIRE && region.getBoolean(RegionSetting.ENABLE_REDSTONE_TRANSFER))
		{
			Location l = event.getBlock().getLocation();
			
			for (int i = 0; i < 4; i++)
			{
				BlockFace face = BlockFace.values()[i];
				loc = new V10Location(new Location(l.getWorld(), l.getX() + face.getModX(), l.getY() + face.getModY(), l.getZ() + face.getModZ()));
				if (plugin.portalManager.insideBlocks.containsKey(loc)) 
				{
					portal = plugin.portalManager.insideBlocks.get(loc);
					if (!portal.open) continue;
					
					Portal destination = portal.getDestination();
					if (destination == null || destination.transmitter) continue;
					
					for (V10Location b: destination.inside)
						b.getHandle().getBlock().setType(Material.AIR);
					portal.transmitter = false;
				}
			 }
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent event) {	
		if (event.getBlock().getType() != Material.WOOL) return;
		
		V10Location loc = new V10Location(event.getBlock().getLocation());
		
		Portal portal = plugin.portalManager.borderBlocks.get(loc);
		if (portal == null) portal = plugin.portalManager.insideBlocks.get(loc);
		if (portal == null) portal = plugin.portalManager.behindBlocks.get(loc);
		if (portal != null)
		{
			event.setCancelled(true);
			return;
		}		
	}	
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Material block = event.getBlock().getType();
		
		//Prevent obstructing funnel
		Bridge bridge = plugin.funnelBridgeManager.bridgeBlocks.get(new V10Location(event.getBlock()));
		if (bridge != null )
		{
			event.setCancelled(true);
		}

		
		if (block == Material.RAILS || block == Material.POWERED_RAIL || block == Material.DETECTOR_RAIL) return;
		 
		Portal portal = plugin.portalManager.insideBlocks.get(event.getBlockPlaced().getLocation());
		if (portal != null)
		{
			event.setCancelled(true);
			return;
		}
	}
	 	 
	@EventHandler(ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (event.getBlock().getType() != Material.SUGAR_CANE_BLOCK) return;
		
		Grill grill = plugin.grillManager.insideBlocks.get(event.getBlock().getLocation());
		if (grill == null ) return;
		event.setCancelled(true);
	}
	
	@EventHandler()
	public void onBlockFromTo(BlockFromToEvent event) {
		V10Location vb = new V10Location(event.getBlock());
		 Region region = plugin.regionManager.getRegion(vb);
		 //Liquid teleporting
			if (region. //TODO: region is null!
					getBoolean(
							RegionSetting.
							TELEPORT_LIQUIDS)
							&& 
							!plugin.
							funnelBridgeManager.
							bridgeBlocks.
							containsKey(vb))
			{
				V10Location loc = new V10Location(event.getBlock().getLocation());
					Portal portal = plugin.portalManager.insideBlocks.get(loc);
					if (portal != null && portal.open && portal.owner != null)
					{
						Portal destination;
						if (portal.orange)
							destination = portal.owner.bluePortal;
						else
							destination = portal.owner.orangePortal;
						
						Material blockt = Material.AIR;
						switch (event.getBlock().getType())
						{
							case WATER:
							case STATIONARY_WATER:
								blockt = Material.WATER;
								break;
							case LAVA:
							case STATIONARY_LAVA:
								blockt = Material.LAVA;
								break;
						}
						
						if (destination != null)
						{
							final Block destb = destination.teleport.getHandle().getBlock();
							final Block source = event.getBlock();
							if (destb.getType() == Material.AIR)
							{
								destb.setType(blockt);
								plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RemoveLiquid(plugin, source, destb, destination), 10L);
	
							}
							
							event.setCancelled(true);
						}
					}
			}
			V10Location tb = new V10Location(event.getToBlock());
				//Funnel
				if (plugin.funnelBridgeManager.bridgeBlocks.containsKey(vb) && plugin.funnelBridgeManager.bridgeBlocks.containsKey(tb)) 
				{
					if (!(plugin.funnelBridgeManager.bridgeBlocks.get(vb) instanceof Funnel && plugin.funnelBridgeManager.bridgeBlocks.get(tb) instanceof Funnel))
						{
							event.setCancelled(true);
							return;
						}
					
					Funnel funnel1 = (Funnel) plugin.funnelBridgeManager.bridgeBlocks.get(vb);
					Funnel funnel2 = (Funnel) plugin.funnelBridgeManager.bridgeBlocks.get(tb);
					if (!funnel1.equals(funnel2))
					{
						event.setCancelled(true);
						return;
					}
					
					int numfrom = funnel1.getCounter(vb);
					int numto = funnel1.getCounter(tb);
					
					if (numfrom < numto || numfrom < 0 || numto < 0)
					{
						event.setCancelled(true);
						return;
					}
					
				
				}
				else if (plugin.funnelBridgeManager.bridgeBlocks.containsKey(vb) || plugin.funnelBridgeManager.bridgeBlocks.containsKey(tb))
				{
					event.setCancelled(true);
					return;
				}
			}
	 
	@EventHandler()
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		 Block block = event.getBlock();
		 V10Location loc = new V10Location(block);
		 Region region = plugin.regionManager.getRegion(loc);
		 
		 //Infinite Dispensers
		 if (region.getBoolean(RegionSetting.INFINITE_DISPENSERS) && event.getNewCurrent() > 0)
		 {
			 Block poweredBlock = null;
			 for (int i = 0; i < 5; i++)
				 if (block.getRelative(BlockFace.values()[i]).getType() == Material.DISPENSER) 
					 	poweredBlock = block.getRelative(BlockFace.values()[i]);
			 
			 if (poweredBlock != null )
			 {
				 Dispenser dispenser = (Dispenser) poweredBlock.getState();
				 ItemStack item = dispenser.getInventory().getItem(4);
				 if (item != null && item.getType() != Material.AIR)
				 {
					 item.setAmount(item.getAmount() + 1);
					 dispenser.getInventory().setItem(4, item);
				 }
			 }
		 }
		 
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
					 		block2 = b.getHandle().getBlock();
					 		if(block2.getType() == mat2)
				 			block2.setType(mat1);
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
				 if (grill == null) 
					 {
					 	grill = plugin.grillManager.borderBlocks.get(block.getRelative(BlockFace.values()[i]).getLocation());
					 	if (grill != null) break;
					 }
			 }
			 
			 if (grill != null )
			 {
				 
				 if (event.getNewCurrent() > 0)
					 grill.disable();
			     else
			    	 grill.enable();
			 }
		 }
		 
		 //Turning off bridges or reversing funnels
		 if (region.getBoolean(RegionSetting.ENABLE_BRIDGE_REDSTONE_DISABLING) && block.getType() != Material.REDSTONE_TORCH_ON && block.getType() != Material.REDSTONE_TORCH_OFF) 
		 {
			 Bridge bridge = null;
			 Boolean cblock = false;
			 for (int i = 0; i < 5; i++)
			 {
				 if (bridge == null) 
					 {
					 	bridge = plugin.funnelBridgeManager.bridgeMachineBlocks.get(block.getRelative(BlockFace.values()[i]));
					 	if (bridge != null) 
					 	{
					 		cblock = block.getRelative(BlockFace.values()[i]) == bridge.creationBlock;
					 		break;
					 	}
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
			 for (int i = 0; i < 5; i++)
			 {
				 if (block.getRelative(BlockFace.values()[i]).getType() == Material.WOOL)
				 {
					 plugin.portalManager.tryPlacingAutomatedPortal(new V10Location(block.getRelative(BlockFace.values()[i])));
				 }
			 }
		 }
		 

		 
			 
	 }
	 
	 public void onBlockPistonExtend(BlockPistonExtendEvent event) 
	 {
		 if (event.isCancelled()) return;
		 Region region = plugin.regionManager.getRegion(new V10Location(event.getBlock()));

		 for (Block b : event.getBlocks())
		 {
			 if (blockedPistonBlocks.contains(b))
			 {
				 event.setCancelled(true);
				 return;
			 }
			 
			 Portal portal = plugin.portalManager.insideBlocks.get(b.getRelative(event.getDirection()).getLocation());
			 if (portal != null && region.getBoolean(RegionSetting.ENABLE_PISTON_BLOCK_TELEPORT))
			 {
				 Portal destP = portal.getDestination();
				 final Block destB = destP.teleport.getHandle().getBlock();
				 
				 if (!portal.open || !destP.open)
				 {
					 event.setCancelled(true);
					 return;
				 }
				 
				 if (destB.isLiquid() || destB.getType() == Material.AIR)
				 {
					 final Block endBlock = b.getRelative(event.getDirection());
					 
					 blockedPistonBlocks.add(endBlock);
					 
					 plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

						    public void run() {
						    	destB.setType(endBlock.getType());
							 	 destB.setData(endBlock.getData(), false);
							 	 
							 	endBlock.setType(Material.AIR);
							 	blockedPistonBlocks.remove(endBlock);
						    }
						}, 2L);
					 
					 
				 }
				 else
				 {
					 event.setCancelled(true);
				 }
			 }
			 else if (plugin.portalManager.borderBlocks.containsKey(b.getLocation()) || plugin.grillManager.borderBlocks.containsKey(b.getLocation()) || plugin.grillManager.insideBlocks.containsKey(b.getLocation()))
			 {
				 event.setCancelled(true);
			 }
		 }
	 }
	 
	 public void onBlockPistonRetract(BlockPistonRetractEvent event) 
	 {
		 if (event.isCancelled() || !event.isSticky()) return;
		 
		 if (blockedPistonBlocks.contains(event.getRetractLocation().getBlock()))
		 {
			 event.setCancelled(true);
			 return;
		 }
		 
		 Region region = plugin.regionManager.getRegion(new V10Location(event.getBlock()));

		 Portal portal = plugin.portalManager.insideBlocks.get(event.getRetractLocation());
		 
		 if (portal != null && region.getBoolean(RegionSetting.ENABLE_PISTON_BLOCK_TELEPORT))
		 {
			 Portal destP = portal.getDestination();
			 final Block sourceB = destP.teleport.getHandle().getBlock();
			 
			 if (!sourceB.isLiquid() && sourceB.getType() != Material.AIR)
			 {
				 final Block endBlock = event.getRetractLocation().getBlock().getRelative(event.getDirection().getOppositeFace());
				 blockedPistonBlocks.add(endBlock);
				 plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

					    public void run() {
					    	endBlock.setType(sourceB.getType());
					    	endBlock.setData(sourceB.getData());
							 
							 sourceB.setType(Material.AIR);
							 blockedPistonBlocks.remove(endBlock);

					    }
					}, 1L);
				 
				 
			 }
		 }
		 else if (plugin.portalManager.borderBlocks.containsKey(event.getRetractLocation()) || plugin.grillManager.borderBlocks.containsKey(event.getRetractLocation()) || plugin.grillManager.insideBlocks.containsKey(event.getRetractLocation()))
				 {
					 event.setCancelled(true);
				 }
		 
		 //Update bridge if piston made space
		 plugin.funnelBridgeManager.updateBridge(new V10Location(event.getRetractLocation()));
	 }
	 
	 public class RemoveLiquid implements Runnable
		{
			PortalStick plugin = null;
			Block source = null;
			Block destination = null;
			Portal exit = null;
			public RemoveLiquid(PortalStick Plugin, Block Source, Block Destination, Portal Exit){
				plugin = Plugin;
				source = Source;
				destination = Destination;
				exit = Exit;
			}
			@Override
			public void run() {
				if (!(source.getTypeId() <12 && source.getTypeId() > 6) || !exit.open)
					destination.setType(Material.AIR);
				else
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RemoveLiquid(plugin, source, destination, exit), 10L);

			    		
			}
		}
	 	 
	 
	 
	 

}
