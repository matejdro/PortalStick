package com.matejdro.bukkit.portalstick.listeners;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.PistonBaseMaterial;

import com.matejdro.bukkit.portalstick.Bridge;
import com.matejdro.bukkit.portalstick.BridgeManager;
import com.matejdro.bukkit.portalstick.Grill;
import com.matejdro.bukkit.portalstick.GrillManager;
import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalManager;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.RegionManager;
import com.matejdro.bukkit.portalstick.util.BlockUtil;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class PortalStickBlockListener extends BlockListener {
	
	private PortalStick plugin;
	private HashSet<Block> blockedPistonBlocks = new HashSet<Block>();	
	
	public PortalStickBlockListener(PortalStick instance)
	{
		plugin = instance;
	}

	public void onBlockBreak(BlockBreakEvent event) {
		Region region = RegionManager.getRegion(event.getBlock().getLocation());
		Material type = event.getBlock().getType();
		Location loc = event.getBlock().getLocation();
			Portal portal = PortalManager.borderBlocks.get(loc);
			if (portal == null) portal = PortalManager.insideBlocks.get(loc);
			if (portal == null) portal = PortalManager.behindBlocks.get(loc);
			if (portal != null)
			{
				portal.delete();
				event.setCancelled(true);
			}
		
		Grill grill = GrillManager.insideBlocks.get(event.getBlock().getLocation());
		if (grill != null )
		{
				event.setCancelled(true);
		}
		
		//Prevend destroying bridge
		Bridge bridge = BridgeManager.bridgeBlocks.get(event.getBlock());
		if (bridge != null )
		{
				event.setCancelled(true);
		}
		//Delete bridge
		bridge = BridgeManager.bridgeMachineBlocks.get(event.getBlock());
		if (bridge != null )
		{
				if (Permission.deleteBridge(event.getPlayer()))
					bridge.delete();
				else
					event.setCancelled(true);
		}
		
		//Update bridge if destroyed block made space
		 BridgeManager.updateBridge(event.getBlock());
		
		
		if (BlockUtil.compareBlockToString(event.getBlock(), region.getString(RegionSetting.GRILL_MATERIAL)))
		{
			grill = GrillManager.borderBlocks.get(event.getBlock().getLocation());
				if (grill == null || !Permission.deleteGrill(event.getPlayer())) return;
				grill.delete();
		}
		
		if (type == Material.REDSTONE_WIRE && region.getBoolean(RegionSetting.ENABLE_REDSTONE_TRANSFER))
		{
			Location l = event.getBlock().getLocation();
			 
			 for (int i = 0; i < 4; i++)
			 {
				 BlockFace face = BlockFace.values()[i];
				 if (PortalManager.insideBlocks.containsKey(new Location(l.getWorld(), l.getX() + face.getModX(), l.getY() + face.getModY(), l.getZ() + face.getModZ()))) 
					 {
					 	portal = PortalManager.insideBlocks.get(new Location(l.getWorld(), l.getX() + face.getModX(), l.getY() + face.getModY(), l.getZ() + face.getModZ()));
					 	if (!portal.isOpen()) continue;
					 
					 	Portal destination = portal.getDestination();
					 	if (destination == null || destination.isTransmitter()) continue;
					 	
				 		for (Block b: destination.getInside())
				 			b.setType(Material.AIR);
				 		portal.setTransmitter(false);
					 }
			 }
		}
			

	}
	
	public void onBlockBurn(BlockBurnEvent event) {
		if (event.getBlock().getType() != Material.WOOL) return;
		
		Location loc = event.getBlock().getLocation();
		
		Portal portal = PortalManager.borderBlocks.get(loc);
		if (portal == null) portal = PortalManager.insideBlocks.get(loc);
		if (portal == null) portal = PortalManager.behindBlocks.get(loc);
		if (portal != null)
		{
			event.setCancelled(true);
			return;
		}		
	}	
	
	public void onBlockPlace(BlockPlaceEvent event) {
		Material block = event.getBlock().getType();
		Location loc = event.getBlockPlaced().getLocation();
		
		if (block == Material.RAILS || block == Material.POWERED_RAIL || block == Material.DETECTOR_RAIL) return;
		 
		
		Portal portal = PortalManager.insideBlocks.get(loc);
		if (portal != null)
		{
			event.setCancelled(true);
			return;
		}
	}
	 	 
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (event.getBlock().getType() != Material.SUGAR_CANE_BLOCK) return;
		
		Grill grill = GrillManager.insideBlocks.get(event.getBlock().getLocation());
		if (grill == null ) return;
		event.setCancelled(true);
	}
	
	 public void onBlockFromTo(BlockFromToEvent event) {
		 Region region = RegionManager.getRegion(event.getBlock().getLocation());
			if (!region.getBoolean(RegionSetting.TELEPORT_LIQUIDS))
				return;
				Portal portal = PortalManager.insideBlocks.get(event.getBlock().getLocation());
				if (portal != null && portal.isOpen() && portal.getOwner() != null)
				{
					Portal destination;
					if (portal.isOrange())
						destination = portal.getOwner().getBluePortal();
					else
						destination = portal.getOwner().getOrangePortal();
					
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
						final Block destb = destination.getTeleportLocation().getBlock();
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
	 
	 public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		 Block block = event.getBlock();
		 Region region = RegionManager.getRegion(block.getLocation());
		 
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
				 if (PortalManager.insideBlocks.containsKey(new Location(l.getWorld(), l.getX() + face.getModX(), l.getY() + face.getModY(), l.getZ() + face.getModZ()))) 
					 {
					 	Portal portal = PortalManager.insideBlocks.get(new Location(l.getWorld(), l.getX() + face.getModX(), l.getY() + face.getModY(), l.getZ() + face.getModZ()));
					 	if (!portal.isOpen()) continue;
					 
					 	Portal destination = portal.getDestination();
					 	if (destination == null || destination.isTransmitter()) continue;
					 
					 	if (event.getNewCurrent() > 0)
					 	{
					 		portal.setTransmitter(true);
					 		for (Block b: destination.getInside())
					 			b.setType(Material.REDSTONE_TORCH_ON);

					 	}
					 	else
					 	{
					 		for (Block b: destination.getInside())
					 			b.setType(Material.AIR);
					 		portal.setTransmitter(false);
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
					 	grill = GrillManager.borderBlocks.get(block.getRelative(BlockFace.values()[i]).getLocation());
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
		 
		 //Turning off bridges
		 if (region.getBoolean(RegionSetting.ENABLE_BRIDGE_REDSTONE_DISABLING) && block.getType() != Material.REDSTONE_TORCH_ON && block.getType() != Material.REDSTONE_TORCH_OFF) 
		 {
			 Bridge bridge = null;
			 for (int i = 0; i < 5; i++)
			 {
				 if (bridge == null) 
					 {
					 	bridge = BridgeManager.bridgeMachineBlocks.get(block.getRelative(BlockFace.values()[i]));
					 	if (bridge != null) break;
					 }
			 }
			 
			 if (bridge != null )
			 {
				 if (event.getNewCurrent() > 0)
					 bridge.deactivate();
			     else
			    	 bridge.activate();
			 }
		 }
		 
			 
	 }
	 
	 public void onBlockPistonExtend(BlockPistonExtendEvent event) 
	 {
		 if (event.isCancelled()) return;
		 Region region = RegionManager.getRegion(event.getBlock().getLocation());

		 for (Block b : event.getBlocks())
		 {
			 if (blockedPistonBlocks.contains(b))
			 {
				 event.setCancelled(true);
				 return;
			 }
			 
			 Portal portal = PortalManager.insideBlocks.get(b.getRelative(event.getDirection()).getLocation());
			 if (portal != null && region.getBoolean(RegionSetting.ENABLE_PISTON_BLOCK_TELEPORT))
			 {
				 Portal destP = portal.getDestination();
				 final Block destB = destP.getTeleportLocation().getBlock();
				 
				 if (!portal.isOpen() || !destP.isOpen())
				 {
					 event.setCancelled(true);
					 return;
				 }
				 
				 if (destB.isLiquid() || destB.getType() == Material.AIR)
				 {
					 final Block endBlock = b.getRelative(event.getDirection());
					 
					 blockedPistonBlocks.add(endBlock);
					 
					 PortalStick.instance.getServer().getScheduler().scheduleSyncDelayedTask(PortalStick.instance, new Runnable() {

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
			 else if (PortalManager.borderBlocks.containsKey(b.getLocation()) || GrillManager.borderBlocks.containsKey(b.getLocation()) || GrillManager.insideBlocks.containsKey(b.getLocation()))
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
		 
		 Region region = RegionManager.getRegion(event.getBlock().getLocation());

		 Portal portal = PortalManager.insideBlocks.get(event.getRetractLocation());
		 
		 if (portal != null && region.getBoolean(RegionSetting.ENABLE_PISTON_BLOCK_TELEPORT))
		 {
			 Portal destP = portal.getDestination();
			 final Block sourceB = destP.getTeleportLocation().getBlock();
			 
			 if (!sourceB.isLiquid() && sourceB.getType() != Material.AIR)
			 {
				 final Block endBlock = event.getRetractLocation().getBlock().getRelative(event.getDirection().getOppositeFace());
				 blockedPistonBlocks.add(endBlock);
				 PortalStick.instance.getServer().getScheduler().scheduleSyncDelayedTask(PortalStick.instance, new Runnable() {

					    public void run() {
					    	endBlock.setType(sourceB.getType());
					    	endBlock.setData(sourceB.getData());
							 
							 sourceB.setType(Material.AIR);
							 blockedPistonBlocks.remove(endBlock);

					    }
					}, 1L);
				 
				 
			 }
		 }
		 else if (PortalManager.borderBlocks.containsKey(event.getRetractLocation()) || GrillManager.borderBlocks.containsKey(event.getRetractLocation()) || GrillManager.insideBlocks.containsKey(event.getRetractLocation()))
				 {
					 event.setCancelled(true);
				 }
		 
		 //Update bridge if piston made space
		 BridgeManager.updateBridge(event.getRetractLocation().getBlock());
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
				if (!(source.getTypeId() <12 && source.getTypeId() > 6) || !exit.isOpen())
					destination.setType(Material.AIR);
				else
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RemoveLiquid(plugin, source, destination, exit), 10L);

			    		
			}
		}
	 	 
	 
	 
	 

}
