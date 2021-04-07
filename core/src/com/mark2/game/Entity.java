package com.mark2.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public interface Entity {
    void checkCollision(Entity otherEntity);
    int getType();
    Body getBody();
    Vector2 getPosition();
}
