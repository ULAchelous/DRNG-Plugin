package io.ula.drng;

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.papermc.paper.event.player.AsyncChatEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static io.ula.drng.Main.*;


public class PlayerListener implements Listener {
    private final JavaPlugin plugin;



    public PlayerListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Component loginMsg = Component.text("");
        if(PLAYER_TITLES.has(player.getName()))
            loginMsg = loginMsg.append(getPlayerTitles(player));
        loginMsg = loginMsg.append(Component.text(player.getName()))
                .append(Component.text("，欢迎回来～").decorate(TextDecoration.BOLD));
        event.joinMessage(loginMsg);//欢迎消息

        initMetadata(player);

        ScoreBoardHelper.createObjective(player);

            BukkitTask login_time_limited = Bukkit.getScheduler().runTaskLater(plugin, () ->{
                for(Player pl : plugin.getServer().getOnlinePlayers())
                    if(!pl.getScoreboardTags().contains("tester")) pl.kick(Component.text("长时间未输入内测码").color(TextColor.color(Color.RED.getRGB())));
            },600L);//超时踢出
            if(!player.getScoreboardTags().contains("tester")){
                PotionEffect effect = new PotionEffect(
                        PotionEffectType.INVISIBILITY,
                        Integer.MAX_VALUE,
                        0,
                        false,
                        false
                );
                player.clearActivePotionEffects();
                player.addPotionEffect(effect);
                player.addScoreboardTag("invisibility_flag");
            }
            if(player.hasMetadata("been_controlled")){
                player.removeMetadata("been_controlled", plugin);
            }
    }

    private void initMetadata(Player player) {
        player.setMetadata("onlineTime",new FixedMetadataValue(plugin,0));
        player.setMetadata("onlineTimeCache",new FixedMetadataValue(plugin,0));
        if(!player.hasMetadata("deathCount"))
            player.setMetadata("deathCount", new FixedMetadataValue(plugin,0));
        if(!player.hasMetadata("digCount"))
            player.setMetadata("digCount", new FixedMetadataValue(plugin,0));
        if(!player.hasMetadata("deathCountCache"))
            player.setMetadata("deathCountCache", new FixedMetadataValue(plugin,0));
        if(!player.hasMetadata("digCountCache"))
            player.setMetadata("digCountCache", new FixedMetadataValue(plugin,0));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = (Player)event.getPlayer();
        player.removeMetadata("onlineTime",plugin);
        player.removeMetadata("onlineTimeCache",plugin);
        Component quitMsg = Component.text("");
        if(PLAYER_TITLES.has(player.getName()))
            quitMsg = quitMsg.append(getPlayerTitles(player));
        quitMsg = quitMsg.append(Component.text(player.getName()))
                .append(Component.text("，再见～").decorate(TextDecoration.BOLD));
        event.quitMessage(quitMsg);
        ScoreBoardHelper.removeObjective(event.getPlayer());
    }

    @EventHandler
    public void onPlayerBreakBlk(BlockBreakEvent event){
        Player player = event.getPlayer();
        if(!event.getPlayer().getScoreboardTags().contains("tester"))
            event.setCancelled(true);
        if(event.getPlayer().hasMetadata("controlling_player")){
            java.util.UUID UUID = (java.util.UUID)event.getPlayer().getMetadata("controlling_player").getFirst().value();
            Player target = Bukkit.getPlayer(UUID);
            target.breakBlock(event.getBlock());
            target.setMetadata("digCountCache",new FixedMetadataValue(plugin,target.getMetadata("digCount").getFirst().asInt()));
            target.setMetadata("digCount",new FixedMetadataValue(plugin,target.getMetadata("digCount").getFirst().asInt()+1));
            ScoreBoardHelper.updateScores(target,2);
            event.setCancelled(true);
        }
        player.setMetadata("digCountCache",new FixedMetadataValue(plugin,player.getMetadata("digCount").getFirst().asInt()));
        player.setMetadata("digCount",new FixedMetadataValue(plugin,player.getMetadata("digCount").getFirst().asInt()+1));
        ScoreBoardHelper.updateScores(player,2);
    }

    @EventHandler
    public void onPlayerDying(PlayerDeathEvent event){
        Player player = event.getPlayer();
        if(player.getScoreboardTags().contains("undying")){
            player.playEffect(player.getLocation(), Effect.END_PORTAL_FRAME_FILL,0);
            event.setCancelled(true);
        }
        player.setMetadata("deathCountCache",new FixedMetadataValue(plugin,player.getMetadata("deathCount").getFirst().asInt()));
        player.setMetadata("deathCount",new FixedMetadataValue(plugin,player.getMetadata("deathCount").getFirst().asInt()+1));
        ScoreBoardHelper.updateScores(player,1);
    }


    @EventHandler
    public void onPlayerSendingMessages(AsyncChatEvent event){
            Player player = event.getPlayer();
            Component message = Component.object(ObjectContents.playerHead(player.getUniqueId()))
                    .append(Component.space())
                    .append(getPlayerTitles(player))//添加头衔
                    .append(Component.text(String.format("<%s> ",player.getName())))
                    .append(event.message());
            player.getWorld().sendMessage(message);
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        if(event.getPlayer().hasMetadata("been_controlled")){
            event.setCancelled(true);//通过玩家的元数据检测玩家是否被控制
        }
        if(event.getPlayer().hasMetadata("controlling_player")){
            java.util.UUID UUID = (java.util.UUID)event.getPlayer().getMetadata("controlling_player").getFirst().value();
            Player player = Bukkit.getPlayer(UUID);

            Map<BlockFace,Location> vectors = new HashMap<>();
            vectors.put(BlockFace.NORTH,new Location(player.getWorld(),0,0,0.6));
            vectors.put(BlockFace.SOUTH,new Location(player.getWorld(),0,0,-0.6));
            vectors.put(BlockFace.EAST,new Location(player.getWorld(),-0.6,0,0));
            vectors.put(BlockFace.WEST,new Location(player.getWorld(),0.6,0,0));
            //存储被控制者移动方向（控制者朝向的反方向）
            if(player.isOnline()) {
                event.getPlayer().sendActionBar(Component.text(String.format("你正在控制%s!", player.getName())).color(TextColor.color(Color.RED.getRGB())));
                player.sendActionBar(Component.text(String.format("你正在被 %s 控制!", event.getPlayer().getName())).color(TextColor.color(Color.RED.getRGB())));
                player.teleport(event.getPlayer().getLocation());
                player.teleport(player.getLocation().add(vectors.getOrDefault(event.getPlayer().getFacing(), new Location(player.getWorld(), 0, 0, 0))));
            }
        }
    }
    @EventHandler
    public void onPlayerExecute(PlayerCommandPreprocessEvent event){
        COMMANDS_TO_LOG.reload();
        Boolean flag = false;
        Player sender = event.getPlayer();
        String[] arguments = event.getMessage().split(" ");
        if(!COMMAND_EXECUTE.has(sender.getName()))
            COMMAND_EXECUTE.addKey(sender.getName(),new JsonArray());
        for(JsonElement element : COMMANDS_TO_LOG.getKey("commands").getAsJsonArray())
            if(arguments[0].equals("/"+element.getAsString()))
                flag = true;
        if(flag) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            JsonObject log = new JsonObject();
            log.addProperty("command", event.getMessage());
            log.addProperty("time", formatter.format(LocalDateTime.now()));
            COMMAND_EXECUTE.getKey(sender.getName()).getAsJsonArray().add(log);
            COMMAND_EXECUTE.write();
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
                }else if(player.getScoreboardTags().contains("invisibility_flag")){
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    player.removeScoreboardTag("invisibility_flag");
                }
                if(player.getScoreboardTags().contains("fly") || player.getScoreboardTags().contains("invisibility_flag")){
                    player.setAllowFlight(true);
                }else if(player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR){
                    player.setAllowFlight(false);
                }
            }//内测相关逻辑
        }
    }


    public static Component getPlayerTitles(Player player) {
        PLAYER_TITLES.reload();
        Component component = Component.empty();
        if(PLAYER_TITLES.has(player.getName())) {
            try {
                for (JsonElement title : PLAYER_TITLES.getKey(player.getName()).getAsJsonArray()) {
                    try {
                        component = component
                                .append(Component.text("["))
                                .append(GsonComponentSerializer.gson().deserialize(title.toString()))//title component
                                .append(Component.text("]"))
                                .append(Component.space())//spacer
                        ;
                    } catch (Exception e) {
                        player.sendMessage(Component.text("错误: 玩家头衔加载中出现问题").color(TextColor.color(Color.RED.getRGB())));
                        LOGGER.error("Error in ./config/player_titles.json");
                        LOGGER.error("Not a valid Component object!");
                        return Component.text("");
                    }
                }
            } catch (ClassCastException e) {
                player.sendMessage(Component.text("错误: 玩家头衔加载中出现问题").color(TextColor.color(Color.RED.getRGB())));
                LOGGER.error("Error in ./config/player_titles.json");
                LOGGER.error(String.format("\"%s\" : ...<(HERE)", player.getName()));
                LOGGER.error("Wrong JsonElement,need JsonArray!");
                return Component.text("");
            }
        }
        return component;
    }

}
