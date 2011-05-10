package com.matejdro.bukkit.portalstick.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.matejdro.bukkit.portalstick.Grill;
import com.matejdro.bukkit.portalstick.GrillManager;
import com.matejdro.bukkit.portalstick.PortalManager;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.RegionManager;
import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.UserManager;

public class Config {
	
	public static PortalStick plugin;
	private static Configuration mainConfig;
	private static Configuration regionConfig;
	private static Configuration grillConfig;
	
	public static HashSet<String> DisabledWorlds;
	public static boolean DeleteOnQuit;
	public static int PortalTool;
	public static boolean CompactPortal;
	public static Region GlobalRegion;
	public static int RegionTool;
	public static boolean RestoreInvOnWorldChange;
	
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
		
		load();

	}
	
	public static void deleteGrill(String grill) {
		List<String> list = grillConfig.getStringList("grills", null);
		list.remove(grill);
		grillConfig.setProperty("grills", list);
		saveAll();
	}
	
	public static void deleteRegion(String name) {
		regionConfig.removeProperty("regions." + name);
		saveAll();
	}
	
	public static void load() {
		
		//Check main settings
		if (mainConfig.getProperty("main.disabled-worlds") == null)
			mainConfig.setProperty("main.disabled-worlds", "");
		if (mainConfig.getProperty("main.compact-portal") == null)
			mainConfig.setProperty("main.compact-portal", false);
		if (mainConfig.getProperty("main.delete-on-quit") == null)
			mainConfig.setProperty("main.delete-on-quit", false);
		if (mainConfig.getProperty("main.portal-tool") == null)
			mainConfig.setProperty("main.portal-tool", 280);
		if (mainConfig.getProperty("main.region-tool") == null)
			mainConfig.setProperty("main.region-tool", 268);
		if (mainConfig.getProperty("main.restore-inventory-on-world-change") == null)
			mainConfig.setProperty("main.restore-inventory-on-world-change", true);
		
		//Check messages
		if (mainConfig.getProperty("messages.cannot-place-portal") == null)
			mainConfig.setProperty("messages.cannot-place-portal", "&cCannot place a portal there!");
		if (mainConfig.getProperty("messages.portal-stick-enabled") == null);
			mainConfig.setProperty("messages.portal-stick-enabled", "&7Aperture Science Handheld Portal Stick Enabled");
		if (mainConfig.getProperty("messages.portal-stick-disabled") == null);
			mainConfig.setProperty("messages.portal-stick-disabled", "&7Aperture Science Handheld Portal Stick Disabled");
		if (mainConfig.getProperty("messages.restricted-world") == null)
			mainConfig.setProperty("messages.restricted-world", "&cYou cannot do that in this world!");

		//Load messages
		MessageCannotPlacePortal = mainConfig.getString("messages.cannot-place-portal");
		MessagePortalStickEnabled = mainConfig.getString("messages.portal-stick-enabled");
		MessagePortalStickDisabled = mainConfig.getString("messages.portal-stick-disabled");
		MessageRestrictedWorld = mainConfig.getString("messages.restricted-world");
        
        //Load main settings
        DisabledWorlds = new HashSet<String>(mainConfig.getStringList("main.disabled-worlds", null));
        DeleteOnQuit = mainConfig.getBoolean("main.delete-on-quit", false);
        PortalTool = mainConfig.getInt("main.portal-tool", 280);
        CompactPortal = mainConfig.getBoolean("main.compact-portal", false);
        RegionTool = mainConfig.getInt("main.region-tool", 268);
        RestoreInvOnWorldChange = mainConfig.getBoolean("main.restore-inventory-on-world-change", true);
		
		//Load all current users
		for (Player player : plugin.getServer().getOnlinePlayers())
			UserManager.createUser(player);
		
        //Load all regions
        if (regionConfig.getKeys("regions") != null)
        	for (String regionName : regionConfig.getKeys("regions"))
        		RegionManager.loadRegion(regionName);
        RegionManager.loadRegion("global");
        Util.info(RegionManager.getRegionMap().size() + " region(s) loaded");
        
        //Load grills
        for (String grill : grillConfig.getStringList("grills", null))
        	GrillManager.loadGrill(grill);
        Util.info(GrillManager.grills.size() + " grill(s) loaded");
        
        saveAll();
		
	}
	
	public static void reLoad() {
		unLoad();
		load();
	}
	
	public static void unLoad() {
		
		PortalManager.deleteAll();
		GrillManager.deleteAll();
		for (Map.Entry<String, User> entry : UserManager.getUserList().entrySet()) {
			User user = entry.getValue();
			Player player = plugin.getServer().getPlayer(entry.getKey());
			if (player != null) {
				if (!RegionManager.getRegion(player.getLocation()).Name.equalsIgnoreCase("global"))
					user.revertInventory(player);
			}
			UserManager.deleteUser(user);
		}
		
	}
	
	public static void loadRegionSettings(Region region) {
		for (RegionSetting setting : RegionSetting.values()) {
			Object prop = regionConfig.getProperty("regions." + region.Name + "." + setting.getYaml());
    		if (prop == null)
    			region.settings.put(setting, setting.getDefault());
    		else
    			region.settings.put(setting, prop);
    		regionConfig.setProperty("regions." + region.Name + "." + setting.getYaml(), region.settings.get(setting));
    	}
		region.updateLocation();
	}
	
	private static Configuration getConfigFile(String filename) {
		Configuration configfile = null;
		File file = new File(plugin.getDataFolder(), filename);
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
	
	public static void saveAll() {
		
		//Save regions
		for (Map.Entry<String, Region> entry : RegionManager.getRegionMap().entrySet()) {
			Region region = entry.getValue();
			for (Entry<RegionSetting, Object> setting : region.settings.entrySet())
				regionConfig.setProperty("regions." + region.Name + "." + setting.getKey().getYaml(), setting.getValue());
		}
		if (!regionConfig.save())
			Util.severe("Error while writing to regions.yml");
		
		//Save grills
		grillConfig.removeProperty("grills");
		List<String> list = new ArrayList<String>();
		for (Grill grill : GrillManager.getGrillList())
			list.add(grill.getStringLocation());
		grillConfig.setProperty("grills", list);
		if (!grillConfig.save())
			Util.severe("Error while writing to grills.yml");
		
		//Save main
		if (!mainConfig.save())
			Util.severe("Error while writing to config.yml");
			
	}
	
}