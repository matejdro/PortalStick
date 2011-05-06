package com.matejdro.bukkit.portalstick.util;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.config.Configuration;

import com.matejdro.bukkit.portalstick.Grill;
import com.matejdro.bukkit.portalstick.GrillManager;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.RegionManager;

public class Config {
	
	public static PortalStick plugin;
	private static Configuration mainConfig;
	private static Configuration regionConfig;
	private static Configuration grillConfig;
	
	public static HashSet<String> EnabledWorlds;
	public static boolean DeleteOnQuit;
	public static int PortalTool;
	public static boolean CompactPortal;
	public static Region GlobalRegion;
	public static int RegionTool;
	
	public static String MessageCannotPlacePortal;
	public static String MessagePortalStickEnabled;
	public static String MessagePortalStickDisabled;
	public static String MessageRestrictedWorld;

	public Config (PortalStick instance) {
		
		plugin = instance;
		mainConfig = plugin.getConfiguration();
		mainConfig.load();
		regionConfig = getConfigFile("regions.yml");
		grillConfig = getConfigFile("grills.yml");
		
		//Check main settings
		List<String> keys = mainConfig.getKeys("main");
		if (!keys.contains("enabled-worlds"))
			mainConfig.setProperty("main.enabled-worlds", plugin.getServer().getWorlds().get(0));
		if (!keys.contains("compact-portal"))
			mainConfig.setProperty("main.compact-portal", false);
		if (!keys.contains("delete-on-quit"))
			mainConfig.setProperty("main.delete-on-quit", false);
		if (!keys.contains("portal-tool"))
			mainConfig.setProperty("main.portal-tool", 280);
		if (!keys.contains("region-tool"))
			mainConfig.setProperty("main.region-tool", 268);
		
		//Check messages
		keys = mainConfig.getKeys("messages");
		if (!keys.contains("cannot-place-portal"))
			mainConfig.setProperty("messages.cannot-place-portal", "Cannot place a portal there!");
		if (!keys.contains("portal-stick-enabled"));
			mainConfig.setProperty("messages.portal-stick-enabled", "You have just turned your crappy piece of wood into Aperture Science Handheld Portal Stick!");
		if (!keys.contains("portal-stick-disabled"));
			mainConfig.setProperty("messages.portal-stick-enabled", "You have just reverted your Aperture Science Handheld Portal Stick back into crappy piece of wood!");
		if (!keys.contains("restricted-world"))
			mainConfig.setProperty("messages.restricted-world", "You cannot do that in this world!");

		//Load messages
		MessageCannotPlacePortal = mainConfig.getString("messages.cannot-place-portal");
		MessagePortalStickEnabled = mainConfig.getString("messages.portal-stick-enabled");
		MessagePortalStickDisabled = mainConfig.getString("messages.portal-stick-disabled");
		MessageRestrictedWorld = mainConfig.getString("messages.restricted-world");
        
        //Load main settings
        EnabledWorlds = new HashSet<String>(mainConfig.getStringList("main.enabled-worlds", null));
        DeleteOnQuit = mainConfig.getBoolean("main.delete-on-quit", false);
        PortalTool = mainConfig.getInt("main.portal-tool", 280);
        CompactPortal = mainConfig.getBoolean("main.compact-portal", false);
        RegionTool = mainConfig.getInt("main.region-tool", 268);
        
        //Load all regions
        for (String regionName : regionConfig.getKeys("regions"))
        	RegionManager.loadRegion(regionName);
        
        //Load grills
        for (String grill : grillConfig.getStringList("grills", null))
        	GrillManager.loadGrill(grill);
        
        saveAll();

	}
	
	public static void deleteGrill(String grill) {
		List<String> list = grillConfig.getStringList("grills", null);
		list.remove(grill);
		grillConfig.setProperty("grills", list);
	}
	
	public static void deleteRegion(String name) {
		regionConfig.removeProperty("regions." + name);
	}
	
	public static void loadRegionSettings(Region region) {
		for (RegionSetting setting : RegionSetting.values()) {
			Object prop = regionConfig.getProperty(mkRegionNode(region.Name, setting.getYaml()));
    		if (prop == null)
    			region.settings.put(setting, setting.getDefault());
    		else
    			region.settings.put(setting, prop);
    		regionConfig.setProperty(mkRegionNode(region.Name, setting.getYaml()), region.settings.get(setting));
    	}
		region.updateLocation();
	}
	
	private static Configuration getConfigFile(String filename) {
		Configuration configfile = null;
		File file = new File(plugin.getDataFolder(), filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
				PrintWriter output = new PrintWriter(file);
				output.println("setup: false");
				output.close();
			} catch (Exception e) {
				Util.severe("Unable to create file " + filename);
			}
		}
		try {
			configfile = new Configuration(file);
			configfile.load();
			configfile.removeProperty("setup");
			configfile.save();
		} catch (Exception e) {
			Util.severe("Unable to load YAML file " + filename);
		}
		return configfile;
	}
	
	private static String mkRegionNode(String node, String region) {
		if (region.equalsIgnoreCase("global"))
			return "global." + node;
		return "regions." + region + "." + node;
	}
	
	public static void saveAll() {
		
		//Save regions
		for (Map.Entry<String, Region> entry : RegionManager.getRegionMap().entrySet()) {
			Region region = entry.getValue();
			for (Entry<RegionSetting, Object> setting : region.settings.entrySet())
				regionConfig.setProperty(mkRegionNode(setting.getKey().getYaml(), region.Name), setting.getValue());
		}
		if (!regionConfig.save())
			Util.severe("Error while writing to regions.yml");
		
		//Save grills
		grillConfig.removeProperty("grills");
		List<String> list = new ArrayList<String>();
		for (Grill grill : GrillManager.getGrillList()) {
			Block b = grill.getFirstBlock();
			Location loc = b.getLocation();
			list.add(b.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ());
		}
		grillConfig.setProperty("grills", list);
		if (!grillConfig.save())
			Util.severe("Error while writing to grills.yml");
		
		//Save main
		if (!mainConfig.save())
			Util.severe("Error while writing to config.yml");
			
	}
	
}