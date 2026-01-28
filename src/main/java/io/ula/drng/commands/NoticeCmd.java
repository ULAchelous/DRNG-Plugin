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
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;

import static io.ula.drng.Main.DRNG_NOTICES;
import static io.ula.drng.Main.LOGGER;
import static io.ula.drng.PlayerListener.getPlayerTitles;

public class NoticeCmd {
    static LiteralArgumentBuilder<CommandSourceStack> noticeCmdBuilder = Commands.literal("notices")
            .then(Commands.literal("list")
                    .executes(commandContext -> {
                        Player player = (Player)commandContext.getSource().getSender();
                        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                        book.editMeta(itemMeta ->{
                        if(itemMeta instanceof BookMeta bookMeta)
                            itemMeta = getNoticeBook(bookMeta);
                        });
                        player.openBook(book);
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
    private static BookMeta getNoticeBook(BookMeta bookMeta){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        if(DRNG_NOTICES.has("notices")) {
            for (JsonElement notice : DRNG_NOTICES.getKey("notices").getAsJsonArray()) {
                String author = notice.getAsJsonObject().get("author").getAsString();
                String content = notice.getAsJsonObject().get("content").getAsString();
                String deadline = notice.getAsJsonObject().get("deadline").getAsString();
                try {
                    if (LocalDate.now(ZoneId.of("Asia/Shanghai")).isAfter(LocalDate.ofInstant(formatter.parse(deadline).toInstant(), ZoneId.of("Asia/Shanghai"))));
                }catch(ParseException e){
                    LOGGER.error(String.format("failed to parse notice deadline: ",e.getMessage()));
                }
                bookMeta.addPages(Component.text("发布者:").append(Component.space())
                        .append(Component.text(author))
                        .append(Component.newline())
                        .append(Component.text(content))
                        .append(Component.newline())
                        .append(Component.text("发布时间：").append(Component.text(notice.getAsJsonObject().get("created_time").getAsString())))
                );
            }
        }
        return bookMeta;
    }
}
