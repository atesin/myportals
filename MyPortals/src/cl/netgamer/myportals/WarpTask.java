package cl.netgamer.myportals;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class WarpTask extends BukkitRunnable {

	private MyListener parent;
	private Player player;
	private Location location;
	
	public WarpTask(MyListener parent, Player player, Location location){
		this.parent = parent;
		this.player = player;
		this.location = location;
	}

	// this runs every tick?
	//@Override
	public void run(){
		// canceled?
		if (!parent.warps.containsValue(this.getTaskId())) return;
		
		// remove nausea effect before
		player.removePotionEffect(PotionEffectType.getById(9));

		// source portal still exists?
		Portal portal = parent.plugin.getPortalByLocation(location);
		if (portal == null) return;
		
		// destination portal exists?
		portal = parent.plugin.getPortalByLocation(portal.getDestination());
		if (portal == null) return;
		
		// player has enough experience points?
		int xp = player.getTotalExperience();
		if (xp < parent.plugin.xpCost) {
			player.sendMessage(MyPortals.msg("notEnoughXP"));
			return;
		}
		
		// allow teleport?
		//if (!portal.canWarp(player)) return;
		
		// all ok, try the teleport and take xp
		if (!portal.warp(player)) return;
		player.setTotalExperience(0);
		player.setLevel(0);
		player.setExp(0);
		player.giveExp(xp - parent.plugin.xpCost);
	}
	
	
}
