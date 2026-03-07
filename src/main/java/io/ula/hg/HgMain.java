package io.ula.hg;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.LodestoneTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.awt.*;

public class HgMain {
    private static JavaPlugin ownerPlugin;
    public static void init(JavaPlugin plugin){
        ownerPlugin = plugin;
        startTasks();
    }
    private static void startTasks(){
        BukkitScheduler scheduler = ownerPlugin.getServer().getScheduler();
        scheduler.runTaskTimer(ownerPlugin,() -> {
            for(Player player : ownerPlugin.getServer().getOnlinePlayers()) {
                if(player.getMetadata("hunter").getFirst().asBoolean()) {
                    ItemStack is = player.getInventory().getItemInMainHand();
                    if (is != null) {
                        Player target = player;
                        float md = Float.MAX_VALUE;
                        for (Player t : ownerPlugin.getServer().getOnlinePlayers()) {
                            if (t.getMetadata("speedrunner").getFirst().asBoolean()) {
                                float d = powDistance(player.getLocation(), t.getLocation());
                                if (!t.equals(player) && d < md) {
                                    md = d;
                                    target = t;
                                }
                            }
                        }
                        if (target.equals(player) || !target.getWorld().equals(player.getWorld()) || md > 2500 * 2500) {
                            if(is.getType().equals(Material.COMPASS))
                                player.sendActionBar(Component.text("距离：").append(Component.text("Unknown").decorate(TextDecoration.OBFUSCATED)));
                            player.setCompassTarget(player.getLocation());
                        } else {
                            if(is.getType().equals(Material.COMPASS))
                                player.sendActionBar(Component.text("距离：").append(Component.text(String.format("%.2f", Math.sqrt(md)), TextColor.color(Color.YELLOW.getRGB()))).append(Component.text(" 格")));
                            player.setCompassTarget(target.getLocation());
                        }
                    }
                }
            }
        },0,5);
    }
    private static float powDistance(Location x, Location y){
        return  (float)((x.x()-y.x()) * (x.x()-y.x())) + (float)((x.z() -y.z())*(x.z() -y.z()));
    }

    /*
    TODO:添加猎人特判
     */
}
