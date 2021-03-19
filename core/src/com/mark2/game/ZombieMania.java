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
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
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

    static final float STEP_TIME = 1f / 60f;
    static final int VELOCITY_ITERATIONS = 6;
    static final int POSITION_ITERATIONS = 2;

    public static final short xZOMBIE       = 0x0001;
    public static final short xBULLET       = 0x0002;
    public static final short xWALL         = 0x0004;
    public static final short xPLAYER       = 0x0008; 
    
    public static final short PLAYER_MASK = xZOMBIE;
    public static final short ZOMBIE_MASK = xPLAYER | xBULLET;
    public static final short BULLET_MASK = xZOMBIE;
    
	final HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();

	int shotgunCounter =0;

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
	ArrayList<Structure> worldStructures;
	ArrayList<Zombie> horde;
	Map<Float,PointOfIntersection> tpIntersections;

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
	Sprite health;
	Sprite minusHealth;
	Structure demo;
	Structure demo1;
	Structure demo2;
	Structure demo3;

	boolean gameLightOn = true;
	boolean tpEngaged = false;
	boolean allowedToTP = false;
	float worldWidth = 200f;
	float worldHeight = 200f;
	float screenWidth;
	float screenHeight;
	float tpX;
	float tpY;

	SpriteBatch batch;
	OrthographicCamera camera;
	ExtendViewport viewport;

	Zombie zombie;
	Line2D.Float tpRay;

	@Override
	public void create () {

		//World
		Box2D.init();
		
		loader = new BodyEditorLoader(Gdx.files.internal("BodyColliders.json"));
		structureLoader = new BodyEditorLoader(Gdx.files.internal("Structures.json"));
		world = new World(new Vector2(0,0),true);
		world.setContactListener(this);

		activeBullets = new ArrayList<>();
		deadEntities = new ArrayList<>();
		//toBeDestroyed = new Array();
		worldStructures = new ArrayList<>();
		tpIntersections = new HashMap<>();

		//Renderers
		debugRenderer = new Box2DDebugRenderer();
		shapeRenderer = new ShapeRenderer();
		textureAtlas = new TextureAtlas("Sprites.txt");

		UI = new TextureAtlas("UI/UI.txt");

        Gdx.input.setInputProcessor(this);
        
        addSprites();

        init_Game_Objects();
        render_Health_Bar();
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

		//Create walls (So that things dont fly off)
		createWall(world,worldWidth+10,0,10,worldHeight);
		createWall(world,0,-10,worldWidth,10);
		createWall(world,-10,0,10,worldHeight);
		createWall(world,10,worldHeight+10,worldWidth,10);


		polyBatch = new PolygonSpriteBatch();
		pix = new Pixmap(1,1,Pixmap.Format.RGBA8888);
		pix.setColor(0xDEDEDEFF);
		pix.fill();
		textureSolid = new Texture(pix);
		triangulator = new EarClippingTriangulator();

	}

	public ArrayList<Vector2[]> setScreenBorders(float screenWidth, float screenHeight)
	{
		camera.update();

		Vector3 topLeftW = camera.unproject(new Vector3(0,0,0));
		System.out.println("TopLeft: "+topLeftW);

		Vector3 bottomLeftW = camera.unproject(new Vector3(0,screenHeight,0));
		System.out.println("BottomLeft: "+bottomLeftW);

		Vector3 bottomRightW = camera.unproject(new Vector3(screenWidth,screenHeight,0));
		System.out.println("BottomRight: "+bottomRightW);

		Vector3 topRightW = camera.unproject(new Vector3(screenWidth,0,0));
		System.out.println("TopRight: "+topRightW);

		ArrayList<Vector2[]> borderShape = new ArrayList<>();
		Vector2[] borderVectors = new Vector2[4];
		borderVectors[0] = new Vector2(topLeftW.x,topLeftW.y);
		borderVectors[1] = new Vector2(bottomLeftW.x,bottomLeftW.y);
		borderVectors[2] = new Vector2(bottomRightW.x,bottomRightW.y);
		borderVectors[3] = new Vector2(topRightW.x,topRightW.y);
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
	}

	public void init_Game_Objects()
	{
		horde = new ArrayList<>();

		for (int i =0; i<15; i++) {

			horde.add(new Zombie(sprites, world, loader,player));
		}
		player = new Player(sprites,world,loader,horde);
		bulletPool  = new Pool<Bullet>()
		{
			@Override
			protected Bullet newObject() {
				return new Bullet(sprites, world, player,player.mam.bulletVel, loader);
			}
		};
	}

	public void render_Health_Bar()
	{
		minusHealth = UI.createSprite("MinusHealth");
		minusHealth.setPosition(85,-91);

		health = UI.createSprite("HealthBar");
		health.setPosition(-75,-100);
	}

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
			shapeRenderer.circle(tpX, tpY, 5);
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
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		//Update Camera, Check input, RenderShapes and world
		camera.update();
		checkInput();
		renderShapesAndWorld();
		//Draw player and zombie whiel updating camera pos
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		if (player.alive) {
            player.updatePlayer(batch);

            if (player.isDashing) {
                updateCameraPos(2f);
            } else {
                updateCameraPos(5f);
            }

        }else{
		    deadEntities.add(player);
		    BitmapFont gameOverText = new BitmapFont(true);
		    gameOverText.draw(batch,"Dead X.X",worldWidth/2,worldHeight/2);

        }
        for (Zombie z: horde)
        {
            if (z.alive) {
                z.updateZombie(batch, player);
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
		for (Entity e : deadEntities)
		{
			if (e.getType() == Constants.BULLET_TYPE) {
				bulletPool.free((Bullet) e);
				activeBullets.remove(e);
			}
			e.getBody().setActive(false);
		}
		deadEntities.clear();
        batch.end();
		world.step(1/120f, 6, 2);
		debugRenderer.render(world, camera.combined);
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
		//Render my stuctues
		for (Structure s : worldStructures)
		{
			for (Vector2[] polygon : s.polygonList)
			{
				float[] vertices = s.getPolygonVectors(polygon);
				shapeRenderer.polygon(vertices);
			}
		}
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
			shapeRenderer.line(ray.x1,ray.y1,x2,y2);
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

	
	
	public void addSprites()
	{
		Array<AtlasRegion> regions = textureAtlas.getRegions();
		
		for(AtlasRegion region : regions)
		{
			region.flip(false, true);

			Sprite sprite = textureAtlas.createSprite(region.name);

            sprites.put(region.name, sprite);
			
		}
		
	}
	
	@Override
	public void resize(int width, int height) {

		float screenWidth = Gdx.graphics.getWidth();
		float screenHeight = Gdx.graphics.getHeight();
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

	}
	
	@Override
	public void dispose () {
		textureAtlas.dispose();
		rayHandler.dispose();
		sprites.clear();
		debugRenderer.dispose();
		world.dispose();
	}

	public void checkInput()
	{
		if (!player.isDashing) {
			if (Gdx.input.isKeyPressed(Keys.W)) {
				player.body.setLinearVelocity(0, -150);
				player.mam.updateMam();
			}
			if (Gdx.input.isKeyPressed(Keys.S)) {
				player.body.setLinearVelocity(0, 150);
				player.mam.updateMam();
			}
			if (Gdx.input.isKeyPressed(Keys.A)) {
				player.body.setLinearVelocity(-150, 0);
				player.mam.updateMam();
			}
			if (Gdx.input.isKeyPressed(Keys.D)) {
				player.body.setLinearVelocity(150, 0);
				player.mam.updateMam();
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
			}
			if (keycode == Keys.S) {
				player.body.setLinearVelocity(0, 0);
				player.body.setAngularVelocity(0);
			}
			if (keycode == Keys.A) {
				player.body.setLinearVelocity(0, 0);
				player.body.setAngularVelocity(0);
			}
			if (keycode == Keys.D) {
				player.body.setLinearVelocity(0, 0);
				player.body.setAngularVelocity(0);
			}

		}
		return false;
	}

	@Override
	public boolean keyTyped(char character){return false;}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){

		if (!tpEngaged) {
			if (itemSelect == 1) {
				spawnBullets();
			} else {
				shotgunBaby(10);
			}
		}else{
			teleportPlayer();
		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){return false;}

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

            player.body.setTransform(player.body.getPosition(), -angle);
            player.mam.updateMam();
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
