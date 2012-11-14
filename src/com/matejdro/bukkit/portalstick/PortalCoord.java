package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.block.BlockFace;

import de.V10lator.PortalStick.V10Location;

class PortalCoord {
	public final HashSet<V10Location> border = new HashSet<V10Location>();
	public final HashSet<V10Location> inside = new HashSet<V10Location>();
	public final HashSet<V10Location> behind = new HashSet<V10Location>();
	public V10Location block;
	public V10Location destLoc;
	public BlockFace tpFace;
	public boolean finished = false;
	public boolean vertical;
}