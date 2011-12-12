package com.matejdro.bukkit.portalstick;

import java.util.HashSet;
import java.util.List;

import net.minecraft.server.EntityFallingBlock;
import net.minecraft.server.EntityItem;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftFallingSand;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingSand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class EntityManager implements Runnable {
	private PortalStick plugin;
	private HashSet<World> processingWorlds = new HashSet<World>();
	private static HashSet<Entity> blockedEntities = new HashSet<Entity>();

	public EntityManager(PortalStick instance)
	{
		plugin = instance;
	}

	public static Location teleport(Entity entity, Location LocTo, Vector vector)
	{
		if (entity == null || entity.isDead()) return null;
		
		if (blockedEntities.contains(entity)) return null;
		
		Region regionTo = RegionManager.getRegion(LocTo);
		Portal portal = PortalManager.insideBlocks.get(LocTo);
		if (portal == null && ((Math.abs(vector.getX()) > 0.5 || (Math.abs(vector.getY()) > 1 || (Math.abs(vector.getZ()) > 0.5))) || entity instanceof Boat)) 
		{
			portal = PortalManager.awayBlocksGeneral.get(LocTo);
			if (portal == null && (Math.abs(vector.getX()) > 0.5)) portal = PortalManager.awayBlocksX.get(LocTo);
			if (portal == null && (Math.abs(vector.getY()) > 1)) portal = PortalManager.awayBlocksY.get(LocTo);
			if (portal == null && (Math.abs(vector.getZ()) > 0.5)) portal = PortalManager.awayBlocksZ.get(LocTo);
		}
		if (portal == null && (entity instanceof FallingSand || entity instanceof TNTPrimed)) portal = PortalManager.awayBlocksY.get(LocTo);
		if (portal != null)
		{
			if (!portal.isOpen() || portal.isDisabled()) return null;
			if (Math.abs(vector.getY()) > 1 && !portal.isVertical()) return null;
			
			for (Block b : portal.getInside())
			{
				if (!portal.isVertical())
				{
					if (b.getX() + 0.5 < entity.getLocation().getX() && vector.getX() > 0) return null;
					else if (b.getX() - 0.5 > entity.getLocation().getX() && vector.getX() < 0) return null;
					else if (b.getZ() + 0.5 < entity.getLocation().getZ() && vector.getZ() > 0) return null;
					else if (b.getZ() - 0.5 > entity.getLocation().getZ() && vector.getZ() < 0) return null;
				}
				else
				{
					if (b.getY() + 0.5 < entity.getLocation().getY() && vector.getY() > 0) return null;
					if (b.getY() - 0.5 > entity.getLocation().getY() && vector.getY() < -0.1) return null;
				}
				
			}
			Location teleport;
			Portal destination = portal.getDestination();
				 				 
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
	        		if (portal.getTeleportFace() != BlockFace.UP || portal.getTeleportFace() != BlockFace.DOWN)
	        		{
		        		pitch = startyaw;
		        		yaw = 0;
	        		}
	        		else
	        		{
	        			pitch = yaw;
	        			startyaw = pitch;
	        		}
	        		outvector = outvector.setY(momentum);
	        		break;
	        	case DOWN:
	        		if (portal.getTeleportFace() != BlockFace.UP || portal.getTeleportFace() != BlockFace.DOWN)
	        		{
		        		pitch = startyaw + 180;
		        		yaw = 0;
	        		}
	        		else
	        		{
	        			pitch = yaw;
	        			startyaw = pitch;
	        		}
	        		outvector = outvector.setY(-momentum);
	        		break;
	        }
			
			if (!(entity instanceof Player) && momentum < 0.5 && (portal.getTeleportFace() == BlockFace.UP || portal.getTeleportFace() == BlockFace.DOWN) && (destination.getTeleportFace() == BlockFace.UP || destination.getTeleportFace() == BlockFace.DOWN))
			 	return null;
			
			entity.setFallDistance(0);	
			entity.setVelocity(entity.getVelocity().zero());
			
			teleport.setPitch(pitch);
			teleport.setYaw(yaw);
			
			if (entity instanceof Arrow)
			{
				teleport.setY(teleport.getY() + 0.5);
				entity.remove();
				teleport.getWorld().spawnArrow(teleport, outvector, (float) (momentum * 1.0f), 12.0f);
			}			
			else if (entity instanceof FallingSand)
			{
				WorldServer world = ((CraftWorld) teleport.getWorld()).getHandle();
				
				EntityFallingBlock sand = (EntityFallingBlock) ((CraftFallingSand) entity).getHandle() ;
				EntityFallingBlock newsand = new EntityFallingBlock(world, teleport.getX(), teleport.getY(), teleport.getZ(), sand.a, 0);
				
				Material db = teleport.getBlock().getType();
				
				if (db == Material.AIR || db == Material.WATER || db == Material.STATIONARY_WATER || db == Material.LAVA || db == Material.STATIONARY_LAVA)
				{
					entity.remove();
					
					world.addEntity((net.minecraft.server.Entity) newsand);	
					newsand.getBukkitEntity().setVelocity(outvector);
				}
				
			}
			else if (entity instanceof Item)
			{
				WorldServer world = ((CraftWorld) teleport.getWorld()).getHandle();
				
				net.minecraft.server.EntityItem item = (net.minecraft.server.EntityItem) ((CraftItem) entity).getHandle();
				EntityItem newitem = new EntityItem(world, teleport.getX(), teleport.getY(), teleport.getZ(), item.itemStack);
				
				entity.remove();
					
				world.addEntity((net.minecraft.server.Entity) newitem);	
				newitem.getBukkitEntity().setVelocity(outvector);
			}
			else if (entity instanceof Player || entity instanceof Vehicle)
			{
				blockedEntities.add(entity);
				final Location tploc = teleport;
				final Vector outVector = outvector;
				final Entity Entity = entity;
				PortalStick.instance.getServer().getScheduler().scheduleSyncDelayedTask(PortalStick.instance, new Runnable() {

				    public void run() {
				        Entity.teleport(tploc);
				        Entity.setVelocity(outVector);
				        PortalStick.instance.getServer().getScheduler().scheduleSyncDelayedTask(PortalStick.instance, new Runnable() {

						    public void run() {
						    	blockedEntities.remove(Entity);
						    }
						}, 1L);
				    }
				}, 1L);
			} 
			else
			{	
				World oldworld = entity.getWorld();
				
				entity.teleport(teleport);
									
				if (oldworld != teleport.getWorld())
				{
					net.minecraft.server.Entity bentity = ((CraftEntity) entity).getHandle();
					WorldServer world = ((CraftWorld) teleport.getWorld()).getHandle();
					world.addEntity(bentity);
				}
				
				entity.setVelocity(outvector);
			}
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
			if (Config.DisabledWorlds.contains(w.getName()) || processingWorlds.contains(w)) return;
			final List<Entity> entities = w.getEntities();
			final World world = w;
			Thread checkworld = new Thread() {
				public void run() {
					for (Entity e : entities)
					{
						if (e instanceof Player || e instanceof Vehicle) continue;
						if (e.isDead()) continue;
						
						Location LocTo = e.getLocation();
						LocTo = new Location(LocTo.getWorld(), LocTo.getBlockX(), LocTo.getBlockY(), LocTo.getBlockZ());

						//Util.info(e.toString());

						Vector vector = e.getVelocity();
										
						teleport(e, LocTo, vector);
					}
					processingWorlds.remove(world);
				}
			};
			processingWorlds.add(world);
			checkworld.start();
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
