package cn.lanink.huntgame.room;

/**
 * @author LT_Name
 */
public enum EventType {

    /**
     * 猎物切换伪装
     */
    PREY_SWITCHED_CAMOUFLAGE,

    /**
     * 猎物弓箭命中猎人
     */
    PREY_BOW_HIT_HUNTER,

    /**
     * 猎物安全嘲讽
     */
    PREY_TAUNT_SAFE,

    /**
     * 猎物危险嘲讽
     */
    PREY_TAUNT_DANGER,

    /**
     * 猎物烟花嘲讽
     */
    PREY_TAUNT_FIREWORKS,

    /**
     * 猎物闪电嘲讽
     */
    PREY_TAUNT_LIGHTNING,

    /**
     * 猎人击杀猎物
     */
    HUNTER_KILL_PREY,

    /**
     * 猎人弓箭命中猎物
     */
    HUNTER_BOW_HIT_PREY,

    /**
     * 猎人使用追踪器
     */
    HUNTER_USE_TRACKER,

}
