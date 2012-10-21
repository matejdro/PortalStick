package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

class PortalCoord {
	public final HashSet<Block> border = new HashSet<Block>();
	public final HashSet<Block> inside = new HashSet<Block>();
	public final HashSet<Block> behind = new HashSet<Block>();
	public Block block;
	public Location destLoc;
	public BlockFace tpFace;
	public boolean finished = false;
	public boolean vertical;
}