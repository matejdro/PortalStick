package com.matejdro.bukkit.portalstick.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;


import com.matejdro.bukkit.portalstick.Grill;
import com.matejdro.bukkit.portalstick.GrillManager;
import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalManager;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.RegionManager;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class PortalStickBlockListener extends BlockListener {
	
	public void onBlockBreak(BlockBreakEvent event) {
		Region region = RegionManager.getRegion(event.getBlock().getLocation());
		Material type = event.getBlock().getType();
		if (type == Material.WOOL)
		{
			for (Portal p : PortalManager.portals)
			{
				for (Block b : p.getBorder())
				{
					if (event.getBlock() == b)
					{
						event.setCancelled(true);
						p.delete();
						return;
					}
				}
				 
				if (!p.isOpen())
				{
					for (Block b : p.getInside())
					{
						if (event.getBlock() == b)
						{
							event.setCancelled(true);
							p.delete();
							return;
						}
					}
				}
			}
		}
		
		
		if (type == Material.SUGAR_CANE_BLOCK)
		{
			for (Grill grill: GrillManager.grills)
			{
				if (grill.getInside().contains(event.getBlock()))
				{
					event.setCancelled(true);
					return;
				}
			}

		}
		
		if (Util.compareBlockToString(event.getBlock(), region.getString(RegionSetting.GRILL_MATERIAL)))
		{
			for (Grill grill: GrillManager.grills)
			{
				if (grill.getBorder().contains(event.getBlock()))
				{
					if (!Permission.deleteGrill(event.getPlayer())) return;
					grill.delete();
					return;
				}
			}
		}

	}
	
	public void onBlockBurn(BlockBurnEvent event) {
		if (event.getBlock().getType() != Material.WOOL) return;
		
		for (Portal p : PortalManager.portals)
		{
			for (Block b : p.getBorder())
			{
				if (event.getBlock() == b)
				{
					event.setCancelled(true);
					return;
				}
			}
			 
			if (!p.isOpen())
			{
				for (Block b : p.getInside())
				{
					if (event.getBlock() == b)
					{
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent event) {
		Material block = event.getBlock().getType();
		 
		if (block == Material.RAILS || block == Material.POWERED_RAIL || block == Material.DETECTOR_RAIL) return;
		 
		for (Portal p : PortalManager.portals)
		{
			for (Block b : p.getInside())
			{
				if (event.getBlockPlaced() == b)
				{
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	 	 
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (event.getBlock().getType() != Material.SUGAR_CANE_BLOCK) return;
		 
		for (Grill grill: GrillManager.grills)
		{
			if (grill.getInside().contains(event.getBlock()))
			{
				event.setCancelled(true);
				return;
			}
		}
	}

}
