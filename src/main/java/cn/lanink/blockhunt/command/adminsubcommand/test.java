package cn.lanink.blockhunt.command.adminsubcommand;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.command.base.BaseSubCommand;
import cn.lanink.blockhunt.entity.EntityCamouflageEntity;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.entity.Entity;

/**
 * @author LT_Name
 */
public class test extends BaseSubCommand {

    public test(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer() && sender.isOp() && BlockHunt.debug;
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        EntityCamouflageEntity.create(player.chunk, Entity.getDefaultNBT(player), "Pig").spawnToAll();
        return false;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }
}
