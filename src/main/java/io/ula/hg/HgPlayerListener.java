package io.ula.hg;



import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.ula.drng.Main;
import io.ula.drng.config.ConfigFile;
import io.ula.drng.config.InlineConfigFile;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.awt.Color;
import java.util.Random;

public class HgPlayerListener implements Listener {
    private Main ownerPlugin;
    private short sr = 1;
    private Boolean nether=false,end=false;
    public HgPlayerListener(Main plugin){
        ownerPlugin = plugin;
    }
    @EventHandler
    public void onPlayerDying(PlayerDeathEvent event){
        Player player = event.getPlayer();
        InlineConfigFile COMMENTARY = (InlineConfigFile) ownerPlugin.getConfigManager().getConfig(Key.key("drng:commentary"));
        if (player.getMetadata("hunter").getFirst().asBoolean()) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            playerHead.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(player.getPlayerProfile()));
            Item item = player.getWorld().dropItemNaturally(player.getLocation(), playerHead);
            item.setGlowing(true);
            item.setCanMobPickup(false);
        }else if(player.getMetadata("speedrunner").getFirst().asBoolean()){
            player.setGameMode(GameMode.SPECTATOR);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH,Integer.MAX_VALUE,1.0f);
            ownerPlugin.getServer().sendMessage(Component.text("_Achelous: "+String.format(COMMENTARY.getKey("commentary.kill_speedrunner."+(sr+1)).getAsString(),player.getName())));
            event.setShowDeathMessages(false);
            event.setCancelled(true);
            if(sr==0){
                finishCommentary();
                if(player.getWorld().getEnvironment().equals(World.Environment.THE_END)){
                    ownerPlugin.getServer().sendMessage(Component.text("_Achelous: "+ COMMENTARY.getKey("commentary.kill_at_end").getAsString()));
                }
            }
            sr--;
        }
            LightningStrike lightningStrike = player.getWorld().strikeLightningEffect(player.getLocation());
            lightningStrike.setFlashCount(10);
    }
    @EventHandler
    public void onHgFinished(PlayerAdvancementDoneEvent event){
        Advancement advancement = event.getAdvancement();
        Player player = event.getPlayer();
        ConfigFile CONFIG = ownerPlugin.getConfigManager().getConfig(Key.key("drng:main"));
        InlineConfigFile COMMENTARY = (InlineConfigFile) ownerPlugin.getConfigManager().getConfig(Key.key("drng:commentary"));
        if(advancement.getKey().equals(Key.key("minecraft:end/kill_dragon"))){
            CONFIG.removeKey("hg_finished");
            CONFIG.addKey("hg_finished",true);
            finishCommentary();
        }
        if(advancement.getKey().equals(Key.key("minecraft:story/enter_the_nether")) && !nether) {
            ownerPlugin.getServer().sendMessage(Component.text("_Achelous: " + String.format(COMMENTARY.getKey("commentary.enter_the_nether").getAsString(), player.getName())));
            nether=true;
        }
        if(advancement.getKey().equals(Key.key("minecraft:story/enter_the_nether")) && !nether) {
            ownerPlugin.getServer().sendMessage(Component.text("_Achelous: " + COMMENTARY.getKey("commentary.enter_the_nether").getAsString()));
            end=true;
        }
    }
    private void finishCommentary(){
        InlineConfigFile COMMENTARY = (InlineConfigFile) ownerPlugin.getConfigManager().getConfig(Key.key("drng:commentary"));
        for(Player player : ownerPlugin.getServer().getOnlinePlayers())
            player.showTitle(Title.title(Component.text("GG!",TextColor.color(Color.green.getRGB()),TextDecoration.BOLD),Component.text("游戏结束！",TextColor.color(Color.CYAN.getRGB()))));
        new BukkitRunnable(){
            int cnt=1;
            @Override
            public void run() {
                if(cnt == 4){
                    ownerPlugin.getServer().sendMessage(Component.text("_Achelous: " + COMMENTARY.getKey("commentary.game_end."+Integer.toString(cnt)).getAsString())
                            .append(Component.text(COMMENTARY.getKey("hg.drng.qq").getAsString(),TextColor.color(Color.CYAN.getRGB()))
                                    .hoverEvent(HoverEvent.showText(Component.text("点击复制到剪贴板！")))
                                    .clickEvent(ClickEvent.copyToClipboard(COMMENTARY.getKey("hg.drng.qq").getAsString()))));
                    this.cancel();
                }else
                    ownerPlugin.getServer().sendMessage(Component.text("_Achelous: " + COMMENTARY.getKey("commentary.game_end."+Integer.toString(cnt)).getAsString()));
                cnt++;
            }
        }.runTaskTimer(ownerPlugin,40,30);
    }

}
