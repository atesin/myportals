package cl.netgamer.myportals;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;

class Data{

	private ConfigAccessor conf;
	
	Data(MyPortals plugin){
		conf = new ConfigAccessor(plugin, "data.yml");
		conf.saveDefaultConfig();
	}

	void savePortal(Portal portal){
		conf.getConfig().set(MyPortals.locationEncode(portal.getLocation()), portal.encode());
		conf.saveConfig();
	}
	
	/** save null = delete */
	void delPortal(Portal portal){
		conf.getConfig().set(MyPortals.locationEncode(portal.getLocation()), null);
		conf.saveConfig();
	}
	
	Map<Location, Portal> loadPortals(){
		Map<Location, Portal> portals = new HashMap<Location, Portal>();
		for (String key: conf.getConfig().getKeys(false)){
			// redundant location for faster searching
			Location loc = MyPortals.locationDecode(key);
			portals.put(loc, new Portal(loc, conf.getConfig().getStringList(key)));
		}
		return portals;
	}
}
