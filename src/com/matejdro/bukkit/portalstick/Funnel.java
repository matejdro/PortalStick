package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

public class Funnel extends Bridge {
	private Boolean reversed = false;
	
	public Funnel(Block CreationBlock, Block startingBlock, BlockFace face, HashSet<Block> machineBlocks) {
		super(CreationBlock, startingBlock, face, machineBlocks);
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
			Portal portal = PortalManager.insideBlocks.get(nextBlock.getLocation());
			if (portal == null) portal = PortalManager.borderBlocks.get(nextBlock.getLocation());
			if (portal != null && portal.isOpen())
			{
				nextBlock = portal.getDestination().getTeleportLocation().getBlock();
				face = portal.getDestination().getTeleportFace().getOppositeFace();
				
				involvedPortals.add(portal);
				FunnelBridgeManager.involvedPortals.put(portal, this);
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
			FunnelBridgeManager.bridgeBlocks.put(nextBlock, this);
			
			nextBlock = nextBlock.getRelative(face);
		}
	}
	
	

}
