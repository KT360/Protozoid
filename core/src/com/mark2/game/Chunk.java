package com.mark2.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

public class Chunk {

    Tile[][] tiles;
    final int MAP_WIDTH;
    final int MAP_HEIGHT;
    TextureAtlas tileAtlas;

    public Chunk(float width, float height, TextureAtlas atlas)
    {
     //Devide the available space to find how many rectangles we can use
     MAP_WIDTH = Math.round(width / Constants.BLOCK_SCALE);
     MAP_HEIGHT = Math.round(height / Constants.BLOCK_SCALE);
     tileAtlas = atlas;

     tiles = new Tile[MAP_HEIGHT][MAP_WIDTH];
     placeRectangles();
     System.out.println("TILE MAP: "+  MAP_HEIGHT +"x"+MAP_WIDTH);

    }

    public void placeRectangles()
    {
        //1 is subtracted from some of these values in order to bring the points closer together
        //to avoid seeing "splitting" of the texture regions
        for (int i =0; i<MAP_HEIGHT; i++)
        {
            for (int j=0; j<MAP_WIDTH; j++)
            {
                float scale = Constants.BLOCK_SCALE;
                TextureRegion texture;

                //////////
                if (i ==0 && j == 0)
                {
                    texture = tileAtlas.findRegion("TOP_LEFT");
                    tiles[i][j] = new Tile(texture,j*scale,i*scale,scale,scale);
                }

                /////////
                else if (i == 0 && j == MAP_WIDTH-1)
                {
                    texture = tileAtlas.findRegion("TOP_RIGHT");
                    tiles[i][j] = new Tile(texture,j*scale-1,i*scale,scale,scale);
                }

                /////////
                else if (i == MAP_HEIGHT-1 && j == 0)
                {
                    texture = tileAtlas.findRegion("BOTTOM_LEFT");
                    tiles[i][j] = new Tile(texture,j*scale,i*scale-1,scale,scale);
                }

                //////////
                else if (i == MAP_HEIGHT-1 && j == MAP_WIDTH-1)
                {
                    texture = tileAtlas.findRegion("BOTTOM_RIGHT");
                    tiles[i][j] = new Tile(texture,j*scale-1,i*scale-1,scale,scale);
                }

                ///////////
                else if (i ==0 && j < MAP_WIDTH-1)
                {
                    texture = tileAtlas.findRegion("TOP_MIDDLE");
                    tiles[i][j] = new Tile(texture,j*scale-1,i*scale,scale,scale);
                }

                //////////
                else if (i > 0 && j< MAP_HEIGHT-1 && j == 0)
                {
                    texture = tileAtlas.findRegion("MIDDLE_LEFT");
                    tiles[i][j] = new Tile(texture,j*scale,i*scale-1,scale,scale);
                }

                //////////
                else if (i == MAP_HEIGHT - 1 && j < MAP_WIDTH - 1)
                {
                    texture = tileAtlas.findRegion("BOTTOM_MIDDLE");
                    tiles[i][j] = new Tile(texture,j*scale-1,i*scale-1,scale,scale);
                }

                //////////
                else if (i > 0 && i < MAP_HEIGHT-1 && j == MAP_WIDTH-1)
                {
                    texture = tileAtlas.findRegion("MIDDLE_RIGHT");
                    tiles[i][j] = new Tile(texture,j*scale-1,i*scale,scale,scale);
                }

                /////////
                else if (i > 0 && i < MAP_HEIGHT - 1 && j < MAP_WIDTH - 1)
                {
                    texture = tileAtlas.findRegion("MIDDLE");
                    tiles[i][j] = new Tile(texture,j*scale-1,i*scale-1,scale,scale);
                }
            }

        }
    }

    public void renderTiles(SpriteBatch batch)
    {
        for (int i =0; i<MAP_HEIGHT; i++)
        {
            for (int j =0; j<MAP_WIDTH; j++)
            {
                TextureRegion texture = tiles[i][j].texture;
                float x = tiles[i][j].x;
                float y = tiles[i][j].y;
                float width = tiles[i][j].width;
                float height = tiles[i][j].height;
                batch.draw(texture,x,y,width,height);
            }
        }
    }


    //TILE CLASS
    class Tile
    {
        TextureRegion texture;
        float x;
        float y;
        float width;
        float height;

        public  Tile(TextureRegion texture, float x, float y, float width, float height)
        {
           texture.flip(false,true);
           this.texture = texture;
           this.x = x;;
           this.y = y;
           this.width = width;
           this.height = height;
           if (!this.texture.isFlipY())
           {
               texture.flip(false,true);
           }
        }
    }
}
