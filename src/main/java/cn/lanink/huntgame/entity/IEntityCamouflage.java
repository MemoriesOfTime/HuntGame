package cn.lanink.huntgame.entity;

import cn.nukkit.Player;

/**
 * @author LT_Name
 */
public interface IEntityCamouflage {

    void setMaster(Player player);

    Player getMaster();

    boolean canSee(Player player);

    void hidePlayer(Player player);

    void showPlayer(Player player);

}
