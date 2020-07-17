package cn.lanink.blockhunt.command.adminsubcommand;

import cn.lanink.blockhunt.command.base.BaseSubCommand;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.utils.Config;

import java.util.List;

/**
 * @author lt_name
 */
public class AddBlock extends BaseSubCommand {

    public AddBlock(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer() && sender.isOp();
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        Item item = player.getInventory().getItemInHand();
        String id = item.getId() + ":" + item.getDamage();
        if (item.getId() != 0 && item instanceof ItemBlock) {
            Config config = this.blockHunt.getRoomConfig(player.getLevel());
            List<String> list = config.getStringList("blocks");
            if (!list.contains(id)) {
                list.add(id);
                config.set("blocks", list);
                config.save();
            }
            sender.sendMessage(this.blockHunt.getLanguage(player).adminAddBlock.replace("%id%", id));
            return true;
        }
        sender.sendMessage(this.blockHunt.getLanguage(player).adminAddBlockFailure.replace("%id%", id));
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }

}
