package com.matejdro.bukkit.portalstick;

import java.util.List;

import org.bukkit.Location;

public class PortalStickRegion {
	
	public boolean TeleportVehicles;
	public boolean ObeyWorldGuard;
	public boolean EnableGrill;
	public int GrillMaterial;
	public List<Integer> TransparentBlocks;
	public List<Integer> PortalBlocks;
	public boolean PortalAllBlocks;
	
	private Location cornerOne;
	private Location cornerTwo;
	
	public PortalStickRegion (boolean tv, boolean wg, boolean eg, int gm, List<Integer> tb, List<Integer> pb, boolean pab, Location one, Location two) {
		TeleportVehicles = tv;
		ObeyWorldGuard = wg;
		EnableGrill = eg;
		GrillMaterial = gm;
		TransparentBlocks = tb;
		PortalBlocks = pb;
		PortalAllBlocks = pab;
		cornerOne = one;
		cornerTwo = two;
	}
	
	
	
}
