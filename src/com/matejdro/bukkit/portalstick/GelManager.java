package com.matejdro.bukkit.portalstick;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.BlockUtil;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class GelManager {
	public static Location useGel(Entity entity, Location LocTo, Vector vector)
	{
		Region region = RegionManager.getRegion(LocTo);
		Block block = LocTo.getBlock();
		
		if (BlockUtil.compareBlockToString(block.getRelative(0,-1,0), region.getString(RegionSetting.RED_GEL_BLOCK))) return redGel(entity, vector, region);
		
		if (vector.getY() <= 0)
		{
			if (BlockUtil.compareBlockToString(block.getRelative(0,-1,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 0);
			else if (Math.abs(vector.getY()) > 0.3 && BlockUtil.compareBlockToString(block.getRelative(0,-2,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 0);
			else if (Math.abs(vector.getY()) > 0.5 && BlockUtil.compareBlockToString(block.getRelative(0,-3,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 0);
		}
		if (vector.getX() >= 0)
		{
			if (BlockUtil.compareBlockToString(block.getRelative(1,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 1);
			else if (Math.abs(vector.getX()) > 0.3 && BlockUtil.compareBlockToString(block.getRelative(2,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 1);
			else if (Math.abs(vector.getX()) > 0.5 &&BlockUtil.compareBlockToString(block.getRelative(3,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 1);

		}
		else if (vector.getX() <= 0)
		{
			if (BlockUtil.compareBlockToString(block.getRelative(-1,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 2);
			else if (Math.abs(vector.getX()) > 0.3 && BlockUtil.compareBlockToString(block.getRelative(-2,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 2);
			else if (Math.abs(vector.getX()) > 0.5 && BlockUtil.compareBlockToString(block.getRelative(-3,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 2);

		}
		if (vector.getZ() >= 0)
		{
			if (BlockUtil.compareBlockToString(block.getRelative(0,0,1), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 3);
			else if (Math.abs(vector.getZ()) > 0.3 && BlockUtil.compareBlockToString(block.getRelative(0,0,2), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 3);
			else if (Math.abs(vector.getZ()) > 0.5 && BlockUtil.compareBlockToString(block.getRelative(0,0,3), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 3);

		}
		else if (vector.getZ() <=0 )
		{
			if (BlockUtil.compareBlockToString(block.getRelative(0,0,-1), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 4);
			else if (Math.abs(vector.getZ()) < 0.3 && BlockUtil.compareBlockToString(block.getRelative(0,0,-2), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 4);
			else if (Math.abs(vector.getZ()) > 0.5 &&BlockUtil.compareBlockToString(block.getRelative(0,0,-3), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region, 4);

		}
		
		return null;
	}
	
	public static Location BlueGel(Entity entity, Vector vector, Region region, int direction)
	{
		if (entity instanceof Player && ((Player) entity).isSneaking()) return null;
			
			switch (direction)
			{
				case 0:
					vector = vector.setX(vector.getX() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					vector = vector.setY(-vector.getY() * region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_VELOCITY_MULTIPLIER));
					vector = vector.setZ(vector.getZ() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					if (vector.getY() < region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_BOUNCE_VELOCITY)) vector.setY(region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_BOUNCE_VELOCITY));
					break;
				case 1:
					vector = vector.setX(-vector.getX() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					vector = vector.setY(vector.getY() * region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_VELOCITY_MULTIPLIER));
					vector = vector.setZ(-vector.getZ() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					if (vector.getY() < 1) vector.setY(1);
					if (vector.getX() > -region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY)) vector.setX(-region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY));
					break;
				case 2:
					vector = vector.setX(-vector.getX() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					vector = vector.setY(vector.getY() * region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_VELOCITY_MULTIPLIER));
					vector = vector.setZ(-vector.getZ() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					if (vector.getY() < 1) vector.setY(1);
					if (vector.getX() < region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY)) vector.setX(region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY));
					break;
				case 3:
					vector = vector.setX(-vector.getX() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					vector = vector.setY(vector.getY() * region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_VELOCITY_MULTIPLIER));
					vector = vector.setZ(-vector.getZ() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					if (vector.getY() < 1) vector.setY(1);
					if (vector.getZ() > -region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY)) vector.setZ(-region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY));
					break;
				case 4:
					vector = vector.setX(-vector.getX() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					vector = vector.setY(vector.getY() * region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_VELOCITY_MULTIPLIER));
					vector = vector.setZ(-vector.getZ() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					if (vector.getY() < 1) vector.setY(1);
					if (vector.getZ() < region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY)) vector.setZ(region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY));
					break;
					
					
			}
			entity.setVelocity(vector);
			
			Util.PlayNote((Player) entity, 4, 5);
		return null;
	}
	
	public static Location redGel(Entity entity, Vector vector, Region region)
	{
		if (((Player) entity).isSneaking())
		{
			vector = vector.setX(0);
			vector = vector.setZ(0);
		}
		else
		{
			vector = vector.setX(vector.getX() * region.getDouble(RegionSetting.RED_GEL_VELOCITY_MULTIPLIER));
			vector = vector.setZ(vector.getZ() * region.getDouble(RegionSetting.RED_GEL_VELOCITY_MULTIPLIER));
		}
		
		if (vector.getX() > region.getDouble(RegionSetting.RED_GEL_MAXIMUM_VELOCITY)) vector = vector.setX(region.getDouble(RegionSetting.RED_GEL_MAXIMUM_VELOCITY));
		if (vector.getZ() > region.getDouble(RegionSetting.RED_GEL_MAXIMUM_VELOCITY)) vector = vector.setZ(region.getDouble(RegionSetting.RED_GEL_MAXIMUM_VELOCITY));
		if (vector.getX() < -region.getDouble(RegionSetting.RED_GEL_MAXIMUM_VELOCITY)) vector = vector.setX(-region.getDouble(RegionSetting.RED_GEL_MAXIMUM_VELOCITY));
		if (vector.getZ() < -region.getDouble(RegionSetting.RED_GEL_MAXIMUM_VELOCITY)) vector = vector.setZ(-region.getDouble(RegionSetting.RED_GEL_MAXIMUM_VELOCITY));
		
		entity.setVelocity(vector);
				return null;
	}
}
