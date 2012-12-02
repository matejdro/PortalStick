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
	
	public final LinkedHashMap<V10Location, Integer> bridgeBlocks = new LinkedHashMap<V10Location, Integer>();
	public final HashSet<Portal> involvedPortals = new HashSet<Portal>();
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
		Portal portal;
		while(true)
		{			
			portal = null;
			if(plugin.portalManager.insideBlocks.containsKey(nextV10Location))
			{
			  portal = plugin.portalManager.insideBlocks.get(nextV10Location);
			  if(portal.open)
			  {
				Portal destP = portal.getDestination();
				if(destP.horizontal || portal.inside[0].equals(nextV10Location))
				  nextV10Location = destP.teleport[0];
				else
				  nextV10Location = destP.teleport[1];
			  }
			  else
				return;
			}
			else if(plugin.portalManager.borderBlocks.containsKey(nextV10Location))
			{
			  portal = plugin.portalManager.borderBlocks.get(nextV10Location);
			  if(portal.open)
				nextV10Location = new V10Location(portal.getDestination().teleport[0].getHandle().getBlock().getRelative(BlockFace.DOWN));
			  else
				return;
			}
			if (portal != null)
			{
				nextBlock = nextV10Location.getHandle().getBlock();
				face = portal.getDestination().teleportFace.getOppositeFace();
				
				involvedPortals.add(portal);
				plugin.funnelBridgeManager.involvedPortals.put(portal, this);
				continue;
			}
			else if (nextBlock.getY() > nextBlock.getWorld().getMaxHeight() - 1 ||
					(!nextBlock.isLiquid() && nextBlock.getType() != Material.AIR))
			  return;
			
			nextBlock.setType(Material.GLASS);
			bridgeBlocks.put(nextV10Location, 0);
			plugin.funnelBridgeManager.bridgeBlocks.put(nextV10Location, this);
			
			if(!nextBlock.getWorld().isChunkLoaded((nextV10Location.x + face.getModX()) / 16,(nextV10Location.z + face.getModX()) / 16))
			  return;
			
			nextBlock = nextBlock.getRelative(face);
			nextV10Location = new V10Location(nextBlock);
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
