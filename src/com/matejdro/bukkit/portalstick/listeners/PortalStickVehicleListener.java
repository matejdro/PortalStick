package com.matejdro.bukkit.portalstick.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class PortalStickVehicleListener implements Listener {
	private final PortalStick plugin;
	
	public PortalStickVehicleListener(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler()
	public void onVehicleMove(VehicleMoveEvent event) {
		Vehicle vehicle = event.getVehicle();
		Vector vector = vehicle.getVelocity();
		Location LocTo = event.getTo();
		LocTo = new Location(LocTo.getWorld(), LocTo.getBlockX(), LocTo.getBlockY(), LocTo.getBlockZ());
		Region regionTo = plugin.regionManager.getRegion(event.getTo());
		
		if (!regionTo.getBoolean(RegionSetting.TELEPORT_VEHICLES)) return;
		
		//Portals
		if (vehicle.getPassenger() != null && vehicle.getPassenger() instanceof Player) 
			if (!plugin.permission.teleport((Player) vehicle.getPassenger())) return;
			 
		plugin.entityManager.teleport((Entity) vehicle, LocTo, vector);
	}

}
