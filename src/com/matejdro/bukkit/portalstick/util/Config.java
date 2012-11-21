package com.matejdro.bukkit.portalstick.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.Bridge;
import com.matejdro.bukkit.portalstick.Grill;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.User;

import de.V10lator.PortalStick.AutoUpdate;

public class Config {
	
	private final PortalStick plugin;
	private final FileConfiguration mainConfig;
	private final FileConfiguration regionConfig;
	private final FileConfiguration grillConfig;
	private final FileConfiguration bridgeConfig;
	
	private final File mainConfigFile;
	private final File regionConfigFile;
	private final File grillConfigFile;
	private final File bridgeConfigFile;
	
	public HashSet<String> DisabledWorlds;
	public int PortalTool;
	public short portalToolData; //Short for spout compatiblity!
	public boolean CompactPortal;
	public Region GlobalRegion;
	public int RegionTool;
	public boolean RestoreInvOnWorldChange;
	public List<String> ColorPresets;
	public int FillPortalBack;
	
	public String MessageCannotPlacePortal;
	
	public boolean useNativeSounds, useSpoutSounds;
	public int soundRange;
	public final String[] soundUrls = new String[Sound.values().length];
	public final String[] soundNative = new String[Sound.values().length];
	
	public Config (PortalStick instance) {
		
		plugin = instance;
		
		mainConfigFile = getConfigFile("config.yml");
		regionConfigFile = getConfigFile("regions.yml");
		grillConfigFile = getConfigFile("grills.yml");
		bridgeConfigFile = getConfigFile("bridges.yml");
		
		
		mainConfig = getConfig(mainConfigFile);
		regionConfig = getConfig(regionConfigFile);
		grillConfig = getConfig(grillConfigFile);
		bridgeConfig = getConfig(bridgeConfigFile);
	}
	
	public void deleteGrill(String grill) {
		List<String> list =  grillConfig.getStringList("grills");
		list.remove(grill);
		grillConfig.set("grills", list);
		saveAll();
	}
	
	public void deleteRegion(String name) {
		regionConfig.set(name, null);
		saveAll();
	}
	
	public void deleteBridge(String bridge) {
		List<String> list = bridgeConfig.getStringList("bridges");
		list.remove(bridge);
		bridgeConfig.set("bridges", list);
		saveAll();
	}

	
	public void load() {
		try {
			mainConfig.load(mainConfigFile);
			regionConfig.load(regionConfigFile);
			grillConfig.load(grillConfigFile);
			bridgeConfig.load(bridgeConfigFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
		boolean aa = false;
		
		//Load messages
		MessageCannotPlacePortal = getString("messages.cannot-place-portal", "&cCannot place a portal there!");
        
        //Load main settings
        DisabledWorlds = new HashSet<String>(getStringList("main.disabled-worlds", new ArrayList<String>()));
        String[] split = getString("main.portal-tool", "280:0").split(":");
        PortalTool = Integer.parseInt(split[0]);
        if(split.length > 1)
          portalToolData = Short.parseShort(split[1]);
        else
          portalToolData = 0;
        CompactPortal = getBoolean("main.compact-portal", false);
        RegionTool = getInt("main.region-tool", 268);
        RestoreInvOnWorldChange = getBoolean("main.restore-inventory-on-world-change", true);
        ColorPresets = getStringList("main.portal-color-presets", Arrays.asList(new String[]{"3-1","2-6","9-10","5-13","8-7","15-4"}));
        FillPortalBack = getInt("main.fill-portal-back", -1);
        
        //Load sound settings
        if(!mainConfig.isSet("sounds.use-minecraft-sounds"))
          aa = true;
        useNativeSounds = getBoolean("sounds.use-minecraft-sounds", true);
        soundNative[Sound.PORTAL_CREATE_BLUE.ordinal()] = getString("sounds.minecraft.create-blue-portal", "STEP_WOOL:0.3");
        soundNative[Sound.PORTAL_CREATE_ORANGE.ordinal()] = getString("sounds.minecraft.create-orange-portal", "STEP_WOOL:0.3");
        soundNative[Sound.PORTAL_EXIT_BLUE.ordinal()] = getString("sounds.minecraft.exit-blue-portal", "ENDERMAN_TELEPORT0");
        soundNative[Sound.PORTAL_EXIT_ORANGE.ordinal()] = getString("sounds.minecraft.exit-orange-portal", "ENDERMAN_TELEPORT");
        soundNative[Sound.PORTAL_CANNOT_CREATE.ordinal()] = getString("sounds.minecraft.cannot-create-portal", "");
        soundNative[Sound.GRILL_EMANCIPATE.ordinal()] = getString("sounds.minecraft.grill-emancipate", "");
        soundNative[Sound.FAITHPLATE_LAUNCH.ordinal()] = getString("sounds.minecraft.faith-plate-launch", "EXPLODE:0.5");
        soundNative[Sound.GEL_BLUE_BOUNCE.ordinal()] = getString("sounds.minecraft.blue-gel-bounce", "SLIME_WALK2");
        
        useSpoutSounds = getBoolean("sounds.use-spout-sounds", false);
        //TODO: Holds compat for < 4.0-beta1 o.O
        if(mainConfig.isSet("sounds.use-bukkitcontrib-sounds"))
        {
          if(!mainConfig.isSet("sounds.use-spout-sounds"))
        	useSpoutSounds = getBoolean("sounds.use-bukkitcontrib-sounds", useSpoutSounds);
          getBoolean("sounds.use-spout-sounds", useSpoutSounds);
          getBoolean("sounds.use-bukkitcontrib-sounds", null);
          aa = true;
        }
        
        soundUrls[Sound.PORTAL_CREATE_BLUE.ordinal()] = getString("sounds.spout.create-blue-portal-url", "");
        soundUrls[Sound.PORTAL_CREATE_ORANGE.ordinal()] = getString("sounds.spout.create-orange-portal-url", "");
        soundUrls[Sound.PORTAL_EXIT_BLUE.ordinal()] = getString("sounds.spout.exit-blue-portal-url", "");
        soundUrls[Sound.PORTAL_EXIT_ORANGE.ordinal()] = getString("sounds.spout.exit-orange-portal-url", "");
        soundUrls[Sound.PORTAL_CANNOT_CREATE.ordinal()] = getString("sounds.spout.cannot-create-portal-url", "");
        soundUrls[Sound.GRILL_EMANCIPATE.ordinal()] = getString("sounds.spout.grill-emancipate-url", "");
        soundUrls[Sound.FAITHPLATE_LAUNCH.ordinal()] = getString("sounds.spout.faith-plate-launch-url", "");
        soundUrls[Sound.GEL_BLUE_BOUNCE.ordinal()] = getString("sounds.spout.blue-gel-bounce-url", "");
        
        soundRange = getInt("sounds.sound-range", 20);
        
		//Load all current users
		for (Player player : plugin.getServer().getOnlinePlayers())
			plugin.userManager.createUser(player);
		
        //Load all regions
        for (String regionName : regionConfig.getKeys(false))
        	if(!regionName.equals("global"))
        		plugin.regionManager.loadRegion(regionName);
        plugin.regionManager.loadRegion("global");
        plugin.getLogger().info(plugin.regionManager.regions.size() + " region(s) loaded");
        
        //Load grills
        for (String grill : (grillConfig.getStringList("grills")).toArray(new String[0]))
        	plugin.grillManager.loadGrill(grill);
        plugin.getLogger().info(plugin.grillManager.grills.size() + " grill(s) loaded");
        //Load bridges
        for (String bridge : bridgeConfig.getStringList("bridges"))
        	plugin.funnelBridgeManager.loadBridge(bridge);
        plugin.getLogger().info(plugin.funnelBridgeManager.bridges.size() + " bridge(s) loaded");
        
        if(aa)
        	plugin.getLogger().info("Your config file has been updated");
        
        //TODO: Enable
/*        try
        {
          if(plugin.au == null)
        	plugin.au = new AutoUpdate(plugin, mainConfig);
          else
        	plugin.au.setConfig(mainConfig);
		} 
        catch (Exception e)
        {
		  plugin.getLogger().info("Auto update error!");
		  e.printStackTrace();
		}
        */
        saveAll();
		
	}
	
	private int getInt(String path, int def)
	{

		if (mainConfig.get(path) == null)
			mainConfig.set(path, def);
	
		return mainConfig.getInt(path, def);
	}

	private String getString(String path, String def)
	{
		if (mainConfig.get(path) == null)
			mainConfig.set(path, def);

		return mainConfig.getString(path, def);
	}

	private List<String> getStringList(String path, List<String> def)
	{
		if (mainConfig.get(path) == null)
			mainConfig.set(path, def);

	return mainConfig.getStringList(path);
	}

	private boolean getBoolean(String path, Boolean def)
	{
		if (mainConfig.get(path) == null)
			mainConfig.set(path, def);

		return mainConfig.getBoolean(path, def);
	}
	
	public void reLoad() {
		unLoad();
		load();
	}
	
	public void unLoad() {
		
		plugin.funnelBridgeManager.deleteAll();
		plugin.portalManager.portals.clear();
		plugin.grillManager.deleteAll();
		for(User user: plugin.userManager.getUsers())
		{
		  if(user.isPlayer)
		  {
			Player player = plugin.getServer().getPlayer(user.name);
			if (player != null)
			  user.revertInventory(player);
		  }
		  plugin.userManager.deleteUser(user);
		}
		
	}
	
	public void loadRegionSettings(Region region) {
		for (RegionSetting setting : RegionSetting.values()) {
			Object prop = regionConfig.get(region.name + "." + setting.getYaml());
    		if (prop == null)
    			region.settings.put(setting, setting.getDefault());
    		else
    			region.settings.put(setting, prop);
    		regionConfig.set(region.name + "." + setting.getYaml(), region.settings.get(setting));
    	}
		region.updateLocation();
	}
	
	private File getConfigFile(String filename)
	{
		if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
		
		File file = new File(plugin.getDataFolder(), filename);
		return file;
	}
	private FileConfiguration getConfig(File file) {
		FileConfiguration config = null;
		try {
			config = new YamlConfiguration();
			if (file.exists())
			{
				config.load(file);
				config.set("setup", null);
			}
			config.save(file);
			
			return config;
		} catch (Exception e) {
			plugin.getLogger().severe("Unable to load YAML file " + file.getAbsolutePath());
		}
		return null;
	}
	
	public void saveAll() {
		
		//Save regions
		for (Map.Entry<String, Region> entry : plugin.regionManager.regions.entrySet()) {
			Region region = entry.getValue();
			for (Entry<RegionSetting, Object> setting : region.settings.entrySet())
				regionConfig.set(region.name + "." + setting.getKey().getYaml(), setting.getValue());
		}
		try
		{
			regionConfig.save(regionConfigFile);
		}
		catch (Exception ex)
		{
			plugin.getLogger().severe("Error while writing to regions.yml");
		}
		
		//Save grills
		grillConfig.set("grills", null);
		List<String> list = new ArrayList<String>();
		for (Grill grill : plugin.grillManager.grills)
			list.add(grill.getStringLocation());
		grillConfig.set("grills", list);
		try
		{
			grillConfig.save(grillConfigFile);
		}
		catch (Exception ex)
		{
			plugin.getLogger().severe("Error while writing to grills.yml");
		}
		
		//Save bridges
		bridgeConfig.set("bridges", null);
		list = new ArrayList<String>();
		for (Bridge bridge : plugin.funnelBridgeManager.bridges)
			list.add(bridge.getStringLocation());
		bridgeConfig.set("bridges", list);
		try
		{
			bridgeConfig.save(bridgeConfigFile);
		}
		catch (Exception ex)
		{
			plugin.getLogger().severe("Error while writing to bridges.yml");
		}
		
		//Save main
		try
		{
			mainConfig.save(mainConfigFile);
		}
		catch (Exception ex)
		{
			plugin.getLogger().severe("Error while writing to config.yml");
		}
			
	}
	
	public enum Sound {
		PORTAL_CREATE_BLUE,
		PORTAL_CREATE_ORANGE,
		PORTAL_EXIT_BLUE,
		PORTAL_EXIT_ORANGE,
		PORTAL_CANNOT_CREATE,
		GRILL_EMANCIPATE,
		FAITHPLATE_LAUNCH,
		GEL_BLUE_BOUNCE
	}
	
}
