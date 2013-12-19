package com.chapslife.theysaidso;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import android.util.Log;

/**
 * This class creates and handles the physics of the game
 * 
 * @author kchapman
 * 
 */
public class PhysicsWorld {

    /** list of all of the {@link Body} in the world **/
    private List<Body> mBodies = new ArrayList<Body>();
    /** reference to the physics world **/
    private World mWorld;
    /** list of the {@link Body} to be removed from the scene **/
    public List<Body> mBodiesToBeRemoved = new ArrayList<Body>();
    /** framerate **/
    private float mFrameRate = 1.0f / 45.0f;
    
    /**
     * Create the physics world and draws the boundries
     * 
     * @param gravity
     */
    public void create(Vec2 gravity) {

        // Create Physics World with Gravity
        mWorld = new World(gravity);
        mWorld.setAllowSleep(false);
        mWorld.setSleepingAllowed(false);
        mWorld.setAutoClearForces(true);

        BodyDef groundBodyDef = new BodyDef();

        // Create Ground Box
        groundBodyDef.position.set(new Vec2(5.0f, -2.0f));
        Body groundBody = mWorld.createBody(groundBodyDef);
        PolygonShape polygonShape = new PolygonShape();
        // polygonShape.setAsBox(7.0f, 2.0f);
        // groundBody.createFixture(polygonShape, 1.0f);

        // Create top bound
        groundBodyDef.position.set(new Vec2(5.0f, 32.0f));
        groundBody = mWorld.createBody(groundBodyDef);
        groundBody.createFixture(polygonShape, 1.0f);

        polygonShape.setAsBox(2.0f, 18.0f);

        // Create left wall
        groundBodyDef.position.set(new Vec2(-2.0f, 16.0f));
        groundBody = mWorld.createBody(groundBodyDef);
        groundBody.createFixture(polygonShape, 1.0f);

        // Create right wall
        groundBodyDef.position.set(new Vec2(12.0f, 16.0f));
        groundBody = mWorld.createBody(groundBodyDef);
        groundBody.createFixture(polygonShape, 1.0f);
        
        //adjust the framerate based on the available ram
        int ram = readTotalRam();
        if(ram < 900){
            mFrameRate = 1.0f / 30.0f;
        }else {
            mFrameRate = 1.0f / 45.0f;
        }
        
    }

    /**
     * Add a gumball to the scene
     * 
     * @param x
     * @param y
     * @param data
     * @param density
     * @param radius
     * @param bounce
     * @param friction
     * @param bodyType
     */
    public void addGumball(float x, float y, Gumball gumball, float density, float radius,
            float bounce, float friction, BodyType bodyType) {
        // Create Shape with Properties
        CircleShape circleShape = new CircleShape();
        circleShape.m_radius = radius;
        addItem(x, y, circleShape, bounce, gumball, density, friction, bodyType);
    }

    /**
     * adds the pipe sides to the scene
     * 
     * @param x
     * @param y
     * @param data
     * @param density
     * @param bounce
     * @param friction
     * @param bodyType
     */
    public void addPipeSides(float x, float y, int data, float density, float bounce,
            float friction, BodyType bodyType) {
        EdgeShape[] edgeShapes = new EdgeShape[2];
        edgeShapes[0] = new EdgeShape();
        edgeShapes[0].set(new Vec2(.23f, -1f), new Vec2(.01f, .48f));
        edgeShapes[1] = new EdgeShape();
        edgeShapes[1].set(new Vec2(1.4f, -1f), new Vec2(1.55f, .45f));
        addItem(x, y, edgeShapes, bounce, data, density, friction, bodyType);
    }

    /**
     * adds the pipe bottom to the scene
     * 
     * @param x
     * @param y
     * @param data
     * @param density
     * @param bounce
     * @param friction
     * @param bodyType
     */
    public void addPipeBottom(float x, float y, int data, float density, float bounce,
            float friction, BodyType bodyType) {
        EdgeShape[] edgeShapes = new EdgeShape[1];
        edgeShapes[0] = new EdgeShape();
        edgeShapes[0].set(new Vec2(.83f, 0f), new Vec2(2.40f, 0f));
        addItem(x, y, edgeShapes, bounce, data, density, friction, bodyType);
    }

    /**
     * add the floor
     * @param x
     * @param y
     * @param data
     * @param density
     * @param bounce
     * @param friction
     * @param bodyType
     */
    public void addFloor(float x, float y, int data, float density, float bounce, float friction,
            BodyType bodyType) {
        EdgeShape[] edgeShapes = new EdgeShape[1];
        edgeShapes[0] = new EdgeShape();
        edgeShapes[0].set(new Vec2(-9f, -.8f), new Vec2(9f, -.8f));
        addItem(x, y, edgeShapes, bounce, data, density, friction, bodyType);
    }

    public void addItem(float x, float y, Shape[] shapes, float bounce, int data, float density,
            float friction, BodyType bodyType) {
        // Create Dynamic Body
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x, y);
        bodyDef.userData = data;
        bodyDef.type = bodyType;
        Body body = mWorld.createBody(bodyDef);
        mBodies.add(body);

        for (int i = 0; i < shapes.length; i++) {
            // Assign shape to Body
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shapes[i];
            fixtureDef.density = density;
            fixtureDef.friction = friction;
            fixtureDef.restitution = bounce;

            body.createFixture(fixtureDef);
        }
    }

    public void addItem(float x, float y, Shape shape, float bounce, int data, float density,
            float friction, BodyType bodyType) {

        // Create Dynamic Body
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x, y);
        bodyDef.userData = data;
        bodyDef.type = bodyType;
        Body body = mWorld.createBody(bodyDef);
        mBodies.add(body);

        // Assign shape to Body
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = bounce;
        body.createFixture(fixtureDef);
    }

    public void addItem(float x, float y, Shape shape, float bounce, Gumball gumball,
            float density, float friction, BodyType bodyType) {

        // Create Dynamic Body
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x, y);
        bodyDef.userData = gumball;
        bodyDef.type = bodyType;
        Body body = mWorld.createBody(bodyDef);
        mBodies.add(body);

        // Assign shape to Body
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = bounce;
        body.createFixture(fixtureDef);
    }

    /**
     * updates the physics world
     */
    public void update() {
        // Update Physics World
        for (int i = 0; i < mBodiesToBeRemoved.size(); i++) {
            if(mBodiesToBeRemoved.get(i) != null){
                try{
                    mWorld.destroyBody(mBodiesToBeRemoved.get(i));
                }catch(NullPointerException e){
                    //TODO ??????
                }
                
            }
        }
        mBodiesToBeRemoved.clear();
        mWorld.step(mFrameRate, 10, 10);
        mWorld.clearForces();
    }

    /**
     * get a reference to the world
     * 
     * @return
     */
    public World getWorld() {
        return mWorld;
    }

    /**
     * get the total ram of the device
     * @return
     */
    public static synchronized int readTotalRam() {
        int tm = 1000;
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/meminfo", "r");
            String load = reader.readLine();
            String[] totrm = load.split(" kB");
            String[] trm = totrm[0].split(" ");
            tm = Integer.parseInt(trm[trm.length - 1]);
            tm = Math.round(tm / 1024);
            reader.close();
        } catch (IOException ex) {
            
        }
        return tm;
    }
}
