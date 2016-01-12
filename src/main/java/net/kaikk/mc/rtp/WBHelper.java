package net.kaikk.mc.rtp;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.WorldBorder;

public class WBHelper {
	static public WBData getInfo(String name) {
		if (WorldBorder.plugin==null) {
			return null;
		}
		
		BorderData bd = WorldBorder.plugin.getWorldBorder(name);
		if (bd==null) {
			return null;
		}

		return new WBData(bd.getRadiusX(), bd.getRadiusZ(), (int) bd.getX(), (int) bd.getZ());
	}
	
	static public boolean insideBorder(String name, int x, int z) {
		if (WorldBorder.plugin!=null) {
			BorderData bd = WorldBorder.plugin.getWorldBorder(name);
			if (bd!=null) {
				return WorldBorder.plugin.getWorldBorder(name).insideBorder(x, z);
			} else {
				return true;
			}
		}
		
		return false;
	}
}
