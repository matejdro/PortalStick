package com.matejdro.bukkit.portalstick;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

import de.V10lator.PortalStick.V10Location;

class EntityManagerHelper implements Runnable
{
	private final PortalStick plugin;
	private final String world;
	private final Iterator<Entity> iter;
	int pid;
	
	EntityManagerHelper(PortalStick plugin, String world, Iterator<Entity> iter)
	{
	  this.plugin = plugin;
	  this.world = world;
	  this.iter = iter;
	}
	
	void setPid(int pid)
	{
	  this.pid = pid;
	}
	
	public void run() {
		World w = plugin.getServer().getWorld(world);
		if(w == null)
		  return;
		
		int c1 = 0, c2 = c1;
		Entity e;
		while(iter.hasNext())
		{
			e = iter.next();
			if (e instanceof Player || e instanceof Vehicle || e.isDead())
			{
			  if(++c2 > 5)
				return;
			  continue;
			}
			
			Location LocTo = e.getLocation();
			LocTo = new Location(LocTo.getWorld(), LocTo.getBlockX(), LocTo.getBlockY(), LocTo.getBlockZ());

			Vector vector = e.getVelocity();
							
			plugin.entityManager.teleport(e, new V10Location(LocTo), vector);
			plugin.funnelBridgeManager.EntityMoveCheck(e);
			if(++c1 > 30)
			  return;
		}
		plugin.getServer().getScheduler().cancelTask(pid);
		plugin.entityManager.processingWorlds.remove(world);
	}
}
