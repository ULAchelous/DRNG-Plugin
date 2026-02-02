package io.ula.drng.commands;

import com.google.gson.JsonElement;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.datacomponent.item.WrittenBookContent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.ula.drng.PlayerListener;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import static io.ula.drng.Main.DRNG_NOTICES;
import static io.ula.drng.Main.LOGGER;
import static io.ula.drng.PlayerListener.getPlayerTitles;

public class NoticeCmd {
    static LiteralArgumentBuilder<CommandSourceStack> noticeCmdBuilder = Commands.literal("notice")
            .then(Commands.literal("list")
                    .executes(commandContext -> {
                        Player player = (Player)commandContext.getSource().getSender();
                        player.openBook(getNoticeBook());
                        return 0;
                    }
            ))
            .then(Commands.literal("write")
                    .executes(context -> {
                        Player player = (Player)context.getSource().getSender();
                        DialogLike dialog = RegistryAccess.registryAccess().getRegistry(RegistryKey.DIALOG).get(Key.key("drng:noticedialog"));
                        player.showDialog(dialog);
                        return 0;
                    })
            )
            .requires(commandSourceStack -> (commandSourceStack.getSender() instanceof Player));
    public static LiteralCommandNode<CommandSourceStack> noticeCmd = noticeCmdBuilder.build();
    public static Book getNoticeBook(){
        DRNG_NOTICES.reload();
        Book.Builder book = Book.book(Component.text("公告栏"),Component.text("Server")).toBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                .withZone(ZoneId.of("Asia/Shanghai"));
        if(DRNG_NOTICES.has("notices")) {
            for (int i=0;i<DRNG_NOTICES.getKey("notices").getAsJsonArray().size();i++) {
                JsonElement notice = DRNG_NOTICES.getKey("notices").getAsJsonArray().get(i);
                String author = notice.getAsJsonObject().get("author").getAsString();
                String content = notice.getAsJsonObject().get("content").getAsString();
                String deadline = notice.getAsJsonObject().get("deadline").getAsString();
                if (LocalDate.now(ZoneId.of("Asia/Shanghai")).isAfter(LocalDate.parse(deadline,formatter))){
                    notice.getAsJsonObject().addProperty("removed",true);
                    DRNG_NOTICES.getKey("notices").getAsJsonArray().set(i,notice);
                    DRNG_NOTICES.write();
                    continue;
                }
                book.addPage(Component.empty()
                        .append(Component.text("发布者:").color(TextColor.color(Color.YELLOW.getRGB())).decorate(TextDecoration.BOLD))
                        .append(Component.space())
                        .append(Component.text(author))
                        .append(Component.newline())
                        .append(Component.text(content))
                        .append(Component.newline())
                        .append(Component.text("发布时间：").color(TextColor.color(Color.CYAN.getRGB())))
                        .append(Component.text(notice.getAsJsonObject().get("created_time").getAsString()))
                        .append(Component.newline())
                        .append(Component.text("截止时间：").color(TextColor.color(Color.green.getRGB())))
                        .append(Component.text(notice.getAsJsonObject().get("deadline").getAsString()))
                );
            }
            for(int i=0;i<DRNG_NOTICES.getKey("notices").getAsJsonArray().size();i++){
                JsonElement notice = DRNG_NOTICES.getKey("notices").getAsJsonArray().get(i);
                if(notice.getAsJsonObject().has("removed")) {
                    DRNG_NOTICES.getKey("notices").getAsJsonArray().remove(i);
                    DRNG_NOTICES.write();
                    i=0;
                }
            }
        }
        return book.build();
    }
}
