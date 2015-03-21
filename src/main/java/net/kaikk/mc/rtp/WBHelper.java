package net.kaikk.mc.rtp;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.WorldBorder;

public class WBHelper {
	static public Integer[] getInfo(String name) {
		BorderData bd = WorldBorder.plugin.getWorldBorder(name);
		if (bd==null) {
			return null;
		}
		
		Integer[] info = new Integer[4];
		info[0]=bd.getRadiusX();
		info[1]=bd.getRadiusZ();
		info[2]=(int) bd.getX();
		info[3]=(int) bd.getZ();
		
		return info;
	}
}
