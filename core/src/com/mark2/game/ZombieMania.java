package com.mark2.game;

import java.awt.*;
import java.awt.geom.Line2D;
import java.security.Key;
import java.util.*;
import java.util.List;


import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import sun.awt.SunHints;

import javax.swing.*;


public class ZombieMania extends ApplicationAdapter implements InputProcessor,ContactListener{
	
	TextureAtlas textureAtlas;
    
	final HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();


	int itemSelect = 1;

	Player player;

	World world;
	BodyEditorLoader loader;
	BodyEditorLoader structureLoader;
	Box2DDebugRenderer debugRenderer;
	RayHandler rayHandler;
	PointLight light;
	PointLight light2;

	Pool<Bullet> bulletPool;
	ArrayList<Bullet> activeBullets;
	ArrayList<Entity> deadEntities;
	//Array<Entity> toBeDestroyed;
	Array<Body> walls;
	ArrayList<Structure> worldStructures;
	ArrayList<Zombie> horde;
	Map<Float,PointOfIntersection> tpIntersections;

	//MAP TEXTURES
	TextureAtlas mapTextures;
	Texture skyBox;

	//RAY CASTING stuff
	PolygonSprite poly;
	PolygonSpriteBatch polyBatch;
	PolygonRegion polyReg;
	Texture textureSolid;
	Pixmap pix;
	float[] rayAngles;
	float[] rayPolygon;
	ShapeRenderer shapeRenderer;
	EarClippingTriangulator triangulator;
	ArrayList<Line2D.Float> rays;
	Vector2[] currentBoundaries;
	Structure boundaries;

	ParticleEffect effect;
	TextureAtlas UI;
	Texture healthBar;
	Pixmap healthPix;
	Texture healthDepleted;
	Pixmap minusPix;
//	Sprite health;
//	Sprite minusHealth;
	Structure demo;
	Structure demo1;
	Structure demo2;
	Structure demo3;

	boolean gameLightOn = true;
	boolean tpEngaged = false;
	boolean allowedToTP = false;
	boolean mouseDown = false;
	boolean hasFinishedLevel = false;
	public static boolean playerIsBuffed = false;
	float worldWidth = 230f;
	float worldHeight = 230f;
	float screenWidth;
	float screenHeight;
	float tpX;
	float tpY;
	int timer = 100;
	int bulletTimer = 10;
	int zombieNumb = Constants.ZOMBIE_NUMB;

	SpriteBatch batch;
	OrthographicCamera camera;
	ExtendViewport viewport;

	Zombie zombie;
	Line2D.Float tpRay;
	Chunk testMap;

	TextureAtlas atlas_for_UI;
	TextureRegion health_bar;
	TextureRegion rifle;
	TextureRegion rifle_selected;
	TextureRegion shotgun;
	TextureRegion shotgun_selected;
	PolygonRegion healthReg;
	PolygonRegion depletedHealthReg;
	PolygonSpriteBatch UI_Batch;
	Matrix4 screenMatrix;

	Animator comboMeter;
	Texture comboFrames;

//	Sound rifleShot;
//	Sound shotgunBlast;
//	Sound bulletHit;

	@Override
	public void create () {

		skyBox = new Texture(Gdx.files.internal("Sprites/SkyBox/SkyBox.png"));

		//World
		Box2D.init();
		
		loader = new BodyEditorLoader(Gdx.files.internal("BodyColliders.json"));
		structureLoader = new BodyEditorLoader(Gdx.files.internal("Structures.json"));
		world = new World(new Vector2(0,0),true);
		world.setContactListener(this);

		activeBullets = new ArrayList<>();
		deadEntities = new ArrayList<>();
		walls = new Array<>();
		worldStructures = new ArrayList<>();
		tpIntersections = new HashMap<>();
		mapTextures = new TextureAtlas("Sprites/Grass/Grass.txt");

		//Renderers
		debugRenderer = new Box2DDebugRenderer();
		shapeRenderer = new ShapeRenderer();
		textureAtlas = new TextureAtlas("Sprites.txt");

		UI = new TextureAtlas("UI/UI.txt");

        Gdx.input.setInputProcessor(this);

        addSprites(sprites, textureAtlas);


        init_Game_Objects();
//        render_Health_Bar();
		init_Lighting();
        init_Particle_Effects();
        Vector2 playerDir = player.mam.currentDir;
		float tpX2 = player.x + (playerDir.x * 60);
		float tpY2 = player.y + (playerDir.y * 60);
        tpRay = new Line2D.Float(player.x,player.y,tpX2,tpY2);

		//Rendering and camera
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		camera.setToOrtho(true,worldHeight * (screenWidth/screenHeight), worldHeight);
		worldWidth = camera.viewportWidth;
		testMap = new Chunk(worldWidth,worldHeight,mapTextures);
		batch.setProjectionMatrix(camera.combined);
		screenMatrix = new Matrix4(batch.getProjectionMatrix().setToOrtho2D(0,0,screenWidth,screenHeight));

		//Initialize structures and screen borders
		demo = new Structure(50,50,world,structureLoader,1);
		demo1 = new Structure(160,20,world,structureLoader,2);
		demo2 = new Structure(0,120,world,structureLoader,3);
		demo3 = new Structure(150,130,world,structureLoader,4);
		boundaries = new Structure(setScreenBorders(Gdx.graphics.getWidth(),Gdx.graphics.getHeight()));
		//Since there is only on element, which represents my screen borders as an array of vectors
		currentBoundaries = setScreenBorders(Gdx.graphics.getWidth(),Gdx.graphics.getHeight()).get(0);
		worldStructures.add(demo);
		worldStructures.add(demo1);
		worldStructures.add(demo2);
		worldStructures.add(demo3);
		worldStructures.add(boundaries);

		//From those sturctures cast a ray towards each of their unique points
		rays = new ArrayList<>();
		for (Structure s : worldStructures)
		{
			for (Vector2 point : s.getUniquePoints())
			{
				rays.add(new Line2D.Float(player.x,player.y,point.x,point.y));
			}
			//Get the slightly rotated points
			for (Vector2 v : s.getOffsetPoints(player))
			{
				rays.add(new Line2D.Float(player.x,player.y,v.x,v.y));
			}
		}

		polyBatch = new PolygonSpriteBatch();
		pix = new Pixmap(1,1,Pixmap.Format.RGBA8888);
		pix.setColor(0, 0.5f, 0,0.2f);
		pix.fill();
		textureSolid = new Texture(pix);
		triangulator = new EarClippingTriangulator();

		createUI();
		//init_Sounds();

	}

	public void init_Sounds()
	{
//		rifleShot = Gdx.audio.newSound(Gdx.files.absolute("C:/Users/kamto/Downloads/Sounds/Rifle-Burst-Fire.mp3"));
//		shotgunBlast = Gdx.audio.newSound(Gdx.files.absolute("Sounds/Shotgun.mp3"));
//		bulletHit = Gdx.audio.newSound(Gdx.files.absolute("bullet-impact.mp3"));
	}

	public void createUI()
	{
		UI_Batch = new PolygonSpriteBatch();
		//Create Healthbar
		atlas_for_UI = new TextureAtlas(Gdx.files.internal("Sprites/GUI/UI.txt"));
		health_bar = atlas_for_UI.findRegion("HEALTHBAR");
		rifle = atlas_for_UI.findRegion("RIFLE");
		rifle_selected = atlas_for_UI.findRegion("RIFLE_SELECTED");
		shotgun = atlas_for_UI.findRegion("SHOTGUN");
		shotgun_selected = atlas_for_UI.findRegion("SHOTGUN_SELECTED");

		healthPix = new Pixmap(1,1,Pixmap.Format.RGBA8888);
		healthPix.setColor(0,1,0,1);
		healthPix.fill();

		healthBar = new Texture(healthPix);

		minusPix = new Pixmap(1,1,Pixmap.Format.RGBA8888);
		minusPix.setColor(1,0,0,1);
		minusPix.fill();

		healthDepleted = new Texture(minusPix);

		healthReg = new PolygonRegion(new TextureRegion(healthBar),new float[]{
				0,0,
				100,0,
				15,15,
				115,15
		},new short[]{
				0,2,3,
				3,1,0
		});

		depletedHealthReg = new PolygonRegion(new TextureRegion(healthDepleted),new float[]{
				0,0,
				100,0,
				15,15,
				115,15
		},new short[]{
				0,2,3,
				3,1,0
		});

		comboFrames = new Texture(Gdx.files.internal("Sprites/GUI/Combo_Meter.png"));
		comboMeter = new Animator(comboFrames,3,3,1/24f,true);
		for (TextureRegion t : comboMeter.animFrames)
		{
			t.flip(false,true);
		}

	}

	public void placeWalls()
	{
		//Destroy previous walls
		for (Body b : walls)
		{
			if (!world.isLocked())
			{
				world.destroyBody(b);
			}
		}
		walls.clear();

		if (worldWidth < 308) {
			//Build new ones
			createWall(world, worldWidth + 10, worldHeight / 2, 10, worldHeight / 2);
			createWall(world, worldWidth / 2, -10, worldWidth / 2, 10);
			createWall(world, -10, worldHeight / 2, 10, worldHeight / 2);
			createWall(world, worldWidth / 2, worldHeight + 10, worldWidth / 2, 10);
		}else{
			createWall(world, 306 + 10, worldHeight / 2, 10, worldHeight / 2);
			createWall(world, 306/ 2f, -10, 306/2f, 10);
			createWall(world, -10, worldHeight / 2, 10, worldHeight / 2);
			createWall(world, 306/2f, worldHeight + 10, 306/ 2f, 10);
		}

	}

	public ArrayList<Vector2[]> setScreenBorders(float screenWidth, float screenHeight)
	{
		camera.update();

		Vector2 topLeftW = new Vector2(0,0);

		Vector2 bottomLeftW =  new Vector2(0,worldHeight);

		Vector2 bottomRightW = new Vector2(worldWidth,worldHeight);

		Vector2 topRightW = new Vector2(worldWidth,0);

		ArrayList<Vector2[]> borderShape = new ArrayList<>();
		Vector2[] borderVectors = new Vector2[4];
		borderVectors[0] = topLeftW;
		borderVectors[1] = bottomLeftW;
		borderVectors[2] = bottomRightW;
		borderVectors[3] = topRightW;
		borderShape.add(borderVectors);
		return  borderShape;
	}


	public void init_Lighting()
	{
		rayHandler = new RayHandler(world);
		light = new PointLight(rayHandler,100,new Color(1,1,1,1),100,0,0);
		light.setSoftnessLength(0f);
		light.attachToBody(player.body);
	}

	public void update_Lights()
	{
		light.setPosition(player.body.getPosition().x-10,player.body.getPosition().y-10);

	}

	public void init_Particle_Effects()
	{
		effect = new ParticleEffect();
		effect.load(Gdx.files.internal("Particles/bleed.p"), Gdx.files.internal("Particles"));
		effect.flipY();
		effect.scaleEffect(0.5f);
		effect.start();
	}
	public void placeZombies()
	{
		zombieNumb+=2;
		for (int i =0; i<zombieNumb; i++) {

			horde.add(new Zombie(sprites, world, loader,player));
		}
	}

	public void init_Game_Objects()
	{
		horde = new ArrayList<>();
		placeZombies();
		player = new Player(sprites,world,loader,horde);
		bulletPool  = new Pool<Bullet>()
		{
			@Override
			protected Bullet newObject() {
				return new Bullet(sprites, world, player,player.mam.bulletVel, loader);
			}
		};
	}

//	public void render_Health_Bar()
//	{
//		minusHealth = UI.createSprite("MinusHealth");
//		minusHealth.setPosition(85,-91);
//
//		health = UI.createSprite("HealthBar");
//		health.setPosition(-75,-100);
//	}

	public void updateTpRayPos()
	{
		Vector2 playerDir = player.mam.currentDir;
		float tpX2 = player.x + (playerDir.x * 60);
		float tpY2 = player.y + (playerDir.y * 60);
		tpRay.x1 = player.x;
		tpRay.y1 = player.y;
		tpRay.x2 = tpX2;
		tpRay.y2 = tpY2;

	}
	public void updateTeleport(Map<Float,PointOfIntersection> allIntersections, ShapeRenderer shapeRenderer)
	{
		//Given an map of intersections, sort the keys of the given map in order to
		//find the furthest point of intersection within the line range
		//Update the available coordinates for "teleportation" using that
		Float[] sortedIntersections = allIntersections.keySet().toArray(new Float[0]);
		Arrays.sort(sortedIntersections);
		int counter =0;
		for (int i = sortedIntersections.length-1 ; i >= 0; i--)
		{
			PointOfIntersection tpSpot = allIntersections.get(sortedIntersections[i]);
			Vector2 lineV = new Vector2(tpRay.x2 - tpRay.x1,tpRay.y2 - tpRay.y1);
			Vector2 pointV = new Vector2(tpSpot.pointOfIntersection.x - player.x,tpSpot.pointOfIntersection.y - player.y);
			float lineLength = getLengthOf(lineV.x,lineV.y);
			float pointLength = getLengthOf(pointV.x,pointV.y);

			if (pointLength < lineLength)
			{
				tpX = tpSpot.pointOfIntersection.x;
				tpY = tpSpot.pointOfIntersection.y;
				allowedToTP = true;
                counter++;
				break;
			}
		}
		//Because we know that the end segements of our world will count as an intersection,
        //and we do not want to tp the player there, looking for length will prevent it from
        //getting counted as a length
		if (counter == sortedIntersections.length)
        {
            allowedToTP = false;
        }
		if (tpEngaged && allowedToTP) {
			shapeRenderer.circle(tpX, tpY, 10);
		}

	}

	public void teleportPlayer()
	{
	    if (allowedToTP) {
            Vector2 transform = new Vector2(tpX, tpY);
            player.setPosition(transform);
        }
	}

	public void shotgunBaby(int bulletNumb)
	{
		//Grab my two end vectors
		Vector2 v1 = player.shotGunVectors.get(0);
		Vector2 v2 = player.shotGunVectors.get(1);
		//find the length of those vectors
		float unitV1 =  (float) Math.sqrt(Math.pow(v1.x,2)+ Math.pow(v1.y,2));
		float unitV2 =  (float) Math.sqrt(Math.pow(v2.x,2)+ Math.pow(v2.y,2));
		//Make two unit vectors
		Vector2 newV1 = new Vector2(v1.x /unitV1, v1.y / unitV1);
		Vector2 newV2 = new Vector2(v2.x / unitV2, v2.y / unitV2);
		//Calculate dot product
		float DP = (newV1.x * newV2.x) + (newV1.y * newV2.y);
		//Determine the angles
		float availableAngle = (float) Math.acos(DP);

		float rotationAngle = availableAngle / (bulletNumb - 1);

			if (bulletNumb > 2)
			{
				for (int i = 0; i < bulletNumb; i++) {
					//since i know my starting vector is, I can just rotate that

					float bulletRotation = (rotationAngle * i);

					float newX = v1.x * (float) Math.cos(bulletRotation) - (v1.y * (float) Math.sin(bulletRotation));
					float newY = v1.x * (float) Math.sin(bulletRotation) + (v1.y * (float) Math.cos(bulletRotation));

					Vector2 bulletDir = new Vector2(newX, newY);

					Bullet item = bulletPool.obtain();
					item.alive = true;
					item.resetBulletVals(player.mam.spriteRelativeVector, bulletDir, player.mam.mBody.getAngle(),20);
					activeBullets.add(item);
				}

			}else {
				for (int i=0; i<2; i++) {
					Bullet item = bulletPool.obtain();
					item.alive = true;
					item.resetBulletVals(player.mam.mBody.getPosition(), player.mam.bulletVel, player.mam.mBody.getAngle(),30);
					item.setDir(player.shotGunVectors.get(i));
					activeBullets.add(item);
				}
			}
		}



	public void spawnBullets()
	{
		Bullet item = bulletPool.obtain();
		item.alive = true;
		item.resetBulletVals(player.mam.spriteRelativeVector,player.mam.bulletVel, player.mam.mBody.getAngle(),120);
		activeBullets.add(item);
//		long id = rifleShot.play();
//		rifleShot.setLooping(id,false);
	}

	public void resetGame()
	{
		placeZombies();

		for (Zombie z : horde)
		{
			deadEntities.remove(z);
			z.reset();
			z.alive = true;
			z.Health = 100;
			z.body.setActive(true);
		}

		player.reset(horde);
		player.alive = true;
		player.health = 100;
		deadEntities.remove(player);
		timer = 100;
		player.body.setActive(true);
	}
	public void resetGame_Player_Dead()
	{

		for (Zombie z : horde)
		{
			deadEntities.remove(z);
			z.reset();
			z.alive = true;
			z.Health = 100;
			z.body.setActive(true);
		}

		player.reset(horde);
		player.alive = true;
		player.health = 100;
		deadEntities.remove(player);
		timer = 100;
		player.body.setActive(true);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//Update Camera, Check input, RenderShapes and world
		camera.update();
		checkInput();

		//Draw player and zombie while updating camera pos
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		Vector3 screenCorner = camera.unproject(new Vector3(0,0,0));
		batch.draw(skyBox,screenCorner.x,screenCorner.y,worldWidth*2, worldHeight*2);
		testMap.renderTiles(batch);
		batch.end();
		renderShapesAndWorld();
		batch.begin();

		if (player.alive) {

            player.updatePlayer(batch);

            //Shoot bullets if mouse-down
			if (mouseDown && itemSelect == 1 && !tpEngaged)
			{
				bulletTimer--;
				if (bulletTimer <= 0) {
					spawnBullets();
					if (playerIsBuffed) {
						bulletTimer = 5;
					}else{
						bulletTimer = 10;
					}
				}
			}

			//"Fast" effect for when player is dashing
            if (player.isDashing) {
                updateCameraPos(1f);
            } else {
                updateCameraPos(5f);
            }

        }else{

			//Draw game over screen, then start timer for the game to restart
		    deadEntities.add(player);
			player.alive = false;
		    BitmapFont gameOverText = new BitmapFont(true);
		    gameOverText.draw(batch,"Dead X.X",worldWidth/2,worldHeight/2);
		    timer--;
		    if (timer <= 0)
			{
				resetGame_Player_Dead();
			}
        }


		//Update zombies
        for (Zombie z: horde)
        {
            if (z.alive) {
                z.updateZombie(batch, player);
//                if (z.isInjured)
//				{
//					effect.setPosition(z.x,z.y);
//					effect.update(Gdx.graphics.getDeltaTime());
//					effect.draw(batch, Gdx.graphics.getDeltaTime());
//				}
            }else {
                deadEntities.add(z);
            }
        }

        //Render bullet if alive, else, reset it and remove it from the array
		//while adding it in the list of bodies to be destroyed
        for (Bullet bullet: activeBullets)
        {
        	if (bullet.alive) {
				bullet.updateBullet(batch);
			}else
			{
				//bullet.reset();
				deadEntities.add(bullet);
				//bulletPool.free(bullet);
			}
        }
        int checkIfFinished = 0;
		for (Entity e : deadEntities)
		{
			if (e.getType() == Constants.BULLET_TYPE) {
				bulletPool.free((Bullet) e);
				activeBullets.remove(e);
			}
			if (e.getType() == Constants.ZOMBIE_TYPE)
			{
				checkIfFinished++;
			}
			e.getBody().setActive(false);
		}
		if (checkIfFinished >= horde.size())
		{
			resetGame();
		}
		deadEntities.clear();
        batch.end();

		//Render Structures
		for (Structure s : worldStructures)
		{
			if (s.equals(boundaries))
			{
				continue;
			}
			s.renderStructure(camera,batch);
		}
		renderHealthBar();
		world.step(1/120f, 6, 2);
		System.out.println(worldWidth);
		//debugRenderer.render(world, camera.combined);
	}


	public void renderHealthBar()
	{
		//For the health bar sprite
		int healthBarWidth = 300;
		int healthBarHeight = 300;
		float x = 0;
		float y = screenHeight - (health_bar.getRegionHeight()*2.5f);

		UI_Batch.setProjectionMatrix(screenMatrix);
		UI_Batch.begin();
		UI_Batch.draw(depletedHealthReg,x+20,screenHeight-52,2.3f,2.4f);
		UI_Batch.draw(healthReg,x+20,screenHeight-52,2.3f*player.health/100,2.4f*player.health/100);
		UI_Batch.draw(health_bar,x,y,healthBarWidth,healthBarHeight);

		if (Player.comboCounter > 0 && Player.comboCounter <=6) {
			int index = Player.comboCounter-1;
			UI_Batch.draw(comboMeter.animFrames[index], 0, 400, 200, 200);

		}else if(Player.comboCounter >= 7){
			playerIsBuffed = true;
			UI_Batch.draw(comboMeter.animFrames[6], 0, 400, 200, 200);
		}else{
			playerIsBuffed = false;
		}


		if (itemSelect == 1) {
			UI_Batch.draw(rifle_selected, 0, 100, 130, 130);
			UI_Batch.draw(shotgun, 0, 200,100,100);
		}else{
			UI_Batch.draw(rifle, 0, 130, 100, 100);
			UI_Batch.draw(shotgun_selected, 0, 200, 130, 130);
		}
		UI_Batch.end();
	}
	public void renderShapesAndWorld()
	{
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		updateRays();
		if (tpEngaged)
		{
			updateTpRayPos();
		}

		Vector2 cursorPosition = player.mam.mBody.getPosition();
		shapeRenderer.rect(cursorPosition.x,cursorPosition.y,4f,4f);

		//Re-initiate my array
		rayAngles = new float[rays.size()];
		rayPolygon = new float[rays.size()*2];
		//Go through each of my rays, calculate their angle and point of intersection, put the latter in a Map
		//Go through the keyset of my map (T1 values) and find which ray is the closest
		//render a ray according to that point of intersection's values
		Map<Float,PointOfIntersection> angleMap = new HashMap<>();
		for (Line2D.Float ray : rays)
		{
			float x2 = 0f;
			float y2 = 0f;

			Map<Float,PointOfIntersection> closestMap =new HashMap<Float,PointOfIntersection>();
			for(Structure structure : worldStructures) {
				for (Line2D.Float[] segments : structure.getBodySegments()) {
					for (int i = 0; i < segments.length; i++) {
						PointOfIntersection currentInt = getIntersection(ray, segments[i]);
						if (currentInt != null) {
							closestMap.put(currentInt.T1, currentInt);
						}
						//If we press ctrl and the tp Line appears, calculate intersections with segements
						//Check in index of the ray first so that this only runs once ;)
						if (tpEngaged && rays.indexOf(ray) == 0){
							PointOfIntersection tpInt = getIntersection(tpRay,segments[i]);
							if (tpInt != null) {
								tpIntersections.put(tpInt.T1, tpInt);
							}
						}
					}

				}
			}

			Float[] allPoints = closestMap.keySet().toArray(new Float[0]);
			if (allPoints.length > 0) {
				float closestPoint = allPoints[0];
				for (int i = 0; i < allPoints.length; i++) {
					if(allPoints[i]<closestPoint)
					{
						closestPoint = allPoints[i];
					}
				}
				PointOfIntersection closestIntersection = closestMap.get(closestPoint);
                x2 = closestIntersection.pointOfIntersection.x;
                y2 = closestIntersection.pointOfIntersection.y;

                Vector2 rV = new Vector2(x2-player.getPos().x,y2-player.getPos().y);
                float angle = (float) Math.atan2(rV.y,rV.x);

				int index = rays.indexOf(ray);
				rayAngles[index] = angle;
				angleMap.put(rayAngles[index],closestIntersection);
			}
			//shapeRenderer.line(ray.x1,ray.y1,x2,y2);
			for (Zombie z : horde)
			{
				shapeRenderer.polygon(z.getPolygonVectors(z.scaledShapeVertices));
			}
		}
		updateTeleport(tpIntersections,shapeRenderer);
		tpIntersections.clear();
		if(tpEngaged) {
			shapeRenderer.line(tpRay.x1, tpRay.y1, tpRay.x2, tpRay.y2);
		}

		//Sort angles, Make a polygon according to those sorted angles and their point of intersection
		//Lets me connect said polygon clockwise
		Arrays.sort(rayAngles);
		for (int i = 0, j = 0; i < rayAngles.length; i++, j += 2) {
			PointOfIntersection point = angleMap.get(rayAngles[i]);
			rayPolygon[j] = point.pointOfIntersection.x;
			rayPolygon[j + 1] = point.pointOfIntersection.y;
		}

		//Check if zombie and polygon are overlapping
		for (Zombie z: horde) {
			FloatArray zombiePolygon = new FloatArray(z.getPolygonVectors(z.scaledShapeVertices));
			FloatArray rPolygon = new FloatArray(rayPolygon);
			if(Intersector.intersectPolygons(rPolygon,zombiePolygon))
			{
				z.spotPlayer = true;
			}
			else
			{
				z.spotPlayer = false;
			}
		}

		shapeRenderer.end();
		//Initialize my Region, texture and batch to render the polygon.
		if (gameLightOn) {
			polyReg = new PolygonRegion(new TextureRegion(textureSolid), rayPolygon, triangulator.computeTriangles(rayPolygon).toArray());

			poly = new PolygonSprite(polyReg);
			poly.setOrigin(0, 0);
			polyBatch = new PolygonSpriteBatch();
			polyBatch.setProjectionMatrix(camera.combined);
			polyBatch.begin();
			poly.draw(polyBatch);
			polyBatch.end();
		}

	}

	public Vector2[] getLineVectors(Line2D.Float line)
	{
		Vector2[] points = new Vector2[2];
		points[0] = new Vector2(line.x1,line.y1);
		points[1] = new Vector2(line.x2,line.y2);

		return points;

	}

	public void createWall(World world,float x, float y, float width, float height) {

		BodyDef wallDef = new BodyDef();
		wallDef.position.set(x, y);
		Body wallBody = world.createBody(wallDef);

		PolygonShape wallShape = new PolygonShape();
		wallShape.setAsBox(width, height);
		wallBody.createFixture(wallShape, 0.0f);

		walls.add(wallBody);
		wallShape.dispose();
	}

	public void updateCameraPos(float increment)
	{
		float lerp = increment;
		Vector3 position = camera.position;
		position.x += (player.getPos().x - position.x) * lerp * Gdx.graphics.getDeltaTime();
		position.y += (player.getPos().y - position.y) * lerp * Gdx.graphics.getDeltaTime();
		camera.position.set(position);
		camera.update();
	}

	public void toggleGameLight()
	{
		if (gameLightOn)
		{
			gameLightOn = false;
		}
		else
		{
			gameLightOn = true;
		}
	}

	public void updateRays()
	{
		for (Line2D.Float ray : rays)
		{
			ray.x1 = player.getPos().x;
			ray.y1 = player.getPos().y;
		}
	}



	public PointOfIntersection getIntersection(Line2D.Float ray, Line2D.Float segment)
	{
		float r_px = ray.x1;
		float r_py = ray.y1;
		float r_dx = ray.x2 - ray.x1;
		float r_dy = ray.y2 - ray.y1;

		float s_px = segment.x1;
		float s_py = segment.y1;
		float s_dx = segment.x2 - segment.x1;
		float s_dy = segment.y2 - segment.y1;

		//If parallel, they do not intersect
		Vector2 rayDirection = new Vector2(r_dx,r_dy);
		Vector2 segmentDirection = new Vector2(s_dx,s_dy);

		float rayVectorLength = (float) Math.sqrt(Math.pow(rayDirection.x,2) + Math.pow(rayDirection.y,2));
		float segmentVectorLength = (float) Math.sqrt(Math.pow(segmentDirection.x,2) + Math.pow(segmentDirection.y,2));

		Vector2 rayUVector = new Vector2(rayDirection.x/rayVectorLength,rayDirection.y/rayVectorLength);
		Vector2 segmentUVector = new Vector2(segmentDirection.x/segmentVectorLength,segmentDirection.y/segmentVectorLength);

		if (rayUVector.x == segmentUVector.x && rayUVector.y == segmentUVector.y)
		{
			return null;

		}else {
			//Since they are intersecting, their x and y value at (t) will be the same

			float T2 = (r_dx*(s_py - r_py) + r_dy*(r_px - s_px))/(s_dx*r_dy - s_dy*r_dx);
			float T1 = (s_px + s_dx*T2-r_px)/r_dx;

			if (T1<0)
				return null;
			else if(T2<0 || T2>1)
				return null;
			else {

				Vector2 pointOfIntersection = new Vector2(r_px + r_dx * T1, r_py + r_dy * T1);

				return new PointOfIntersection(pointOfIntersection,T1);
			}
		}



	}

	
	
	public void addSprites(HashMap<String,Sprite> map, TextureAtlas atlas)
	{
		Array<AtlasRegion> regions = atlas.getRegions();
		
		for(AtlasRegion region : regions)
		{
			region.flip(false, true);

			Sprite sprite = atlas.createSprite(region.name);

            map.put(region.name, sprite);
			
		}
		
	}
	
	@Override
	public void resize(int width, int height) {

		float screenWidth = Gdx.graphics.getWidth();
		float screenHeight = Gdx.graphics.getHeight();
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		camera.viewportWidth = worldHeight * (screenWidth/screenHeight);
		worldWidth = camera.viewportWidth;
		camera.viewportHeight = worldHeight;

		//On resize remove the current border rays
		ArrayList<Line2D.Float> oldBorders = new ArrayList<>();
		for (Line2D.Float ray : rays)
		{
			Vector2 rayV = new Vector2(ray.x2,ray.y2);
			for (Vector2 initialPoint : currentBoundaries) {
				if (rayV.equals(initialPoint))
				{
					oldBorders.add(ray);
				}
			}
		}
		rays.removeAll(oldBorders);
		//Update the border segements
		boundaries.updateSegements(setScreenBorders(width,height));
		//Add new Vectors for new segments
		for (Vector2 newBound : boundaries.getUniquePoints())
		{
			rays.add(new Line2D.Float(player.getPos().x,player.getPos().y,newBound.x,newBound.y));
		}
		//update the current borders
		currentBoundaries = setScreenBorders(width,height).get(0);
		batch.setProjectionMatrix(camera.combined);
		screenMatrix = new Matrix4(batch.getProjectionMatrix().setToOrtho2D(0,0,screenWidth,screenHeight));
		System.out.println("World width: "+worldWidth);

		placeWalls();

	}
	
	@Override
	public void dispose () {
		textureAtlas.dispose();
		mapTextures.dispose();
		rayHandler.dispose();
		sprites.clear();
		debugRenderer.dispose();
		world.dispose();
		effect.dispose();
//		rifleShot.dispose();
//		shotgunBlast.dispose();
//		bulletHit.dispose();
	}

	public void checkInput()
	{
		float angle = player.mam.mBody.getAngle();
		if (!player.isDashing) {
			if (Gdx.input.isKeyPressed(Keys.W)) {
				player.body.setLinearVelocity(0, -150);
				player.mam.updateMam(angle);
				player.idle = false;
			}
			if (Gdx.input.isKeyPressed(Keys.S)) {
				player.body.setLinearVelocity(0, 150);
				player.mam.updateMam(angle);
				player.idle = false;
			}
			if (Gdx.input.isKeyPressed(Keys.A)) {
				player.body.setLinearVelocity(-150, 0);
				player.mam.updateMam(angle);
				player.idle = false;
			}
			if (Gdx.input.isKeyPressed(Keys.D)) {
				player.body.setLinearVelocity(150, 0);
				player.mam.updateMam(angle);
				player.idle = false;
			}
			if (Gdx.input.isKeyPressed(Keys.C)) {
				//shotgunCounter++;
			}
			if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT))
			{
				tpEngaged = true;
			}else
			{
				tpEngaged = false;
			}
		}
	}
	
	//KeyBoard Input
	@Override
	public boolean keyDown(int keycode) {
		
		if(keycode == Keys.UP)
		{}
		
		if(keycode == Keys.DOWN)
		{}
		
		if(keycode == Keys.LEFT)
		{}
		
		if(keycode == Keys.RIGHT)
		{}
		if (keycode == Keys.SPACE)
		{
			player.isDashing = true;
			player.dashAnimationRotation = MathUtils.radiansToDegrees * player.mam.mBody.getAngle();
			player.dashAnimationX = player.mam.mBody.getPosition().x;
			player.dashAnimationY = player.mam.mBody.getPosition().y;
		}
		if (keycode == Keys.SHIFT_LEFT)
		{
			itemSelect++;
			if (itemSelect > 2)
			{
				itemSelect =1;
			}
		}
		if (keycode == Keys.C)
		{
			toggleGameLight();
		}
		
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (!player.isDashing) {
			if (keycode == Keys.W) {
				player.body.setLinearVelocity(0, 0);
				player.body.setAngularVelocity(0);
				player.idle = true;
			}
			if (keycode == Keys.S) {
				player.body.setLinearVelocity(0, 0);
				player.body.setAngularVelocity(0);
				player.idle = true;
			}
			if (keycode == Keys.A) {
				player.body.setLinearVelocity(0, 0);
				player.body.setAngularVelocity(0);
				player.idle = true;
			}
			if (keycode == Keys.D) {
				player.body.setLinearVelocity(0, 0);
				player.body.setAngularVelocity(0);
				player.idle = true;
			}

		}
		return false;
	}

	@Override
	public boolean keyTyped(char character){return false;}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		mouseDown = true;
		if (!tpEngaged && player.alive) {
			if (itemSelect == 2) {
				if (playerIsBuffed) {
					shotgunBaby(15);
				}else{
					shotgunBaby(5);
				}
			}
		}else{
			teleportPlayer();
		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		mouseDown = false;
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {return false;}

	//TODO: FIX the ray movement
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
        if (player.alive) {
            Vector3 mouseScreen = new Vector3(screenX, screenY, 0);
            Vector3 mouseWorld = camera.unproject(mouseScreen);
            Vector2 relativeMousePos = new Vector2(mouseWorld.x - player.getPos().x, mouseWorld.y - player.getPos().y);
            float mouseLength = (float) Math.sqrt(Math.pow(relativeMousePos.x, 2) + Math.pow(relativeMousePos.y, 2));
            Vector2 mouseVec = new Vector2(relativeMousePos.x / mouseLength, relativeMousePos.y / mouseLength);

            Vector2 initialPlayerV = new Vector2(0, 1);

            float DP = (mouseVec.x * initialPlayerV.x) + (mouseVec.y * initialPlayerV.y);
            float DE = (mouseVec.x * initialPlayerV.y) - (mouseVec.y * initialPlayerV.x);
            float angle = (float) Math.atan2(DE, DP);

            player.mam.updateMam(-angle);
        }
		return false;
	}

	public float getLengthOf(float vectorX, float vectorY)
	{
		float length = (float) Math.sqrt(Math.pow(vectorX,2)+Math.pow(vectorY,2));
		return length;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {return false;}

	
	
	//ContactListener Stuff
	@Override
	public void beginContact(Contact contact) {
		Entity e1 = (Entity) contact.getFixtureA().getBody().getUserData();
		Entity e2 = (Entity) contact.getFixtureB().getBody().getUserData();
		if (e1 != null)
		{
			e1.checkCollision(e2);
		}
	}

	@Override
	public void endContact(Contact contact) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub
		
	}

	class PointOfIntersection
	{
		Vector2 pointOfIntersection;
		float T1;
		public PointOfIntersection(Vector2 POI, float T1)
		{
			this.pointOfIntersection = POI;
			this.T1 = T1;
		}

	}
}
