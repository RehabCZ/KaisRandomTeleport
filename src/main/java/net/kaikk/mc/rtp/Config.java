package net.kaikk.mc.rtp;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

class Config {
	final static String configFilePath = "plugins" + File.separator + "KaisRandomTP" + File.separator + "config.yml";
	private File configFile;
	FileConfiguration config;
	
	int range, timelimit;
	Integer[] blockIdBlacklist;
	String[] worldBlacklist;
	
	Config() {
		this.configFile = new File(configFilePath);
		this.config = YamlConfiguration.loadConfiguration(this.configFile);
		this.load();
	}

	void load() {
		this.range=config.getInt("range", 5000);
		this.timelimit=config.getInt("timelimit", 30);
		
		List<?> mbl=config.getList("blockIdBlacklist");
		List<?> wbl=config.getList("worldBlacklist");

		if (mbl!=null && mbl.size()>0) {
			this.blockIdBlacklist=mbl.toArray(new Integer[mbl.size()]);
		} else {
			this.blockIdBlacklist=new Integer[1];
			this.blockIdBlacklist[0]=0;
		}

		if (wbl!=null && wbl.size()>0) {
			this.worldBlacklist = wbl.toArray(new String[wbl.size()]);
		} else {
			this.worldBlacklist=new String[2];
			this.worldBlacklist[0] = "FakeWorld1";
			this.worldBlacklist[1] = "FakeWorld2";
		}
		
		this.save();
	}
	
	void save() {
		try {
			this.config.set("range", this.range);
			this.config.set("timelimit", this.timelimit);
			
			this.config.set("blockIdBlacklist", this.blockIdBlacklist);
			this.config.set("worldBlacklist", this.worldBlacklist);
			
			this.config.save(this.configFile);
		} catch (IOException e) {
			KaisRandomTP.instance.getLogger().warning("Couldn't create or save config file.");
			e.printStackTrace();
		}
	}
}