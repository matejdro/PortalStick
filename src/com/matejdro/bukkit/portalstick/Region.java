package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.List;

import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.Setting;

public class Region {
	
	public HashMap<Setting, Object> settings = new HashMap<Setting, Object>();
	
	public String Name;
	public Vector PointOne;
	public Vector PointTwo;
	public String World;
	
	public Region(String name) {
		Name = name;
	}
	
	public void updateLocation() {
		String[] loc = ((String)settings.get(Setting.LOCATION)).split(":");
		String[] loc1 = loc[1].split(",");
		PointOne = new Vector(Integer.parseInt(loc1[0]), Integer.parseInt(loc1[1]), Integer.parseInt(loc1[2]));
		String[] loc2 = loc[2].split(",");
		PointOne = new Vector(Integer.parseInt(loc2[0]), Integer.parseInt(loc2[1]), Integer.parseInt(loc2[2]));
		World = loc[0];
	}
	
	public boolean contains(Vector vector) {
		return vector.isInAABB(PointOne, PointTwo);
	}
	
	public boolean getBoolean(Setting setting) {
		return (Boolean)settings.get(setting);
	}
	public int getInt(Setting setting) {
		return (Integer)settings.get(setting);
	}
	public List<?> getList(Setting setting) {
		return (List<?>)settings.get(setting);
	}
	public String getString(Setting setting) {
		return (String)settings.get(setting);
	}
	
}
