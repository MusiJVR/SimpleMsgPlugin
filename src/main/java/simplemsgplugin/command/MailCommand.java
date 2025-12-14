package simplemsgplugin.command;

import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplemsgplugin.utils.DatabaseDriver;
import simplemsgplugin.utils.MessageUtils;

import java.util.List;
import java.util.Map;

public class MailCommand implements CommandExecutor {
    private final DatabaseDriver dbDriver;

    public MailCommand(DatabaseDriver dbDriver) {
        this.dbDriver = dbDriver;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length != 0) {
            MessageUtils.sendColoredIfPresent(sender, "messages.incorrectcommand");
            return true;
        }

        List<Map<String, Object>> rsOfflineMessage = dbDriver.selectData("sender, message", "offline_msg", "WHERE LOWER(receiver) = LOWER(?)", player.getName());
        if (!rsOfflineMessage.isEmpty()) {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.yoursunreadmsg");

            for (Map<String, Object> i : rsOfflineMessage) {
                String senderName = (String) i.get("sender");
                String messageText = (String) i.get("message");

                MessageUtils.sendMiniMessageIfPresent(player, "messages.msgofflinepattern",
                        raw -> raw
                                .replace("%sender%", senderName)
                                .replace("%receiver%", player.getName())
                                .replace("%message%", messageText),
                        component -> component
                                .hoverEvent(HoverEvent.showText(MessageUtils.safeText("messages.clickmsgsendreply")))
                                .clickEvent(ClickEvent.suggestCommand("/msg " + senderName + " "))
                );
            }

            dbDriver.deleteData("offline_msg", "LOWER(receiver) = LOWER(?)", player.getName());
        } else {
            MessageUtils.sendMiniMessageIfPresent(player, "messages.nounreadmsg");
        }

        return true;
    }
}
