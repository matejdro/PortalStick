package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class PortalCoord {
	public HashSet<Block> border;
	public HashSet<Block> inside;
	public HashSet<Block> behind;
	public Block block;
	public Location destLoc;
	public BlockFace tpFace;
	public Boolean finished;
	public Boolean vertical;
	
	public PortalCoord()
	{
		border = new HashSet<Block>();
		inside = new HashSet<Block>();
		behind = new HashSet<Block>();
		finished = false;
	}
}