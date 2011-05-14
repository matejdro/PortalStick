package com.matejdro.bukkit.portalstick;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class GelManager {
	public static Location useGel(Entity entity, Location LocTo, Vector vector)
	{
		Region region = RegionManager.getRegion(LocTo);
		Block block = LocTo.getBlock();
		if (Util.compareBlockToString(block.getRelative(0,-1,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region);
		else if (Math.abs(vector.getY()) > 0.3 && Util.compareBlockToString(block.getRelative(0,-2,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region);
		else if (Math.abs(vector.getY()) > 0.5 && Util.compareBlockToString(block.getRelative(0,-3,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region);
		
		else if (Util.compareBlockToString(block.getRelative(1,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region);
		else if (Util.compareBlockToString(block.getRelative(2,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region);
		else if (Util.compareBlockToString(block.getRelative(3,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region);
		else if (Util.compareBlockToString(block.getRelative(-1,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region);
		else if (Util.compareBlockToString(block.getRelative(-2,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region);
		else if (Util.compareBlockToString(block.getRelative(-3,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region);

		else if (Util.compareBlockToString(block.getRelative(0,0,1), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region);
		else if (Util.compareBlockToString(block.getRelative(0,0,2), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region);
		else if (Util.compareBlockToString(block.getRelative(0,0,3), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region);
		else if (Util.compareBlockToString(block.getRelative(0,0,-1), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region);
		else if (Util.compareBlockToString(block.getRelative(0,0,-2), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region);
		else if (Util.compareBlockToString(block.getRelative(0,0,-3), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(entity, vector, region);
		
		return null;
	}
	
	public static Location BlueGel(Entity entity, Vector vector, Region region)
	{
		((Player) entity).sendMessage(String.valueOf(vector.getY()));
		if (entity instanceof Player && ((Player) entity).isSneaking()) return null;
			vector = vector.setX(-vector.getX() * region.getDouble(RegionSetting.BLUE_GEL_VELOCITY_MULTIPLIER));
			vector = vector.setY(-vector.getY() * region.getDouble(RegionSetting.BLUE_GEL_VELOCITY_MULTIPLIER));
			vector = vector.setZ(-vector.getZ() * region.getDouble(RegionSetting.BLUE_GEL_VELOCITY_MULTIPLIER));
			if (vector.getY() < 0.6 * region.getDouble(RegionSetting.BLUE_GEL_VELOCITY_MULTIPLIER)) vector.setY(0.6 * region.getDouble(RegionSetting.BLUE_GEL_VELOCITY_MULTIPLIER));
			entity.setVelocity(vector);
		return null;
	}
	
	public Location redGel()
	{
		return null;
	}
}
