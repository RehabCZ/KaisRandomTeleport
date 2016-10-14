package net.kaikk.mc.rtp;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class KaisRandomTP extends JavaPlugin {
	static KaisRandomTP instance;
	static final String messagePrefix = ChatColor.GREEN + "[" + ChatColor.GOLD + "RTP" + ChatColor.GREEN + "]";
	Config config;
	Map<UUID,Long> lastUsed = new ConcurrentHashMap<UUID,Long>();

	Class<?> worldBorder;
	
	public void onEnable() {
		instance=this;
		
		this.config=new Config(this);
		
		try {
			worldBorder=Class.forName("com.wimbli.WorldBorder.WorldBorder");
			this.getLogger().info("Found WorldBorder");
		} catch (ClassNotFoundException | SecurityException e) { }
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("The console is omnipresent.");
			return false;
		}
		
		Player player = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("randomtp")) {
			if(!player.hasPermission("rtp.use")) {
				player.sendMessage(messagePrefix + ChatColor.RED + " " + Messages.get("NoPermission"));
				return false;
			}
			
			if (this.config.warningMessage && !player.hasPermission("rtp.bypass") && this.lastUsed.get(player.getUniqueId())==null) {
				player.sendMessage(messagePrefix + " " + ChatColor.RED + Messages.get("WarningMessage"));
				this.lastUsed.put(player.getUniqueId(), 0L);
				return false;
			}
			
			rtp(player);
			return true;
		}
		return false;
	}
	
	public void rtp(Player player) {
		World world = player.getWorld();
		
		if (player.getHealth()<4) {
			player.sendMessage(messagePrefix + ChatColor.RED + " " + Messages.get("LowHealth"));
			return;
		}
		
		if (player.getNoDamageTicks()>0) {
			player.sendMessage(messagePrefix + ChatColor.RED + " " + Messages.get("GotDamaged"));
			return;
		}
		
		if (!player.hasPermission("rtp.bypass")) {
			if (isBlacklistedWorld(world.getName())) {
				player.sendMessage(messagePrefix + ChatColor.RED +  " " + Messages.get("BlacklistedWorld"));
				return;
			}
			
			Long lastUsed = this.lastUsed.get(player.getUniqueId());

			if (lastUsed!=null && System.currentTimeMillis()-lastUsed < this.config.cooldown*1000) {
				player.teleport(player.getLocation());
				player.sendMessage(messagePrefix + ChatColor.RED +  Messages.get("Cooldown").replace("%seconds", String.valueOf((this.config.cooldown-((System.currentTimeMillis()-lastUsed)/1000)))));
				return;
			}
		} else {
			Long lastUsed = this.lastUsed.get(player.getUniqueId());

			if (lastUsed!=null && System.currentTimeMillis()-lastUsed < 5000) {
				player.teleport(player.getLocation());
				player.sendMessage(messagePrefix + ChatColor.RED + Messages.get("Cooldown").replace("%seconds", String.valueOf((5-((System.currentTimeMillis()-lastUsed)/1000)))));
				return;
			}
		}

		player.sendMessage(messagePrefix + ChatColor.GOLD +  " " + Messages.get("PendingTeleport"));
		KaisRandomTP.instance.lastUsed.put(player.getUniqueId(), System.currentTimeMillis());
		Location loc=player.getLocation();
		new SearchTeleportLocationTask(player, new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ())).runTask(this);
	}
	
	
	// This method search for a safe y level at the specified world, x, z location
	// It uses a custom algorithm that tries to skip empty blocks (fastforward)
	@SuppressWarnings("deprecation")
	int searchSuitableYLevel(World world, int x, int z) {
		int y=126; // Max y level
		int fastforward=8; // default fastforward
		if (world.getEnvironment()==World.Environment.THE_END) { // 
			fastforward=2; // fastforward for the end
		}
		
		Block block = world.getBlockAt(x, y, z);
		int c, solid=Integer.MAX_VALUE, shift=0;
		do {
			if (block.getType().isSolid() || isWhitelisted(block.getType())) {
				if (shift==fastforward && solid>y) { // Stop fastforward
					solid=y;
					y+=fastforward-1;
					block = block.getRelative(BlockFace.UP, fastforward-1);
					continue;
				} else if (this.isBlacklisted(block.getType())) { // blacklisted block
					shift=4;
				} else {
					Block upperBlock = block.getRelative(BlockFace.UP);
					
					c=0;
					while(c<4) { // a y level is suitable if there are 4 empty blocks above a valid solid block
						if (upperBlock.getTypeId()!=0) { // if it's not empty
							if (upperBlock.isLiquid()) { // if you get a liquid, it could be an ocean or a lava lake... there won't be any good y level here.
								return -1;
							}
							break;
						}
						upperBlock = upperBlock.getRelative(BlockFace.UP);
						c++;
					}
					
					if (c==4) {
						return y; // Found a suitable y level
					} else {
						shift=4; // 
					}
				}
			} else {
				if (solid>y) { // fastforward mode
					shift=fastforward;
				} else {
					shift=1;
				}
			}
			
			y-=shift;
			block = block.getRelative(BlockFace.DOWN, shift);
		} while(y>0);
		
		return -1;
	}

	boolean isBlacklisted(Material blockMaterial) {
		return this.config.blockBlacklist.contains(blockMaterial);
	}
	
	boolean isBlacklistedWorld(String world) {
		return this.config.worldBlacklist.contains(world.toLowerCase());
	}
	
	boolean isWhitelisted(Material blockMaterial) {
		return this.config.blockWhitelist.contains(blockMaterial);
	}
}
