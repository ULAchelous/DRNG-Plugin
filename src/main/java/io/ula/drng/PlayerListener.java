package io.ula.drng;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.papermc.paper.event.player.AsyncChatEvent;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static io.ula.drng.Main.LOGGER;
import static io.ula.drng.config.Configs.*;


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
                    .append(Component.text(getPlayerChatMsg(LegacyComponentSerializer.legacyAmpersand().serialize(event.message()), player)));
            player.getServer().sendMessage(message);
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        if(event.getPlayer().hasMetadata("been_controlled")){
            event.setCancelled(true);//通过玩家的元数据检测玩家是否被控制
        }
        if(event.getPlayer().hasMetadata("controlling_player")){
            java.util.UUID UUID = (java.util.UUID)event.getPlayer().getMetadata("controlling_player").getFirst().value();
            Player target = Bukkit.getPlayer(UUID);

            Map<BlockFace,Location> vectors = new HashMap<>();
            vectors.put(BlockFace.NORTH,new Location(target.getWorld(),0,0,0.6));
            vectors.put(BlockFace.SOUTH,new Location(target.getWorld(),0,0,-0.6));
            vectors.put(BlockFace.EAST,new Location(target.getWorld(),-0.6,0,0));
            vectors.put(BlockFace.WEST,new Location(target.getWorld(),0.6,0,0));
            //存储被控制者移动方向（控制者朝向的反方向）
            if(target.isOnline()) {
                player.sendActionBar(Component.text(String.format("你正在控制%s!", target.getName())).color(TextColor.color(Color.RED.getRGB())));
                target.sendActionBar(Component.text(String.format("你正在被 %s 控制!", player.getName())).color(TextColor.color(Color.RED.getRGB())));
                target.teleport(player.getLocation());
                target.teleport(target.getLocation().add(vectors.getOrDefault(player.getFacing(), new Location(target.getWorld(), 0, 0, 0))));
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
    public  void onPlayerInteractEntity(PlayerInteractEntityEvent event){
        if (event.getRightClicked() instanceof  Player && event.getHand() == EquipmentSlot.HAND)
            event.getRightClicked().addPassenger(event.getPlayer());
    }
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event){
        Entity entity = event.getEntity();
        if (event.getEntityType() == EntityType.CREEPER){
            event.blockList().clear();
            event.getEntity().getWorld().spawn(entity.getLocation(), Firework.class,firework -> {
                FireworkEffect fireworkEffect = FireworkEffect.builder()
                        .with(FireworkEffect.Type.CREEPER)
                        .withColor(org.bukkit.Color.GREEN)
                        .build();
                FireworkMeta meta = firework.getFireworkMeta();
                meta.addEffect(fireworkEffect);
                firework.setFireworkMeta(meta);
            });
        }
    }

    @EventHandler
    public void onPlayerAttack(PrePlayerAttackEntityEvent event){
        Entity target = event.getAttacked();
        Player player = event.getPlayer();
        if(player.getPassengers().contains(target)) {
            player.removePassenger(target);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTick(ServerTickStartEvent event){
        if(event.getTickNumber() % 20 == 0){
            for(Player player : plugin.getServer().getOnlinePlayers()){
                if(player.getScoreboardTags().contains("fly")){
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

    public static String getPlayerChatMsg(String message,Player player){
        CHAT_REPLACEMENTS.reload();
        JsonArray array = CHAT_REPLACEMENTS.getKey(player.getName()).getAsJsonArray();
        for (int i=0;i<array.size();i++){
            JsonElement element = array.get(i);
            if (element.getAsJsonObject().has("removed")){
                CHAT_REPLACEMENTS.getKey(player.getName()).getAsJsonArray().remove(element);
                continue;
            }
            message+="☐";
            String key = element.getAsJsonObject().get("key").getAsString();
            String replace = element.getAsJsonObject().get("replace").getAsString();
            String[] temp = message.split(key);
            message="";
            int len = temp.length;
            if(temp[len -1].equals("☐")){
                for(int idx = 0; idx < len-1; idx++) message += temp[idx] + replace;
            }else {
                for (int idx = 0; idx < len; idx++) {
                    if(idx == len-1)
                        message += temp[idx].substring(0,temp[idx].length()-1);
                    else
                        message += temp[idx]+replace;
                }
            }
        }
        CHAT_REPLACEMENTS.write();
        return  message;
    }
}
