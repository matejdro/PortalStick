package com.matejdro.bukkit.portalstick;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class PortalStickBlockListener extends BlockListener {
	private PortalStick plugin;

	public PortalStickBlockListener(PortalStick instance)
	{
		plugin = instance;
	}
	
	
	public void onBlockBreak(BlockBreakEvent event) {
		Region region = Config.getRegion(event.getBlock().getLocation());
		Material type = event.getBlock().getType();
		if (type == Material.WOOL)
		{
			for (Portal p : PortalStick.portals)
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
			for (Grill grill: PortalStick.grills)
			 {
				 if (grill.getInside().contains(event.getBlock()))
				 {
					 event.setCancelled(true);
					 return;
				 }
			 }

		}
		
		if (event.getBlock().getTypeId() == region.getInt(Setting.GRILL_MATERIAL))
			for (Grill grill: PortalStick.grills)
			 {
				 if (grill.getBorder().contains(event.getBlock()))
				 {
					 if (!PortalStick.permission(event.getPlayer(), "portalstick.deletegrill", true)) return;
					 grill.delete();
					 return;
				 }
			 }


	}
	
	public void onBlockBurn(BlockBurnEvent event) {
		if (event.getBlock().getType() != Material.WOOL) return;
		
		for (Portal p : PortalStick.portals)
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
		 
		 for (Portal p : PortalStick.portals)
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
		 
		 for (Grill grill: PortalStick.grills)
		 {
			 if (grill.getInside().contains(event.getBlock()))
			 {
				 event.setCancelled(true);
				 return;
			 }
		 }
			
	 }
	 
	 public void onBlockIgnite(BlockIgniteEvent event) {
		 if (event.getPlayer() == null) return;
		 
		 if (event.getCause() == IgniteCause.FLINT_AND_STEEL)
		 {
			 if (!PortalStick.permission(event.getPlayer(), "portalstick.creategrill", true)) return;
			 if (plugin.PlaceEmancipationGrill(event.getBlock().getRelative(0, -1, 0), event.getPlayer())) event.setCancelled(true);
			 
		 }
			 
	 }


	 

	
}
