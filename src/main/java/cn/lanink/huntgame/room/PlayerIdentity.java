package cn.lanink.huntgame.room;

/**
 * @author LT_Name
 */
public enum PlayerIdentity {

    /**
     * 无身份
     */
    NULL,

    /**
     * 猎物
     */
    PREY,

    /**
     * 猎人
     */
    HUNTER,

    /**
     * 猎物转化成的猎人
     */
    CHANGE_HUNTER

}