package me.wizzledonker.plugins.telepads;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 *
 * @author Win
 */
public class telepadsPlayerListener implements Listener{
    public static Telepads plugin;
    
    private Set<Player> onPad = new HashSet<Player>();
    
    public telepadsPlayerListener(Telepads instance) {
        plugin = instance;
    }
    
    @EventHandler
    public void whenPlayerMoves(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        
        final Location from = event.getFrom();
        final Location to = event.getTo();
        
        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ() && from.getBlockY() == to.getBlockY()) {
            return; // ignore if they're moving on the same block
        }
        
        if (onPad.contains(player)) return;
        if (player.hasPermission("telepads.use")) {
            Block block = to.getBlock().getRelative(BlockFace.DOWN);
            final Location loc = new Location(null, block.getX(), block.getY(), block.getZ());
            if (!checkPad(block, loc)) {
                return;
            }
            onPad.add(player);
            player.sendMessage(ChatColor.GRAY + plugin.wait_msg.replace("%time%", plugin.telepad_teleport_time + " Seconds"));
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                public void run() {
                    onPad.remove(player);
                    Block cBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
                    Location fLoc = new Location(null, cBlock.getX(), cBlock.getY(), cBlock.getZ());
                    if (!checkPad(cBlock, fLoc)) return;
                    if (plugin.telepads.get(loc) != plugin.telepads.get(fLoc)) return;
                    plugin.gotoPad(loc, player);
                }
            }, plugin.telepad_teleport_time * 20L);
        }
    }
    
    @EventHandler
    public void whenPlayerBreaksBlock(BlockBreakEvent event) {
        //Ensure inpermissable players can't break telepads
        Player player = event.getPlayer();
        if (player.hasPermission("telepads.create")) {
            return;
        }
        Block block = event.getBlock();
        final Location loc = new Location(null, block.getX(), block.getY(), block.getZ());
        if (!checkPad(block, loc)) {
            return;
        }
        player.sendMessage(ChatColor.RED + "You're not allowed to break that pad!");
        event.setCancelled(true);
    }
    
    private boolean checkPad(Block block, Location loc) {
        if (block.getTypeId() != plugin.telepad_item_id) {
            return false;
        }
        if (!plugin.telepads.containsKey(loc)) {
            return false;
        }
        return true;
    }
    
}
