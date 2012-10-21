package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

public class Funnel extends Bridge {
	private boolean reversed = false;
	
	Funnel(PortalStick plugin, Block CreationBlock, Block startingBlock, BlockFace face, HashSet<Block> machineBlocks) {
		super(plugin, CreationBlock, startingBlock, face, machineBlocks);
	}
	
	public void setReverse(Boolean value)
	{
		reversed = value;
		activate();
	}
	
	public Boolean isReversed(Boolean value)
	{
		return reversed;
	}
	
	public BlockFace getDirection(Block block)
	{
		if (!bridgeBlocks.containsKey(block)) return null;
		
		int curnum = bridgeBlocks.get(block);
		BlockFace face = null;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN})
		{
			Block cblock = block.getRelative(check);
			if (bridgeBlocks.containsKey(cblock) && (curnum - bridgeBlocks.get(cblock) == 1 || bridgeBlocks.get(cblock) > curnum + 1) )			{
				face = check;
				break;
			}
		}
		if (face == null) return null;
		
		if (reversed) face = face.getOppositeFace();
		
		return face;
	}
	
	public BlockFace getDirection(Entity entity)
	{
		Block eb = entity.getLocation().getBlock();
		BlockFace face = getDirection(eb);
		if (face == null)
		{
			for (BlockFace check : BlockFace.values())
			{
				face = getDirection(eb.getRelative(check));
				if (face != null) break;
			}
		}
		
		return face;
	}
	
	public int getCounter(Block block)
	{
		return bridgeBlocks.get(block);
	}
	
	@Override
	public void activate()
	{
		//deactivate first for cleanup
		deactivate();
		
		BlockFace face = facingSide;
		Block nextBlock = startBlock;
		int counter = reversed ? 1 : 8;
		while (true)
		{			
			Portal portal = plugin.portalManager.insideBlocks.get(nextBlock.getLocation());
			if (portal == null) portal = plugin.portalManager.borderBlocks.get(nextBlock.getLocation());
			if (portal != null && portal.open)
			{
				nextBlock = portal.getDestination().teleport.getBlock();
				face = portal.getDestination().teleportFace.getOppositeFace();
				
				involvedPortals.add(portal);
				plugin.funnelBridgeManager.involvedPortals.put(portal, this);
				continue;
			}
			else if (nextBlock.getY() > 127 || (!nextBlock.isLiquid() && nextBlock.getType() != Material.AIR)) break;
			
			if (!nextBlock.getWorld().isChunkLoaded(nextBlock.getChunk())) return;
			
			if (reversed)
			{
if (counter < 0) counter = 8;
				
				if (counter > 0)
				{
					nextBlock.setType(Material.WATER);
					if (face != BlockFace.UP && face != BlockFace.DOWN) nextBlock.setData((byte) (counter - 1));
				}
				
				counter--;
			}
			else
			{
				if (counter < 0) counter = 8;
				
				if (counter > 0)
				{
					nextBlock.setType(Material.WATER);
					if (face != BlockFace.UP && face != BlockFace.DOWN) nextBlock.setData((byte) (8 - counter));
				}
				
				counter--;

			}
			
						
			bridgeBlocks.put(nextBlock, counter);
			plugin.funnelBridgeManager.bridgeBlocks.put(nextBlock, this);
			
			nextBlock = nextBlock.getRelative(face);
		}
	}
	
	@Override
	public void deactivate()
	{
		for (Block b : bridgeBlocks.keySet())
			b.setType(Material.AIR);
		
		for (Block b: bridgeBlocks.keySet())
			plugin.funnelBridgeManager.bridgeBlocks.remove(b);
		bridgeBlocks.clear();
		for (Portal p: involvedPortals)
			plugin.funnelBridgeManager.involvedPortals.remove(p);
		for (Entity e : plugin.funnelBridgeManager.glassBlocks.keySet())
			plugin.funnelBridgeManager.EntityExitsFunnel(e);
		
		involvedPortals.clear();
	}
	
	

}
