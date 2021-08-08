package cn.lanink.huntgame.command.usersubcommand;

import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.command.base.BaseSubCommand;
import cn.lanink.huntgame.room.BaseRoom;
import cn.lanink.huntgame.room.RoomStatus;
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
        if (this.huntGame.getRooms().size() > 0) {
            Player player = (Player) sender;
            for (BaseRoom room : this.huntGame.getRooms().values()) {
                if (room.isPlaying(player)) {
                    sender.sendMessage(this.huntGame.getLanguage(sender).translateString("joinRoomOnRoom"));
                    return true;
                }
            }
            if (player.riding != null) {
                sender.sendMessage(this.huntGame.getLanguage(sender).translateString("joinRoomOnRiding"));
                return true;
            }
            if (args.length < 2) {
                LinkedList<BaseRoom> rooms = new LinkedList<>();
                for (BaseRoom room : this.huntGame.getRooms().values()) {
                    if ((room.getStatus() == RoomStatus.TASK_NEED_INITIALIZED || room.getStatus() == RoomStatus.WAIT) &&
                            room.getPlayers().size() < 16) {
                        if (room.getPlayers().size() > 0) {
                            room.joinRoom(player);
                            sender.sendMessage(this.huntGame.getLanguage(sender).translateString("joinRandomRoom"));
                            return true;
                        }
                        rooms.add(room);
                    }
                }
                if (rooms.size() > 0) {
                    BaseRoom room = rooms.get(HuntGame.RANDOM.nextInt(rooms.size()));
                    room.joinRoom(player);
                    sender.sendMessage(this.huntGame.getLanguage(sender).translateString("joinRandomRoom"));
                    return true;
                }
            }else if (this.huntGame.getRooms().containsKey(args[1])) {
                BaseRoom room = this.huntGame.getRooms().get(args[1]);
                if (room.getStatus() == RoomStatus.GAME || room.getStatus() == RoomStatus.VICTORY) {
                    sender.sendMessage(this.huntGame.getLanguage(sender).translateString("joinRoomIsPlaying"));
                }else if (room.getPlayers().values().size() >= 16) {
                    sender.sendMessage(this.huntGame.getLanguage(sender).translateString("joinRoomIsFull"));
                } else {
                    room.joinRoom(player);
                }
                return true;
            }else {
                sender.sendMessage(this.huntGame.getLanguage(sender).translateString("joinRoomIsNotFound"));
                return true;
            }
        }
        sender.sendMessage(this.huntGame.getLanguage(sender).translateString("joinRoomNotAvailable"));
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { CommandParameter.newType("roomName", CommandParamType.TEXT) };
    }

}
