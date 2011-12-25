 /*
 * TELEPADS - By WizzleDonker
 * Originally made for LordOfJustice
 */
package me.wizzledonker.plugins.telepads;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Telepads extends JavaPlugin {
    PlayerListener telepadsPlayerListener = new telepadsPlayerListener(this);
    public Map<Location,String> telepads = new HashMap<Location,String>();
    
    public int telepad_item_id = 1;
    public boolean nodestmsg_enable = false;
    
    telepadsCommands commandex = new telepadsCommands(this);
    
    public void onDisable() {
        // TODO: Place any custom disable code here.
        saveConfig();
        System.out.println(this + " is now disabled!");
    }

    public void onEnable() {
        PluginManager pm = this.getServer().getPluginManager();
        
        //Registering events
        pm.registerEvent(Type.PLAYER_MOVE, telepadsPlayerListener, Priority.Normal, this);
        
        //Register all the commands to be used
        getCommand("createpad").setExecutor(commandex);
        getCommand("padlink").setExecutor(commandex);
        getCommand("delpad").setExecutor(commandex);
        getCommand("linkpadhere").setExecutor(commandex);
        
        reloadprops();
        System.out.println(this + " by wizzledonker is now enabled!");
    }
    
    public void createPad(Player player, String name) {
        if (!player.hasPermission("telepads.create")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to make a teleport pad!");
            return;
        }
        Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        if (block.getTypeId() == telepad_item_id) {
            getConfig().set("pads." + name + ".X", block.getX());
            getConfig().set("pads." + name + ".Y", block.getY());
            getConfig().set("pads." + name + ".Z", block.getZ());
            getConfig().set("pads." + name + ".world", block.getLocation().getWorld().getName());
            getConfig().set("pads." + name + ".dest", null);
            saveConfig();
            player.sendMessage(ChatColor.GREEN + "Successfully created teleport pad " + name);
            reloadprops();
        } else {
            player.sendMessage(ChatColor.RED + "The block below you is not of the right type!");
            return;
        }
        
    }
    
    public void deletePad(CommandSender player, String name) {
        if (!player.hasPermission("telepads.delete")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to delete this Teleport Pad!");
            return;
        }
        if (!getConfig().getConfigurationSection("pads").contains(name)) {
            player.sendMessage(ChatColor.RED + "That pad does not exist!");
            return;
        }
        getConfig().set("pads." + name, null);
        saveConfig();
        reloadprops();
        player.sendMessage(ChatColor.DARK_GREEN + "Teleport pad removed.");
    }
    
    public void linkPads(String pad1, String pad2, Player player) {
        if ((!getConfig().getConfigurationSection("pads").contains(pad1)) || (!getConfig().getConfigurationSection("pads").contains(pad2))) {
            player.sendMessage(ChatColor.RED + "One of the pads doesn't exist!");
            return;
        }
        getConfig().set("pads." + pad1 + ".dest", pad2);
        getConfig().set("pads." + pad2 + ".dest", pad1);
        player.sendMessage(ChatColor.GREEN + "Successfully linked " + pad1 + " to " + pad2);
    }
    
    public void linkPadHere(Player player, String pad) {
        if (!player.hasPermission("telepads.linkhere")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to link this pad here!");
            return;
        }
        if (!getConfig().getConfigurationSection("pads").contains(pad)) {
            player.sendMessage(ChatColor.RED + "That pad does not exist!");
            return;
        }
        getConfig().set("pads." + pad + ".dest", "location/" + player.getLocation().toString());
    }
    
    public void gotoPad(Location loc, Player player) {
        String dest = null;
        dest = getConfig().getString("pads." + getPad(loc) + ".dest");
//        if (dest.contains("location")) {
//            //TODO: Handle teleporting the player DTL
//            return;
//        }
        if (dest == null || (!getConfig().getConfigurationSection("pads").contains(dest))) {
            if (nodestmsg_enable) {
                player.sendMessage("No destination!");
            }
            return;
        }
        player.teleport(new Location((getServer().getWorld(getConfig().getString("pads." + dest + ".world"))),
                getConfig().getInt("pads." + dest + ".X") - 1,
                getConfig().getInt("pads." + dest + ".Y"),
                getConfig().getInt("pads." + dest + ".Z") + 1));
        player.sendMessage(ChatColor.GREEN + "Successfully teleported to " + ChatColor.WHITE + dest);
    }
    
    public void reloadprops() {
        if (!new File(this.getDataFolder(), "config.yml").exists()) {
            System.out.println(this + ": No config found. Aborting search, generating one...");
            getConfig().options().header("For type ID's, go to www.minecraftwiki.net/wiki/Data_values");
            getConfig().addDefault("pads.properties.type_id", 1);
            getConfig().addDefault("pads.properties.nodestmsg_enabled", false);
            getConfig().options().copyDefaults(true);
            saveConfig();
            return;
        }
        
        telepad_item_id = getConfig().getInt("pads.properties.type_id", 1);
        
        nodestmsg_enable = getConfig().getBoolean("pads.properties.nodestmsg_enabled", false);
        //This function imports the properties to a hashmap

        Location locationTemp = null;
        // TODO: Make this getList... a proper while statement rather than something that doesn't work.
        List<String> names = new ArrayList<String>(getConfig().getConfigurationSection("pads").getKeys(false));
        if (names.isEmpty()) {
            System.out.println(this + ": No pads were loaded!");
            return;
        }
        for (String nextit : names) {
            if (!getConfig().contains("pads." + nextit)) {
                System.out.println(this + ": Finished re-scanning pads");
                return;
            }
            locationTemp = new Location(null, getConfig().getInt("pads." + nextit + ".X"), 
                    getConfig().getInt("pads." + nextit + ".Y"),
                    getConfig().getInt("pads." + nextit + ".Z"));
            telepads.put(locationTemp, nextit);
        }
    }
    
    public String getPad(Location loc) {
        return telepads.get(loc);
    }
}
