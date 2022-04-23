package cn.lanink.huntgame.room.block;

import lombok.Data;

/**
 * @author LT_Name
 */
@Data
public class BlockInfo {

    private int id;
    private int damage;

    public BlockInfo(int id) {
        this(id, 0);
    }

    public BlockInfo(int id, int damage) {
        if (id == 0) {
            throw new RuntimeException("id cannot be 0");
        }
        this.id = id;
        this.damage = damage;
    }

}
