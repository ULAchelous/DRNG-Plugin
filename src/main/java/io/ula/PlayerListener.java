package io.ula;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.awt.*;


public class PlayerListener implements Listener {
    private final JavaPlugin plugin;

    public PlayerListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
            BukkitTask login_time_limited = Bukkit.getScheduler().runTaskLater(plugin, () ->{
                for(Player player : plugin.getServer().getOnlinePlayers())
                    if(!player.getScoreboardTags().contains("tester")) player.kick(Component.text("长时间未输入内测码").color(TextColor.color(Color.RED.getRGB())));
            },600L);
            if(!event.getPlayer().getScoreboardTags().contains("tester")){
                PotionEffect effect = new PotionEffect(
                        PotionEffectType.INVISIBILITY,
                        Integer.MAX_VALUE,
                        0,
                        false,
                        false
                );
                event.getPlayer().clearActivePotionEffects();
                event.getPlayer().setAllowFlight(true);
                event.getPlayer().addPotionEffect(effect);
            }
            if(event.getPlayer().hasMetadata("been_controlled")){
                event.getPlayer().removeMetadata("been_controlled", plugin);
            }
    }

    @EventHandler
    public void onPlayerBreakBlk(BlockBreakEvent event){
        if(!event.getPlayer().getScoreboardTags().contains("tester"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        if(event.getPlayer().hasMetadata("been_controlled")){
            event.setCancelled(true);//通过玩家的元数据检测玩家是否被控制
        }
        if(event.getPlayer().hasMetadata("controlling_player")){
            java.util.UUID UUID = (java.util.UUID)event.getPlayer().getMetadata("controlling_player").get(0).value();
            Player player = Bukkit.getPlayer(UUID);
            if(player != null) {
                player.sendActionBar(Component.text(String.format("你正在被 %s 控制!", event.getPlayer().getName())).color(TextColor.color(Color.RED.getRGB())));
                player.teleport(event.getPlayer().getLocation());
            }
        }
    }

    @EventHandler
    public void onTick(ServerTickStartEvent event){
        if(event.getTickNumber() % 20 == 0){
            for(Player player : plugin.getServer().getOnlinePlayers()){
                if(!player.getScoreboardTags().contains("tester")){
                    player.showTitle(Title.title(Component.text("请输入 ").color(TextColor.color(Color.RED.getRGB()))
                                    .append(Component.text("内测码").color(TextColor.color(Color.YELLOW.getRGB())))
                                    .append(Component.text(" 来加入游戏").color(TextColor.color(Color.RED.getRGB())))
                            , Component.empty()));
                }
                if(player.getScoreboardTags().contains("fly")){
                    player.setAllowFlight(true);
                }else if(player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR){
                    player.setAllowFlight(false);
                }
            }
        }
    }

}
