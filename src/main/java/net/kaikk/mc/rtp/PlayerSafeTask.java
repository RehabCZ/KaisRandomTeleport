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
				this.player.sendMessage(KaisRandomTP.messagePrefix + ChatColor.RED +  Messages.get("OnDeath"));
			} else {
				this.player.sendMessage(KaisRandomTP.messagePrefix + ChatColor.RED + Messages.get("UnsafeLocation"));
				double health = this.player.getHealth()+this.player.getLastDamage();
				if (health>20) {
					health = 20;
				} else if (health<1) {
					health = 1;
				}
				this.player.setHealth(health);
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
