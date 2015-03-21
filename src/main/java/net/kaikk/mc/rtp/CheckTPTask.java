package net.kaikk.mc.rtp;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CheckTPTask extends BukkitRunnable  {
	KaisRandomTP instance;
	Player player;
	Location loc;
	int count=0;
	
	CheckTPTask(KaisRandomTP instance, Player player, Location loc) {
		this.instance=instance;
		this.player=player;
		this.loc=loc;
	}
	
	@Override
	public void run() {
		if (this.player.isOnline() && this.player.getLocation().getBlockY()!=this.loc.getBlockY()) {
			this.player.teleport(loc);
		}
		if(count>4) {
			this.cancel();
		}
		count++;
	}
}
