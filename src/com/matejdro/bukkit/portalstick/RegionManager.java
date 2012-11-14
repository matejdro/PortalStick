package com.matejdro.bukkit.portalstick;

import java.util.HashMap;

import org.bukkit.Location;

import de.V10lator.PortalStick.V10Location;

public class RegionManager {
	private final PortalStick plugin;
	
	RegionManager(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public final HashMap<String, Region> regions = new HashMap<String, Region>();
	
	public Region loadRegion(String name) {
		Region region = getRegion(name);
		if (region == null)
			region = new Region(name);
		plugin.config.loadRegionSettings(region);
		regions.put(name, region);
		return region;
	}
	
	public void deleteRegion(String name) {
		Region region = getRegion(name);
		regions.remove(region.name);
		plugin.config.deleteRegion(name);
	}
	
	public void createRegion(String name, V10Location one, V10Location two) {
		Region region = loadRegion(name);
		region.setLocation(one, two);
		plugin.config.saveAll();
	}
	
	public Region getRegion(V10Location location) {
		Location rl = location.getHandle();
		for (Region region : regions.values())
			if (region.contains(rl.toVector()) && rl.getWorld().getName().equalsIgnoreCase(region.world) && !region.name.equalsIgnoreCase("global"))
				return region;
		return getRegion("global");
	}
	
	public Region getRegion(String name) {
		return regions.get(name.toLowerCase());
	}
	
}
