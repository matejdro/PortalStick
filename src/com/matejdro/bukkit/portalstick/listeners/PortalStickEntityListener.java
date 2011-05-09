package com.matejdro.bukkit.portalstick.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityListener;

import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.RegionManager;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class PortalStickEntityListener extends EntityListener {
	
	public void onEntityDamage(EntityDamageEvent event) {
		
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player)event.getEntity();
		if (!Permission.damageBoots(player)) return;
		Region region = RegionManager.getRegion(player.getLocation());
		if (event.getCause() == DamageCause.FALL && region.getBoolean(RegionSetting.ENABLE_FALL_DAMAGE_BOOTS) && region.getInt(RegionSetting.FALL_DAMAGE_BOOTS) == player.getInventory().getBoots().getTypeId())
			event.setCancelled(true);
		
	}

}