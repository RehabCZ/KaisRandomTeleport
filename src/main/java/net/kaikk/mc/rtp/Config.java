package net.kaikk.mc.rtp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
	private KaisRandomTP instance;

	int range, cooldown;
	List<Material> blockBlacklist = new ArrayList<Material>();
	List<Material> blockWhitelist = new ArrayList<Material>();
	List<String> worldBlacklist;
	
	boolean warningMessage;
		
	Config(KaisRandomTP instance) {
		this.instance = instance;
		instance.getConfig().options().copyDefaults(true);
		instance.saveDefaultConfig();
		
		this.range = instance.getConfig().getInt("Range");
		this.cooldown = instance.getConfig().getInt("Cooldown");
		
		for (String s : instance.getConfig().getStringList("BlockBlacklist")) {
			Material material = Material.matchMaterial(s);
			if (material!=null) {
				blockBlacklist.add(material);
			} else {
				instance.getLogger().warning("Blacklisted material '"+s+"' is invalid.");
			}
		}

		for (String s : instance.getConfig().getStringList("BlockWhitelist")) {
			Material material = Material.matchMaterial(s);
			if (material!=null) {
				blockWhitelist.add(material);
			} else {
				instance.getLogger().warning("Whitelisted material '"+s+"' is invalid.");
			}
		}
		
		worldBlacklist = instance.getConfig().getStringList("WorldBlacklist");
		
		warningMessage = instance.getConfig().getBoolean("WarningMessage");
		
		// Messages	
		instance.saveResource("messages.yml", false);
		FileConfiguration messages = YamlConfiguration.loadConfiguration(new File(instance.getDataFolder(), "messages.yml"));
		
		if (messages==null) {
			instance.getLogger().severe("There was an error while loading messages.yml!");
		} else {
			Messages.messages.clear();
			for (String key : messages.getKeys(false)) {
				Messages.messages.put(key, ChatColor.translateAlternateColorCodes('&', messages.getString(key)));
			}
		}
	}
	
	void saveResource(String name) {
		if (!new File(instance.getDataFolder(), name).exists()) {
			instance.saveResource(name, false);
		}
	}
}
