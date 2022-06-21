package net.kaikk.mc.rtp;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportTask extends BukkitRunnable {
	Player player;
	Location rtpLocation, playerOldLocation;
	int count=0;
	boolean flight;
	
	TeleportTask(Player player, Location rtpLocation, Location playerOldLocation) {
		this.player=player;
		this.rtpLocation=rtpLocation;
		this.playerOldLocation=playerOldLocation;
	}
	
	@Override
	public void run() {
		if (!this.player.isOnline() || this.player.isDead()) {
			this.cancel();
			return;
		}
		
		if (!this.player.getLocation().equals(this.rtpLocation)) {
			this.rtpLocation.getChunk().load(true);
			if (this.count==0) {
				this.flight=this.player.getAllowFlight();
				this.player.setAllowFlight(true);
				this.player.setFlying(true);
				Block block = this.player.getWorld().getBlockAt(this.rtpLocation);
				block.setType(Material.AIR);
				block.getRelative(BlockFace.UP).setType(Material.AIR);
			}
			this.player.teleport(this.rtpLocation, TeleportCause.PLUGIN);
			this.player.setFallDistance(-100);
		}
		if(this.count>4) {
			this.player.setFlying(false);
			if (!this.flight) {
				this.player.setAllowFlight(false);
			}
			this.player.sendMessage(KaisRandomTP.messagePrefix + Messages.get("TeleportedTo")+" "+this.rtpLocation.getBlockX()+", "+this.rtpLocation.getBlockY()+", "+this.rtpLocation.getBlockZ()+".");
			new PlayerSafeTask(this.player, this.playerOldLocation).runTaskTimer(KaisRandomTP.instance, 0L, this.player.getMaximumNoDamageTicks()-1);
			this.cancel();
		}
		count++;
	}
}
