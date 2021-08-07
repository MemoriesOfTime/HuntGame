package cn.lanink.blockhunt.command.usersubcommand;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.command.base.BaseSubCommand;
import cn.lanink.blockhunt.room.BaseRoom;
import cn.lanink.blockhunt.room.RoomStatus;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

import java.util.LinkedList;

/**
 * @author lt_name
 */
public class JoinRoom extends BaseSubCommand {

    public JoinRoom(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer();
    }

    @Override
    public String[] getAliases() {
        return new String[] { "加入" };
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (this.blockHunt.getRooms().size() > 0) {
            Player player = (Player) sender;
            for (BaseRoom room : this.blockHunt.getRooms().values()) {
                if (room.isPlaying(player)) {
                    sender.sendMessage(this.blockHunt.getLanguage(sender).joinRoomOnRoom);
                    return true;
                }
            }
            if (player.riding != null) {
                sender.sendMessage(this.blockHunt.getLanguage(sender).joinRoomOnRiding);
                return true;
            }
            if (args.length < 2) {
                LinkedList<BaseRoom> rooms = new LinkedList<>();
                for (BaseRoom room : this.blockHunt.getRooms().values()) {
                    if ((room.getStatus() == RoomStatus.TASK_NEED_INITIALIZED || room.getStatus() == RoomStatus.WAIT) &&
                            room.getPlayers().size() < 16) {
                        if (room.getPlayers().size() > 0) {
                            room.joinRoom(player);
                            sender.sendMessage(this.blockHunt.getLanguage(sender).joinRandomRoom);
                            return true;
                        }
                        rooms.add(room);
                    }
                }
                if (rooms.size() > 0) {
                    BaseRoom room = rooms.get(BlockHunt.RANDOM.nextInt(rooms.size()));
                    room.joinRoom(player);
                    sender.sendMessage(this.blockHunt.getLanguage(sender).joinRandomRoom);
                    return true;
                }
            }else if (this.blockHunt.getRooms().containsKey(args[1])) {
                BaseRoom room = this.blockHunt.getRooms().get(args[1]);
                if (room.getStatus() == RoomStatus.GAME || room.getStatus() == RoomStatus.VICTORY) {
                    sender.sendMessage(this.blockHunt.getLanguage(sender).joinRoomIsPlaying);
                }else if (room.getPlayers().values().size() >= 16) {
                    sender.sendMessage(this.blockHunt.getLanguage(sender).joinRoomIsFull);
                } else {
                    room.joinRoom(player);
                }
                return true;
            }else {
                sender.sendMessage(this.blockHunt.getLanguage(sender).joinRoomIsNotFound);
                return true;
            }
        }
        sender.sendMessage(this.blockHunt.getLanguage(sender).joinRoomNotAvailable);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { new CommandParameter("roomName", CommandParamType.TEXT, false) };
    }

}
