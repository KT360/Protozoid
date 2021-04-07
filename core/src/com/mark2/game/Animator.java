package com.mark2.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


public class Animator {

    Animation<TextureRegion> animation;
    Texture sheet;
    SpriteBatch spriteBatch;
    TextureRegion currentFrame;
    TextureRegion[] animFrames;

    int FRAME_COLS;
    int FRAME_ROWS;
    //A variable for tracking elapsed time for the animation
    float stateTime;
    float duration;
    boolean isLooping;

    public Animator(Texture spriteSheet,int cols, int rows, float frameDuration, boolean looping)
    {
        this.sheet = spriteSheet;
        FRAME_COLS = cols;
        FRAME_ROWS = rows;
        this.duration = frameDuration;
        isLooping = looping;
        // Use the split utility method to create a 2D array of TextureRegions. This is
        // possible because this sprite sheet contains frames of equal size and they are
        // all aligned.
        TextureRegion[][] tmp = TextureRegion.split(sheet,
                sheet.getWidth() / FRAME_COLS,
                sheet.getHeight()/FRAME_ROWS);

        // Place the regions into a 1D array in the correct order, starting from the top
        // left, going across first. The Animation constructor requires a 1D array.\
        animFrames = new TextureRegion[FRAME_COLS*FRAME_ROWS];
        int index = 0;
        for (int i =0; i<FRAME_ROWS; i++)
        {
            for (int j =0; j<FRAME_COLS; j++)
            {
                tmp[i][j].flip(false,true);
                animFrames[index++] = tmp[i][j];
            }
        }

        animation = new Animation<TextureRegion>(duration, animFrames);
        currentFrame = new TextureRegion();
        stateTime = 0f;
    }

    //Subtract by half the width and the height because there is an offset with the body position
    public void renderAnimation(SpriteBatch spriteBatch, Entity gameObject,float width, float height)
    {
        float x,y;
        x = gameObject.getPosition().x;
        y = gameObject.getPosition().y;

        stateTime += Gdx.graphics.getDeltaTime();
        currentFrame = animation.getKeyFrame(stateTime,isLooping);
        spriteBatch.draw(currentFrame,x-width/2,y-height/2,width,height);

    }
    public void renderAnimation(SpriteBatch spriteBatch, Entity gameObject,float width, float height, float scaleX,float scaleY,float rotation)
    {
        float x,y;
        x = gameObject.getPosition().x;
        y = gameObject.getPosition().y;

        stateTime += Gdx.graphics.getDeltaTime();
        currentFrame = animation.getKeyFrame(stateTime,isLooping);
        spriteBatch.draw(currentFrame,x-width/2,y-height/2,height/2,width/2,width,height,scaleX,scaleY,rotation);

    }
    public void renderAnimation(SpriteBatch spriteBatch,float x, float y,float width, float height,float scaleX,float scaleY, float rotation)
    {

        stateTime += Gdx.graphics.getDeltaTime();
        currentFrame = animation.getKeyFrame(stateTime,isLooping);
        spriteBatch.draw(currentFrame,x-width/2,y-height/2,width/2,height/2,width,height,scaleX,scaleY,rotation);
    }
    public void renderAnimation(SpriteBatch spriteBatch,float x, float y,float width, float height)
    {

        stateTime += Gdx.graphics.getDeltaTime();
        currentFrame = animation.getKeyFrame(stateTime,isLooping);
        spriteBatch.draw(currentFrame,x-width/2,y-height/2,width,height);
    }

    public int keyFrameIndex()
    {
        return animation.getKeyFrameIndex(stateTime);
    }

    public float getStateTime()
    {
        return stateTime;
    }
    public TextureRegion getCurrentFrame()
    {
        return currentFrame;
    }

}
