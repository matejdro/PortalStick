package com.matejdro.bukkit.portalstick;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class EntityManager implements Runnable {
	private PortalStick plugin;

	public EntityManager(PortalStick instance)
	{
		plugin = instance;
	}

	public static Location teleport(Entity entity, Location LocTo, Vector vector)
	{
		Region regionTo = RegionManager.getRegion(LocTo);
		Portal portal = PortalManager.insideBlocks.get(LocTo);
		if (portal == null && ((Math.abs(vector.getX()) > 0.5 || (Math.abs(vector.getY()) > 1 || (Math.abs(vector.getZ()) > 0.5))) || entity instanceof Boat)) 
		{
			portal = PortalManager.awayBlocksGeneral.get(LocTo);
			if (portal == null && (Math.abs(vector.getX()) > 0.5)) portal = PortalManager.awayBlocksX.get(LocTo);
			if (portal == null && (Math.abs(vector.getY()) > 1)) portal = PortalManager.awayBlocksY.get(LocTo);
			if (portal == null && (Math.abs(vector.getZ()) > 0.5)) portal = PortalManager.awayBlocksZ.get(LocTo);
		}
		
		
		if (portal != null)
		{
			if (!portal.isOpen() || portal.isDisabled()) return null;
			if (Math.abs(vector.getY()) > 1 && !portal.isVertical()) return null;
			User owner = portal.getOwner();
				 
			Location teleport;
			Portal destination;
			if (portal.isOrange())
				destination = owner.getBluePortal();
			else
				destination = owner.getOrangePortal();
				 				 
			teleport = destination.getTeleportLocation().clone();
								 
			float yaw = entity.getLocation().getYaw();
			float pitch = entity.getLocation().getPitch();
			float startyaw = yaw;
			switch(portal.getTeleportFace())
	        {
	        	case EAST:
	        		yaw -= 90;
	        		break;
	        	case SOUTH:
	        		yaw -= 180;
	        		break;
	        	case WEST:
	        		yaw = -270;
	        		break;
	        	case UP:
	        		yaw = pitch;
	        		pitch = 0;
	        		break;
	        	case DOWN:
	        		yaw = pitch;
	        		pitch = 0;
	        		break;
	        }
				
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
			Vector outvector = entity.getVelocity().zero();
			switch(destination.getTeleportFace())
	        {
	        	case NORTH:
	        		yaw += 180;
	        		outvector = outvector.setX(momentum);
	        		break;
	        	case EAST:
	        		yaw += 270;
	        		outvector = outvector.setZ(momentum);
	        		break;
	        	case SOUTH:
	        		yaw += 360;
	        		outvector = outvector.setX(-momentum);
	        		break;
	        	case WEST:
	        		yaw += 430;
	        		outvector = outvector.setZ(-momentum);
	        		break;
	        	case UP:
	        		pitch = startyaw;
	        		yaw = 0;
	        		outvector = outvector.setY(momentum);
	        		break;
	        	case DOWN:
	        		pitch = startyaw + 180;
	        		yaw = 0;
	        		
	        		outvector = outvector.setY(-momentum);
	        		break;
	        }
				 				
			entity.setFallDistance(0);	
			entity.setVelocity(entity.getVelocity().zero());
				 
			teleport.setPitch(pitch);
			teleport.setYaw(yaw);
				 
			entity.teleport(teleport);
				 				 
			entity.setVelocity(outvector);
				 
			destination.setDisabled(true);
			PortalStick.instance.getServer().getScheduler().scheduleSyncDelayedTask(PortalStick.instance, new enablePortal(destination), 10L);
		
			return teleport;
		}
		return null;
	}
	
	@Override
	public void run() {
		
		for (World w : plugin.getServer().getWorlds())
		{
			if (Config.DisabledWorlds.contains(w.getName())) return;
			for (Entity e : w.getEntities())
			{
				if (e instanceof Player || e instanceof Vehicle) return;
				Location LocTo = e.getLocation();
				LocTo = new Location(LocTo.getWorld(), LocTo.getBlockX(), LocTo.getBlockY(), LocTo.getBlockZ());
				Util.info(e.toString());

				Vector vector = e.getVelocity();
				
				teleport(e, LocTo, vector);
			}
		}
	    		
	}
	
	public static class enablePortal implements Runnable
	{
		Portal portal;
		public enablePortal(Portal instance)
		{
			portal = instance;
		}

		@Override
		public void run() {
			if (portal != null) portal.setDisabled(false);
			// TODO Auto-generated method stub
			
		}
		
	}
}
