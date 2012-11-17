package com.matejdro.bukkit.portalstick;

import java.util.HashSet;
import java.util.LinkedHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import de.V10lator.PortalStick.V10Location;

//import de.V10lator.PortalStick.V10Location;

public class Bridge {
	final PortalStick plugin;
	
	final LinkedHashMap<V10Location, Integer> bridgeBlocks = new LinkedHashMap<V10Location, Integer>();
	final HashSet<Portal> involvedPortals = new HashSet<Portal>();
	HashSet<V10Location> bridgeMachineBlocks = new HashSet<V10Location>();
	V10Location startBlock;
	public V10Location creationBlock;
	BlockFace facingSide;

	Bridge(PortalStick plugin, V10Location creationBlock, V10Location startingBlock, BlockFace face, HashSet<V10Location> machineBlocks)
	{
		this.plugin = plugin;
		startBlock = startingBlock;
		facingSide = face;
		bridgeMachineBlocks = machineBlocks;
		this.creationBlock = creationBlock;
	}
	/*
	public Block getCreationBlock()
	{
		return creationBlock;
	}
	*/
	public void activate()
	{
		//deactivate first for cleanup
		deactivate();
		
		BlockFace face = facingSide;
		V10Location nextV10Location = startBlock;
		Block nextBlock = nextV10Location.getHandle().getBlock();
		while (true)
		{			
			Portal portal = plugin.portalManager.insideBlocks.get(nextV10Location);
			if (portal == null) portal = plugin.portalManager.borderBlocks.get(nextV10Location);
			if (portal != null && portal.open)
			{
				nextV10Location = portal.getDestination().teleport;
				nextBlock = nextV10Location.getHandle().getBlock();
				face = portal.getDestination().teleportFace.getOppositeFace();
				
				involvedPortals.add(portal);
				plugin.funnelBridgeManager.involvedPortals.put(portal, this);
				continue;
			}
			else if (nextBlock.getY() > nextBlock.getWorld().getMaxHeight() - 1 ||
					(!nextBlock.isLiquid() && nextBlock.getType() != Material.AIR) ||
					!nextBlock.getWorld().isChunkLoaded(nextBlock.getChunk()))
			  return;
			
			nextBlock.setType(Material.GLASS);
			bridgeBlocks.put(nextV10Location, 0);
			plugin.funnelBridgeManager.bridgeBlocks.put(nextV10Location, this);
			
			nextBlock = nextBlock.getRelative(face);
		}
	}
	
	public void deactivate()
	{
		for (V10Location b : bridgeBlocks.keySet())
		{
			b.getHandle().getBlock().setType(Material.AIR);
			plugin.funnelBridgeManager.bridgeBlocks.remove(b);
		}
		bridgeBlocks.clear();
		for (Portal p: involvedPortals)
			plugin.funnelBridgeManager.involvedPortals.remove(p);
		involvedPortals.clear();
	}
	
	public void delete()
	{
		deactivate();
		for (V10Location b: bridgeMachineBlocks)
			plugin.funnelBridgeManager.bridgeMachineBlocks.remove(b);
	}
	
	public boolean isBlockNextToBridge(V10Location check)
	{
		BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
		for (V10Location b : bridgeBlocks.keySet())
			for (BlockFace face : faces)
				if (new V10Location(b.getHandle().getBlock().getRelative(face)).equals(check)) return true;
		return false;
	}
	
	public String getStringLocation()
	{
		Location loc = creationBlock.getHandle();
		return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
	}
}
