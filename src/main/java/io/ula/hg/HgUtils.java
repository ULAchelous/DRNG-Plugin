package io.ula.hg;

import io.ula.drng.Main;
import io.ula.drng.ScoreBoardHelper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Objective;

import java.awt.*;

public class HgUtils {
    private static Boolean clock=false;
    private static volatile int playerNum;
    public static void hgPreProcess(Main plugin, Player player){
        if(plugin.getConfigManager().getConfig(Key.key("drng:main")).getKey("hg_started").getAsBoolean())
            return;
        playerNum = plugin.getServer().getOnlinePlayers().size();
        player.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(PotionEffect.INFINITE_DURATION,255));
        if(playerNum < 3)
            player.showTitle(Title.title(Component.text(String.format("玩家 %d/6",playerNum),TextColor.color(Color.green.getRGB())),Component.text("等待更多玩家加入",TextColor.color(Color.YELLOW.getRGB()))));
        if(playerNum >= 6)
            player.showTitle(Title.title(Component.text(String.format("玩家 %d/6",playerNum),TextColor.color(Color.green.getRGB())),Component.text("游戏即将开始",TextColor.color(Color.YELLOW.getRGB()))));
        else if(playerNum >= 3)
            player.showTitle(Title.title(Component.text(String.format("玩家 %d/6",playerNum),TextColor.color(Color.green.getRGB())),Component.text("游戏将等待30秒以便更多玩家加入",TextColor.color(Color.YELLOW.getRGB()))));
        if(!clock){
            new BukkitRunnable(){
                int cnt=0;
                int cnt_cache=-1;
                int max=0;
                @Override
                public void run() {
                    clock = true;
                    if(playerNum >= 3){
                        max = 30;
                        if(playerNum >= 6) max = 6;
                        if(max != 0 && max - cnt <= (playerNum <6?10:5)){
                            for(Player p  : plugin.getServer().getOnlinePlayers()){
                                Objective objective = ScoreBoardHelper.getObjective(p);
                                if(max-cnt == 0){
                                    if(p.getMetadata("speedrunner").getFirst().asBoolean()){
                                        p.addPotionEffect(PotionEffectType.HEALTH_BOOST.createEffect(PotionEffect.INFINITE_DURATION,4));
                                        p.showTitle(Title.title(Component.text("你是 ").append(Component.text("l",TextColor.color(Color.GRAY.getRGB()),TextDecoration.OBFUSCATED)).append(Component.text("速通者",TextColor.color(Color.RED.getRGB()), TextDecoration.BOLD)).append(Component.text("l",TextColor.color(Color.GRAY.getRGB()),TextDecoration.OBFUSCATED)),Component.text("在猎人的追杀下完成速通！")));
                                    }else{
                                        p.getInventory().setItemInMainHand(new ItemStack(Material.COMPASS));
                                        p.showTitle(Title.title(Component.text("你是 ").append(Component.text("l",TextColor.color(Color.GRAY.getRGB()),TextDecoration.OBFUSCATED)).append(Component.text("猎人",TextColor.color(Color.CYAN.getRGB()), TextDecoration.BOLD)).append(Component.text("l",TextColor.color(Color.GRAY.getRGB()),TextDecoration.OBFUSCATED)),Component.text("追杀并阻止速通者！")));
                                    }
                                    p.removePotionEffect(PotionEffectType.BLINDNESS);
                                    p.getScoreboard().resetScores(String.format("倒计时: §b§l%d",cnt_cache));
                                    continue;
                                }
                                p.showTitle(Title.title(Component.text(max-cnt,TextColor.color(Color.YELLOW.getRGB())),Component.empty()));
                                if(cnt_cache == -1){
                                    p.getScoreboard().resetScores("§e等待更多玩家");
                                }else{
                                    p.getScoreboard().resetScores(String.format("倒计时: §b§l%d",cnt_cache));
                                }
                                objective.getScore(String.format("倒计时: §b§l%d",max-cnt)).setScore(4);
                            }
                            if(max - cnt == 0){
                                plugin.getConfigManager().getConfig(Key.key("drng:main")).removeKey("hg_started");
                                plugin.getConfigManager().getConfig(Key.key("drng:main")).addKey("hg_started",true);
                                Bukkit.getWorld(Key.key("minecraft:overworld")).getWorldBorder().setSize(300000);
                                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN,Integer.MAX_VALUE,1.0f);
                                this.cancel();
                            }
                            cnt_cache = max-cnt;
                        }
                        cnt++;
                    }
                }
            }.runTaskTimer(plugin,0,20);
        }
    }


}
