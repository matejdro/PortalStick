package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.V10Location;

public class Region extends User {
	
	public HashMap<RegionSetting, Object> settings = new HashMap<RegionSetting, Object>();
	
	public final String name;
	public final Vector min = new Vector();
	public final Vector max = new Vector();
	public String world;
	
	public HashSet<Portal> portals = new HashSet<Portal>();
	public Portal bluePortalDest;
	public Portal orangePortalDest;
	
	Region(String name) {
		super("region_" + name);
		this.name = name;
	}
	
	public void updateLocation() {
		String[] loc = getString(RegionSetting.LOCATION).split(":");
		String[] loc1 = loc[1].split(",");
		Vector one = new Vector(Double.parseDouble(loc1[0]), Double.parseDouble(loc1[1]), Double.parseDouble(loc1[2]));
		String[] loc2 = loc[2].split(",");
		Vector two = new Vector(Double.parseDouble(loc2[0]), Double.parseDouble(loc2[1]), Double.parseDouble(loc2[2]));

		min.setX(one.getX() < two.getX()?one.getX():two.getX());
		max.setX(one.getX() > two.getX()?one.getX():two.getX());
		min.setY(one.getY() < two.getY()?one.getY():two.getY());
		max.setY(one.getY() > two.getY()?one.getY():two.getY());
		min.setZ(one.getZ() < two.getZ()?one.getZ():two.getZ());
		max.setZ(one.getZ() > two.getZ()?one.getZ():two.getZ());

		world = loc[0];
	}
	
	public void setLocation(V10Location one, V10Location two) {
		settings.remove(RegionSetting.LOCATION);
		Location a = one.getHandle();
		settings.put(RegionSetting.LOCATION, a.getWorld().getName() + ":" + a.toVector().toString() + ":" + two.getHandle().toVector().toString());
		updateLocation();
	}
	
	public void regionPortalOpened(boolean orange)
	{
		for (Portal p : portals)
		{
			if (orange)
			{
				if (p.getDestination() == bluePortal && bluePortal != null) 
				{
					bluePortal.open();
					if (orangePortalDest == null || !orangePortalDest.open) orangePortalDest = p;
					break;
				}
			}
			else
			{
				if (p.getDestination() == orangePortal && orangePortal != null) 
				{
					orangePortal.open();
					if (bluePortalDest == null || !bluePortalDest.open) bluePortalDest = p;
					break;
				}
			}
		}			
	}
	
	public void regionPortalClosed(boolean orange)
	{
		for (Portal p : portals)
		{
			if (orange)
			{
				if (p.open && p.getDestination() == bluePortal && bluePortal != null) 
				{
					if (orangePortalDest == null || !orangePortalDest.open) orangePortalDest = p;
					break;
				}
			}
			else
			{
				if (p.open && p.getDestination() == orangePortal && orangePortal != null) 
				{
					if (bluePortalDest == null || !bluePortalDest.open) bluePortalDest = p;
					break;
				}
			}
		}
		
		if (orange)
		{
			if (orangePortal != null) orangePortal.close();
			orangePortalDest = null;
		}
		else
		{
			if (bluePortal != null) bluePortal.close();
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
	
	public void regionPortalCreated(boolean orange)
	{
		for (Portal p : portals)
		{
			if (p.open != orange && p.getDestination() == null) p.open();
		}
	}
	
	public boolean contains(Vector vector) {
		return vector.isInAABB(min, max);
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
