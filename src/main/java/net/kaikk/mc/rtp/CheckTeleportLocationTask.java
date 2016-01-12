package net.kaikk.mc.rtp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

class CheckTeleportLocationTask extends BukkitRunnable {
	Player player;
	Location startLocation;
	int x, z, count;
	
	CheckTeleportLocationTask(Player player, Location startLocation, int x, int z, int count) {
		this.player = player;
		this.x = x;
		this.z = z;
		this.startLocation=startLocation;
		this.count=count;
	}

	@Override
	public void run() {
		if (!this.player.isOnline() || this.player.isDead()) {
			this.cancel();
			return;
		}
		
		int y=KaisRandomTP.instance.searchSuitableYLevel(this.player.getWorld(), x, z);
		Location toLocation = null;
		
		if (y!=-1) {
			toLocation = new Location(this.player.getWorld(), x+0.5, y+1, z+0.5);
			// call a teleport event
			PlayerTeleportEvent event = new PlayerTeleportEvent(player, startLocation, toLocation);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				y=-1;
			}
		}
		
		if (y==-1) {
			if (count<500) {
				new SearchTeleportLocationTask(this.player, this.startLocation, this.count+5).runTask(KaisRandomTP.instance);
			} else {
				this.player.sendMessage(ChatColor.RED + KaisRandomTP.messagePrefix + " " + Messages.get("NoLocationFound"));
				KaisRandomTP.instance.lastUsed.put(this.player.getUniqueId(), System.currentTimeMillis()-((KaisRandomTP.instance.config.cooldown-5)*1000));
			}
		} else {
			long lastUsed=KaisRandomTP.instance.lastUsed.get(this.player.getUniqueId());
			long wait=5000-(System.currentTimeMillis()-lastUsed);

			if (wait<=0) {
				new TeleportTask(this.player, toLocation, this.startLocation).runTaskTimer(KaisRandomTP.instance, 0L, 4L);
			} else {
				new PlayerWaitingForTeleportationTask(this.player, this.startLocation, toLocation, lastUsed+wait).runTaskTimer(KaisRandomTP.instance, 4L, 4L);
			}
		}
	}
}
