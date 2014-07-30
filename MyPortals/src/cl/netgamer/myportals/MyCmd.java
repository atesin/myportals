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
	String[] tags;
	//private String[] privacy;
	private List<String> privacyCmd = Arrays.asList("public", "lock", "hide", "private");

	
	// CONSTRUCTORS
	
	MyCmd(MyPortals plugin, HashSet<Byte> transparents){
		this.plugin = plugin;
		lang = new Lang(plugin, plugin.getConfig().getString("locale"));
		this.transparents = transparents;
		msg("infoHead").split("`", -1);
		tags = msg("tags").split(";", -1);
	}
	
	// METHODS
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args){
		
		if(cmd.getName().equalsIgnoreCase("portal")){
			
			
			// with no subparameter show tutorial
			if (args.length == 0){
				//sender.sendMessage("MyPortals help (1/4) :\n"+msg("help1"));
				sender.sendMessage("/portal\n"+msg("portal"));
				return true;
			}
			
			// get subcommand, will use a lot
			String subCmd = args[0].toLowerCase();

			// validate sender
			if (!subCmd.equals("list") && !subCmd.equals("rebuild") && !subCmd.equals("info") && !(sender instanceof Player)){
				sender.sendMessage("portal "+args[0]+": "+msg("bePlayer"));
				return true;
			}
			
			
			// PARAMETER COUNT
			
			
			switch (subCmd){
			
			// no subparamters: public, lock, hide, private, info
			case "public":
			case "lock":
			case "hide":
			case "private":
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
				
			// up to 1 subparameter: help, info, rebuild
			case "help":
			case "info":
			case "rebuild":
				if (args.length > 2){
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
				
			// unknown subcommand
			default:
				sender.sendMessage("portal : "+msg("unknownCmd"));
				return true;
			}
			
			
			// HELP PAGES
			
			
			if (subCmd.equalsIgnoreCase("help")){
				if (args.length == 1){
					sender.sendMessage("/portal help\n"+msg("help"));
					return true;
				}
				switch (args[1].toLowerCase()){
				case "name":
					sender.sendMessage("/portal help name\n"+msg("helpName"));
					return true;
				case "dest":
					sender.sendMessage("/portal help dest\n"+msg("helpDest"));
					return true;
				case "public":
				case "lock":
				case "hide":
				case "private":
					sender.sendMessage("/portal help "+args[1].toLowerCase()+"\n"+msg("helpPrivacy"));
					return true;
				case "give":
					sender.sendMessage("/portal help give\n"+msg("helpGive"));
					return true;
				case "list":
					sender.sendMessage("/portal help list\n"+msg("helpList"));
					return true;
				case "info":
					sender.sendMessage("/portal help info\n"+msg("helpInfo"));
					return true;
				}
			}
			
			
			// LIST PORTALS: CAN BE PERFORMED BY ANYONE
			
			
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
			
			
			// REBUILD PORTALS (SERVER CONSOLE COMMAND)
			
			
			if (subCmd.equals("rebuild")){
				
				// only by console
				if (!sender.getName().equals("CONSOLE")){
					sender.sendMessage("portal rebuild: "+msg("beConsole"));
					return true;
				}

				// did not confirm
				if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")){
					sender.sendMessage("portal rebuild: "+String.format(msg("mustConfirm"), "portal rebuild"));
					return true;
				}
				
				// rebuild portals
				sender.sendMessage("portal rebuild: "+msg("rebuilding"));
				plugin.rebuild();
				sender.sendMessage("portal rebuild: "+msg("rebuilded"));
				return true;
			}

			
			// INFO FOR SERVER CONSOLE
			
			
			if (subCmd.equalsIgnoreCase("info") && !(sender instanceof Player)){
				if (args.length != 2){
					sender.sendMessage("portal info: "+msg("argsNotMatch"));
					return true;
				}
				String info = plugin.info(args[1], sender.getName());
				if (info == null) sender.sendMessage("portal info: "+msg("hidden"));
				else if (info.length() < 1) sender.sendMessage("portal info: "+msg("nameNotFound"));
				else sender.sendMessage("portal info:\n"+info);
				return true;
			}
			
			
			// GET PLAYER AND HIS SELECTED PORTAL
			
			
			// get player
			if (!(sender instanceof Player)){
				sender.sendMessage("portal "+args[0]+": "+msg("bePlayer"));
				return true;
			}
			Player player = (Player) sender;
			//player.sendMessage("id data = "+player.getTargetBlock(null, 100).getTypeId()+" "+player.getTargetBlock(null, 100).getData());

			// get looked location
			Location viewLoc = getTarget(player);
			if (viewLoc == null){
				player.sendMessage("portal "+args[0]+": "+msg("tooFar"));
				return true;
			}
			
			// get looked portal
			Portal portal = plugin.getPortalByLocation(viewLoc);
			if (portal == null){
				player.sendMessage("portal "+args[0]+": "+msg("lookNotPortal"));
				return true;
			}
			
			
			// LOOKING PORTAL COMMANDS
			
			
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
	
	String msg(String key){
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
