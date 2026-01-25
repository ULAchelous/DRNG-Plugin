package io.ula;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.papermc.paper.event.player.AsyncChatEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Color;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingFormatArgumentException;

import static io.ula.drng.PLAYER_TITLES;


public class PlayerListener implements Listener {
    private final JavaPlugin plugin;



    public PlayerListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
            BukkitTask login_time_limited = Bukkit.getScheduler().runTaskLater(plugin, () ->{
                for(Player player : plugin.getServer().getOnlinePlayers())
                    if(!player.getScoreboardTags().contains("tester")) player.kick(Component.text("长时间未输入内测码").color(TextColor.color(Color.RED.asRGB())));
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
                event.getPlayer().addPotionEffect(effect);
                event.getPlayer().addScoreboardTag("invisibility_flag");
            }
            if(event.getPlayer().hasMetadata("been_controlled")){
                event.getPlayer().removeMetadata("been_controlled", plugin);
            }
    }

    @EventHandler
    public void onPlayerBreakBlk(BlockBreakEvent event){
        if(!event.getPlayer().getScoreboardTags().contains("tester"))
            event.setCancelled(true);
        if(event.getPlayer().hasMetadata("controlling_player")){
            java.util.UUID UUID = (java.util.UUID)event.getPlayer().getMetadata("controlling_player").get(0).value();
            Player player = Bukkit.getPlayer(UUID);
            player.breakBlock(event.getBlock());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSendingMessages(AsyncChatEvent event){
            PLAYER_TITLES.reload();
            Component title = Component.text("");
            try {
                for (JsonElement element : PLAYER_TITLES.getKey(event.getPlayer().getName()).getAsJsonArray()) {
                    title = title.append(Component.text(element.getAsJsonObject().get("content").getAsString())
                            .append(Component.space())
                            .color(TextColor.fromCSSHexString(element.getAsJsonObject().get("color").getAsString())));
                }
                title = title.append(Component.text(String.format("<%s> ",event.getPlayer().getName())));
                event.getPlayer().sendMessage(title.append(event.message()));
                event.setCancelled(true);
            }catch(JsonParseException e){
                event.getPlayer().sendMessage(Component.text(String.format("警告: 玩家%s的头衔出现错误",event.getPlayer().getName())).color(TextColor.color(Color.YELLOW.asRGB())));
            }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        if(event.getPlayer().hasMetadata("been_controlled")){
            event.setCancelled(true);//通过玩家的元数据检测玩家是否被控制
        }
        if(event.getPlayer().hasMetadata("controlling_player")){
            java.util.UUID UUID = (java.util.UUID)event.getPlayer().getMetadata("controlling_player").get(0).value();
            Player player = Bukkit.getPlayer(UUID);

            Map<BlockFace,Location> vectors = new HashMap<>();
            vectors.put(BlockFace.NORTH,new Location(player.getWorld(),0,0,0.6));
            vectors.put(BlockFace.SOUTH,new Location(player.getWorld(),0,0,-0.6));
            vectors.put(BlockFace.EAST,new Location(player.getWorld(),-0.6,0,0));
            vectors.put(BlockFace.WEST,new Location(player.getWorld(),0.6,0,0));
            //存储被控制者移动方向（控制者朝向的反方向）
            if(player.isOnline()) {
                event.getPlayer().sendActionBar(Component.text(String.format("你正在控制%s!", player.getName())).color(TextColor.color(Color.RED.asRGB())));
                player.sendActionBar(Component.text(String.format("你正在被 %s 控制!", event.getPlayer().getName())).color(TextColor.color(Color.RED.asRGB())));
                player.teleport(event.getPlayer().getLocation());
                player.teleport(player.getLocation().add(vectors.getOrDefault(event.getPlayer().getFacing(), new Location(player.getWorld(), 0, 0, 0))));
            }
        }
    }

    @EventHandler
    public void onTick(ServerTickStartEvent event){
        if(event.getTickNumber() % 20 == 0){
            for(Player player : plugin.getServer().getOnlinePlayers()){
                if(!player.getScoreboardTags().contains("tester")){
                    player.showTitle(Title.title(Component.text("请输入 ").color(TextColor.color(Color.RED.asRGB()))
                                    .append(Component.text("内测码").color(TextColor.color(Color.YELLOW.asRGB())))
                                    .append(Component.text(" 来加入游戏").color(TextColor.color(Color.RED.asRGB())))
                            , Component.empty()));
                }else if(player.getScoreboardTags().contains("invisibility_flag")){
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    player.removeScoreboardTag("invisibility_flag");
                }
                if(player.getScoreboardTags().contains("fly") || player.getScoreboardTags().contains("invisibility_flag")){
                    player.setAllowFlight(true);
                }else if(player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR){
                    player.setAllowFlight(false);
                }
            }
        }
    }

}
