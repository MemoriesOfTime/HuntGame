package cn.lanink.blockhunt.entity.data;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author LT_Name
 */
@AllArgsConstructor
@Data
public class EntityData {

    private int networkID;
    private float width;
    private float height;

    public static EntityData getEntityDataByName(String entityName) {
        Entity entity = Entity.createEntity(entityName, Server.getInstance().getDefaultLevel().getSafeSpawn());
        return new EntityData(entity.getNetworkId(), entity.getWidth(), entity.getHeight());
    }

}
