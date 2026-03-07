package io.ula.drng;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.ula.drng.config.ConfigFile;
import io.ula.drng.utils.PlayerUtils;
import io.ula.hg.HgUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static io.ula.drng.config.Configs.*;

public class ServerJoinListener implements Listener {
    private Main plugin;
    public ServerJoinListener(Main plg){this.plugin = plg;}

    Map<UUID, CompletableFuture<Boolean>> awaitResponse = new ConcurrentHashMap<>();
    @EventHandler
    public void onPlayerConnected(AsyncPlayerConnectionConfigureEvent event){
        PlayerConfigurationConnection connection = event.getConnection();
        Audience audience = connection.getAudience();
        PlayerProfile profile = event.getConnection().getProfile();
        UUID uuid = profile.getId();
        ConfigFile PLAYER_EULA = plugin.getConfigManager().getConfig(Key.key("drng:eula"));

        if(!PLAYER_EULA.has(uuid.toString())) {
            PLAYER_EULA.addKey(uuid.toString(), false);
        }
        if(!PLAYER_EULA.getKey(uuid.toString()).getAsBoolean()) {
            CompletableFuture<Boolean> response = new CompletableFuture<>();
            response.completeOnTimeout(false, 1, TimeUnit.MINUTES);
            awaitResponse.put(uuid, response);
            DialogLike dialogLike = RegistryAccess.registryAccess().getRegistry(RegistryKey.DIALOG).get(Key.key("drng:eula"));
            audience.showDialog(dialogLike);
            if (!response.join()) {
                audience.closeDialog();
                connection.disconnect(Component.text("未同意许可协议", TextColor.color(Color.RED.getRGB())));
            }
            awaitResponse.remove(profile.getId());
        }
    }
    @EventHandler
    void onHandleDialog(PlayerCustomClickEvent event){
        if(!(event.getCommonConnection() instanceof PlayerConfigurationConnection playerConfigurationConnection))
            return;
        Key key = event.getIdentifier();
        UUID uuid = playerConfigurationConnection.getProfile().getId();

        ConfigFile PLAYER_EULA = plugin.getConfigManager().getConfig(Key.key("drng:eula"));

        if(!PLAYER_EULA.getKey(uuid.toString()).getAsBoolean()) {
            if (key.equals(Key.key("drng:eula/disagree"))) {
                setConnectionJoinResult(uuid, false);
            } else if (key.equals(Key.key("drng:eula/agree"))) {
                setConnectionJoinResult(uuid, true);
                PLAYER_EULA.removeKey(uuid.toString());
                PLAYER_EULA.addKey(uuid.toString(), true);
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        ConfigFile CONFIG = plugin.getConfigManager().getConfig(Key.key("drng:titles"));

        Player player = event.getPlayer();

        event.joinMessage(PlayerUtils.getPlayerLoginMsg(player));//欢迎消息

        PlayerUtils.initPlayerStatus(player,plugin);

    }
    private void setConnectionJoinResult(UUID uniqueId, boolean value) {
        CompletableFuture<Boolean> future = awaitResponse.get(uniqueId);
        if (future != null) {
            future.complete(value);
        }
    }
}
