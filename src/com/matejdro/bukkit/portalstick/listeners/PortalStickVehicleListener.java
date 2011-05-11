package com.matejdro.bukkit.portalstick.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalManager;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.RegionManager;
import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class PortalStickVehicleListener extends VehicleListener {
	private PortalStick plugin;
	
	public PortalStickVehicleListener(PortalStick instance)
	{
		plugin = instance;
	}
	
	public void onVehicleMove(VehicleMoveEvent event) {
		Vehicle vehicle = event.getVehicle();
		Vector vector = vehicle.getVelocity();
		Block blockTo = event.getTo().getBlock();
		Region regionTo = RegionManager.getRegion(event.getTo());
		
		if (!regionTo.getBoolean(RegionSetting.TELEPORT_VEHICLES)) return;
		
		//Portals
		if (vehicle.getPassenger() != null && vehicle.getPassenger() instanceof Player) 
			if (!Permission.teleport((Player) vehicle.getPassenger())) return;
			 
		//Aiming assistant: 
		double addX = 1.0;
		double addY = 1.0;
		double addZ = 1.0;
		if (Math.abs(vector.getX()) > 1)
		{
			addX += 3.0;
			addY += 2.0;
			addZ += 2.0;
		}
		if (Math.abs(vector.getZ()) > 1)
		{
			addX += 2.0;
			addY += 2.0;
			addZ += 3.0;
		}
		if (Math.abs(vector.getY()) > 1)
		{
			addX += 2.0;
			addY += 3.0;
			addZ += 2.0;
		}
				 
		Portal portal = null;
		for (Portal p : PortalManager.portals)
		{
			for (Block b : p.getInside())
			{
				if (offsetequals(b.getLocation().getX(), blockTo.getX(),addX) && offsetequals(b.getLocation().getZ(), blockTo.getZ(), addZ) && offsetequals(b.getLocation().getY(), blockTo.getY(),addY))
				{
					portal = p;
					break;
				}
			}	 
		}
			 
		if (portal != null)
		{
			if (!portal.isOpen() || portal.isDisabled()) return;
			User owner = portal.getOwner();
				 
			Location teleport;
			Portal destination;
			if (portal.isOrange())
				destination = owner.getBluePortal();
			else
				destination = owner.getOrangePortal();
				 				 
			teleport = destination.getTeleportLocation().clone();
								 
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
			momentum = momentum * regionTo.getDouble(RegionSetting.VELOCITY_MULTIPLIER);

			//reposition velocity to match output portal's orientation
			Vector outvector = vehicle.getVelocity().zero();
			switch(destination.getTeleportFace())
	        {
	        	case NORTH:
	        		yaw = 270;
	        		outvector = outvector.setX(momentum);
	        		break;
	        	case EAST:
	        		yaw = 0;
	        		outvector = outvector.setZ(momentum);
	        		break;
	        	case SOUTH:
	        		yaw = 90;
	        		outvector = outvector.setX(-momentum);
	        		break;
	        	case WEST:
	        		yaw = 180;
	        		outvector = outvector.setZ(-momentum);
	        		break;
	        	case UP:
	        		pitch = 90;
	        		outvector = outvector.setY(momentum);
	        		break;
	        	case DOWN:
	        		pitch = -90;
	        		outvector = outvector.setY(-momentum);
	        		break;
	        }
				 				
			vehicle.setFallDistance(0);	
			vehicle.setVelocity(vehicle.getVelocity().zero());
				 
			teleport.setPitch(pitch);
			teleport.setYaw(yaw);
				 
			vehicle.teleport(teleport);
				 				 
			vehicle.setVelocity(outvector);
				 
			destination.setDisabled(true);
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new EnablePortal(destination), 10L);
		}
	}
	
	private Boolean offsetequals(double x, double y, double difference)
	{
		return (x + difference >= y && y + difference >= x );
	}
	
	public class EnablePortal implements Runnable
	{
		Portal portal = null;
		public EnablePortal(Portal p){
			portal = p;
		}
		@Override
		public void run() {
			// this part knows plugin
			if (portal != null) portal.setDisabled(false);
		}
	}


}
