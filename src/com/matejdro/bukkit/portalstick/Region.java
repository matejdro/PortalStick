package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class Region {
	
	public HashMap<RegionSetting, Object> settings = new HashMap<RegionSetting, Object>();
	
	public String Name;
	public Vector PointOne;
	public Vector PointTwo;
	public String World;
	
	public Region(String name) {
		Name = name;
	}
	
	public void updateLocation() {
		String[] loc = ((String)settings.get(RegionSetting.LOCATION)).split(":");
		String[] loc1 = loc[1].split(",");
		PointOne = new Vector(Integer.parseInt(loc1[0]), Integer.parseInt(loc1[1]), Integer.parseInt(loc1[2]));
		String[] loc2 = loc[2].split(",");
		PointTwo = new Vector(Integer.parseInt(loc2[0]), Integer.parseInt(loc2[1]), Integer.parseInt(loc2[2]));
		World = loc[0];
	}
	
	public void setLocation(Location one, Location two) {
		settings.remove(RegionSetting.LOCATION);
		settings.put(RegionSetting.LOCATION, one.getWorld().getName() + ":" + one.toVector().toString() + ":" + two.toVector().toString());
		updateLocation();
	}
	
	public boolean contains(Vector vector) {
		return vector.isInAABB(PointOne, PointTwo);
	}
	
	public boolean getBoolean(RegionSetting setting) {
		return (Boolean)settings.get(setting);
	}
	public int getInt(RegionSetting setting) {
		return (Integer)settings.get(setting);
	}
	public List<?> getList(RegionSetting setting) {
		return (List<?>)settings.get(setting);
	}
	public String getString(RegionSetting setting) {
		return (String)settings.get(setting);
	}
	public double getDouble(RegionSetting setting) {
		return (Double)settings.get(setting);
	}
	
}
