package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.block.BlockFace;

import de.V10lator.PortalStick.V10Location;

class PortalCoord {
	public final HashSet<V10Location> border = new HashSet<V10Location>();
	public final V10Location[] inside = new V10Location[2];
	public final V10Location[] behind = new V10Location[2];
	public V10Location block;
	public V10Location[] destLoc = new V10Location[2];
	public BlockFace tpFace;
	public boolean finished = false;
	public boolean vertical;
}