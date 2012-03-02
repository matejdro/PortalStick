package com.matejdro.bukkit.portalstick.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.matejdro.bukkit.portalstick.Grill;
import com.matejdro.bukkit.portalstick.GrillManager;
import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalManager;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.RegionManager;
import com.matejdro.bukkit.portalstick.util.BlockUtil;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class PortalStickEntityListener implements Listener {
	
	@EventHandler()
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) return;

		if (event.getEntity() instanceof Player)
		{
			Player player = (Player)event.getEntity();
			if (!Permission.damageBoots(player)) return;
			Region region = RegionManager.getRegion(player.getLocation());
			if (event.getCause() == DamageCause.FALL && region.getBoolean(RegionSetting.ENABLE_FALL_DAMAGE_BOOTS) && region.getInt(RegionSetting.FALL_DAMAGE_BOOTS) == player.getInventory().getBoots().getTypeId())
				event.setCancelled(true);
		}
	}
	
	@EventHandler()
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled()) return;

		Region region = RegionManager.getRegion(event.getLocation());
		for (Block block : event.blockList().toArray(new Block[0])) {
			Location loc = block.getLocation();

			if (block.getType() == Material.WOOL)
			{
				Portal portal = PortalManager.borderBlocks.get(loc);
				if (portal == null) portal = PortalManager.insideBlocks.get(loc);
				if (portal == null) portal = PortalManager.behindBlocks.get(loc);
				if (portal != null)
				{
					portal.delete();
					if (region.getBoolean(RegionSetting.PREVENT_TNT_NEAR_PORTALS)) event.setCancelled(true);
					return;
				}
			}
			
			if (block.getType() == Material.SUGAR_CANE_BLOCK || BlockUtil.compareBlockToString(block, region.getString(RegionSetting.GRILL_MATERIAL)))
			{
				Grill grill = GrillManager.insideBlocks.get(loc);
				if (grill == null) grill = GrillManager.borderBlocks.get(loc);
				if (grill != null )
				{
						event.setCancelled(true);
						return;
				}
			}
		}
	}
	}
