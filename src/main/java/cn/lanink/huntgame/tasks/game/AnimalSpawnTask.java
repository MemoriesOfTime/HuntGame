package cn.lanink.huntgame.tasks.game;

import cn.lanink.huntgame.HuntGame;
import cn.lanink.huntgame.entity.EntityCamouflageEntity;
import cn.lanink.huntgame.room.RoomStatus;
import cn.lanink.huntgame.room.animal.AnimalModeRoom;
import cn.lanink.huntgame.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.scheduler.PluginTask;

import java.util.LinkedList;

/**
 * @author LT_Name
 */
public class AnimalSpawnTask extends PluginTask<HuntGame> {

    private final AnimalModeRoom room;

    public AnimalSpawnTask(HuntGame owner, AnimalModeRoom room) {
        super(owner);
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (this.room.getStatus() != RoomStatus.GAME) {
            this.cancel();
            return;
        }
        for (Player player : this.room.getPlayers().keySet()) {
            if (this.room.getAnimalSpawnList().size() > 200) {
                break;
            }
            if (this.room.getPlayers(player) != 1) {
                continue;
            }
            int count = 0;
            for (Entity entity : this.room.getLevel().getEntities()) {
                if (entity instanceof EntityCamouflageEntity) {
                    if (player.distance(entity) <= 50) {
                        count++;
                    }
                }
            }
            if (count < 20) {
                LinkedList<Position> positions = new LinkedList<>();
                int needCount = Tools.rand(5, 10);
                for (int c = 0; c < needCount; c++) {
                    positions.add(player.add(Tools.rand(-40, 40), player.getFloorY(), Tools.rand(-40, 40)));
                }
                for (Position position : positions) {
                    //检查地面
                    for (int y = position.getFloorY() + 10; y > 0; y--) {
                        if (!this.getBlockFast(position).canPassThrough()) {
                            break;
                        }
                        position.setY(y);
                    }
                    if (position.getFloorY() == 0) {
                        continue;
                    }
                    position.y += 1;
                }
                this.room.getAnimalSpawnList().addAll(positions);
            }
        }
    }

    public Block getBlockFast(Position position) {
        return this.getBlockFast(position.getLevel(), position.getFloorX(), position.getFloorY(), position.getFloorZ());
    }

    /**
     * 快速获取方块
     *
     * @param x 坐标x
     * @param y 坐标y
     * @param z 坐标z
     * @return 方块
     */
    public Block getBlockFast(Level level, int x, int y, int z) {
        int fullState = 0;
        if (y >= 0 && y < 256) {
            int cx = x >> 4;
            int cz = z >> 4;
            BaseFullChunk chunk = level.getChunk(cx, cz);

            if (chunk != null) {
                fullState = chunk.getFullBlock(x & 15, y, z & 15);
            }
        }

        Block block = Block.fullList[fullState & 4095].clone();
        block.x = x;
        block.y = y;
        block.z = z;
        block.level = level;
        return block;
    }

}
