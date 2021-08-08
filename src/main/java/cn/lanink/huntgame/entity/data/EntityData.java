package cn.lanink.huntgame.entity.data;

import cn.lanink.huntgame.HuntGame;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LT_Name
 */
@AllArgsConstructor
@Data
public class EntityData implements Cloneable {

    private final static List<String> SUPPORT_ENTITY_NAME_LIST =
            Arrays.asList(
                    "Cow",
                    "Pig",
                    "Sheep"
            );
    private final static ConcurrentHashMap<String, EntityData> ENTITY_DATA_CACHE = new ConcurrentHashMap<>();

    private int networkID;
    private float width;
    private float height;

    public static String getRandomEntityName() {
        return SUPPORT_ENTITY_NAME_LIST.get(HuntGame.RANDOM.nextInt(SUPPORT_ENTITY_NAME_LIST.size()));
    }

    public static EntityData getEntityDataByName(String entityName) {
        if (!SUPPORT_ENTITY_NAME_LIST.contains(entityName)) {
            if (HuntGame.debug) {
                throw new RuntimeException("不支持的实体名称：" + entityName);
            }
            entityName = getRandomEntityName();
        }
        if (!ENTITY_DATA_CACHE.containsKey(entityName)) {
            Entity entity = Entity.createEntity(entityName, Server.getInstance().getDefaultLevel().getSafeSpawn());
            ENTITY_DATA_CACHE.put(entityName, new EntityData(entity.getNetworkId(), entity.getWidth(), entity.getHeight()));
            entity.close();
        }
        return ENTITY_DATA_CACHE.get(entityName).clone();
    }

    @Override
    public EntityData clone() {
        try {
            return (EntityData) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
