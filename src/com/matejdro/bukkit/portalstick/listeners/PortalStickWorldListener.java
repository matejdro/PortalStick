package com.matejdro.bukkit.portalstick.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.matejdro.bukkit.portalstick.PortalStick;

public class PortalStickWorldListener implements Listener {
	private final PortalStick plugin;
	
	public PortalStickWorldListener(PortalStick plugin)
	{
		this.plugin = plugin;
	}
//TODO: Disabled for stupiness:
	/*
	@EventHandler()
	public void onChunkUnload(ChunkUnloadEvent event) {
		if (event.isCancelled()) return;
		for (V10Location l : plugin.portalManager.borderBlocks.keySet())
			if (l.getBlock().getChunk() == event.getChunk()) event.setCancelled(true);
	}
	*/
	
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onChunkLoad(ChunkLoadEvent event)
  {
	for(Entity e: event.getChunk().getEntities())
	  if(!(e instanceof Player))
		plugin.userManager.createUser(e);
  }
  
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onChunkUnload(ChunkUnloadEvent event)
  {
	for(Entity e: event.getChunk().getEntities())
	  if(!(e instanceof Player))
		plugin.userManager.deleteUser(e);
  }
}
