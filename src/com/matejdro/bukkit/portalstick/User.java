package com.matejdro.bukkit.portalstick;

import org.bukkit.Location;
import org.bukkit.inventory.PlayerInventory;


public class User {
	private Portal blueportal;
	private Portal orangeportal;
	private PlayerInventory inventory;
	private Location pointOne;
	private Location pointTwo;
	private Boolean usingTool;
	
	public Portal getBluePortal() {
		return blueportal;
	}
	
	public void setBluePortal(Portal portal) {
		blueportal = portal;
	}
	
	public Portal getOrangePortal() {
		return orangeportal;
	}
	
	public void setOrangePortal(Portal portal) {
		orangeportal = portal;
	}
	
	public PlayerInventory getInventory() {
		return inventory;
	}
	
	public void setInventory(PlayerInventory inv) {
		inventory = inv;
	}

	public void setPointTwo(Location positiontwo) {
		this.pointTwo = positiontwo;
	}

	public Location getPointTwo() {
		return pointTwo;
	}

	public void setPointOne(Location positionone) {
		this.pointOne = positionone;
	}

	public Location getPointOne() {
		return pointOne;
	}

	public void setUsingTool(Boolean usingTool) {
		this.usingTool = usingTool;
	}

	public Boolean getUsingTool() {
		return usingTool;
	}
}
