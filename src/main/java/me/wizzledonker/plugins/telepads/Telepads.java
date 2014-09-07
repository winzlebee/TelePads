 /*
 * TELEPADS - By WizzleDonker
 * Originally made for LordOfJustice
 */
package me.wizzledonker.plugins.telepads;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.wizzledonker.plugins.telepads.config.padConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Telepads extends JavaPlugin {
    telepadsPlayerListener telepadsPlayerListener = new telepadsPlayerListener(this);
    public Map<Location,String> telepads = new HashMap<Location,String>();
    public Map<String, String> teleLink = new HashMap<String, String>();
    telepadsCommands commandex = new telepadsCommands(this);
    public padConfiguration pads = new padConfiguration(this);
    
    public int telepad_item_id = 1;
    public int telepad_teleport_time = 3;
    public boolean nodestmsg_enable = true;
    
    public String teleport_msg = "Teleported to %pad%";
    public String teleport_msg_denied = "You're not allowed to teleport on this pad";
    public String wait_msg = "Teleporting in %time%, stay on the pad!";
    public boolean teleport_msg_enable = true;
    
    FileConfiguration padConfig = null;
    
    @Override
    public void onDisable() {
        // Save the pad config file to disk
        pads.savePadConfig();
        System.out.println(this + " is now disabled!");
    }

    @Override
    public void onEnable() {
        PluginManager pm = this.getServer().getPluginManager();
        padConfig = pads.getPadConfig();
        
        //Registering events
        pm.registerEvents(telepadsPlayerListener, this);
        
        //Register all the commands to be used
        getCommand("createpad").setExecutor(commandex);
        getCommand("padlink").setExecutor(commandex);
        getCommand("delpad").setExecutor(commandex);
        getCommand("linkpadhere").setExecutor(commandex);
        getCommand("padlist").setExecutor(commandex);
        
        reloadprops();
        System.out.println(this + " by wizzledonker is now enabled!");
    }
    
    public void createPad(Player player, String name) {
        //Creation of a teleport pad
        if (!player.hasPermission("telepads.create")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to make a teleport pad!");
            return;
        }
        if (teleLink.containsKey(name)) {
            player.sendMessage(ChatColor.RED + "This pad already exists! Remove it before creating a new one.");
            return;
        }
        Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        if (block.getTypeId() == telepad_item_id) {
            padConfig.set("pads." + name + ".X", block.getX());
            padConfig.set("pads." + name + ".Y", block.getY());
            padConfig.set("pads." + name + ".Z", block.getZ());
            padConfig.set("pads." + name + ".pitch", player.getLocation().getPitch());
            padConfig.set("pads." + name + ".yaw", player.getLocation().getYaw());
            padConfig.set("pads." + name + ".world", block.getLocation().getWorld().getName());
            padConfig.set("pads." + name + ".dest", null);
            pads.savePadConfig();
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
        if (!teleLink.containsKey(name)) {
            player.sendMessage(ChatColor.RED + "That pad does not exist!");
            return;
        }
        padConfig.set("pads." + name, null);
        pads.savePadConfig();
        getServer().getPluginManager().removePermission("telepads.pads." + name);
        reloadprops();
        player.sendMessage(ChatColor.DARK_GREEN + getConfig().getString("messages.delete").replace("%pad%", name));
    }
    
    public void linkPads(String pad1, String pad2, Player player) {
        if (!player.hasPermission("telepads.link")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to link these pads!");
        }
        if ((!teleLink.containsKey(pad1)) || (!teleLink.containsKey(pad2))) {
            player.sendMessage(ChatColor.RED + "One of the pads doesn't exist!");
            return;
        }
        padConfig.set("pads." + pad1 + ".dest", pad2);
        padConfig.set("pads." + pad2 + ".dest", pad1);
        pads.savePadConfig();
        reloadprops();
        player.sendMessage(ChatColor.GREEN + getConfig().getString("messages.link").replace("%pad%", pad1).replace("%pad2%", pad2));
    }
    
    public void unLinkPad(String pad, CommandSender cs) {
        if (!cs.hasPermission("telepads.unlink")) {
            cs.sendMessage(ChatColor.RED + "You do not have permission to unlink this Teleport Pad!");
            return;
        }
        if ((!teleLink.containsKey(pad))) {
            cs.sendMessage(ChatColor.RED + "That pad does not exist!");
            return;
        }
        padConfig.set("pads." + pad + ".dest", null);
        pads.savePadConfig();
        reloadprops();
        cs.sendMessage(ChatColor.GREEN + getConfig().getString("messages.unlink").replace("%pad%", pad));
    }

    public void gotoPad(Location loc, Player player) {
        if ((!(player.hasPermission("telepads.pads." + getPad(loc)))) && (!(player.hasPermission("telepads.pads")))) {
            player.sendMessage(ChatColor.RED + teleport_msg_denied);
            return;
        }
        String dest = null;
        dest = teleLink.get(getPad(loc));
//        if (dest.contains("location")) {
//            //TODO: Handle teleporting the player DTL
//            return;
//        }
        if ((dest == null) || ("nowhere".equals(dest))) {
            if (nodestmsg_enable) {
                player.sendMessage("No destination!");
            }
            return;
        }
        Location tele = new Location((getServer().getWorld(padConfig.getString("pads." + dest + ".world"))),
                padConfig.getInt("pads." + dest + ".X") + 0.5,
                padConfig.getInt("pads." + dest + ".Y") + 1,
                padConfig.getInt("pads." + dest + ".Z") + 0.5);
        
        tele.setPitch((float) padConfig.getDouble("pads." + dest + ".pitch"));
        tele.setYaw((float) padConfig.getDouble("pads." + dest + ".yaw"));
        
        if (tele.getBlock().getType() != Material.AIR) {
            tele.add(0, 2, 0);
        }
        
        player.teleport(tele);
        
        if (teleport_msg_enable) {
            player.sendMessage(ChatColor.GREEN + teleport_msg.replace("%pad%", ChatColor.WHITE + dest + ChatColor.GREEN));
        }
    }
    
    public void reloadprops() {
        if (!new File(this.getDataFolder(), "config.yml").exists()) {
            System.out.println(this + ": No config found. Aborting search, generating one...");
            
            //Set the defaults
            getConfig().options().header("For type ID's, go to www.minecraftwiki.net/wiki/Data_values");
            getConfig().addDefault("pads.properties.type_id", 1);
            getConfig().addDefault("pads.properties.nodestmsg_enabled", true);
            getConfig().addDefault("pads.properties.teleport_time", 3);
            
            //Messages
            getConfig().addDefault("messages.delete", "Teleport pad %pad% removed.");
            getConfig().addDefault("messages.link", "Successfully linked %pad% to %pad2%");
            getConfig().addDefault("messages.wait", "Teleporting in %time%, stay on the pad!");
            getConfig().addDefault("messages.unlink", "Set destination of %pad% to nowhere.");
            getConfig().addDefault("messages.teleport.enable", true);
            getConfig().addDefault("messages.teleport.message", "Successfully teleported to %pad%");
            getConfig().addDefault("messages.teleport.denied", "You are not allowed to use this pad!");
            
            getConfig().options().copyDefaults(true);
            saveConfig();
        }
        
        telepad_item_id = getConfig().getInt("pads.properties.type_id", 1);
        telepad_teleport_time = getConfig().getInt("pads.properties.teleport_time", 3);
        
        nodestmsg_enable = getConfig().getBoolean("pads.properties.nodestmsg_enabled", true);
        teleport_msg = getConfig().getString("messages.teleport.message");
        teleport_msg_denied = getConfig().getString("messages.teleport.denied");
        teleport_msg_enable = getConfig().getBoolean("messages.teleport.enable");
        wait_msg = getConfig().getString("messages.wait");
        
        if (!new File(getDataFolder(), "pads.yml").exists()) {
            System.out.println(this + ": No pads found! Generating header...");
            
            File configFile = new File(this.getDataFolder(), "pads.yml");
            try {
                configFile.createNewFile();
            } catch (IOException ex) {
                System.out.println(this + ": Error saving config file!\n" + ex);
            }
            
            padConfig.options().header("Teleportation pads are stored in this config file. I do not recommend manual editing.");
            padConfig.options().copyHeader(true);
            return;
        }
        
        //This function imports the properties to a hashmap

        Location locationTemp = null;
        // TODO: Make this getList... a proper while statement rather than something that doesn't work.
        if (!padConfig.contains("pads")) {
            System.out.println(this + ": No pads were loaded!");
            return;
        }
        List<String> names = new ArrayList<String>(padConfig.getConfigurationSection("pads").getKeys(false));
        telepads.clear();
        teleLink.clear();
        
        PluginManager pm = getServer().getPluginManager();
        for (String nextit : names) {
            if (!padConfig.contains("pads." + nextit)) {
                System.out.println(this + ": Finished re-scanning pads");
                return;
            }
            locationTemp = new Location(null, padConfig.getInt("pads." + nextit + ".X"), 
                    padConfig.getInt("pads." + nextit + ".Y"),
                    padConfig.getInt("pads." + nextit + ".Z"));
            String dest = padConfig.getString("pads." + nextit + ".dest");
            teleLink.put(nextit, (dest == null ? "nowhere" : dest));
            telepads.put(locationTemp, nextit);
            
            Permission perm = new Permission("telepads.pads." + nextit, PermissionDefault.OP);
            if(pm.getPermission(perm.getName()) == null) {
                pm.addPermission(perm);
            }
        }
    }
    
    public String getPad(Location loc) {
        return telepads.get(loc);
    }
}
