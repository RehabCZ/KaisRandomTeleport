package net.kaikk.mc.rtp;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class KaisRandomTP extends JavaPlugin {
	static KaisRandomTP instance;
	Config config;
	ConcurrentHashMap<UUID,Long> lastUsed = new ConcurrentHashMap<UUID,Long>();
	Class<?> worldBorder;
	
	public void onEnable() {
		instance=this;
		
		this.config=new Config();
		
		try {
			worldBorder=Class.forName("com.wimbli.WorldBorder.WorldBorder");
			this.getLogger().info("Found WorldBorder");
		} catch (ClassNotFoundException | SecurityException e) { }
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You're the console.");
			return false;
		}
		
		if (cmd.getName().equalsIgnoreCase("randomtp")) {
			if(!sender.hasPermission("rtp.use")) {
				sender.sendMessage("No permission.");
				return false;
			}
			
			Player player = (Player) sender;
			return rtp(player);
		}
		return false;
	}
	
	public boolean rtp(Player player) {
		World world = player.getWorld();

		if (!player.hasPermission("rtp.bypass")) {
			if (isBlacklistedWorld(world.getName())) {
				player.sendMessage("You can't use /rtp in this world.");
				return false;
			}
			
			Long lastUsed = this.lastUsed.get(player.getUniqueId());

			if (lastUsed!=null && System.currentTimeMillis()-lastUsed < this.config.timelimit*1000) {
				player.sendMessage("Wait "+(this.config.timelimit-((System.currentTimeMillis()-lastUsed)/1000))+" seconds before using /rtp again.");
				return false;
			}
		}

		int x, y=0, z, count=0, rx, rz, sx, sz;
		Integer[] info = null;
		
		if (this.worldBorder!=null) {
			info = WBHelper.getInfo(world.getName());
		}
		
		if (info!=null) {
			rx=info[0];
			rz=info[1];
			sx=info[2];
			sz=info[3];
		} else {
			Location spawn = world.getSpawnLocation();
			rx=(int) world.getWorldBorder().getSize()/2;
			if (this.config.range<rx) {
				rx=this.config.range;
			}
			rz=rx;
			sx=spawn.getBlockX();
			sz=spawn.getBlockZ();
		}

		do {
			x = (int) (Math.random()*rx*2)-rx+sx;
			z = (int) (Math.random()*rz*2)-rz+sz;
			if (this.worldBorder==null || WBHelper.insideBorder(world.getName(), x, z)) {
				y=searchSuitableYLevel(world, x, z);
				count++;
			}
		} while(y==-1&&count<10);
		
		if (y==-1) {
			player.sendMessage("No suitable location found!");
			return false;
		}
		y+=2;
		Location rtp = new Location(world, x,y,z);
		player.sendMessage("Teleporting you at "+x+", "+y+", "+z);
		world.getChunkAt(rtp).load(true);
		this.lastUsed.put(player.getUniqueId(), System.currentTimeMillis());
		new CheckTPTask(this, player, rtp).runTaskTimer(this, 4L, 4L);
		return true;
	}
	
	private int searchSuitableYLevel(World world, int x, int z) {
		int y=60;
		Block block = world.getBlockAt(x, y, z);
		do {
			block = block.getRelative(BlockFace.UP);
			if (block.isLiquid()) {
				return -1;
			}
			
			if (isSuitableBlock(block)) {
				return y;
			}
			y++;
		} while(y<126);

		y=59;
		block = world.getBlockAt(x, y, z);
		do {
			block = block.getRelative(BlockFace.DOWN);
			if (block.isLiquid()) {
				return -1;
			}
			
			if (isSuitableBlock(block)) {
				return y;
			}
			y--;
		} while(y>0);
		
		return -1;
	}
	
	@SuppressWarnings("deprecation")
	private boolean isSuitableBlock(Block block) {
		if (block.getType().isSolid() && !this.isBlacklistedId(block.getType().getId())) {
			Block upperBlock = block.getRelative(BlockFace.UP);
			
			int c=0;
			while(c<5) {
				if (!upperBlock.isEmpty()) {
					break;
				}
				upperBlock = upperBlock.getRelative(BlockFace.UP);
				c++;
			}
			
			if (c==5) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isBlacklistedId(int blockId) {
		for (Integer blacklistedId : this.config.blockIdBlacklist) {
			if (blacklistedId==blockId) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isBlacklistedWorld(String world) {
		for (String blacklistedWorld : this.config.worldBlacklist) {
			if (world.equalsIgnoreCase(blacklistedWorld)) {
				return true;
			}
		}
		
		return false;
	}
}
