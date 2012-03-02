package com.matejdro.bukkit.portalstick.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.matejdro.bukkit.portalstick.PortalManager;

public class PortalStickWorldListener implements Listener {

	@EventHandler()
	public void onChunkUnload(ChunkUnloadEvent event) {
		if (event.isCancelled()) return;
		for (Location l : PortalManager.borderBlocks.keySet())
			if (l.getBlock().getChunk() == event.getChunk()) event.setCancelled(true);
	}
}
