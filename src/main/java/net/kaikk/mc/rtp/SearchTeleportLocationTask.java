package net.kaikk.mc.rtp;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

class SearchTeleportLocationTask extends BukkitRunnable {
	Player player;
	Location startLocation;
	int count;

	SearchTeleportLocationTask(Player player, Location startLocation) {
		this(player, startLocation, 0);
		player.setLastDamageCause(null);
	}
	
	SearchTeleportLocationTask(Player player, Location startLocation, int count) {
		this.player = player;
		this.startLocation = startLocation;
		this.count = count;
	}
	
	@Override
	public void run() {
		if (!this.player.isOnline() || this.player.isDead()) {
			this.cancel();
			return;
		}
		
		if (this.player.getLastDamageCause()!=null || this.player.getLocation().distanceSquared(startLocation)>2) {
			this.player.sendMessage(KaisRandomTP.messagePrefix + ChatColor.RED + Messages.get("MovedOrDamaged"));
			KaisRandomTP.instance.lastUsed.put(this.player.getUniqueId(), 0L);
			return;
		}
		
		int x, z;
		World world = this.player.getWorld();
		WBData wb = null;
		
		if (KaisRandomTP.instance.worldBorder!=null) {
			wb = WBHelper.getInfo(world.getName());
		}
		
		if (wb==null) {
			Location spawn = world.getSpawnLocation();
			wb=new WBData(KaisRandomTP.instance.config.range, KaisRandomTP.instance.config.range, spawn.getBlockX(), spawn.getBlockZ());
		}
		
		do {
			x = (int) ((Math.random()*wb.radiusX*2)-wb.radiusX+wb.spawnX);
			z = (int) ((Math.random()*wb.radiusZ*2)-wb.radiusZ+wb.spawnZ);
			this.count++;
		} while(!(KaisRandomTP.instance.worldBorder==null || WBHelper.insideBorder(world.getName(), x, z)) && this.count<50);
			
		if (this.count<500) {
			((Location) new Location(world, x,1,z)).getChunk().load(true);
			
			new CheckTeleportLocationTask(this.player, startLocation, x, z, this.count).runTaskLaterAsynchronously(KaisRandomTP.instance, 1L);
		} else {
			this.player.sendMessage(KaisRandomTP.messagePrefix + ChatColor.RED + Messages.get("NoLocation"));
			KaisRandomTP.instance.lastUsed.put(this.player.getUniqueId(), System.currentTimeMillis()-((KaisRandomTP.instance.config.cooldown-5)*1000));
		}
	}
}
