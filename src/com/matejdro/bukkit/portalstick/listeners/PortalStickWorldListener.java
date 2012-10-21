package com.matejdro.bukkit.portalstick.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.matejdro.bukkit.portalstick.PortalStick;

public class PortalStickWorldListener implements Listener {
	private final PortalStick plugin;
	
	public PortalStickWorldListener(PortalStick plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler()
	public void onChunkUnload(ChunkUnloadEvent event) {
		if (event.isCancelled()) return;
		for (Location l : plugin.portalManager.borderBlocks.keySet())
			if (l.getBlock().getChunk() == event.getChunk()) event.setCancelled(true);
	}
}
