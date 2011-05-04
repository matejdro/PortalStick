package com.matejdro.bukkit.portalstick;

import java.io.File;
import java.util.logging.Level;
public class InputOutput {
    private PortalStick plugin;
    private PropertiesFile pf;
        
	public InputOutput(PortalStick instance)
	{
		plugin = instance;
		if (!new File("plugins" + File.separator + "PortalStick").exists()) {
			try {
			(new File("plugins" + File.separator + "PortalStick")).mkdir();
			} catch (Exception e) {
			PortalStick.log.log(Level.SEVERE, "[PortalStick]: Unable to create plugins/PortalStick/ directory");
			}
			}
		pf = new PropertiesFile(new File("plugins" + File.separator + "PortalStick","PortalStick.properties")); 
	}
    
    
    public void LoadSettings()
	{
    	Settings.PortalTool = pf.getInt("PortalTool", 280, "What tool is used to create portals. Default: Stick (280)");
    	Settings.TeleportVehicles = pf.getBoolean("TeleportVehicles", true, "Can vehicles(boats, minecarts) also be teleported?");
    	Settings.EnableMaterialEmancipationGrill = pf.getBoolean("EnableMaterialEmancipationGrill", true, "can Material Emancipation Grill? be created?");
    	String tmp[] = pf.getString("TransparentBlocks", "0,8,9,10,11,20", "Blocks that can portals be shot through").split(",");
    	for (String i : tmp)
    		Settings.TransparentBlocks.add(Integer.parseInt(i));
    	tmp = pf.getString("PortallableBlocks", "1", "On which blocks can portals be created? You can separate them by comma(,). Default: Stone (1)").split(",");
    	for (String i : tmp)
    		Settings.PortallableBlocks.add(Integer.parseInt(i));
    	tmp = pf.getString("EnabledWorlds", plugin.getServer().getWorlds().get(0).getName(), "In which worlds can portals be placed? Separate multiple worlds by comma(,)").split(",");
    	for (String i : tmp)
    		Settings.EnabledWorlds.add(i);
    	Settings.AnyBlockIsPortallable = pf.getBoolean("AnyBlockIsPortallable", false, "Can portals be put on any block? This ignores PortallableBlocks list.");
    	Settings.MaterialEmancipationGrillFrameBlock = pf.getInt("MaterialEmancipationGrillFrameBlock", 48, "Which block is used for frame of Material Emancipation Grill? Default: Mossy Cobblestone (48)");
    	Settings.VelocityMultiplier = pf.getDouble("VelocityMultiplier", 1.0, "By default, minecraft falling does not provide not nearly as much velocity as original portal does. But you can multiply it using this setting.");
    	Settings.CompactPortal = pf.getBoolean("CompactPortal", false, "Use compacter portal design rather than bulky one. HIGHLY EXPERIMENTAL and it won't work properly when shooting portal to longer distances,so use with caution!");
    	
    	Settings.MessagePortalEnabled = pf.getString("MessagePortalEnabled", "You have just turned your crappy piece of wood into Aperture Science Handheld Portal Stick!", "");
    	Settings.MessagePortalDisabled = pf.getString("MessagePortalDisabled", "You have just reverted your Aperture Science Handheld Portal Stick back into crappy piece of wood!", "");
    	Settings.MessagePortalCannotCreate = pf.getString("MessagePortalCannotCreate", "You cannot place portal here!", "");
    	Settings.MessageRestrictedWorld = pf.getString("MessageRestrictedWorld", "You cannot do that in this world!", "");
    	Settings.DeletePortalsOnLeave = pf.getBoolean("DeletePortalsOnLeave", true, "When player leaves server, should his portals be deleted?");
    	pf.save();
	}
    
}
