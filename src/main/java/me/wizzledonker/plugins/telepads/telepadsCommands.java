package me.wizzledonker.plugins.telepads;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Win
 */
public class telepadsCommands implements CommandExecutor {
    public static Telepads plugin;
    
    public telepadsCommands(Telepads instance) {
        plugin = instance;
    }
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String alias, String[] args) {
        if (cmnd.getName().equalsIgnoreCase("createpad")) {
            //First, convert the arguments to a string for the function
            if (cs instanceof Player) {
                if (args.length == 1) {
                    Player commandSendPlayer = (Player) cs;
                    String argsStr = args[0];
                    plugin.createPad(commandSendPlayer, argsStr);
                } else {
                    cs.sendMessage(ChatColor.RED + "Correct Usage:");
                    return false;
                }
            } else {
                //else, tell the console that he may not use the command
                cs.sendMessage("Only players may use that command!");
                return false;
            }
        }
        if (cmnd.getName().equalsIgnoreCase("padlink")) {
            if (cs instanceof Player) {
                if (args.length == 2) {
                    Player player = (Player) cs;
                    plugin.linkPads(args[0], args[1], player);
                    return true;
                } else {
                    cs.sendMessage(ChatColor.RED + "Correct Usage:");
                    return false;
                }
            } else {
                cs.sendMessage("Only players may use this command!");
                return false;
            }
        }
        if (cmnd.getName().equalsIgnoreCase("delpad")) {
            if (args.length == 1) {
                plugin.deletePad(cs, args[0]);
                return true;
            } else {
                cs.sendMessage(ChatColor.RED + "Correct Usage:");
                return false;
            }
        }


        return true;
    }
    
}
