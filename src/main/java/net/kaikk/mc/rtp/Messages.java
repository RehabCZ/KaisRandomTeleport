package net.kaikk.mc.rtp;

import java.util.HashMap;
import java.util.Map;

public class Messages {
	static Map<String, String> messages = new HashMap<String, String>();
	
	public static String get(String id) {
		return messages.get(id);
	}
}
