package cn.lanink.huntgame.utils;

import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.room.BaseRoom;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityFirework;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemFirework;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.utils.DyeColor;

import java.util.List;
import java.util.SplittableRandom;


public class Tools {

    public static final SplittableRandom random = new SplittableRandom();

    public static int rand(int min, int max) {
        if (min == max) {
            return max;
        }
        return random.nextInt(max + 1 - min) + min;
    }

    public static double rand(double min, double max) {
        if (min == max) {
            return max;
        }
        return min + Math.random() * (max-min);
    }

    public static float rand(float min, float max) {
        if (min == max) {
            return max;
        }
        return min + (float) Math.random() * (max-min);
    }

    public static boolean rand() {
        return random.nextBoolean();
    }

    public static String getStringIdentity(BaseRoom room, Player player) {
        switch (room.getPlayers(player)) {
            case 1:
                return HuntGame.getInstance().getLanguage(player).translateString("prey");
            case 2:
            case 12:
                return HuntGame.getInstance().getLanguage(player).translateString("hunters");
            default:
                return HuntGame.getInstance().getLanguage(player).translateString("death");
        }
    }

    /**
     * 执行命令
     * @param player 玩家
     * @param cmds 命令
     */
    public static void cmd(Player player, List<String> cmds) {
        if (player == null || cmds == null || cmds.size() == 0) {
            return;
        }
        for (String s : cmds) {
            String[] cmd = s.split("&");
            if ((cmd.length > 1) && (cmd[1].equals("con"))) {
                Server.getInstance().dispatchCommand(new ConsoleCommandSender(), cmd[0].replace("@p", player.getName()));
            } else {
                Server.getInstance().dispatchCommand(player, cmd[0].replace("@p", player.getName()));
            }
        }
    }

    /**
     * 根据编号获取物品
     * @param tagNumber 道具编号
     * @param player 玩家
     * @return 物品
     */
    public static Item getHuntGameItem(int tagNumber, Player player) {
        Item item = Item.get(0);
        switch (tagNumber) {
            case 1:
                item = Item.get(261, 0, 1);
                break;
            case 2:
                item = Item.get(267, 0, 1);
                break;
            case 10:
                item = Item.get(324, 0, 1);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isHuntGameItem", true)
                        .putInt("HuntGameType", 10));
                item.setCustomName(HuntGame.getInstance().getLanguage(player).translateString("itemQuitRoom"));
                item.setLore(HuntGame.getInstance().getLanguage(player).translateString("itemQuitRoomLore").split("\n"));
                break;
            case 20:
                item = Item.get(289);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isHuntGameItem", true)
                        .putInt("HuntGameType", 20));
                item.setCustomName("冷却中");
                break;
            case 21:
                item = Item.get(353);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isHuntGameItem", true)
                        .putInt("HuntGameType", 21));
                item.setCustomName("安全的嘲讽");
                break;
            case 22:
                item = Item.get(331);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isHuntGameItem", true)
                        .putInt("HuntGameType", 22));
                item.setCustomName("危险的嘲讽");
                break;
            case 23:
                item = Item.get(401);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isHuntGameItem", true)
                        .putInt("HuntGameType", 23));
                item.setCustomName("烟花嘲讽");
                break;
            case 24:
                item = Item.get(377);
                item.setNamedTag(new CompoundTag()
                        .putBoolean("isHuntGameItem", true)
                        .putInt("HuntGameType", 24));
                item.setCustomName("闪电嘲讽");
                break;
        }
        return item;
    }

    /**
     * 设置玩家是否隐身
     * @param player 玩家
     * @param invisible 是否隐身
     */
    public static void setPlayerInvisible(Player player, boolean invisible) {
        player.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_INVISIBLE, invisible);
    }

    /**
     * 重置玩家状态
     * @param player 玩家
     * @param joinRoom 是否为加入房间
     */
    public static void rePlayerState(Player player, boolean joinRoom) {
        player.setGamemode(0);
        player.setScale(1);
        player.removeAllEffects();
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setLevel(player.getFoodData().getMaxLevel());
        if (joinRoom) {
            player.setNameTag("");
            player.setNameTagVisible(false);
            player.setNameTagAlwaysVisible(false);
            player.setAllowModifyWorld(false);
        }else {
            player.setNameTag(player.getName());
            player.setNameTagVisible(true);
            player.setNameTagAlwaysVisible(true);
            setPlayerInvisible(player, false);
            player.setAllowModifyWorld(true);
        }
        player.setAdventureSettings((new AdventureSettings(player)).set(AdventureSettings.Type.ALLOW_FLIGHT, false));
    }

    /**
     * 添加声音
     * @param room 房间
     * @param sound 声音
     */
    public static void addSound(BaseRoom room, Sound sound) {
        for (Player player : room.getPlayers().keySet()) {
            addSound(player, sound);
        }
    }

    public static void addSound(Player player, Sound sound) {
        PlaySoundPacket packet = new PlaySoundPacket();
        packet.name = sound.getSound();
        packet.volume = 1.0F;
        packet.pitch = 1.0F;
        packet.x = player.getFloorX();
        packet.y = player.getFloorY();
        packet.z = player.getFloorZ();
        player.dataPacket(packet);
    }

    public static void cleanEntity(Level level) {
        cleanEntity(level, false);
    }

    /**
     * 清理实体
     * @param level 世界
     * @param cleanAll 是否清理全部
     */
    public static void cleanEntity(Level level, boolean cleanAll) {
        for (Entity entity : level.getEntities()) {
            if (!(entity instanceof Player)) {
                CompoundTag tag = entity.namedTag;
                if (tag != null && tag.getBoolean("isHuntGameEntity")) {
                    if (cleanAll) {
                        entity.close();
                    }
                }else {
                    entity.close();
                }
            }
        }
    }

    /**
     * 获取底部 Y
     * 调用前应判断非空
     * @param player 玩家
     * @return Y
     */
    public static double getFloorY(Player player) {
        if (player.getFloorY() <= 0) return 1;
        for (int y = 0; y < 15; y++) {
            Level level = player.getLevel();
            Block block = level.getBlock(player.getFloorX(), player.getFloorY() - y, player.getFloorZ());
            if (block != null && block.getId() != 0) {
                if (block.getBoundingBox() != null) {
                    return block.getBoundingBox().getMaxY() + 0.2;
                }
                return block.getMinY() + 0.2;
            }
        }
        return player.getFloorY();
    }

    /**
     * 放烟花
     * GitHub：https://github.com/PetteriM1/FireworkShow
     * @param position 位置
     */
    public static void spawnFirework(Position position) {
        ItemFirework item = new ItemFirework();
        CompoundTag tag = new CompoundTag();
        CompoundTag ex = new CompoundTag();
        ex.putByteArray("FireworkColor",new byte[]{
                (byte) DyeColor.values()[HuntGame.RANDOM.nextInt(ItemFirework.FireworkExplosion.ExplosionType.values().length)].getDyeData()
        });
        ex.putByteArray("FireworkFade",new byte[0]);
        ex.putBoolean("FireworkFlicker", HuntGame.RANDOM.nextBoolean());
        ex.putBoolean("FireworkTrail", HuntGame.RANDOM.nextBoolean());
        ex.putByte("FireworkType",ItemFirework.FireworkExplosion.ExplosionType.values()
                [HuntGame.RANDOM.nextInt(ItemFirework.FireworkExplosion.ExplosionType.values().length)].ordinal());
        tag.putCompound("Fireworks",(new CompoundTag("Fireworks"))
                .putList(new ListTag<CompoundTag>("Explosions").add(ex)).putByte("Flight",1));
        item.setNamedTag(tag);
        CompoundTag nbt = new CompoundTag();
        nbt.putList(new ListTag<DoubleTag>("Pos")
                .add(new DoubleTag("",position.x+0.5D))
                .add(new DoubleTag("",position.y+0.5D))
                .add(new DoubleTag("",position.z+0.5D))
        );
        nbt.putList(new ListTag<DoubleTag>("Motion")
                .add(new DoubleTag("",0.0D))
                .add(new DoubleTag("",0.0D))
                .add(new DoubleTag("",0.0D))
        );
        nbt.putList(new ListTag<FloatTag>("Rotation")
                .add(new FloatTag("",0.0F))
                .add(new FloatTag("",0.0F))

        );
        nbt.putCompound("FireworkItem", NBTIO.putItemHelper(item));
        EntityFirework entity = new EntityFirework(position.getLevel().getChunk((int)position.x >> 4, (int)position.z >> 4), nbt);
        entity.spawnToAll();
    }

}
