package com.matejdro.bukkit.portalstick.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;

import com.matejdro.bukkit.portalstick.PortalStick;

public class BlockUtil {
	public static boolean compareBlockToString(Block block, String blockData) {
		String[] blockArr = blockData.split(":");
		if (blockArr.length > 1)
			return (block.getTypeId() == Integer.parseInt(blockArr[0]) && block.getData() == Integer.parseInt(blockArr[1]));
		else
			return block.getTypeId() == Integer.parseInt(blockArr[0]);
	}

	public static void setBlockData(Block block, String blockData) {
		String[] blockArr = blockData.split(":");
		block.setTypeId(Integer.parseInt(blockArr[0]));
		if (blockArr.length > 1)
			block.setData((byte) Integer.parseInt(blockArr[1]));
	}

	public static String getBlockData(Block block) {
		if (block.getData() != 0)
			return block.getTypeId() + ":" + block.getData();
		return Integer.toString(block.getTypeId());
	}
	
	public static BlockFace getFaceOfMaterial(Block block, BlockFace[] faces, String material) {
		for (BlockFace face : faces)
			if (compareBlockToString(block.getRelative(face), material))
				return face;
		return null;
	}
	
	public static void setBlockThreadSafe(final Block block, final Material material)
	{
		PortalStick.instance.getServer().getScheduler().scheduleSyncDelayedTask(PortalStick.instance, new Runnable() {

		    public void run() {
		       block.setType(material);
		    }
		}, 1L);
	}
	
	public static Byte rotateBlock(Material block, Byte bdata, BlockFace origin, BlockFace newOrientation)
	{
		if (block.getNewData(bdata) instanceof Directional)
		{
			Directional directional = (Directional) block.getNewData(bdata);
			
			int diff = newOrientation.ordinal() - origin.ordinal();
			
			if (directional.getFacing().ordinal() + diff < 0) diff += 4;
			if (directional.getFacing().ordinal() + diff > 3) diff -= 4;
			
			directional.setFacingDirection(BlockFace.values()[directional.getFacing().ordinal() + diff]);
			return ((MaterialData) directional).getData();
		}
		else
			return bdata;
		
	}

}
