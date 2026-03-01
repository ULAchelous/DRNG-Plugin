package io.ula.drng;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import io.papermc.paper.event.player.AsyncChatEvent;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.audience.Audience;
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

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import static io.ula.drng.config.Configs.*;


public class PlayerBehaviourListener implements Listener {
    private final JavaPlugin plugin;

    public PlayerBehaviourListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = (Player)event.getPlayer();

        player.removeMetadata("onlineTime",plugin);
        Component quitMsg = Component.text("");
        if(PLAYER_TITLES.has(player.getName()))
            quitMsg = quitMsg.append(PlayerUtils.getPlayerTitles(player));
        quitMsg = quitMsg.append(Component.text(player.getName()))
                .append(Component.text("，再见～").decorate(TextDecoration.BOLD));
        event.quitMessage(quitMsg);
        ScoreBoardHelper.removeObjective(event.getPlayer());
    }

    @EventHandler
    public void onPlayerBreakBlk(BlockBreakEvent event){
        Player player = event.getPlayer();
        if(event.getPlayer().hasMetadata("controlling_player")){
            java.util.UUID UUID = (java.util.UUID)event.getPlayer().getMetadata("controlling_player").getFirst().value();
            Player target = Bukkit.getPlayer(UUID);
            target.breakBlock(event.getBlock());
            target.setMetadata("digCount",new FixedMetadataValue(plugin,target.getMetadata("digCount").getFirst().asInt()+1));
            ScoreBoardHelper.updateScores(target,2);
            event.setCancelled(true);
        }
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
        player.setMetadata("deathCount",new FixedMetadataValue(plugin,player.getMetadata("deathCount").getFirst().asInt()+1));
        ScoreBoardHelper.updateScores(player,1);
    }


    @EventHandler
    public void onPlayerSendingMessages(AsyncChatEvent event){
            Player player = event.getPlayer();
            Component message = Component.object(ObjectContents.playerHead(player.getUniqueId()))
                    .append(Component.space())
                    .append(PlayerUtils.getPlayerTitles(player))//添加头衔
                    .append(Component.text(String.format("<%s> ",player.getName())))
                    .append(Component.text(PlayerUtils.getPlayerChatMsg(LegacyComponentSerializer.legacyAmpersand().serialize(event.message()), player)));
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
            }
        }
    }

}
