/**
 * test
 */
package com.chapslife.theysaidso;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.contacts.Contact;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

/**
 * @author kchapman
 * 
 */
public class GumballActivity extends Activity implements SensorEventListener, AnimationListener, ContactListener {

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
    
    /** the background music sound **/
    private MediaPlayer mBackgroundMusic;
    /** sound for a small gumball bounce **/
    private int mBounceSmall = -1;
    /** sound for a medium gum ball bounce **/
    private int mBounceMed = -1;
    /** sound for a large gum ball bounce */
    private int mBounceLarge = -1;
    /** sound when the ball enters the machine */
    private int mBallInMachine = -1;
    /** sound when the ball fails */
    private int mBallFailId = -1;
    /** sond when the ball enters the scene **/
    private int mBallDropId = -1;
    /** sound when the game is over */
    private int mGameOver = -1;
    private int mBeepSound;
    
    /** instance of the soundpool */
    private SoundPool mSoundPool;
    /** number of gumballs left in the level */
    private int mGameBallsLeft = 2;
    /** number of gumballs collected */
    private int mNumberCollected = 0;
    /** reference to the pause state of the game */
    private boolean wasPaused = false;
    private GestureDetector mGestureDetector;
    
	/**
	 * queue for the gumballs in the level. gumballs are popped for the queue
	 * when they enter the machine
	 */
	private Queue<Gumball> mGumballQueue;

	/** the current gumball that is release from the outlet */
	private Gumball mCurrentGumball;
	/** number of times the ball respawned in a round */
    private int mBallRespawns = 0;
    private int mTotlaBallRespawns = 0;
    private View mSnoView;
    private AnimationSet mSnoAnimSet;
    
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
		mGestureDetector = createGestureDetector(this);
		
		mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		mBounceSmall = mSoundPool.load(this, R.raw.gbg_ball_bounce_1, 1);
        mBounceMed = mSoundPool.load(this, R.raw.gbg_ball_bounce_2, 1);
        mBounceLarge = mSoundPool.load(this, R.raw.gbg_ball_bounce_3, 1);
        mBallInMachine = mSoundPool.load(this, R.raw.gbg_ball_into_machine, 1);
        mBallFailId = mSoundPool.load(this, R.raw.gbg_ball_fall_out, 1);
        mBallDropId = mSoundPool.load(this, R.raw.gbg_new_ball_bounce_drop, 1);
        mGameOver = mSoundPool.load(this, R.raw.gameover, 1);
        
        mSnoAnimSet = new AnimationSet(true);
        TranslateAnimation mSnomanAnim = new TranslateAnimation(150, 0, 150, 0);
        mSnomanAnim.setDuration(1000);
        mSnoAnimSet.addAnimation(mSnomanAnim);
        TranslateAnimation mSnoAnimBack = new TranslateAnimation(0, 150, 0, 150);
        mSnoAnimBack.setDuration(1000);
        mSnoAnimBack.setStartOffset(1500);
        mSnoAnimBack.setAnimationListener(this);
        mSnoAnimSet.addAnimation(mSnoAnimBack);
        mSnoAnimSet.setAnimationListener(this);
        mSnoView = findViewById(R.id.match_snowman);
        mBeepSound = mSoundPool.load(this, R.raw.mmg_open_door_2, 1);
        
        mWorld.getWorld().setContactListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		// game loop
		mRunnable = new Runnable() {

			@Override
			public void run() {
				synchronized (mWorld) {
				    if (!wasPaused) {
				        if (mCurrentLevelNum == 0) {
	                        mCurrentLevelNum++;
	                        loadLevel(mCurrentLevelNum);
	                    }
	                    mWorld.update();
	                    mGameView.invalidate();
				    }
				}
				getWindow().getDecorView().postDelayed(mRunnable, 10);
			}

		};
		getWindow().getDecorView().postDelayed(mRunnable, 50);
		mRotation = getWindowManager().getDefaultDisplay().getRotation();
		SensorManager sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
		Sensor sensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (sensor != null) {
			sensorManager.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_GAME);
		}
		loadBackgroundMusic();
		wasPaused = false;
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
		turnOffBackgroundMusic();
		wasPaused = true;
	}

	@Override
    public void openOptionsMenu() {
	    super.openOptionsMenu();
	    wasPaused = true;
    }
	
	@Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        wasPaused = false;
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gumball, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.quit_game_menu_item:
                finish();
                return true;
            case R.id.toggle_sound_menu_item:
                if(mBackgroundMusic != null && mBackgroundMusic.isPlaying()){
                    turnOffBackgroundMusic();
                }else{
                    loadBackgroundMusic();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	/** load the background music of the game */
    private void loadBackgroundMusic() {
        mBackgroundMusic = MediaPlayer.create(this, R.raw.santatracker_musicloop);
        mBackgroundMusic.setLooping(true);
        mBackgroundMusic.setVolume(.1f, .1f);
        mBackgroundMusic.start();
    }
    
    /** turn off background music of the game */
    private void turnOffBackgroundMusic() {
        if (mBackgroundMusic != null) {
            mBackgroundMusic.stop();
            mBackgroundMusic.release();
            mBackgroundMusic = null;
        }
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
	    mBallRespawns = 0;
        mNumberCollected = 0;
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
			mGameBallsLeft = gumballs.length();
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
        }else if (animation == mSnoAnimSet) {
            mSnoView.clearAnimation();
            mSnoView.setVisibility(View.GONE);
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
	public void onAnimationStart(Animation animation) {
	    if (animation == mSnoAnimSet) {
            mSnoView.setVisibility(View.VISIBLE);
            mSnoView.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mSoundPool.play(mBeepSound, .5f, .5f, 0, 0, 1.0f);
                }
            }, 800);
        }
	}

    /* (non-Javadoc)
     * @see org.jbox2d.callbacks.ContactListener#beginContact(org.jbox2d.dynamics.contacts.Contact)
     */
    @Override
    public void beginContact(Contact arg0) {
        // do nothing
        
    }

    /* (non-Javadoc)
     * @see org.jbox2d.callbacks.ContactListener#endContact(org.jbox2d.dynamics.contacts.Contact)
     */
    @Override
    public void endContact(Contact contact) {
     // if the gumball goes in the pipe, remove it from the scene
        if (contact.getFixtureA().getBody().getUserData() != null
                && !(contact.getFixtureA().getBody().getUserData() instanceof Gumball)
                && (contact.getFixtureA().getBody().getUserData()
                        .equals(GumballGameView.mPipeBottomId) || contact.getFixtureA().getBody()
                        .getUserData().equals(GumballGameView.mPipeSidesId))) {
            mWorld.mBodiesToBeRemoved.add(contact.getFixtureB().getBody());
            checkForEndOfRound();
        } else if (contact.getFixtureB().getBody().getUserData() != null
                && !(contact.getFixtureB().getBody().getUserData() instanceof Gumball)
                && (contact.getFixtureA().getBody().getUserData()
                        .equals(GumballGameView.mPipeBottomId) || contact.getFixtureA().getBody()
                        .getUserData().equals(GumballGameView.mPipeSidesId))) {
            mWorld.mBodiesToBeRemoved.add(contact.getFixtureA().getBody());

            checkForEndOfRound();
        } else if (contact.getFixtureA().getBody().getUserData() != null
                && !(contact.getFixtureA().getBody().getUserData() instanceof Gumball)
                && contact.getFixtureA().getBody().getUserData().equals(GumballGameView.mGameFloorId)) {
            Gumball gumball = ((Gumball) contact.getFixtureB().getBody().getUserData());
            mWorld.mBodiesToBeRemoved.add(contact.getFixtureB().getBody());
            mSoundPool.play(mBallFailId, 1, 1, 0, 0, 1.0f);
            mWorld.getWorld().step(1.0f / 60.0f, 10, 10);
            moveOutlet((mCurrentGumball.mXInitPos));
            mBallRespawns++;
        } else if (contact.getFixtureB().getBody().getUserData() != null
                && !(contact.getFixtureB().getBody().getUserData() instanceof Gumball)
                && contact.getFixtureB().getBody().getUserData().equals(GumballGameView.mGameFloorId)) {
            Gumball gumball = ((Gumball) contact.getFixtureB().getBody().getUserData());
            mWorld.mBodiesToBeRemoved.add(contact.getFixtureA().getBody());
            mSoundPool.play(mBallFailId, 1, 1, 0, 0, 1.0f);
            mWorld.getWorld().step(1.0f / 60.0f, 10, 10);
            moveOutlet((mCurrentGumball.mXInitPos));
            mBallRespawns++;
        }
    }

    /* (non-Javadoc)
     * @see org.jbox2d.callbacks.ContactListener#postSolve(org.jbox2d.dynamics.contacts.Contact, org.jbox2d.callbacks.ContactImpulse)
     */
    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        if (contact.getFixtureA().getBody().getUserData() != null
                && !(contact.getFixtureA().getBody().getUserData() instanceof Gumball)
                && ((Integer) contact.getFixtureA().getBody().getUserData()) > GumballGameView.mGumballPurpleId) {
            if (impulse.normalImpulses[0] > 80) {
                playBounceSound(impulse.normalImpulses[0]);
            }
        } else if (contact.getFixtureB().getBody().getUserData() != null
                && !(contact.getFixtureB().getBody().getUserData() instanceof Gumball)
                && ((Integer) contact.getFixtureB().getBody().getUserData()) > GumballGameView.mGumballPurpleId) {
            if (impulse.normalImpulses[0] > 80) {
                playBounceSound(impulse.normalImpulses[0]);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.jbox2d.callbacks.ContactListener#preSolve(org.jbox2d.dynamics.contacts.Contact, org.jbox2d.collision.Manifold)
     */
    @Override
    public void preSolve(Contact arg0, Manifold arg1) {
        // do nothing
        
    }
    
    /**
     * Checks for the end of round. If it's the end of the round start animation
     * for the next round
     */
    private void checkForEndOfRound() {
        mSoundPool.play(mBallInMachine, 1, 1, 0, 0, 1.0f);
        mGameBallsLeft--;
        mNumberCollected++;
        
        if (mGameBallsLeft == 0) {
            mTotlaBallRespawns += mBallRespawns;
            if((mCurrentLevelNum % 3 == 0) && mTotlaBallRespawns == 0){
                mSnoView.startAnimation(mSnoAnimSet);
            }
            mCurrentLevelNum++;
            loadLevel(mCurrentLevelNum);
        }
    }
    
    /**
     * Play the ball hit sound based on the collision impulse
     * 
     * @param impulse
     */
    private void playBounceSound(float impulse) {
        if (impulse > 80) {
            mSoundPool.play(mBounceLarge, 1, 1, 0, 0, 1.0f);
        } else if (impulse > 60) {
            mSoundPool.play(mBounceMed, 1, 1, 0, 0, 1.0f);
        } else if (impulse > 30) {
            mSoundPool.play(mBounceSmall, 1, 1, 0, 0, 1.0f);
        }
    }
    
    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        //Create a base listener for generic gestures
        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                    openOptionsMenu();
                    return true;
                }
                return false;
            }
        });
        
        return gestureDetector;
    }
    
    /**
     * Send generic motion events to the gesture detector
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }
}
