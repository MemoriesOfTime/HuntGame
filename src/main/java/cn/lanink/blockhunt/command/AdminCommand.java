package cn.lanink.blockhunt.command;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.command.adminsubcommand.*;
import cn.lanink.blockhunt.command.base.BaseCommand;
import cn.lanink.blockhunt.ui.GuiCreate;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

/**
 * 管理命令
 *
 * @author lt_name
 */
public class AdminCommand extends BaseCommand {

    public AdminCommand(String name) {
        super(name, "BlockHunt 管理命令");
        this.setPermission("blockhunt.command.admin");
        this.addSubCommand(new SetWaitSpawn("setwaitspawn"));
        this.addSubCommand(new AddRandomSpawn("addrandomspawn"));
        this.addSubCommand(new AddBlock("addblock"));
        this.addSubCommand(new SetWaitTime("setwaittime"));
        this.addSubCommand(new SetGameTime("setgametime"));
        this.addSubCommand(new SetGameMode("setgamemode"));
        this.addSubCommand(new StartRoom("startroom"));
        this.addSubCommand(new StopRoom("stoproom"));
        this.addSubCommand(new ReloadRoom("reloadroom"));
        this.addSubCommand(new UnloadRoom("unloadroom"));
        if (BlockHunt.debug) {
            this.addSubCommand(new test("test"));
        }
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(this.blockHunt.getLanguage(sender).adminHelp.replace("%cmdName%", this.getName()));
    }

    @Override
    public void sendUI(CommandSender sender) {
        GuiCreate.sendAdminMenu((Player) sender);
    }

}
