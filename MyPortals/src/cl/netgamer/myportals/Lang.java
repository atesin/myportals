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

		// §B: cyan: success - §E: yellow: info - §D: magenta: error
		// paragraph sign "§": win = alt+21 (keypad), linux = alt+167 (keypad)
		// on encoding problems save as ansi
		
		// help
		msg.put("portal", "§B");
		msg.put("help", "§B");
		msg.put("helpName", "§B");
		msg.put("helpDest", "§B");
		msg.put("helpPrivacy", "§B");
		msg.put("helpGive", "§B");
		msg.put("helpList", "§B");
		msg.put("helpInfo", "§B");
		// info
		msg.put("tags", "");
		msg.put("welcome", "§E");
		msg.put("listHead", "§E");
		msg.put("rebuilding", "§E");
		// error
		msg.put("bePlayer", "§D");
		msg.put("argsNotMatch", "§D");
		msg.put("unknownCmd", "§D");
		msg.put("tooFar", "§D");
		msg.put("lookNotPortal", "§D");
		msg.put("hidden", "§D");
		msg.put("locked", "§D");
		msg.put("invalidName", "§D");
		msg.put("busyName", "§D");
		msg.put("nameNotFound", "§D");
		msg.put("diffrentWorlds", "§D");
		msg.put("noName", "§D");
		msg.put("notYours", "§D");
		msg.put("offlinePlayer", "§D");
		msg.put("beConsole", "§D");
		msg.put("mustConfirm", "§D");
		// success
		msg.put("activatedOk", "§B");
		msg.put("namedOk", "§B");
		msg.put("destOk", "§B");
		msg.put("privacyOk", "§B");
		msg.put("giveOk", "§B");
		msg.put("recieveOk", "§B");
		msg.put("rebuilded", "§B");
		
		for (String key: msg.keySet()) msg.put(key, msg.get(key)+conf.getConfig().getString(key));
	}
}
