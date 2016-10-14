package net.kaikk.mc.rtp;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerWaitingForTeleportationTask extends BukkitRunnable {
	Player player;
	Location rtpLocation, startLocation;
	long time;
	
	PlayerWaitingForTeleportationTask(Player player, Location startLocation, Location loc, long time) {
		this.player=player;
		this.startLocation=startLocation;
		this.rtpLocation=loc;
		this.time=time;
	}
	
	PlayerWaitingForTeleportationTask(Player player, Location startLocation, Location loc) {
		this.player=player;
		this.startLocation=startLocation;
		this.rtpLocation=loc;
	}
	
	@Override
	public void run() {
		if (!this.player.isOnline() || this.player.isDead()) {
			this.cancel();
			return;
		}
		if (this.player.getLastDamageCause()!=null || this.player.getLocation().distanceSquared(this.startLocation)>2) {
			this.player.sendMessage(KaisRandomTP.messagePrefix + ChatColor.RED + Messages.get("MovedOrDamaged"));
			KaisRandomTP.instance.lastUsed.put(this.player.getUniqueId(), 0L);
			this.cancel();
			return;
		}
		
		if (System.currentTimeMillis()>=this.time) {
			new TeleportTask(this.player, new Location(this.player.getWorld(), this.rtpLocation.getX(), this.rtpLocation.getY(), this.rtpLocation.getZ()), this.startLocation).runTaskTimer(KaisRandomTP.instance, 0L, 4L);
			this.cancel();
			return;
		}
	}
}
