package com.matejdro.bukkit.portalstick.listeners;

import org.bukkit.Location;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;

import com.matejdro.bukkit.portalstick.PortalManager;

public class PortalStickWorldListener extends WorldListener {

	public void onChunkUnload(ChunkUnloadEvent event) {
		if (event.isCancelled()) return;
		for (Location l : PortalManager.borderBlocks.keySet())
			if (l.getBlock().getChunk() == event.getChunk()) event.setCancelled(true);
	}
}
