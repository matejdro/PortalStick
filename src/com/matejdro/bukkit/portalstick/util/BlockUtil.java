package com.matejdro.bukkit.portalstick.util;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

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

}
