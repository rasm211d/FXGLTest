package game.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.time.LocalTimer;
import game.BasicGameApp;
import game.BasicGameTypes;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.util.Duration;

public class BaronOfHellComponent extends Component {
    private LocalTimer enemyAttackInterval;
    private Entity player;
    private PhysicsComponent physics;
    private HPComponent hp;
    private AnimatedTexture texture;
    private AnimationChannel animIdle, animWalk, animAtk, animPain, animDeath;
    private boolean active = false;
    private boolean testIdle;
    private boolean moving;
    private int cycleAttacks;
    private boolean movedLeftOnce = false;
    private boolean purpleOnce = false;
    private boolean tripleOnce = false;
    private int triples = 0;
    private int frenzyCooldown = 2;
    private boolean dead = false;
    private int walkLeftLimit = 0;

    public BaronOfHellComponent() {
        Image image = FXGL.image("testBoH.png");
        Image image2 = FXGL.image("enemyBOHDeath.png");

        animIdle = new AnimationChannel(image, 9, 354, 352, Duration.seconds(1), 0, 0);
        animWalk = new AnimationChannel(image, 9, 354, 352, Duration.seconds(1), 2, 5);
        animAtk = new AnimationChannel(image, 9, 354, 352, Duration.seconds(1), 6, 8);
        animPain = new AnimationChannel(image2, 8, 354, 359, Duration.seconds(1), 0, 0);
        animDeath = new AnimationChannel(image2, 8, 354, 359, Duration.seconds(1), 1, 7);

        texture = new AnimatedTexture(animIdle);
        texture.loop();

    }

    @Override
    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(173, 176));
        entity.getViewComponent().addChild(texture);

        enemyAttackInterval = FXGL.newLocalTimer();
        enemyAttackInterval.capture();
    }

    @Override
    public void onUpdate(double tpf) {
        if (!active)
            return;

        if (dead)
            return;

        if (hp.getValue() <= hp.getMaxHP() * 0.5) {
            frenzyCooldown = -1;
        }

        if (enemyAttackInterval.elapsed(Duration.seconds((Math.random() * frenzyCooldown) + 2))) {
            if (cycleAttacks == 2) {
//                double randomSpecial = Math.random();
//                if (randomSpecial <= 0.33 && !tripleOnce) {
//                    triple();
//                    tripleOnce = true;
//                    cycleAttacks = 0;
//                }
//                else if (randomSpecial > 0.33) {
//                    attackPurple();
//                    cycleAttacks = 0;
//                }
//                else if (purpleOnce && tripleOnce) {
//                    walkLeft();
//                    cycleAttacks = 0;
//                }
                if (triples <= 2) {
                    triple();
                    cycleAttacks = 0;
                }
                else if (walkLeftLimit <= 2) {
                    walkLeft();
                    triples = 0;
                    cycleAttacks = 0;
                }
                else if (frenzyCooldown == -1) {
                    triple();
                }

            } else {
                attack();
                cycleAttacks++;
            }
        }
//        if (movedLeftOnce && tripleOnce && purpleOnce) {
//            FXGL.runOnce(() -> {
//                movedLeftOnce = false;
//                tripleOnce = false;
//                purpleOnce = false;
//            }, Duration.seconds(4));
//        }

        if (movedLeftOnce &&tripleOnce) {
            FXGL.runOnce(() -> {
                movedLeftOnce = false;
                tripleOnce = false;
            }, Duration.seconds(4));
        }
    }

    public void walkLeft() {
        if (walkLeftLimit == 2)
            return;

        walkLeftLimit++;
        movedLeftOnce = true;
        moving = true;
        physics.setVelocityX(-150);
        texture.loopAnimationChannel(animWalk);
        entity.setScaleX(1);
        enemyAttackInterval.capture();
        FXGL.runOnce(() -> {
            physics.setVelocityX(0);
            moving = false;
        }, Duration.seconds(1.5));
    }

    public void walkLeftCutscene() {
        physics.setVelocityX(-150);
        texture.loopAnimationChannel(animWalk);
        entity.setScaleX(1);
        FXGL.runOnce(() -> {
            physics.setVelocityX(0);
            texture.loopAnimationChannel(animIdle);
            FXGL.play("enemyAlert.wav");
        }, Duration.seconds(3));
    }

    public void attack() {
        if (dead)
            return;



        if (Math.random() < 0.1)
            FXGL.play("enemyAction.wav");

        enemyAttackInterval.capture();
        player = FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER);
        texture.playAnimationChannel(animAtk);
        FXGL.runOnce(() -> {
            FXGL.play("fireballFire.wav");
            Point2D enemyPosition = entity.getBoundingBoxComponent().getCenterWorld();
            Point2D enemyTarget = player.getBoundingBoxComponent().getCenterWorld().add(-50, -30).subtract(entity.getBoundingBoxComponent().getCenterWorld());
            FXGL.getGameWorld().spawn("normalBOH", new SpawnData(enemyPosition).put("direction", enemyTarget));
        }, Duration.seconds(0.5));

//        FXGL.runOnce(() -> {
//            texture.loopAnimationChannel(animIdle);
//        }, Duration.seconds(5));
    }

    public void attackPurple() {
        if (dead)
            return;

        purpleOnce = true;
        enemyAttackInterval.capture();
        player = FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER);
        texture.playAnimationChannel(animAtk);
        Point2D enemyPosition = entity.getBoundingBoxComponent().getCenterWorld();
        Point2D enemyTarget = player.getBoundingBoxComponent().getCenterWorld().add(-50, -110).subtract(entity.getBoundingBoxComponent().getCenterWorld());
        FXGL.runOnce(() -> {
            FXGL.play("fireballFire.wav");
            FXGL.getGameWorld().spawn("purpleBOH", new SpawnData(enemyPosition).put("direction", enemyTarget));
        }, Duration.seconds(0.5));
    }

    public void melee() {
        if (dead)
            return;

        FXGL.play("enemyPunch.wav");
        texture.playAnimationChannel(animAtk);
        player = FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER);
        player.getComponent(PhysicsComponent.class).setLinearVelocity(-1000, -200);
        player.getComponent(PlayerComponent.class).setKnockedBack(true);
        player.getComponent(PlayerComponent.class).onHit(3 * BasicGameApp.enemyDamageModifier, new Point2D(-1, 0));
        FXGL.runOnce(() -> {
            player.getComponent(PlayerComponent.class).setKnockedBack(false);
        }, Duration.seconds(1.5));
    }

    public void triple() {
        if (dead)
            return;

        triples++;
        tripleOnce = true;
        enemyAttackInterval.capture();
        player = FXGL.getGameWorld().getSingleton(BasicGameTypes.PLAYER);
        texture.playAnimationChannel(animAtk);
        Point2D enemyPosition = entity.getBoundingBoxComponent().getCenterWorld();
        Point2D enemyTarget = player.getBoundingBoxComponent().getCenterWorld().add(-50, -30).subtract(entity.getBoundingBoxComponent().getCenterWorld());
        FXGL.runOnce(() -> {
            FXGL.play("fireballFire.wav");
            FXGL.getGameWorld().spawn("normalBOH", new SpawnData(enemyPosition).put("direction", enemyTarget));
        }, Duration.seconds(0.5));
        FXGL.runOnce(() -> {
            FXGL.play("fireballFire.wav");
            FXGL.getGameWorld().spawn("normalBOH", new SpawnData(enemyPosition).put("direction", enemyTarget.add(0, Math.random() * -50)));
        }, Duration.seconds(0.55));
        FXGL.runOnce(() -> {
            FXGL.play("fireballFire.wav");
            FXGL.getGameWorld().spawn("normalBOH", new SpawnData(enemyPosition).put("direction", enemyTarget.add(0, Math.random() * 50)));
        }, Duration.seconds(0.60));
    }

    public void onHit(int damage) {
        if (!active)
            return;

        hp.setValue(hp.getValue() - damage);

        if (hp.getValue() <= 0 && !dead) {
            FXGL.getAudioPlayer().stopMusic(BasicGameApp.music);
            FXGL.play("enemyPain.wav");
            dead = true;
            physics.setVelocityX(0);
            texture.loopAnimationChannel(animPain);
            entity.getComponent(FlickerComponent.class).flicker();
            FXGL.runOnce(() -> {
                FXGL.play("enemyDeath.wav");
                texture.playAnimationChannel(animDeath);
                physics.getBody().setActive(false);
            }, Duration.seconds(2));
        }

        if (Math.random() <= 0.02 && !dead)
            FXGL.play("enemyPain.wav");
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
