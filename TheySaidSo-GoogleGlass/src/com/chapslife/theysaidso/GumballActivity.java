/**
 * test
 */
package com.chapslife.theysaidso;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;

/**
 * @author kchapman
 * 
 */
public class GumballActivity extends Activity implements SensorEventListener, AnimationListener {

	/** runnable used for a game loop **/
	private Runnable mRunnable;
	/** instance of the box2d physics world **/
	private PhysicsWorld mWorld;
	
	/** instance of the gumball game view **/
	private GumballGameView mGameView;
	/** View for the gumball outlet */
    private View mGameOutlet;
    
	private int mRotation;
	/** the current level number */
	private int mCurrentLevelNum = 0;
	/** keep the previous y value so the ball doesn't float **/
	private float prevY = 0f;
	private float prevX = 0f;
	
	/** animation to move the gumball outlet */
    private Animation mOutletAnimation;
    /** the previous x position of the gumball */
    private float prevXPos = 0;
    
	/**
	 * queue for the gumballs in the level. gumballs are popped for the queue
	 * when they enter the machine
	 */
	private Queue<Gumball> mGumballQueue;

	/** the current gumball that is release from the outlet */
	private Gumball mCurrentGumball;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gumball);
		mGameView = (GumballGameView) findViewById(R.id.gumballGameView);
		mGameOutlet = findViewById(R.id.tiltGameOutlet);
		Vec2 grav = new Vec2(0.0f, 0.0f);
		// create the physics world
		mWorld = new PhysicsWorld();
		mWorld.create(grav);
		mGameView.setModel(mWorld);
		mGameView.setKeepScreenOn(true);
		mGumballQueue = new LinkedList<Gumball>();
	}

	@Override
	public void onResume() {
		super.onResume();
		// game loop
		mRunnable = new Runnable() {

			@Override
			public void run() {
				synchronized (mWorld) {
					if (mCurrentLevelNum == 0) {
						mCurrentLevelNum++;
						loadLevel(mCurrentLevelNum);
					}
					mWorld.update();
					mGameView.invalidate();
				}
				getWindow().getDecorView().postDelayed(mRunnable, 10);
			}

		};
		getWindow().getDecorView().postDelayed(mRunnable, 1000);
		mRotation = getWindowManager().getDefaultDisplay().getRotation();
		SensorManager sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
		Sensor sensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (sensor != null) {
			sensorManager.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_GAME);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.hardware.SensorEventListener#onAccuracyChanged(android.hardware
	 * .Sensor, int)
	 */
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.hardware.SensorEventListener#onSensorChanged(android.hardware
	 * .SensorEvent)
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		float x, y;
		mRotation = getWindowManager().getDefaultDisplay().getRotation();

		if (mRotation == Surface.ROTATION_0) {
			x = -event.values[0];
			y = -event.values[1];
		} else if (mRotation == Surface.ROTATION_90) {
			x = event.values[1];
			y = -event.values[0];
		} else if (mRotation == Surface.ROTATION_180) {
			x = event.values[0];
			y = event.values[1];
		} else {
			x = -event.values[1];
			y = event.values[0];
		}
		// keep y low to simulate gravity
		if (prevY == 0f) {
			prevY = -9;
		} else if (prevY > y) {
			prevY = y;
		}
		// restrict x to ~+-45 degrees
		if (x > 1.7) {
			x = 2;
		} else if (x < -1.7) {
			x = -2;
		}
		mWorld.getWorld().setGravity(new Vec2(x, prevY));
	}

	@Override
	public void onPause() {
		super.onPause();
		getWindow().getDecorView().removeCallbacks(mRunnable);
		SensorManager sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
		sensorManager.unregisterListener(this);
	}

	/**
	 * add a gumball to the scene
	 * 
	 * @param xPos
	 * @param yPos
	 */
	private void addGumball(float xPos, float yPos) {
		Gumball gumball = new Gumball();
		gumball.mXInitPos = xPos;
		gumball.mYInitPos = yPos;
		gumball.mSoundPoolId = UUID.randomUUID();
		// mGumballMap.put(gumball.mSoundPoolId, false);
		mGameView.addGumball(gumball);
	}

	/**
	 * load the given level
	 * 
	 * @param levelNumber
	 */
	private void loadLevel(int levelNumber) {
		Body body = mWorld.getWorld().getBodyList();
		while (body != null) {
			if (body.m_userData == null) {
				body = body.getNext();
				continue;
			}
			mWorld.mBodiesToBeRemoved.add(body);
			body = body.getNext();
		}
		mWorld.getWorld().step(1.0f / 60.0f, 10, 10);
		try {
			InputStream is = getResources().openRawResource(
					Utils.getLevelRawFile(levelNumber));
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			String json = new String(buffer, "UTF-8");
			JSONObject level = new JSONObject(json);
			JSONArray canes = level.getJSONArray("candycanes");
			for (int i = 0; i < canes.length(); i++) {
				JSONObject canePart = canes.getJSONObject(i);
				int type = canePart.getInt("type");
				float xPos = (float) canePart.getDouble("xPos");
				float yPos = (float) canePart.getDouble("yPos");
				mWorld.addItem(xPos, yPos, Edges.getEdges(type), 0.2f, type,
						185.77f, 0.2f, BodyType.STATIC);
			}
			mWorld.addItem(3.37f, 0f, Edges.getPipeSideEdges(), 0.2f,
					GumballGameView.mPipeSidesId, 185.77f, 0.2f,
					BodyType.STATIC);
			mWorld.addFloor(3.37f, 0f, GumballGameView.mGameFloorId, 185.77f,
					0.2f, 0.8f, BodyType.STATIC);
			mWorld.addPipeBottom(3.37f, 0f, GumballGameView.mPipeBottomId,
					185.77f, 0.2f, 0.8f, BodyType.STATIC);
			JSONArray gumballs = level.getJSONArray("gumballs");
			// mGameBallsLeft = gumballs.length();
			// setIndicators(mGameBallsLeft);
			for (int j = 0; j < gumballs.length(); j++) {
				JSONObject gumball = gumballs.getJSONObject(j);
				float xPos = (float) gumball.getDouble("xPos");
				float yPos = (float) gumball.getDouble("yPos");
				Gumball gumballObject = new Gumball();
				gumballObject.mXInitPos = xPos;
				gumballObject.mYInitPos = yPos;
				mGumballQueue.add(gumballObject);
			}
			mCurrentGumball = mGumballQueue.poll();
			if (mCurrentGumball != null) {
				moveOutlet((mCurrentGumball.mXInitPos));
			}
		} catch (IOException e) {
		} catch (JSONException e) {
		}
	}
	
	/**
     * move the outlet to the gumball drop position
     * @param xPos
     */
    private void moveOutlet(float xPos) {
        float scale = getWindow().getDecorView().getWidth() / 10.0f;
        mOutletAnimation = new TranslateAnimation(prevXPos, (scale * xPos) - 40, 0, 0);
        mOutletAnimation.setDuration(700);
        mOutletAnimation.setFillAfter(true);
        mOutletAnimation.setStartOffset(400);
        mOutletAnimation.setAnimationListener(this);
        mGameOutlet.startAnimation(mOutletAnimation);
        prevXPos = (scale * xPos) - 40;
    }

	/* (non-Javadoc)
	 * @see android.view.animation.Animation.AnimationListener#onAnimationEnd(android.view.animation.Animation)
	 */
	@Override
	public void onAnimationEnd(Animation animation) {
		if (animation == mOutletAnimation) {
            addGumball(mCurrentGumball.mXInitPos, mCurrentGumball.mYInitPos);
            if (mGumballQueue.peek() != null) {
                mCurrentGumball = mGumballQueue.poll();
                moveOutlet(mCurrentGumball.mXInitPos);
            }
        }
	}

	/* (non-Javadoc)
	 * @see android.view.animation.Animation.AnimationListener#onAnimationRepeat(android.view.animation.Animation)
	 */
	@Override
	public void onAnimationRepeat(Animation arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see android.view.animation.Animation.AnimationListener#onAnimationStart(android.view.animation.Animation)
	 */
	@Override
	public void onAnimationStart(Animation arg0) {
		// TODO Auto-generated method stub
		
	}
}
