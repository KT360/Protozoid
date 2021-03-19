package com.mark2.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


public class Structure implements Entity{
    BodyDef bd;
    Body body;
    FixtureDef fd;
    float x;
    float y;
    float scale = Constants.STRUCTURE_SCALING;
    ArrayList<Vector2[]> polygonList;
    List<List<Vector2>> polygons;
    String name;

    public Structure(float x, float y,World world, BodyEditorLoader bodyEditorLoader,int id) {
        this.x = x;
        this.y = y;
        name = "Structure"+id;

        bd = new BodyDef();
        bd.type = BodyDef.BodyType.StaticBody;
        bd.position.set(x, y);
        body = world.createBody(bd);
        body.setUserData(this);

        fd = new FixtureDef();
        fd.density = 1;
        fd.friction = 1;
        fd.restitution = 1;

        polygonList = new ArrayList<>();
        polygons = new ArrayList<>();

        bodyEditorLoader.attachFixture(body, name, fd, scale, this);
        polygons = bodyEditorLoader.vertices;
        for (List<Vector2> polygon : polygons)
        {
            Vector2[] scaledPolygon = getScaledShapeVertices(polygon);
            polygonList.add(scaledPolygon);
        }
        //initVerticesFromModel();
    }
    public Structure(ArrayList<Vector2[]> shapes)
    {
        polygonList = shapes;

    }

    public void updateSegements(ArrayList<Vector2[]> shape)
    {
        polygonList = shape;
    }

    public void initVerticesFromModel()
    {
        for(List<Vector2> polygon : polygons)
        {
            for(Vector2 vertex : polygon)
            {
                System.out.print(vertex+"/");
            }
            System.out.println("\n");
        }
    }

    public float[] getPolygonVectors(Vector2 [] vertices)
    {
        float[] copiedVertices = new float[vertices.length * 2];

        for (int i=0, j=0; i<copiedVertices.length; i+=2,j++)
        {
            copiedVertices[i] = vertices[j].x;
            copiedVertices[i+1] = vertices[j].y;

        }

        return copiedVertices;

    }



    public Vector2[] getScaledShapeVertices(List<Vector2> polygon)
    {
        Vector2[] scaledShapeVertices = new Vector2[polygon.size()];
        for(Vector2 vertex : polygon) {
            Vector2 newVertex = new Vector2((vertex.x*scale) + body.getPosition().x, (vertex.y*scale) + body.getPosition().y);
            scaledShapeVertices[polygon.indexOf(vertex)] = newVertex;
        }
        return scaledShapeVertices;
    }


    public List<Line2D.Float[]> getBodySegments()
    {

        List<Line2D.Float[]> allSegments = new ArrayList<>();
        for(Vector2[] polygon : polygonList)
        {
            Line2D.Float[] segments = new Line2D.Float[polygon.length];

            for (int i =0; i<segments.length; i++)
            {
                int nextPoint = i+1;
                if (nextPoint >= segments.length)
                {
                    nextPoint = segments.length-1;
                    segments[i] = new Line2D.Float(polygon[nextPoint].x,polygon[nextPoint].y,polygon[0].x,polygon[0].y);
                }
                else {
                    segments[i] = new Line2D.Float(polygon[i].x, polygon[i].y, polygon[nextPoint].x, polygon[nextPoint].y);
                }
            }

            allSegments.add(segments);
        }

        return allSegments;

    }

    public HashSet<Vector2> getUniquePoints()
    {
        int arraySize = 0;
        for(Vector2[] polygon : polygonList)
        {
           arraySize+=polygon.length;
        }
        Vector2[] allPoints = new Vector2[arraySize];
        int j =0;
        for (Vector2[] polygon : polygonList)
        {
            for (int i=0; i<polygon.length; i++)
            {
                allPoints[j] = polygon[i];
                j++;
            }
        }
        HashSet<Vector2> uniquePoints = new HashSet<>(Arrays.asList(allPoints));

        return uniquePoints;
    }
    public List<Vector2> getOffsetPoints(Player player)
    {
        List<Vector2> offsetVectors = new ArrayList<>();
        for (Vector2 point : getUniquePoints())
        {
           float newX1 = point.x * (float) Math.cos(0.00001f) - (point.y * (float) Math.sin(0.00001f));
           float newY1 = point.x * (float) Math.sin(0.00001f) + (point.y * (float) Math.cos(0.00001f));

           float newX2 = point.x * (float) Math.cos(-0.00001f) - (point.y * (float) Math.sin(-0.00001f));
           float newY2 = point.x * (float) Math.sin(-0.00001f) + (point.y * (float) Math.cos(-0.00001f));

           Vector2 newV1 = new Vector2(newX1,newY1);
           Vector2 newV2 = new Vector2(newX2,newY2);
           offsetVectors.add(newV1);
           offsetVectors.add(newV2);
        }
        return offsetVectors;
    }
    public float getAngle(Vector2 target, Vector2 dest) {
        float angle = (float) Math.atan2(target.y - dest.y, target.x - dest.x);
        return angle;
    }

    @Override
    public void checkCollision(Entity otherEntity) {
        if(otherEntity != null){}

    }
    @Override
    public int getType()
    {
        return Constants.STRUCTURE_TYPE;
    }

    @Override
    public Body getBody() {
        return this.body;
    }

}
