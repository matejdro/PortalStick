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

public class PortalStickBlockListener implements Listener {
	
	private PortalStick plugin;
	private HashSet<Block> blockedPistonBlocks = new HashSet<Block>();	
	
	public PortalStickBlockListener(PortalStick instance)
	{
		plugin = instance;
	}

	@EventHandler()
	public void onBlockBreak(BlockBreakEvent event) {
		Region region = plugin.regionManager.getRegion(event.getBlock().getLocation());
		Material type = event.getBlock().getType();
		Location loc = event.getBlock().getLocation();
			Portal portal = plugin.portalManager.borderBlocks.get(loc);
			if (portal == null) portal = plugin.portalManager.insideBlocks.get(loc);
			if (portal == null) portal = plugin.portalManager.behindBlocks.get(loc);
			if (portal != null)
			{
				portal.delete();
				event.setCancelled(true);
			}
		
		if (event.isCancelled()) return;	
			
		Grill grill = plugin.grillManager.insideBlocks.get(event.getBlock().getLocation());
		if (grill != null )
		{
				event.setCancelled(true);
		}
		
		//Prevent destroying bridge
		Bridge bridge = plugin.funnelBridgeManager.bridgeBlocks.get(event.getBlock());
		if (bridge != null )
		{
				event.setCancelled(true);
				return;
		}
		//Delete bridge
		bridge = plugin.funnelBridgeManager.bridgeMachineBlocks.get(event.getBlock());
		if (bridge != null )
		{
			if (plugin.hasPermission(event.getPlayer(), plugin.PERM_DELETE_BRIDGE))
				bridge.delete();
			else
				event.setCancelled(true);
			return;
		}
		
		//Update bridge if destroyed block made space
		plugin.funnelBridgeManager.updateBridge(event.getBlock());
		
		
		if (plugin.blockUtil.compareBlockToString(event.getBlock(), region.getString(RegionSetting.GRILL_MATERIAL)))
		{
			grill = plugin.grillManager.borderBlocks.get(event.getBlock().getLocation());
				if (grill == null || !plugin.hasPermission(event.getPlayer(), plugin.PERM_DELETE_GRILL)) return;
				grill.delete();
		}
		
		if (type == Material.REDSTONE_WIRE && region.getBoolean(RegionSetting.ENABLE_REDSTONE_TRANSFER))
		{
			Location l = event.getBlock().getLocation();
			 
			 for (int i = 0; i < 4; i++)
			 {
				 BlockFace face = BlockFace.values()[i];
				 if (plugin.portalManager.insideBlocks.containsKey(new Location(l.getWorld(), l.getX() + face.getModX(), l.getY() + face.getModY(), l.getZ() + face.getModZ()))) 
					 {
					 	portal = plugin.portalManager.insideBlocks.get(new Location(l.getWorld(), l.getX() + face.getModX(), l.getY() + face.getModY(), l.getZ() + face.getModZ()));
					 	if (!portal.open) continue;
					 
					 	Portal destination = portal.getDestination();
					 	if (destination == null || destination.transmitter) continue;
					 	
				 		for (Block b: destination.inside)
				 			b.setType(Material.AIR);
				 		portal.transmitter = false;
					 }
			 }
		}
			

	}
	
	@EventHandler()
	public void onBlockBurn(BlockBurnEvent event) {
		if (event.isCancelled()) return;	
		if (event.getBlock().getType() != Material.WOOL) return;
		
		Location loc = event.getBlock().getLocation();
		
		Portal portal = plugin.portalManager.borderBlocks.get(loc);
		if (portal == null) portal = plugin.portalManager.insideBlocks.get(loc);
		if (portal == null) portal = plugin.portalManager.behindBlocks.get(loc);
		if (portal != null)
		{
			event.setCancelled(true);
			return;
		}		
	}	
	
	@EventHandler()
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) return;	
		Material block = event.getBlock().getType();
		Location loc = event.getBlockPlaced().getLocation();
		
		//Prevent obstructing funnel
		Bridge bridge = plugin.funnelBridgeManager.bridgeBlocks.get(event.getBlock());
		if (bridge != null )
		{
				event.setCancelled(true);
		}

		
		if (block == Material.RAILS || block == Material.POWERED_RAIL || block == Material.DETECTOR_RAIL) return;
		 
		Portal portal = plugin.portalManager.insideBlocks.get(loc);
		if (portal != null)
		{
			event.setCancelled(true);
			return;
		}
	}
	 	 
	@EventHandler()
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (event.isCancelled()) return;	
		if (event.getBlock().getType() != Material.SUGAR_CANE_BLOCK) return;
		
		Grill grill = plugin.grillManager.insideBlocks.get(event.getBlock().getLocation());
		if (grill == null ) return;
		event.setCancelled(true);
	}
	
	@EventHandler()
	public void onBlockFromTo(BlockFromToEvent event) {
		 Region region = plugin.regionManager.getRegion(event.getBlock().getLocation());
		 
		 //Liquid teleporting
			if (region.getBoolean(RegionSetting.TELEPORT_LIQUIDS) && !plugin.funnelBridgeManager.bridgeBlocks.containsKey(event.getBlock()))
			{
					Portal portal = plugin.portalManager.insideBlocks.get(event.getBlock().getLocation());
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
							final Block destb = destination.teleport.getBlock();
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
			
				//Funnel
				if (plugin.funnelBridgeManager.bridgeBlocks.containsKey(event.getBlock()) && plugin.funnelBridgeManager.bridgeBlocks.containsKey(event.getToBlock())) 
				{
					if (!(plugin.funnelBridgeManager.bridgeBlocks.get(event.getBlock()) instanceof Funnel && plugin.funnelBridgeManager.bridgeBlocks.get(event.getToBlock()) instanceof Funnel))
						{
							event.setCancelled(true);
							return;
						}
					
					Funnel funnel1 = (Funnel) plugin.funnelBridgeManager.bridgeBlocks.get(event.getBlock());
					Funnel funnel2 = (Funnel) plugin.funnelBridgeManager.bridgeBlocks.get(event.getToBlock());
					if (funnel1 != funnel2) 
					{
						event.setCancelled(true);
						return;
					}
					
					int numfrom = funnel1.getCounter(event.getBlock());
					int numto = funnel1.getCounter(event.getToBlock());
					
					if (numfrom < numto || numfrom < 0 || numto < 0)
					{
						event.setCancelled(true);
						return;
					}
					
				
				}
				else if (plugin.funnelBridgeManager.bridgeBlocks.containsKey(event.getBlock()) || plugin.funnelBridgeManager.bridgeBlocks.containsKey(event.getToBlock()))
				{
					event.setCancelled(true);
					return;
				}
			}
	 
	@EventHandler()
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		 Block block = event.getBlock();
		 Region region = plugin.regionManager.getRegion(block.getLocation());
		 
		 //Infinite Dispensers
		 Block poweredBlock = null;
		 if (region.getBoolean(RegionSetting.INFINITE_DISPENSERS) && event.getNewCurrent() > 0)
		 { 
			 for (int i = 0; i < 5; i++)
			 {
				 if (block.getRelative(BlockFace.values()[i]).getType() == Material.DISPENSER) 
					 {
					 	poweredBlock = block.getRelative(BlockFace.values()[i]);
					 }
			 }
			 
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
			 
			 for (int i = 0; i < 5; i++)
			 {
				 BlockFace face = BlockFace.values()[i];
				 if (plugin.portalManager.insideBlocks.containsKey(new Location(l.getWorld(), l.getX() + face.getModX(), l.getY() + face.getModY(), l.getZ() + face.getModZ()))) 
					 {
					 	Portal portal = plugin.portalManager.insideBlocks.get(new Location(l.getWorld(), l.getX() + face.getModX(), l.getY() + face.getModY(), l.getZ() + face.getModZ()));
					 	if (!portal.open) continue;
					 
					 	Portal destination = portal.getDestination();
					 	if (destination == null || destination.transmitter) continue;
					 
					 	if (event.getNewCurrent() > 0)
					 	{
					 		portal.transmitter = true;
					 		for (Block b: destination.inside)
					 			b.setType(Material.REDSTONE_TORCH_ON);

					 	}
					 	else
					 	{
					 		for (Block b: destination.inside)
					 			b.setType(Material.AIR);
					 		portal.transmitter = false;
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
					 		cblock = block.getRelative(BlockFace.values()[i]) == bridge.getCreationBlock();
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
					 plugin.portalManager.tryPlacingAutomatedPortal(block.getRelative(BlockFace.values()[i]));
				 }
			 }
		 }
		 

		 
			 
	 }
	 
	 public void onBlockPistonExtend(BlockPistonExtendEvent event) 
	 {
		 if (event.isCancelled()) return;
		 Region region = plugin.regionManager.getRegion(event.getBlock().getLocation());

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
				 final Block destB = destP.teleport.getBlock();
				 
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
		 
		 Region region = plugin.regionManager.getRegion(event.getBlock().getLocation());

		 Portal portal = plugin.portalManager.insideBlocks.get(event.getRetractLocation());
		 
		 if (portal != null && region.getBoolean(RegionSetting.ENABLE_PISTON_BLOCK_TELEPORT))
		 {
			 Portal destP = portal.getDestination();
			 final Block sourceB = destP.teleport.getBlock();
			 
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
		 plugin.funnelBridgeManager.updateBridge(event.getRetractLocation().getBlock());
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
