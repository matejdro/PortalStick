package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class Region extends User {
	
	public HashMap<RegionSetting, Object> settings = new HashMap<RegionSetting, Object>();
	
	public String Name;
	public Vector Min = new Vector();
	public Vector Max = new Vector();
	public String World;
	
	public HashSet<Portal> portals = new HashSet<Portal>();
	public Portal bluePortalDest;
	public Portal orangePortalDest;
	
	public Region(String name) {
		super("region_" + name);
		Name = name;
	}
	
	public void updateLocation() {
		String[] loc = getString(RegionSetting.LOCATION).split(":");
		String[] loc1 = loc[1].split(",");
		Vector one = new Vector(Double.parseDouble(loc1[0]), Double.parseDouble(loc1[1]), Double.parseDouble(loc1[2]));
		String[] loc2 = loc[2].split(",");
		Vector two = new Vector(Double.parseDouble(loc2[0]), Double.parseDouble(loc2[1]), Double.parseDouble(loc2[2]));

		Min.setX(one.getX() < two.getX()?one.getX():two.getX());
		Max.setX(one.getX() > two.getX()?one.getX():two.getX());
		Min.setY(one.getY() < two.getY()?one.getY():two.getY());
		Max.setY(one.getY() > two.getY()?one.getY():two.getY());
		Min.setZ(one.getZ() < two.getZ()?one.getZ():two.getZ());
		Max.setZ(one.getZ() > two.getZ()?one.getZ():two.getZ());

		World = loc[0];
	}
	
	public void setLocation(Location one, Location two) {
		settings.remove(RegionSetting.LOCATION);
		settings.put(RegionSetting.LOCATION, one.getWorld().getName() + ":" + one.toVector().toString() + ":" + two.toVector().toString());
		updateLocation();
	}
	
	public void regionPortalOpened(Boolean orange)
	{
		for (Portal p : portals)
		{
			if (orange)
			{
    			Util.info(String.valueOf(p.getDestination() == getBluePortal()));
				if (p.getDestination() == getBluePortal() && getBluePortal() != null) 
				{
					getBluePortal().open();
					if (orangePortalDest == null || !orangePortalDest.isOpen()) orangePortalDest = p;
					break;
				}
			}
			else
			{
				if (p.getDestination() == getOrangePortal() && getOrangePortal() != null) 
				{
					getOrangePortal().open();
					if (bluePortalDest == null || !bluePortalDest.isOpen()) bluePortalDest = p;
					break;
				}
			}
		}			
	}
	
	public void regionPortalClosed(Boolean orange)
	{
		for (Portal p : portals)
		{
			if (orange)
			{
				if (p.isOpen() && p.getDestination() == getBluePortal() && getBluePortal() != null) 
				{
					if (orangePortalDest == null || !orangePortalDest.isOpen()) orangePortalDest = p;
					break;
				}
			}
			if (p.isOpen() && p.getDestination() == getOrangePortal() && getOrangePortal() != null) 
			{
				if (bluePortalDest == null || !bluePortalDest.isOpen()) bluePortalDest = p;
				break;
			}
		}
		
		if (orange)
		{
			if (getOrangePortal() != null) getOrangePortal().close();
			orangePortalDest = null;
		}
		else
		{
			if (getBluePortal() != null) getBluePortal().close();
			bluePortalDest = null;
		}

	}
	
	public void regionPortalDeleted(Portal portal)
	{
		for (Portal p : portals)
		{
			if (p.getDestination() == portal) p.close();
		}
	}
	
	public void regionPortalCreated(Boolean orange)
	{
		for (Portal p : portals)
		{
			if (p.isOrange() != orange && p.getDestination() == null) p.open();
		}
	}
	
	public boolean contains(Vector vector) {
		return vector.isInAABB(Min, Max);
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
