package cn.lanink.huntgame.room;

import lombok.Getter;

/**
 * @author LT_Name
 */
public enum EventType {

    /**
     * 猎物切换伪装
     */
    PREY_SWITCHED_CAMOUFLAGE(false),

    /**
     * 猎物弓箭命中猎人
     */
    PREY_BOW_HIT_HUNTER(true),

    /**
     * 猎物安全嘲讽
     */
    PREY_TAUNT_SAFE(true),

    /**
     * 猎物危险嘲讽
     */
    PREY_TAUNT_DANGER(true),

    /**
     * 猎物烟花嘲讽
     */
    PREY_TAUNT_FIREWORKS(true),

    /**
     * 猎物闪电嘲讽
     */
    PREY_TAUNT_LIGHTNING(true),

    /**
     * 猎人击杀猎物
     */
    HUNTER_KILL_PREY(true),

    /**
     * 猎人弓箭命中猎物
     */
    HUNTER_BOW_HIT_PREY(false),

    /**
     * 猎人使用追踪器
     */
    HUNTER_USE_TRACKER(false),

    /**
     * 参与游戏
     */
    COMPLETE_GAME(true);

    @Getter
    private final boolean hasIntegral;

    EventType(boolean hasIntegral) {
        this.hasIntegral = hasIntegral;
    }

}
