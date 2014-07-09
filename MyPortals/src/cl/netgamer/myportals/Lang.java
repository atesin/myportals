package cl.netgamer.myportals;

import java.util.HashMap;
import java.util.Map;

public class Lang {

	// PROPERTIES
	
	private ConfigAccessor conf = null;
	Map<String, String> msg = new HashMap<String, String>();

	// CONSTRUCTORS
	
	public Lang(MyPortals plugin, String locale){
		conf = new ConfigAccessor(plugin, "locale-"+locale+".yml");
		conf.saveDefaultConfig();

		// �B: cyan: success - �E: yellow: info - �D: magenta: error
		// paragraph sign "�": win = alt+21 (keypad), linux = alt+167 (keypad)
		msg.put("welcome", "�B");
		msg.put("help1", "�E");
		msg.put("help2", "�E");
		msg.put("help3", "�E");
		msg.put("help4", "�E");
		msg.put("tags", "");
		msg.put("bePlayer", "�D");
		msg.put("argsNotmatch", "�D");
		msg.put("unknownCmd", "�D");
		msg.put("listHead", "�E");
		msg.put("tooFar", "�D");
		msg.put("lookNotPortal", "�D");
		msg.put("hidden", "�D");
		msg.put("locked", "�D");
		msg.put("invalidName", "�D");
		msg.put("busyName", "�D");
		msg.put("namedOk", "�B");
		msg.put("nameNotFound", "�D");
		msg.put("diffrentWorlds", "�D");
		msg.put("unnamed", "�D");
		msg.put("destOk", "�B");
		msg.put("privacyOk", "�B");
		msg.put("notYours", "�D");
		msg.put("offlinePlayer", "�D");
		msg.put("giveOk", "�B");
		
		for (String key: msg.keySet()) msg.put(key, msg.get(key)+conf.getConfig().getString(key));
	}
}
