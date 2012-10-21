package com.matejdro.bukkit.portalstick;

import java.util.HashSet;
import java.util.List;

import net.minecraft.server.EntityFallingBlock;
import net.minecraft.server.EntityItem;
import net.minecraft.server.Item;
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
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class EntityManager implements Runnable {
	private final PortalStick plugin;
	private final HashSet<World> processingWorlds = new HashSet<World>();
	private final HashSet<Entity> blockedEntities = new HashSet<Entity>();

	EntityManager(PortalStick instance)
	{
		plugin = instance;
	}

	public Location teleport(Entity entity, Location LocTo, Vector vector)
	{
		if (entity == null || entity.isDead() || blockedEntities.contains(entity)) return null;
		
		Region regionTo = plugin.regionManager.getRegion(LocTo);
		Portal portal = plugin.portalManager.insideBlocks.get(LocTo);
		if (portal == null && ((Math.abs(vector.getX()) > 0.5 || (Math.abs(vector.getY()) > 1 || (Math.abs(vector.getZ()) > 0.5))) || entity instanceof Boat)) 
		{
			portal = plugin.portalManager.awayBlocksGeneral.get(LocTo);
			if (portal == null && (Math.abs(vector.getX()) > 0.5)) portal = plugin.portalManager.awayBlocksX.get(LocTo);
			if (portal == null && (Math.abs(vector.getY()) > 1)) portal = plugin.portalManager.awayBlocksY.get(LocTo);
			if (portal == null && (Math.abs(vector.getZ()) > 0.5)) portal = plugin.portalManager.awayBlocksZ.get(LocTo);
		}
		if (portal == null && (entity instanceof FallingBlock || entity instanceof TNTPrimed)) portal = plugin.portalManager.awayBlocksY.get(LocTo);
		if (portal != null)
		{
			if (!portal.open || portal.disabled) return null;
			if (Math.abs(vector.getY()) > 1 && !portal.vertical) return null;
			
			for (Block b : portal.inside)
			{
				if (!portal.vertical)
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
				 				 
			teleport = destination.teleport.clone();
								 
			float yaw = entity.getLocation().getYaw();
			float pitch = entity.getLocation().getPitch();
			float startyaw = yaw;
			switch(portal.teleportFace)
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
	        	case DOWN:
	        		yaw = pitch;
	        		pitch = 0;
	        		teleport.add(0, 1, 0);
	        		break;
	        	case UP:
	        		yaw = pitch;
	        		pitch = 0;
	        		break;
	        }
				
			//Read input velocity
			Double momentum = 0.0;
			switch(portal.teleportFace)
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
			switch(destination.teleportFace)
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
	        	case DOWN:
	        		if (portal.teleportFace != BlockFace.UP && portal.teleportFace != BlockFace.DOWN) //TODO: || to &&
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
	        	case UP:
	        		if (portal.teleportFace != BlockFace.UP && portal.teleportFace != BlockFace.DOWN) //TODO: || to &&
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
			
			if (!(entity instanceof Player) && momentum < 0.5 && (portal.teleportFace == BlockFace.UP || portal.teleportFace == BlockFace.DOWN) && (destination.teleportFace == BlockFace.UP || destination.teleportFace == BlockFace.DOWN))
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
			else if (entity instanceof FallingBlock)
			{
				WorldServer world = ((CraftWorld) teleport.getWorld()).getHandle();
				
				EntityFallingBlock sand = (EntityFallingBlock) ((CraftFallingSand) entity).getHandle() ;
				EntityFallingBlock newsand = new EntityFallingBlock(world, teleport.getX(), teleport.getY(), teleport.getZ(), sand.id, sand.data);
				
				
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
				final Entity entity2 = entity;
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

				    public void run() {
				        entity2.teleport(tploc);
				        entity2.setVelocity(outVector);
				        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

						    public void run() {
						    	blockedEntities.remove(entity2);
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
			destination.disabled = true;
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new enablePortal(destination), 10L);
		
			if (portal.orange)
				Util.PlaySound(Sound.PORTAL_EXIT_ORANGE, entity instanceof Player ? (Player) entity : null, teleport);
			else
				Util.PlaySound(Sound.PORTAL_EXIT_BLUE, entity instanceof Player ? (Player) entity : null, teleport);

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
						plugin.funnelBridgeManager.EntityMoveCheck(e);
					}
					processingWorlds.remove(world);
				}
			};
			processingWorlds.add(world);
			checkworld.start();
		}
	    		
	}
	
	public class enablePortal implements Runnable
	{
		Portal portal;
		public enablePortal(Portal instance)
		{
			portal = instance;
		}

		@Override
		public void run() {
			if (portal != null) portal.disabled = false;
			// TODO Auto-generated method stub
			
		}
		
	}
}
