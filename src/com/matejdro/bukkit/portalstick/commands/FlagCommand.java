package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class FlagCommand extends BaseCommand {

	public FlagCommand(PortalStick plugin) {
		super(plugin, "flag", 3, "<region> <flag> <value> <- flag a region", false);
	}
	
	public boolean execute() {
		
		Region editRegion = plugin.regionManager.getRegion(args[0]);
		if (editRegion == null) {
			plugin.util.sendMessage(sender, plugin.i18n.getString("RegionNotFound", playerName, args[0]));
			return true;
		}
		
		for (RegionSetting setting : RegionSetting.values()) {
			if (setting.getYaml().equalsIgnoreCase(args[1]) && setting.getEditable()) {
				Object old = editRegion.settings.remove(setting);
				try {
					
					if (setting.getDefault() instanceof Integer)
						editRegion.settings.put(setting, Integer.parseInt(args[2]));
					else if (setting.getDefault() instanceof Double)
						editRegion.settings.put(setting, Double.parseDouble(args[2]));
					else if (setting.getDefault() instanceof Boolean)
						editRegion.settings.put(setting, Boolean.parseBoolean(args[2]));
					else
						editRegion.settings.put(setting, args[2]);
					
					plugin.util.sendMessage(sender, plugin.i18n.getString("RegionUpdated", playerName, editRegion.name));
					plugin.config.saveAll();
				} catch (Throwable t) {
					plugin.util.sendMessage(sender, plugin.i18n.getString("InvalidRegionFlagValue", playerName, setting.getYaml()));
					editRegion.settings.put(setting, old);
				}
				return true;
			}
		}
		plugin.util.sendMessage(sender, plugin.i18n.getString("RegionUpdated", playerName, args[1]));
		StringBuilder sb = new StringBuilder("&c");
		for (RegionSetting setting : RegionSetting.values())
			if (setting.getEditable()) 
				sb.append("&c").append(setting.getYaml()).append("&7, ");
		sb.delete(sb.length() - 2, sb.length());
		plugin.util.sendMessage(sender, sb.toString());
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_ADMIN_REGIONS);
	}

}
