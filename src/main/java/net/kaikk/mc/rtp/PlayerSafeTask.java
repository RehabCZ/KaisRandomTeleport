package net.kaikk.mc.rtp;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerSafeTask extends BukkitRunnable {
	Player player;
	Location playerOldLocation;
	int count=0;

	PlayerSafeTask(Player player, Location playerOldLocation) {
		this.player = player;
		this.playerOldLocation = playerOldLocation;
	}

	@Override
	public void run() {
		if (this.player.getLastDamageCause()!=null) {
			this.cancel();
			if (this.player.isDead()) {
				this.player.sendMessage(ChatColor.RED + KaisRandomTP.messagePrefix + Messages.get("OnDeath"));
			} else {
				this.player.sendMessage(ChatColor.RED + KaisRandomTP.messagePrefix + Messages.get("UnsafeLocation"));

				this.player.setHealth(this.player.getHealth()+this.player.getLastDamage());
				this.player.setFireTicks(0);
				this.player.teleport(this.playerOldLocation);
				this.player.setLastDamageCause(null);
				KaisRandomTP.instance.lastUsed.put(player.getUniqueId(), System.currentTimeMillis());
				new SearchTeleportLocationTask(this.player, this.playerOldLocation).runTaskLater(KaisRandomTP.instance, 4L);
			}
			return;
		}
		
		if (player.getMaximumNoDamageTicks()*this.count>100) {
			this.cancel();
			return;
		}
		this.count++;
	}
}
