
import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BasicGameFactory implements EntityFactory {

    @Spawns("playerBullet")
    public Entity newBullet(SpawnData data) {
        return entityBuilder()
                .type(BasicGameApp.EntityType.BULLET)
                .at((Point2D) data.get("position"))
                .viewWithBBox(new Rectangle(12,3, Color.BLACK))
                .with(new CollidableComponent(true))
                .with(new ProjectileComponent(data.get("direction"), 550))
                .with(new OffscreenCleanComponent())
                .build();
    }

}