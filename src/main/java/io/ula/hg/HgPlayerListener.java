package io.ula.hg;



import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
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

import java.awt.*;

public class HgPlayerListener implements Listener {
    private JavaPlugin ownerPlugin;
    public HgPlayerListener(JavaPlugin plugin){
        ownerPlugin = plugin;
    }
    @EventHandler
    public void onPlayerDying(PlayerDeathEvent event){
        Player player = event.getPlayer();
        if (player.getMetadata("hunter").getFirst().asBoolean()) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            playerHead.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(player.getPlayerProfile()));
            Item item = player.getWorld().dropItemNaturally(player.getLocation(), playerHead);
            item.setGlowing(true);
            item.setCanMobPickup(false);
        }else if(player.getMetadata("speedrunner").getFirst().asBoolean()){
            player.setGameMode(GameMode.SPECTATOR);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH,Integer.MAX_VALUE,1.0f);
            ownerPlugin.getServer().sendMessage(Component.text("_Achelous: 很不巧看来").append(Component.text(player.getName(), TextColor.color(Color.YELLOW.getRGB()), TextDecoration.BOLD)).append(Component.text("他已经...死了。")));
            event.setCancelled(true);
        }
            LightningStrike lightningStrike = player.getWorld().strikeLightningEffect(player.getLocation());
            lightningStrike.setFlashCount(10);
    }
    @EventHandler
    public void onHgFinished(PlayerAdvancementDoneEvent event){
        Advancement advancement = event.getAdvancement();
        Server server = ownerPlugin.getServer();
        if(advancement.getKey().equals(Key.key("minecraft:end/kill_dragon"))){

        }
    }
}
