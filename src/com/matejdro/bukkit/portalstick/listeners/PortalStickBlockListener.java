package com.matejdro.bukkit.portalstick.listeners;

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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;

import com.matejdro.bukkit.portalstick.Grill;
import com.matejdro.bukkit.portalstick.GrillManager;
import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalManager;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.RegionManager;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class PortalStickBlockListener extends BlockListener {
private PortalStick plugin;

public PortalStickBlockListener(PortalStick instance)
{
	plugin = instance;
}

	public void onBlockBreak(BlockBreakEvent event) {
		Region region = RegionManager.getRegion(event.getBlock().getLocation());
		Material type = event.getBlock().getType();
		Location loc = event.getBlock().getLocation();
		if (type == Material.WOOL)
		{
			Portal portal = PortalManager.borderblocks.get(loc);
			if (portal == null) portal = PortalManager.insideblocks.get(loc);
			if (portal != null)
			{
				portal.delete();
				event.setCancelled(true);
				return;
			}		
		}
		
		Grill grill = GrillManager.insideblocks.get(event.getBlock().getLocation());
		if (grill != null )
		{
				event.setCancelled(true);
				return;
		}
		
		
		if (Util.compareBlockToString(event.getBlock(), region.getString(RegionSetting.GRILL_MATERIAL)))
		{
			grill = GrillManager.borderblocks.get(event.getBlock().getLocation());
				if (grill == null || !Permission.deleteGrill(event.getPlayer())) return;
				grill.delete();
				return;
		}

	}
	
	public void onBlockBurn(BlockBurnEvent event) {
		if (event.getBlock().getType() != Material.WOOL) return;
		
		Location loc = event.getBlock().getLocation();
		
		Portal portal = PortalManager.borderblocks.get(loc);
		if (portal == null) portal = PortalManager.insideblocks.get(loc);
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
		 
		
		Portal portal = PortalManager.insideblocks.get(loc);
		if (portal != null)
		{
			event.setCancelled(true);
			return;
		}
	}
	 	 
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (event.getBlock().getType() != Material.SUGAR_CANE_BLOCK) return;
		 
		Grill grill = GrillManager.insideblocks.get(event.getBlock().getLocation());
		if (grill == null ) return;
		event.setCancelled(true);
		return;

	}
	
	 public void onBlockFromTo(BlockFromToEvent event) {
		 Region region = RegionManager.getRegion(event.getBlock().getLocation());
			if (!region.getBoolean(RegionSetting.TELEPORT_LIQUIDS))
				return;
				Portal portal = PortalManager.insideblocks.get(event.getBlock().getLocation());
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
		 if (event.getNewCurrent() == 0) return;
		 Block block = event.getBlock();
		 Block dispenserb = null;
		 
		 Region region = RegionManager.getRegion(block.getLocation());
		 if (!region.getBoolean(RegionSetting.INFINITE_DISPENSERS))
				return;

		 
		 if (block.getFace(BlockFace.NORTH).getType() == Material.DISPENSER) dispenserb = block.getFace(BlockFace.NORTH);
		 else if (block.getFace(BlockFace.SOUTH).getType() == Material.DISPENSER) dispenserb = block.getFace(BlockFace.SOUTH);
		 else if (block.getFace(BlockFace.WEST).getType() == Material.DISPENSER) dispenserb = block.getFace(BlockFace.WEST);
		 else if (block.getFace(BlockFace.EAST).getType() == Material.DISPENSER) dispenserb = block.getFace(BlockFace.EAST);
		 
		 if (dispenserb != null)
		 {
			 Dispenser dispenser = (Dispenser) dispenserb.getState();
			 ItemStack item = dispenser.getInventory().getItem(4);
			 if (item != null && item.getType() != Material.AIR)
			 {
				 item.setAmount(item.getAmount() + 1);
				 dispenser.getInventory().setItem(4, item);
			 }
		 }
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
