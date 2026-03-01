package io.ula.drng;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
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

import static io.ula.drng.config.Configs.PLAYER_EULA;
import static io.ula.drng.config.Configs.PLAYER_TITLES;

public class ServerJoinListener implements Listener {
    private JavaPlugin plugin;
    public ServerJoinListener(JavaPlugin plg){this.plugin = plg;}

    Map<UUID, CompletableFuture<Boolean>> awaitResponse = new ConcurrentHashMap<>();
    @EventHandler
    public void onPlayerConnected(AsyncPlayerConnectionConfigureEvent event){
        PLAYER_EULA.reload();
        PlayerConfigurationConnection connection = event.getConnection();
        Audience audience = connection.getAudience();
        PlayerProfile profile = event.getConnection().getProfile();
        UUID uuid = profile.getId();
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
        PLAYER_EULA.reload();
        if(!(event.getCommonConnection() instanceof PlayerConfigurationConnection playerConfigurationConnection))
            return;
        Key key = event.getIdentifier();
        UUID uuid = playerConfigurationConnection.getProfile().getId();
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
        Player player = event.getPlayer();
        Component loginMsg = Component.text("");
        if(PLAYER_TITLES.has(player.getName()))
            loginMsg = loginMsg.append(PlayerUtils.getPlayerTitles(player));
        loginMsg = loginMsg.append(Component.text(player.getName()))
                .append(Component.text("，欢迎回来～").decorate(TextDecoration.BOLD));
        event.joinMessage(loginMsg);//欢迎消息

        initMetadata(player);

        ScoreBoardHelper.createObjective(player);
    }

    private void initMetadata(Player player) {
        player.setMetadata("onlineTime",new FixedMetadataValue(plugin,0));
        if(!player.hasMetadata("deathCount"))
            player.setMetadata("deathCount", new FixedMetadataValue(plugin,0));
        if(!player.hasMetadata("digCount"))
            player.setMetadata("digCount", new FixedMetadataValue(plugin,0));
    }
    private void setConnectionJoinResult(UUID uniqueId, boolean value) {
        CompletableFuture<Boolean> future = awaitResponse.get(uniqueId);
        if (future != null) {
            future.complete(value);
        }
    }
}
