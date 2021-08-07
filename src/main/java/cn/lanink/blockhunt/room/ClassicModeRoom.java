package cn.lanink.blockhunt.room;

import cn.lanink.blockhunt.BlockHunt;
import cn.lanink.blockhunt.entity.EntityCamouflageBlock;
import cn.lanink.blockhunt.entity.EntityPlayerCorpse;
import cn.lanink.blockhunt.utils.Tools;
import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.potion.Effect;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;

import java.util.*;

/**
 * 经典模式房间类
 *
 * @author lt_name
 */
public class ClassicModeRoom extends BaseRoom {

    protected final ArrayList<String> camouflageBlocks;

    protected final HashMap<Player, Integer[]> playerCamouflageBlock = new HashMap<>();
    protected final HashMap<Player, EntityCamouflageBlock> entityCamouflageBlocks = new HashMap<>();

    /**
     * 初始化
     *
     * @param config 配置文件
     */
    public ClassicModeRoom(Config config) {
        super(config);
        this.camouflageBlocks = (ArrayList<String>) config.getStringList("blocks");
    }

    public List<String> getListeners() {
        List<String> list = super.getListeners();
        list.add("ClassicGameListener");
        return list;
    }

    @Override
    public void initData() {
        super.initData();
        if (this.playerCamouflageBlock != null) {
            this.playerCamouflageBlock.clear();
        }
        if (this.entityCamouflageBlocks != null) {
            this.entityCamouflageBlocks.clear();
        }
    }

    /**
     * 房间开始游戏
     */
    @Override
    public synchronized void gameStart() {
        int x = 0;
        for (Player player : this.getPlayers().keySet()) {
            if (this.getPlayers(player) == 2) continue;
            if (x >= this.getRandomSpawn().size()) {
                x = 0;
            }
            player.teleport(this.getRandomSpawn().get(x));
            x++;
            player.setScale(0.5F);
            String[] s = this.camouflageBlocks.get(new Random().nextInt(this.camouflageBlocks.size())).split(":");
            Integer[] integers = new Integer[2];
            integers[0] = Integer.parseInt(s[0]);
            integers[1] = Integer.parseInt(s[1]);
            this.playerCamouflageBlock.put(player, integers);
            player.getInventory().setItem(8, Item.get(integers[0], integers[1]));
            CompoundTag tag = Entity.getDefaultNBT(player);
            tag.putCompound("Skin", new CompoundTag()
                    .putByteArray("Data", this.blockHunt.getDefaultSkin().getSkinData().data)
                    .putString("ModelId", this.blockHunt.getDefaultSkin().getSkinId()));
            tag.putFloat("Scale", 1.0F);
            tag.putString("playerName", player.getName());
            EntityCamouflageBlock entity = new EntityCamouflageBlock(player.getChunk(), tag);
            entity.setSkin(this.blockHunt.getDefaultSkin());
            entity.spawnToAll();
            this.entityCamouflageBlocks.put(player, entity);
            this.players.keySet().forEach(p -> p.hidePlayer(player));
        }
    }

    /**
     * 计时Task
     */
    @Override
    public void asyncTimeTask() {
        int time = this.gameTime - (this.getSetGameTime() - 60);
        if (time >= 0) {
            this.players.keySet().forEach(player -> player.sendTip(this.blockHunt.getLanguage(player)
                    .huntersDispatchedTimeBottom.replace("time%", time + "")));
            if (time%10 == 0) {
                Effect e1 = Effect.getEffect(15); //失明
                e1.setDuration(400).setVisible(false);
                Effect e2 = Effect.getEffect(1); //速度提升
                e2.setDuration(400).setVisible(false);
                for (Map.Entry<Player, Integer> entry : this.players.entrySet()) {
                    if (entry.getValue() == 2) {
                        entry.getKey().addEffect(e1);
                    }else {
                        entry.getKey().addEffect(e2);
                    }
                }
            }
            if (time <= 5) {
                Tools.addSound(this, Sound.RANDOM_CLICK);
            }
            if (time == 0) {
                for (Map.Entry<Player, Integer> entry : this.players.entrySet()) {
                    entry.getKey().removeAllEffects();
                    if (entry.getValue() == 2) {
                        entry.getKey().teleport(randomSpawn.get(BlockHunt.RANDOM.nextInt(randomSpawn.size())));
                        Item[] armor = new Item[4];
                        armor[0] = Item.get(306);
                        armor[1] = Item.get(307);
                        armor[2] = Item.get(308);
                        armor[3] = Item.get(309);
                        entry.getKey().getInventory().setArmorContents(armor);
                        entry.getKey().getInventory().setItem(0, Item.get(276));
                    }
                }
            }
        }
        //计时与胜利判断
        if (this.gameTime > 0) {
            this.gameTime--;
            int x = 0;
            boolean hunters = false;
            for (Integer integer : this.players.values()) {
                switch (integer) {
                    case 1:
                        x++;
                        break;
                    case 2:
                    case 12:
                        hunters = true;
                        break;
                }
            }
            if (!hunters) {
                this.victory(1);
            }else if (x <= 0) {
                this.victory(2);
            }
        }else {
            this.victory(1);
        }
        //复活
        for (Map.Entry<Player, Integer> entry : this.playerRespawnTime.entrySet()) {
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
                entry.getKey().sendTip(this.blockHunt.getLanguage(entry.getKey())
                        .respawnTimeBottom.replace("%time%", entry.getValue() + ""));
                if (entry.getValue() == 0) {
                    this.playerRespawnEvent(entry.getKey());
                }
            }
        }
        if (this.gameTime%5 == 0) {
            for (Map.Entry<Player, Integer> entry : players.entrySet()) {
                if (entry.getValue() != 1) continue;
                Set<Player> p = new HashSet<>(players.keySet());
                p.remove(entry.getKey());
                Integer[] integers = getPlayerCamouflageBlock(entry.getKey());
                Block block = Block.get(integers[0], integers[1], entry.getKey().floor());
                entry.getKey().getLevel().sendBlocks(p.toArray(new Player[0]), new Vector3[] { block });
            }
        }

        for (Map.Entry<Player, Integer> entry : this.getPlayers().entrySet()) {
            entry.getKey().setNameTag("");
            LinkedList<String> ms = new LinkedList<>();
            for (String string : this.blockHunt.getLanguage(entry.getKey()).gameTimeScoreBoard.split("\n")) {
                ms.add(string.replace("%mode%", Tools.getStringIdentity(this, entry.getKey()))
                        .replace("%playerNumber%", this.getSurvivorPlayerNumber() + "")
                        .replace("%time%", this.gameTime + ""));
            }
            this.blockHunt.getScoreboard().showScoreboard(entry.getKey(), this.blockHunt.getLanguage(entry.getKey()).scoreBoardTitle, ms);
        }
    }

    /**
     * 获取存活人数
     *
     * @return 存活人数
     */
    @Override
    public int getSurvivorPlayerNumber() {
        int x = 0;
        for (Integer integer : this.getPlayers().values()) {
            if (integer == 1) {
                x++;
            }
        }
        return x;
    }

    /**
     * 符合游戏条件的攻击
     *
     * @param damager 攻击者
     * @param player 被攻击者
     */
    @Override
    protected void playerDamage(Player damager, Player player) {
        if (this.getPlayers(player) == 1) {
            this.playerDeathEvent(player);
            for (Player p : this.players.keySet()) {
                p.sendMessage(this.blockHunt.getLanguage(p).huntersKillPrey
                        .replace("%damagePlayer%", damager.getName())
                        .replace("%player%", player.getName()));
            }
        }
    }

    /**
     * 玩家死亡
     *
     * @param player 玩家
     */
    @Override
    protected void playerDeath(Player player) {
        if (this.getPlayers(player) == 0) return;
        this.level.sendBlocks(this.players.keySet().toArray(new Player[0]), new Vector3[] { player.floor() });
        player.getInventory().clearAll();
        player.getUIInventory().clearAll();
        player.setAdventureSettings((new AdventureSettings(player)).set(AdventureSettings.Type.ALLOW_FLIGHT, true));
        player.setGamemode(3);
        this.players.put(player, 0);
        Tools.setPlayerInvisible(player, true);
        Tools.addSound(this, Sound.GAME_PLAYER_HURT);
        this.playerCorpseSpawnEvent(player);
        this.playerRespawnTime.put(player, 20);
    }

    @Override
    protected void playerRespawn(Player player) {
        Server.getInstance().getScheduler().scheduleDelayedTask(this.blockHunt, new Task() {
            @Override
            public void onRun(int i) {
                for (Entity entity : level.getEntities()) {
                    if (entity instanceof EntityPlayerCorpse) {
                        if (entity.namedTag != null &&
                                entity.namedTag.getString("playerName").equals(player.getName())) {
                            entity.close();
                        }
                    }
                }
                players.put(player, 12);
                players.keySet().forEach(p -> p.showPlayer(player));
                player.teleport(randomSpawn.get(
                        new Random().nextInt(randomSpawn.size())));
                Tools.rePlayerState(player, true);
                Item[] armor = new Item[4];
                armor[0] = Item.get(306);
                armor[1] = Item.get(307);
                armor[2] = Item.get(308);
                armor[3] = Item.get(309);
                player.getInventory().setArmorContents(armor);
                player.getInventory().addItem(Item.get(276));
            }
        }, 1);
    }

    /**
     * 尸体生成
     *
     * @param player 玩家
     */
    @Override
    protected void playerCorpseSpawn(Player player) {
        Skin skin = player.getSkin();
        switch(skin.getSkinData().data.length) {
            case 8192:
            case 16384:
            case 32768:
            case 65536:
                break;
            default:
                skin = this.blockHunt.getDefaultSkin();
        }
        if ("".equals(skin.getSkinResourcePatch().trim())) {
            skin.setSkinResourcePatch(Skin.GEOMETRY_CUSTOM);
        }
        CompoundTag nbt = EntityPlayerCorpse.getDefaultNBT(player);
        nbt.putCompound("Skin", new CompoundTag()
                .putByteArray("Data", skin.getSkinData().data)
                .putString("ModelId", skin.getSkinId()));
        nbt.putFloat("Scale", -1.0F);
        nbt.putString("playerName", player.getName());
        EntityPlayerCorpse corpse = new EntityPlayerCorpse(player.getChunk(), nbt);
        corpse.setSkin(skin);
        corpse.setPosition(new Vector3(player.getFloorX(), Tools.getFloorY(player), player.getFloorZ()));
        corpse.setGliding(true);
        corpse.setRotation(player.getYaw(), 0);
        corpse.spawnToAll();
        corpse.updateMovement();
    }

    public HashMap<Player, Integer[]> getPlayerCamouflageBlock() {
        return this.playerCamouflageBlock;
    }

    /**
     * 获取玩家伪装的方块
     * @param player 玩家
     * @return 方块id
     */
    public Integer[] getPlayerCamouflageBlock(Player player) {
        return this.playerCamouflageBlock.get(player);
    }

    public HashMap<Player, EntityCamouflageBlock> getEntityCamouflageBlocks() {
        return this.entityCamouflageBlocks;
    }

    public EntityCamouflageBlock getEntityCamouflageBlocks(Player player) {
        return this.entityCamouflageBlocks.get(player);
    }

}
