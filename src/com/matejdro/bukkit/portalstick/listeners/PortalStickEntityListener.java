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
import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class PortalStickEntityListener implements Listener {
	private final PortalStick plugin;
	
	public PortalStickEntityListener(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler()
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) return;

		if (event.getEntity() instanceof Player)
		{
			Player player = (Player)event.getEntity();
			if (!plugin.hasPermission(player, plugin.PERM_DAMAGE_BOOTS)) return;
			Region region = plugin.regionManager.getRegion(player.getLocation());
			if (event.getCause() == DamageCause.FALL && region.getBoolean(RegionSetting.ENABLE_FALL_DAMAGE_BOOTS) && region.getInt(RegionSetting.FALL_DAMAGE_BOOTS) == player.getInventory().getBoots().getTypeId())
				event.setCancelled(true);
		}
	}
	
	@EventHandler()
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled()) return;

		Region region = plugin.regionManager.getRegion(event.getLocation());
		for (Block block : event.blockList().toArray(new Block[0])) {
			Location loc = block.getLocation();

			if (block.getType() == Material.WOOL)
			{
				Portal portal = plugin.portalManager.borderBlocks.get(loc);
				if (portal == null) portal = plugin.portalManager.insideBlocks.get(loc);
				if (portal == null) portal = plugin.portalManager.behindBlocks.get(loc);
				if (portal != null)
				{
					portal.delete();
					if (region.getBoolean(RegionSetting.PREVENT_TNT_NEAR_PORTALS)) event.setCancelled(true);
					return;
				}
			}
			
			if (block.getType() == Material.SUGAR_CANE_BLOCK || plugin.blockUtil.compareBlockToString(block, region.getString(RegionSetting.GRILL_MATERIAL)))
			{
				Grill grill = plugin.grillManager.insideBlocks.get(loc);
				if (grill == null) grill = plugin.grillManager.borderBlocks.get(loc);
				if (grill != null )
				{
						event.setCancelled(true);
						return;
				}
			}
		}
	}
	}
