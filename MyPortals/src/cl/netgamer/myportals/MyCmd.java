package cl.netgamer.myportals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cl.netgamer.tabtext.TabText;

public class MyCmd implements CommandExecutor{

	// PROPERTIES
	
	protected MyPortals plugin;
	private Lang lang;
	private HashSet<Byte> transparents;
	static String[] tags;
	//private String[] privacy;
	private List<String> privacyCmd = Arrays.asList("public", "lock", "hide", "private");

	
	// CONSTRUCTORS
	
	MyCmd(MyPortals plugin, String locale, HashSet<Byte> transparents){
		this.plugin = plugin;
		lang = new Lang(plugin, locale);
		this.transparents = transparents;
		msg("infoHead").split("`", -1);
		tags = msg("tags").split(";", -1);
	}
	
	// METHODS
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args){
		
		if(cmd.getName().equalsIgnoreCase("portal")){
			
			
			if (args.length == 0){
				sender.sendMessage("MyPortals help (1/4) :\n"+msg("help1"));
				return true;
			}
			
			// java 6 switch compliance
			//int subCmd = Arrays.asList("name", "dest", "public", "locked", "hidden", "private", "give", "2", "3", "4", "info", "list").indexOf(args[0].toLowerCase());
			String subCmd = args[0].toLowerCase();
			// collapse, rebuild
			
			
			// validate sender
			if (!subCmd.equals("list") && !(sender instanceof Player)){
				sender.sendMessage("portal "+args[0]+": "+msg("bePlayer"));
				return true;
			}
			
			// parameters count
			//switch (subCmd){
			switch (subCmd){
			
			// no subparamters: public, lock, hide, private, 2, 3, 4, info
			case "public":
			case "lock":
			case "hide":
			case "private":
			case "2":
			case "3":
			case "4":
			case "info":
				if (args.length != 1){
					sender.sendMessage("portal "+args[0]+": "+msg("argsNotMatch"));
					return true;
				}
				break;
				
			// 1 subparameter:name, dest, give
			case "name":
			case "dest":
			case "give":
				if (args.length != 2){
					sender.sendMessage("portal "+args[0]+": "+msg("argsNotMatch"));
					return true;
				}
				break;
				
			// up to 2 subparameters: list
			case "list":
				if (args.length > 3){
					sender.sendMessage("portal "+args[0]+": "+msg("argsNotMatch"));
					return true;
				}
				break;
			default:
				sender.sendMessage("portal "+args[0]+": "+msg("unknownCmd"));
				return true;
			}
			
			
			// help pages
			switch (subCmd){
			
			// 2, 3, 4
			case "2":
			case "3":
			case "4":
				sender.sendMessage("MyPortals help ("+args[0]+"/4) :\n"+msg("help"+args[0]));
				return true;
			}
			
			// list can be performed by anyone
			if (subCmd.equals("list")){
				// parameters
				String owner; 
				int page;
				
				// default parameters by sender
				if (sender.getName().equals("CONSOLE")){
					owner = "*";
					page = 0;
				}else{
					owner = sender.getName();
					page = 1;
				}
				
				// get parameters by length
				switch (args.length){
				
				// owner or page?
				case 2:
					// list help
					try{
						page = Integer.parseInt(args[1]);
					}catch (NumberFormatException e){
						owner = args[1];
					}
					break;
					
				// owner and page
				case 3:
					owner = args[1];
					try{
						page = Integer.parseInt(args[2]);
					}catch (NumberFormatException e){}
				}
	
				// get header and data
				// owner(10), name(12), privacy(8), destination fullname(23)
				TabText tt = new TabText(msg(tags[5]+"----------`"+tags[6]+"------------`"+tags[7]+"--------`"+tags[8]+"-----------------------"));
				tt.setTabs(10, 22, 30);
				String h = tt.getPage(0, sender.getName().equals("CONSOLE"))+"\n";
				
				tt.setText(plugin.list(sender.getName(), owner, page));
				tt.sortByFields(1, 2);
				
				String ans = String.format(msg("listHead"), owner, page, tt.setPageHeight(8))+"\n"+h;
				ans += tt.getPage(page, sender.getName().equals("CONSOLE"));
				
				sender.sendMessage("portal list: "+ans);
				return true;
			}
			
			// get player
			Player player = (Player) sender;
			// get target location
			Location viewLoc = getTarget(player);
			if (viewLoc == null){
				player.sendMessage("portal "+args[0]+": "+msg("tooFar"));
				return true;
			}
			Portal portal = plugin.getPortalByLocation(viewLoc);
			if (portal == null){
				player.sendMessage("portal "+args[0]+": "+msg("lookNotPortal"));
				return true;
			}
			
			// execute looking portal commands
			switch (subCmd){
			
			case "info":
				String info = plugin.info(portal, player.getName());
				if (info == null) player.sendMessage("portal info: "+msg("hidden"));
				else player.sendMessage("portal info:\n"+info);
				return true;
				
			case "name":
				player.sendMessage("portal name: "+msg(plugin.name(portal, args[1], player)));
				return true;
				
			case "dest":
				player.sendMessage("portal dest: "+msg(plugin.dest(portal, args[1], player)));
				return true;
				
			case "public":
			case "lock":
			case "hide":
			case "private":
				player.sendMessage("portal dest: "+msg(plugin.setPrivacy(portal, privacyCmd.indexOf(args[0]), player)));
				return true;
				
			case "give":
				player.sendMessage("portal give: "+msg(plugin.give(portal, args[1], player.getName())));
				return true;
				
			}

		}
		return true;
	}
	
	// UTILITY
	
	private String msg(String key){
		if (key.length() < 20 && lang.msg.containsKey(key)) return lang.msg.get(key);
		else return key;
	}
	
	private Location getTarget(Player player){
		Location src = player.getEyeLocation();
		Location target = player.getTargetBlock(transparents, 100).getLocation();
		if (src.distanceSquared(target) > 25 || src.getWorld() != target.getWorld()) return null;
		else return target;
	}
}
