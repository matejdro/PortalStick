package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class PortalCoord {
	public HashSet<Block> border;
	public HashSet<Block> inside;
	public Location destloc;
	public BlockFace tpface;
	public Boolean finished;
	
	public PortalCoord()
	{
		border = new HashSet<Block>();
		inside = new HashSet<Block>();
		finished = false;
	}
}