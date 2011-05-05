package com.matejdro.bukkit.portalstick.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalManager;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.RegionManager;
import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.util.Setting;

public class PortalStickVehicleListener extends VehicleListener {
	
	public void onVehicleMove(VehicleMoveEvent event) {
		Vehicle vehicle = event.getVehicle();
		Vector vector = vehicle.getVelocity();
		Block loc = event.getTo().getBlock();
		Region region = RegionManager.getRegion(loc.getLocation());
		
		if (!region.getBoolean(Setting.TELEPORT_VEHICLES))
			return;
			
		Portal portal = null;
		for (Portal p : PortalManager.portals)
		{
			for (Block b : p.getInside())
			{
				if (offsetequals(b.getLocation().getX(), loc.getX(),1) && offsetequals(b.getLocation().getZ(), loc.getZ(), 1) && offsetequals(b.getLocation().getY(), loc.getY(),1))
				{
					portal = p;
					break;
				}
			}
		 }
		 
		 if (portal != null)
		 {
			if (!portal.isOpen()) return;
			User owner = portal.getOwner();
			 
			Location teleport;
			Portal destination;
			if (portal.isOrange())
				destination = owner.getBluePortal();
			else
				destination = owner.getOrangePortal();
			 
			teleport = destination.getTeleportLocation();
							 
			float yaw = 0;
			float pitch = 0;
			
			//Read input velocity
			
			Double momentum = 0.0;
			switch(portal.getTeleportFace())
			{
				case NORTH:
				case SOUTH:
					momentum = vector.getX();
					break;
				case EAST:
				case WEST:
					momentum = vector.getZ();
					break;
				case UP:
				case DOWN:
					momentum = vector.getY();
					break;
			}
			
			momentum = Math.abs(momentum);

			//reposition velocity to match output portal's orientation
			
			Vector outvector = vehicle.getVelocity().zero();
			switch(destination.getTeleportFace())
			{
				case NORTH:
					yaw = 270;
					outvector.setX(momentum);
					break;
				case EAST:
					yaw = 0;
					outvector.setZ(momentum);
					break;
				case SOUTH:
					yaw = 90;
					outvector.setX(-momentum);
					break;
				case WEST:
					yaw = 180;
					outvector.setZ(-momentum);
					break;
				case UP:
					pitch = 90;
					outvector.setY(momentum);
					break;
				case DOWN:
					pitch = -90;
					outvector.setY(-momentum);
					break;
			}		        	
			 
			vehicle.setFallDistance(0);
			 			 
			vehicle.teleport(teleport);
			 				 
			teleport.setYaw(yaw);
			teleport.setPitch(pitch);
			vehicle.setVelocity(outvector);
			 
		 }
	}
	
	private Boolean offsetequals(double x, double y, double difference)
	{
		return (x + difference >= y && y + difference >= x );
	}

}
