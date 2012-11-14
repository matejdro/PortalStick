package com.matejdro.bukkit.portalstick.listeners;

import org.bukkit.event.Listener;

import com.matejdro.bukkit.portalstick.PortalStick;

public class PortalStickWorldListener implements Listener {
//	private final PortalStick plugin;
	
	public PortalStickWorldListener(PortalStick plugin)
	{
//		this.plugin = plugin;
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
}
