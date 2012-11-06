package com.matejdro.bukkit.portalstick.util;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.SpoutManager;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.util.Config.Sound;

public class Util {
	private final PortalStick plugin;
	private int maxLength = 105;
	
	public Util(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public void sendMessage(CommandSender player, String msg) {
		int i;
		String part;
		CustomColor lastColor = CustomColor.WHITE;
		for (String line : msg.split("`n")) {
			i = 0;
			while (i < line.length()) {
				part = getMaxString(line.substring(i));
				if (i+part.length() < line.length() && part.contains(" "))
					part = part.substring(0, part.lastIndexOf(" "));
				part = lastColor.getCustom() + part;
				player.sendMessage(replaceColors(part));
				lastColor = getLastColor(part);
				i = i + part.length() -1;
			}
		}
	}
	
	public Location getSimpleLocation(Location location) {
		location.setX((double)Math.round(location.getX() * 10) / 10);
		location.setY((double)Math.round(location.getY() * 10) / 10);
		location.setZ((double)Math.round(location.getZ() * 10) / 10);
		return location;
	}
	
	public String stripColors(String str) {
		str = str.replaceAll("(?i)\u00A7[0-F]", "");
		str = str.replaceAll("(?i)&[0-F]", "");
		return str;
	}
	
	public CustomColor getLastColor(String str) {
		int i = 0;
		CustomColor lastColor = CustomColor.WHITE;
		while (i < str.length()-2) {
			for (CustomColor color: CustomColor.values()) {
				if (str.substring(i, i+2).equalsIgnoreCase(color.getCustom()))
					lastColor = color;
			}
			i = i+2;
		}
		return lastColor;
	}
	
    public String replaceColors(String str) {
    	for (CustomColor color : CustomColor.values())
    		str = str.replace(color.getCustom(), color.getString());
        return str;
    }
    
    private String getMaxString(String str) {
    	for (int i = 0; i < str.length(); i++) {
    		if (stripColors(str.substring(0, i)).length() == maxLength) {
    			if (stripColors(str.substring(i, i+1)) == "")
    				return str.substring(0, i-1);
    			else
    				return str.substring(0, i);
    		}
    	}
    	return str;
    }
    
    public void PlayNote(Player player, byte instrument, byte note)
    {
    	Location loc = player.getLocation();
        if (!plugin.regionManager.getRegion(loc).getBoolean(RegionSetting.ENABLE_SOUNDS)) return;
        player.playNote(loc, instrument, note);
    }
    
    public void PlaySound(Sound sound, Player player, Location loc)
    {
    	if (!plugin.regionManager.getRegion(loc).getBoolean(RegionSetting.ENABLE_SOUNDS)) return;
        Plugin spoutPlugin = plugin.getServer().getPluginManager().getPlugin("Spout");
        if (spoutPlugin == null || !plugin.config.useBukkitContribSounds)
        {
        	if (player != null && !plugin.config.soundNotes[sound.ordinal()].trim().equals(""))
        	{
        		Byte instrument = Byte.parseByte(plugin.config.soundNotes[sound.ordinal()].split("-")[0]);
        		Byte note = Byte.parseByte(plugin.config.soundNotes[sound.ordinal()].split("-")[1]);
        		PlayNote(player, instrument, note);
        	}
        }
        if (spoutPlugin != null && plugin.config.useBukkitContribSounds)
        {
        	if (!plugin.config.soundUrls[sound.ordinal()].trim().equals(""))
        	{
                SpoutManager.getSoundManager().playGlobalCustomSoundEffect(plugin, plugin.config.soundUrls[sound.ordinal()], false, loc, plugin.config.soundRange);
        	}
        }
        
    }
       
    
    public int getLeftPortalColor(int preset)
    {
    	return Integer.parseInt(plugin.config.ColorPresets.get(preset).split("-")[0]);
    }
    
    public int getRightPortalColor(int preset)
    {
    	return Integer.parseInt(plugin.config.ColorPresets.get(preset).split("-")[1]);
    }
    
    public ItemStack getItemData(String itemString)
    {
    	int num;
    	int id;
    	short data;
    	
    	String[] split = itemString.split(",");
    	if (split.length < 2)
    		num = 1;
    	else
    		num = Integer.parseInt(split[1]);
    	split = split[0].split(":");
    	if (split.length < 2)
    		data = 0;
    	else
    		data = Short.parseShort(split[1]);

    	id = Integer.parseInt(split[0]);
    	return new ItemStack(id, num, data);
    }

           
    private enum CustomColor {
    	
    	RED("c", 0xC),
    	DARK_RED("4", 0x4),
    	YELLOW("e", 0xE),
    	GOLD("6", 0x6),
    	GREEN("a", 0xA),
    	DARK_GREEN("2", 0x2),
    	AQUA("b", 0xB),
    	DARK_AQUA("8", 0x8),
    	BLUE("9", 0x9),
    	DARK_BLUE("1", 0x1),
    	LIGHT_PURPLE("d", 0xD),
    	DARK_PURPLE("5", 0x5),
    	BLACK("0", 0x0),
    	DARK_GRAY("8", 0x8),
    	GRAY("7", 0x7),
    	WHITE("f", 0xf);
    	
    	private String custom;
    	private int code;
    	
    	private CustomColor(String custom, int code) {
    		this.custom = custom;
    		this.code = code;
    	}
    	public String getCustom() {
    		return "&" + custom;
    	}
    	public String getString() {
    		return String.format("\u00A7%x", code);
    	}
    	
    }

}
