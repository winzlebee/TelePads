package me.wizzledonker.plugins.telepads;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 *
 * @author Win
 */
public class telepadsPlayerListener extends PlayerListener{
    public static Telepads plugin;
    
    public telepadsPlayerListener(Telepads instance) {
        plugin = instance;
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("telepads.use")) {
            Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
            if (block.getTypeId() != plugin.telepad_item_id) {
                return;
            }
            Location loc = new Location(null, block.getX(), block.getY(), block.getZ());
            if (!plugin.telepads.containsKey(loc)) {
                return;
            }
            plugin.gotoPad(loc, player);
        }
    }
    
}
