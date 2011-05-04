package com.matejdro.bukkit.portalstick;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.bukkit.util.config.Configuration;

public class Config {
	
	public PortalStick plugin;
	private Configuration config;
	
	public HashSet<String> EnabledWorlds;
	public boolean DeleteOnQuit;

	public Config (PortalStick instance) {
		
		this.plugin = instance;
		this.config = plugin.getConfiguration();
		config.load();
		
		List<String> keys = config.getKeys("");
		
		//Check main settings
		if (!keys.contains("enabled-worlds"))
			config.setProperty("main.enabled-worlds", true);
		if (!keys.contains("compact-portal"))
			config.setProperty("main.compact-portal", true);
		if (!keys.contains("velocity-multiplier"))
			config.setProperty("main.velocity-multiplier", true);
		if (!keys.contains("delete-on-quit"))
			config.setProperty("main.delete-on-quit", true);
		if (!keys.contains("portal-tool"))
			config.setProperty("main.portal-tool", 280);
		
		//Check default region settings
        checkDefaultRegionValues("global");
        
        //Load all regions
        for (String region : config.getKeys("regions")) {
        	
        }
		
		//Attempt save
		if (!config.save())
			Util.severe("Error while writing to config.yml");

	}
	
	public void checkDefaultRegionValues(String region) {
		if (config.getProperty("regions." + region + ".portals.teleport-vehicles") == null)
			config.setProperty("regions." + region + ".portals.teleport-vehicles", true);
		if (config.getProperty("regions." + region + ".portals.obey-worldguard-permissions") == null)
			config.setProperty("regions." + region + ".portals.obey-worldguard-permissions", true);
		if (config.getProperty("regions." + region + ".grill.enable-emancipation-grill") == null)
			config.setProperty("regions." + region + ".grill.enable-emancipation-grill", true);
		if (config.getProperty("regions." + region + ".grill.grill-frame-material") == null)
			config.setProperty("regions." + region + ".grill.grill-frame-material", 48);
		if (config.getProperty("regions." + region + ".blocks.transparent-blocks") == null)
			config.setProperty("regions." + region + ".blocks.transparent-blocks", Arrays.asList(new Integer[]{0,8,9,10,11,20}));
		if (config.getProperty("regions." + region + ".blocks.portal-blocks") == null)
			config.setProperty("regions." + region + ".blocks.portal-blocks", Arrays.asList(new Integer[]{1}));
		if (config.getProperty("regions." + region + ".blocks.portal-all-blocks") == null)
			config.setProperty("regions." + region + ".blocks.portal-all-blocks", false);
	}
	
}