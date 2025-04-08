package lobby.find_base;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.ChatPaginator;

import java.util.logging.Logger;

public class Commands implements CommandExecutor {
    public Logger logger = find_base.getInstance().getLogger();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("set_base")) {
            logger.info(Integer.toString(args.length));
            logger.info("set_base");
            if (args.length == 1) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    find_base.addBase(args[0], player.getLocation());
                }
            }
        }

        if (command.getName().equals("get_findbase")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみが使用できます。");
                return true;
            }

            Player player = (Player) sender;
            ItemStack findBaseCompass = createFindBaseCompass();

            player.getInventory().addItem(findBaseCompass);
            player.sendMessage(ChatColor.GREEN + "Find_baseコンパスを受け取りました！");

            return true;
        }
            return false;
    }

    private ItemStack createFindBaseCompass () {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + "Find_base");

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(find_base.getInstance().find_base_key, PersistentDataType.STRING,"find_base");
        compass.setItemMeta(meta);

        return compass;
    }
}
