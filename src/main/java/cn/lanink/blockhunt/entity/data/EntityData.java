package cn.lanink.blockhunt.entity.data;

import cn.lanink.blockhunt.BlockHunt;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * @author LT_Name
 */
@AllArgsConstructor
@Data
public class EntityData {

    private final static List<String> SUPPORT_ENTITY_NAME_LIST =
            Arrays.asList(
                    "Cow",
                    "Pig",
                    "Sheep"
            );

    private int networkID;
    private float width;
    private float height;

    public static String getRandomEntityName() {
        return SUPPORT_ENTITY_NAME_LIST.get(BlockHunt.RANDOM.nextInt(SUPPORT_ENTITY_NAME_LIST.size()));
    }

    public static EntityData getEntityDataByName(String entityName) {
        if (!SUPPORT_ENTITY_NAME_LIST.contains(entityName)) {
            if (BlockHunt.debug) {
                throw new RuntimeException("不支持的实体名称：" + entityName);
            }
            entityName = getRandomEntityName();
        }
        Entity entity = Entity.createEntity(entityName, Server.getInstance().getDefaultLevel().getSafeSpawn());
        return new EntityData(entity.getNetworkId(), entity.getWidth(), entity.getHeight());
    }

}
