package cn.lanink.huntgame.utils;

import cn.lanink.gamecore.api.Info;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;

import java.util.SplittableRandom;

/**
 * @author <a href="mailto:kniffman@googlemail.com">Michael Gertz (kniffo80)</a>
 */
@Info("https://github.com/Nukkit-coders/MobPlugin/blob/master/src/main/java/nukkitcoders/mobplugin/utils/Utils.java")
public class Utils {

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

    public static final int ACCORDING_X_OBTAIN_Y = 0;
    public static final int ACCORDING_Y_OBTAIN_X = 1;

    public static double calLinearFunction(Vector3 pos1, Vector3 pos2, double element, int type) {
        if (pos1.getFloorY() != pos2.getFloorY()) return Double.MAX_VALUE;
        if (pos1.getX() == pos2.getX()) {
            if (type == ACCORDING_Y_OBTAIN_X) return pos1.getX();
            else return Double.MAX_VALUE;
        } else if (pos1.getZ() == pos2.getZ()) {
            if (type == ACCORDING_X_OBTAIN_Y) return pos1.getZ();
            else return Double.MAX_VALUE;
        } else {
            if (type == ACCORDING_X_OBTAIN_Y) {
                return (element-pos1.getX()) * (pos1.getZ()-pos2.getZ()) / (pos1.getX()-pos2.getX()) + pos1.getZ();
            } else {
                return (element-pos1.getZ()) * (pos1.getX()-pos2.getX()) / (pos1.getZ()-pos2.getZ()) + pos1.getX();
            }
        }
    }

    public static boolean entityInsideWaterFast(Entity ent) {
        double y = ent.y + ent.getEyeHeight();
        int b = ent.level.getBlockIdAt(NukkitMath.floorDouble(ent.x), NukkitMath.floorDouble(y), NukkitMath.floorDouble(ent.z));
        return b == BlockID.WATER || b == BlockID.STILL_WATER;
    }

    public static boolean hasCollisionBlocks(Level level, AxisAlignedBB bb) {
        int minX = NukkitMath.floorDouble(bb.getMinX());
        int minY = NukkitMath.floorDouble(bb.getMinY());
        int minZ = NukkitMath.floorDouble(bb.getMinZ());
        int maxX = NukkitMath.ceilDouble(bb.getMaxX());
        int maxY = NukkitMath.ceilDouble(bb.getMaxY());
        int maxZ = NukkitMath.ceilDouble(bb.getMaxZ());

        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    Block block = level.getBlock(x, y, z, false);
                    if (block != null && block.getId() != 0 && block.collidesWithBB(bb)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
