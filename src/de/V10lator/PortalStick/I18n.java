package de.V10lator.PortalStick;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import com.matejdro.bukkit.portalstick.PortalStick;

public class I18n
{
  private final PortalStick plugin;
  private YamlConfiguration lang;
  private final YamlConfiguration fallback;
  
  public I18n(PortalStick plugin, File myself) //We can't access getFile() from outside of the main class *facepalm*
  {
	//Init fallback:
	this.plugin = plugin;
	YamlConfiguration yml = new YamlConfiguration();
	try
	{
	  yml.load(getClass().getResourceAsStream("/lang/en_US.txt"));
	}
	catch(Exception e)
	{
	  e.printStackTrace();
	}
	fallback = yml;
	
	//Create lang files:
	try
	{
	  JarFile jar = new JarFile(myself);
	  Enumeration<JarEntry> entries = jar.entries();
	  String pkg = "lang/";
	  File ld = new File(plugin.getDataFolder(), "lang");
	  if(!ld.exists())
		ld.mkdirs();
	  File f;
	  JarEntry entry;
	  InputStream in;
	  FileOutputStream out;
	  int bl = 4096;
	  byte[] buffer = new byte[bl];
	  int r;
	  while(entries.hasMoreElements())
	  {
		entry = entries.nextElement();
		String en = entry.getName();
		if(!en.startsWith(pkg))
		  continue;
		f = new File(ld, en.substring(5));
		if(f.exists())
		  continue;
		f.createNewFile();
		in = jar.getInputStream(entry);
		out = new FileOutputStream(f);
		while((r = in.read(buffer, 0, bl)) > 0)
		  out.write(buffer, 0, r);
		in.close();
		out.flush();
		out.close();
	  }
	}
	catch(Exception e)
	{
	  e.printStackTrace();
	}
	
	//Try setting the correct language:
	setLang(plugin.config.lang);
  }
  
  public boolean setLang(String lang)
  {
	File f = new File(plugin.getDataFolder(), "lang");
	if(!f.exists())
	  f.mkdirs();
	f = new File(f, lang+".txt");
	if(!f.exists())
	{
	  plugin.getLogger().info("Language file "+f.getAbsolutePath()+" not found!");
	  return false;
	}
	YamlConfiguration yml = new YamlConfiguration();
	try
	{
	  yml.load(f);
	  this.lang = yml;
	}
	catch(Exception e)
	{
	  plugin.getLogger().info("Internal error loading "+f.getAbsolutePath());
	  e.printStackTrace();
	  return false;
	}
	return true;
  }
  
  public String getString(String node, String ... replace)
  {
	if(lang.contains(node))
	  node = lang.getString(node);
	else if(fallback.contains(node))
	  node = fallback.getString(node);
	else
	  node = ChatColor.RED+"I18n error: Node "+node+" not found!";
	for(int i = 0; i < replace.length; i++)
	  node = node.replaceAll("%"+i+"%", replace[i]);
	return node;
  }
}
