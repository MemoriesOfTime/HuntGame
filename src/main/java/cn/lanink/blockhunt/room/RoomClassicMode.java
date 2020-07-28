package cn.lanink.blockhunt.room;

import cn.lanink.blockhunt.entity.EntityCamouflageBlock;
import cn.lanink.blockhunt.entity.EntityPlayerCorpse;
import cn.lanink.blockhunt.tasks.game.TimeTask;
import cn.lanink.blockhunt.tasks.game.TipsTask;
import cn.lanink.blockhunt.utils.SavePlayerInventory;
import cn.lanink.blockhunt.utils.Tips;
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
public class RoomClassicMode extends RoomBase {

    /**
     * 初始化
     *
     * @param config 配置文件
     */
    public RoomClassicMode(Config config) {
        super(config);
    }

    @Override
    public boolean useDefaultListener() {
        return true;
    }

    /**
     * 加入房间
     *
     * @param player 玩家
     */
    @Override
    public synchronized void joinRoom(Player player) {
        if (this.players.values().size() < 16) {
            if (this.status == 0) {
                this.initTask();
            }
            this.addPlaying(player);
            Tools.rePlayerState(player, true);
            SavePlayerInventory.save(player);
            player.getInventory().setItem(8, Tools.getBlockHuntItem(10, player));
            if (player.teleport(this.getWaitSpawn())) {
                if (Server.getInstance().getPluginManager().getPlugins().containsKey("Tips")) {
                    Tips.closeTipsShow(this.level.getName(), player);
                }
                player.sendMessage(this.blockHunt.getLanguage(player).joinRoom
                        .replace("%name%", this.level.getName()));
            }else {
                this.quitRoom(player);
            }
        }
    }

    /**
     * 退出房间
     *
     * @param player 玩家
     */
    @Override
    public synchronized void quitRoom(Player player) {
        if (this.isPlaying(player)) {
            this.players.remove(player);
        }
        if (Server.getInstance().getPluginManager().getPlugins().containsKey("Tips")) {
            Tips.removeTipsConfig(this.level.getName(), player);
        }
        this.players.keySet().forEach(player::showPlayer);
        this.players.keySet().forEach(p -> p.showPlayer(player));
        this.blockHunt.getScoreboard().closeScoreboard(player);
        player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
        Tools.rePlayerState(player, false);
        SavePlayerInventory.restore(player);
        this.players.keySet().forEach(p -> p.showPlayer(player));
    }

    /**
     * 房间开始游戏
     */
    @Override
    public synchronized void gameStart() {
        if (this.status == 2) return;
        this.setStatus(2);
        Tools.cleanEntity(this.getLevel(), true);
        this.assignIdentity();
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
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.blockHunt, new TimeTask(this.blockHunt, this), 20,true);
        Server.getInstance().getScheduler().scheduleRepeatingTask(
                this.blockHunt, new TipsTask(this.blockHunt, this), 18, true);
    }

    /**
     * 结束本局游戏
     * @param normal 正常关闭
     */
    @Override
    protected synchronized void endGame(boolean normal, int victory) {
        this.status = 0;
        HashSet<Player> victoryPlayers = new HashSet<>();
        HashSet<Player> defeatPlayers = new HashSet<>();
        for (Map.Entry<Player, Integer> entry : this.players.entrySet()) {
            players.keySet().forEach(player -> entry.getKey().showPlayer(player));
            switch (victory) {
                case 1:
                    if (entry.getValue() == 1) {
                        victoryPlayers.add(entry.getKey());
                    }else {
                        defeatPlayers.add(entry.getKey());
                    }
                    break;
                case 2:
                    if (entry.getValue() == 2) {
                        victoryPlayers.add(entry.getKey());
                    }else {
                        defeatPlayers.add(entry.getKey());
                    }
                    break;
            }
        }
        Server.getInstance().getScheduler().scheduleDelayedTask(this.blockHunt, new Task() {
            @Override
            public void onRun(int i) {
                if (normal) {
                    Iterator<Map.Entry<Player, Integer>> it = players.entrySet().iterator();
                    while(it.hasNext()) {
                        Map.Entry<Player, Integer> entry = it.next();
                        it.remove();
                        quitRoom(entry.getKey());
                    }
                }else {
                    getLevel().getPlayers().values().forEach(
                            player -> player.kick(blockHunt.getLanguage(player).roomSafeKick));
                }
                initTime();
                playerCamouflageBlock.clear();
                playerRespawnTime.clear();
                entityCamouflageBlocks.clear();
                Tools.cleanEntity(getLevel(), true);
                //所有玩家退出房间后再给奖励，防止物品被清
                victoryPlayers.forEach(player -> Tools.cmd(player, blockHunt.getVictoryCmd()));
                defeatPlayers.forEach(player -> Tools.cmd(player, blockHunt.getDefeatCmd()));
            }
        }, 1);
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
                Server.getInstance().getScheduler().scheduleDelayedTask(this.blockHunt, new Task() {
                    @Override
                    public void onRun(int i) {
                        for (Map.Entry<Player, Integer> entry : players.entrySet()) {
                            entry.getKey().removeAllEffects();
                            if (entry.getValue() == 2) {
                                entry.getKey().teleport(randomSpawn.get(
                                        new Random().nextInt(randomSpawn.size())));
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
                }, 1);
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
            Server.getInstance().getScheduler().scheduleDelayedTask(this.blockHunt, new Task() {
                @Override
                public void onRun(int i) {
                    for (Map.Entry<Player, Integer> entry : players.entrySet()) {
                        if (entry.getValue() != 1) continue;
                        Set<Player> p = new HashSet<>(players.keySet());
                        p.remove(entry.getKey());
                        Integer[] integers = getPlayerCamouflageBlock(entry.getKey());
                        Block block = Block.get(integers[0], integers[1], entry.getKey().floor());
                        entry.getKey().getLevel().sendBlocks(p.toArray(new Player[0]), new Vector3[] { block });
                    }
                }
            }, 1);
        }
    }

    /**
     * 分配玩家身份
     */
    @Override
    public void assignIdentity() {
        LinkedHashMap<Player, Integer> players = this.getPlayers();
        int random = new Random().nextInt(players.size()) + 1;
        int x = 0;
        for (Player player : players.keySet()) {
            player.getInventory().clearAll();
            player.getUIInventory().clearAll();
            x++;
            if (x == random) {
                this.players.put(player, 2);
                player.sendTitle(this.blockHunt.getLanguage(player).titleHuntersTitle,
                        this.blockHunt.getLanguage(player).titleHuntersSubtitle, 10, 40, 10);
                continue;
            }
            this.players.put(player, 1);
            player.sendTitle(this.blockHunt.getLanguage(player).titlePreyTitle,
                    this.blockHunt.getLanguage(player).titlePreySubtitle, 10, 40, 10);
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
        if (skin.getSkinResourcePatch().trim().equals("")) {
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

}
