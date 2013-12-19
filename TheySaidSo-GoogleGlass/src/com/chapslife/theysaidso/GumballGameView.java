/**
 * test
 */
package com.chapslife.theysaidso;

import java.util.ArrayList;
import java.util.Random;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author kchapman
 *
 */
public class GumballGameView extends View{
	
	private Paint mPaint;

	private PhysicsWorld mWorld;
	
	//gumball bitmaps
    private Bitmap mGumballBlue;
    private Bitmap mGumballYellow;
    private Bitmap mGumballRed;
    private Bitmap mGumballGreen;
    private Bitmap mGumballOrange;
    private Bitmap mGumballPurple;
    //candy cane bitmaps
    private Bitmap mCaneMainLong;
    private Bitmap mCaneMainLongReverse;
    private Bitmap mCaneMainMed;
    private Bitmap mCaneMainMedReverse;
    private Bitmap mCaneMainSmall;
    private Bitmap mCaneMainSmallReverse;
    private Bitmap mCaneMainTiny;
    private Bitmap mCaneMainTinyReverse;
    private Bitmap mCaneHook;
    private Bitmap mCaneHookFlip;
    private Bitmap mCaneHookReverse;
    private Bitmap mCaneHookReverseFlip;
    private Bitmap mCaneEnd;
    private Bitmap mCaneEndFlip;
    private Bitmap mCaneEndReverse;
    private Bitmap mCaneEndReverseFlip;
    private Bitmap mCaneMainSmallAngleNine;
    private Bitmap mCaneMainSmallAngleSix;
    private Bitmap mCaneMainSmallAngleTwelve;
    private Bitmap mCaneMainReverseTinyAngleTwelve;
    private Bitmap mCaneMainLargeAngleSix;
    private Bitmap mCaneMainMedAngleSix;
    // /** bitmap of a pipe where the gumball drop **/
    private Bitmap mPipeSides;
    
    /** gumball user data **/
    public static final int mGumballRedId = 0;
    public static final int mGumballBlueId = 1;
    public static final int mGumballYellowId = 2;
    public static final int mGumballGreenId = 3;
    public static final int mGumballOrangeId = 4;
    public static final int mGumballPurpleId = 5;

    /** cane user data **/
    public static final int mCaneMainLongId = 6;
    /** cane user data **/
    public static final int mCaneMainLongReverseId = 7;
    /** cane user data **/
    public static final int mCaneMainMedId = 8;
    /** cane user data **/
    public static final int mCaneMainMedReverseId = 9;
    /** cane user data **/
    public static final int mCaneMainSmallId = 10;
    public static final int mCaneMainSmallReverseId = 11;
    public static final int mCaneMainTinyId = 12;
    public static final int mCaneMainTinyReverseId = 13;
    public static final int mCaneHookId = 14;
    public static final int mCaneHookFlipId = 15;
    public static final int mCaneHookReverseId = 16;
    public static final int mCaneHookReverseFlipId = 17;
    public static final int mCaneEndId = 18;
    public static final int mCaneEndFlipId = 19;
    public static final int mCaneEndReverseId = 20;
    public static final int mCaneEndReverseFlipId = 21;

    public static final int mCaneMainSmallAngleNineId = 22;
    public static final int mCaneMainSmallAngleSixId = 23;
    public static final int mCaneMainSmallAngleTwelveId = 24;
    public static final int mCaneMainReverseTinyAngleSixId = 25;
    public static final int mCaneMainLargeAngleSixId = 26;
    public static final int mCaneMainMedAngleSixId = 27;
    
    /** sides of the pipe user data **/
    public static final int mPipeSidesId = -1;
    /**
     * bottom of the pipe user data, this is separate from the side because the
     * gumball is removed from the scene on collision with it
     **/
    public static final int mPipeBottomId = -2;
    public static final int mGameFloorId = -3;
    
    /** array of gumballs so we can drop a random color */
    private static ArrayList<Integer> mGumballIds = new ArrayList<Integer>();
    private static Random mRandomGenerator = new Random();
    private static int mSize = 0;
    private static int mMaxSize = 950;
	public GumballGameView(Context context) {
        super(context);
        init();
    }

    public GumballGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
    	mGumballIds.add(mGumballRedId);
        mGumballIds.add(mGumballBlueId);
        mGumballIds.add(mGumballYellowId);
        mGumballIds.add(mGumballGreenId);
        mGumballIds.add(mGumballOrangeId);
        mGumballIds.add(mGumballPurpleId);
        mPaint = new Paint();
        setClickable(true);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPipeSides == null) {
        	// set the bitmaps
            Resources res = getResources();
            mSize = getWidth();
            mGumballBlue = getImageFromArray(res, R.drawable.gbg_gumball_blue_480, -360f, true, 1);
            mGumballRed = getImageFromArray(res, R.drawable.gbg_gumball_red_480, -360f, true, 1);
            mGumballYellow = getImageFromArray(res, R.drawable.gbg_gumball_yellow_480, -360f, true, 1);
            mGumballGreen = getImageFromArray(res, R.drawable.gbg_gumball_green_480, -360f, true, 1);
            mGumballOrange = getImageFromArray(res, R.drawable.gbg_gumball_orange_480, -360f, true, 1);
            mGumballPurple = getImageFromArray(res, R.drawable.gbg_gumball_purple_480, -360f, true, 1);

            mCaneMainLong = getImageFromArray(res, R.drawable.gbg_candycane_main_480, 180f, false, 1);
            mCaneMainLongReverse = getImageFromArray(res, R.drawable.gbg_candycane_main_reverse_480, 180f,
                    false, 1);
            mCaneMainMed = getImageFromArray(res, R.drawable.gbg_candycane_main_480, 180f, false, .75f);
            mCaneMainMedReverse = getImageFromArray(res, R.drawable.gbg_candycane_main_reverse_480, 180f, false,
                    .75f);

            mCaneMainSmall = getImageFromArray(res, R.drawable.gbg_candycane_main_480, 180f, false, .50f);
            mCaneMainSmallReverse = getImageFromArray(res, R.drawable.gbg_candycane_main_reverse_480, 180f,
                    false, .50f);
            mCaneMainTiny = getImageFromArray(res, R.drawable.gbg_candycane_main_480, 180f, false, .25f);
            mCaneMainTinyReverse = getImageFromArray(res, R.drawable.gbg_candycane_main_reverse_480, 180f,
                    false, .25f);

            mCaneMainSmallAngleNine = getImageFromArray(res, R.drawable.gbg_candycane_main_angle_nine, 180f,
                    true, 1f);
            mCaneMainSmallAngleSix = getImageFromArray(res, R.drawable.gbg_candycane_small_angle_six, 180f,
                    true, 1f);
            mCaneMainSmallAngleTwelve = getImageFromArray(res, R.drawable.gbg_candycane_small_angle_twelve,
                    180f, true, 1f);
            mCaneMainReverseTinyAngleTwelve = getImageFromArray(res, R.drawable.gbg_candycane_tiny_reverse_angle_six,
                    180f, true, 1f);
            mCaneMainLargeAngleSix = getImageFromArray(res, R.drawable.gbg_candycane_large_angle_six,
                    180f, true, 1f);
            mCaneMainMedAngleSix = getImageFromArray(res, R.drawable.gbg_candycane_med_angle_six, 180f,
                    true, 1f);

            mCaneHook = getImageFromArray(res, R.drawable.gbg_candycane_hook_480, 180f, false, 1);
            mCaneHookFlip = getImageFromArray(res, R.drawable.gbg_candycane_hook_480, 180f, true, 1);
            mCaneHookReverse = getImageFromArray(res, R.drawable.gbg_candycane_hook_reverse_480, 180f, false, 1);
            mCaneHookReverseFlip = getImageFromArray(res, R.drawable.gbg_candycane_hook_reverse_480, 180f, true,
                    1);
            mCaneEnd = getImageFromArray(res, R.drawable.gbg_candycane_end_480, 180f, false, 1);
            mCaneEndFlip = getImageFromArray(res, R.drawable.gbg_candycane_end_480,180f, true, 1);
            mCaneEndReverse = getImageFromArray(res, R.drawable.gbg_candycane_end_reverse_480, 180f, false, 1);
            mCaneEndReverseFlip = getImageFromArray(res, R.drawable.gbg_candycane_end_reverse_480, 180f, true, 1);
            mPipeSides = getImageFromArray(res, R.drawable.gbg_gumball_funnel_480, 180f, true, 1);
        }
        canvas.drawColor(Color.TRANSPARENT);
        canvas.translate(0, getHeight());
        canvas.scale(1.0f, -1.0f);
        float scale = getWidth() / 10.0f;
        mPaint.setAntiAlias(true);
        Body body = mWorld.getWorld().getBodyList();
     // iterate through all of the bodies and draw the corresponding bitmaps
        // on them
        while (body != null) {
            if (body.m_userData == null || body.m_userData.equals(mPipeBottomId)) {
                body = body.getNext();
                continue;
            }
            Vec2 position = body.getPosition();
            // Log.v("test", "xxxxx: " + (position == position2) + " " +
            // position.x + " " + position.y);
            Fixture fixture = body.getFixtureList();
            if (fixture == null) {
                body = body.getNext();
                continue;
            }
            Shape shape = fixture.getShape();
            if (shape == null) {
                body = body.getNext();
                continue;
            }
            Bitmap bitmap = null;
            if (body.getUserData() instanceof Gumball) {
                Gumball gumball = (Gumball) body.getUserData();
                if (gumball.mGumballColorId == mGumballBlueId) {
                    bitmap = mGumballBlue;
                } else if (gumball.mGumballColorId == mGumballYellowId) {
                    bitmap = mGumballYellow;
                } else if (gumball.mGumballColorId == mGumballRedId) {
                    bitmap = mGumballRed;
                } else if (gumball.mGumballColorId == mGumballGreenId) {
                    bitmap = mGumballGreen;
                } else if (gumball.mGumballColorId == mGumballOrangeId) {
                    bitmap = mGumballOrange;
                } else if (gumball.mGumballColorId == mGumballPurpleId) {
                    bitmap = mGumballPurple;
                }
            } else if (body.m_userData.equals(mCaneMainLongId)) {
                bitmap = mCaneMainLong;
            } else if (body.m_userData.equals(mCaneMainLongReverseId)) {
                bitmap = mCaneMainLongReverse;
            } else if (body.m_userData.equals(mCaneMainMedId)) {
                bitmap = mCaneMainMed;
            } else if (body.m_userData.equals(mCaneMainMedReverseId)) {
                bitmap = mCaneMainMedReverse;
            } else if (body.m_userData.equals(mCaneMainSmallId)) {
                bitmap = mCaneMainSmall;
            } else if (body.m_userData.equals(mCaneMainSmallReverseId)) {
                bitmap = mCaneMainSmallReverse;
            } else if (body.m_userData.equals(mCaneMainTinyId)) {
                bitmap = mCaneMainTiny;
            } else if (body.m_userData.equals(mCaneMainTinyReverseId)) {
                bitmap = mCaneMainTinyReverse;
            } else if (body.m_userData.equals(mCaneHookId)) {
                bitmap = mCaneHook;
            } else if (body.m_userData.equals(mCaneHookFlipId)) {
                bitmap = mCaneHookFlip;
            } else if (body.m_userData.equals(mCaneHookReverseId)) {
                bitmap = mCaneHookReverse;
            } else if (body.m_userData.equals(mCaneHookReverseFlipId)) {
                bitmap = mCaneHookReverseFlip;
            } else if (body.m_userData.equals(mCaneEndId)) {
                bitmap = mCaneEnd;
            } else if (body.m_userData.equals(mCaneEndFlipId)) {
                bitmap = mCaneEndFlip;
            } else if (body.m_userData.equals(mCaneEndReverseId)) {
                bitmap = mCaneEndReverse;
            } else if (body.m_userData.equals(mCaneEndReverseFlipId)) {
                bitmap = mCaneEndReverseFlip;
            } else if (body.m_userData.equals(mPipeSidesId)) {
                bitmap = mPipeSides;
            } else if (body.m_userData.equals(mCaneMainSmallAngleNineId)) {
                bitmap = mCaneMainSmallAngleNine;
            } else if (body.m_userData.equals(mCaneMainSmallAngleSixId)) {
                bitmap = mCaneMainSmallAngleSix;
            } else if (body.m_userData.equals(mCaneMainSmallAngleTwelveId)) {
                bitmap = mCaneMainSmallAngleTwelve;
            } else if (body.m_userData.equals(mCaneMainReverseTinyAngleSixId)) {
                bitmap = mCaneMainReverseTinyAngleTwelve;
            } else if (body.m_userData.equals(mCaneMainLargeAngleSixId)) {
                bitmap = mCaneMainLargeAngleSix;
            } else if (body.m_userData.equals(mCaneMainMedAngleSixId)) {
                bitmap = mCaneMainMedAngleSix;
            }

            // draw the gumballs
            if (shape instanceof CircleShape && bitmap != null) {
                CircleShape circleShape = (CircleShape) shape;
                canvas.save();
                canvas.rotate((float) (180 * body.getAngle() / Math.PI), scale * position.x, scale
                        * position.y);
                canvas.drawBitmap(bitmap, scale * (position.x - circleShape.m_radius), scale
                        * (position.y - circleShape.m_radius), mPaint);
                canvas.restore();
            } else if (shape instanceof EdgeShape && bitmap != null) {// draw
                                                                      // everything
                                                                      // else

                canvas.save(Canvas.MATRIX_SAVE_FLAG);
                canvas.rotate((float) (180 * body.getAngle() / Math.PI), scale * position.x, scale
                        * position.y);
                canvas.drawBitmap(bitmap, scale * (position.x), scale * (position.y), mPaint);
                canvas.restore();
                
//                mPaint.setColor(Color.GREEN);
//                while (fixture != null) {
//                    EdgeShape edgeShape = (EdgeShape) fixture.getShape();
//                    float x1 = edgeShape.m_vertex1.x + position.x;
//                    float y1 = edgeShape.m_vertex1.y + position.y;
//                    float x2 = edgeShape.m_vertex2.x + position.x;
//                    float y2 = edgeShape.m_vertex2.y + position.y;
//
//                    canvas.drawLine(scale * x1, scale * y1, scale * x2, scale * y2, mPaint);
//                    fixture = fixture.getNext();
//                }

            }

            body = body.getNext();
        }
    }
    
    /**
     * Gives the custom view a reference to the world
     * 
     * @param world
     */
    public void setModel(PhysicsWorld world) {
        mWorld = world;
    }
    
    /**
     * Get the correct bitmap based on screen size, also rotates and flips the
     * image if desired
     * 
     * @param res
     * @param resIds
     * @param sizes
     * @param size
     * @param rotationDegrees
     * @param isFlipped
     * @return
     */
    private static Bitmap getImageFromArray(Resources res, int resId,
            float rotationDegrees, boolean isFlipped, float caneScale) {
        Bitmap bmp = null;

        if (bmp == null) {
            Matrix matrix = new Matrix();
            bmp = BitmapFactory.decodeResource(res, resId);

            if (rotationDegrees != 361f) {
                matrix.setRotate(rotationDegrees, bmp.getWidth() / 2, bmp.getHeight() / 2);
            }
            if (isFlipped) {
                matrix.preScale(-1, 1);
            } else {
                // matrix.preScale(caneScale, 1);
            }
            float scale = ((float) mSize) / mMaxSize;
            matrix.postScale(scale, scale);
            
            bmp = Bitmap.createBitmap(bmp, 0, 0, (int) (bmp.getWidth() * caneScale),
                    bmp.getHeight(), matrix, true);
        }
        return bmp;
    }
    
    /**
     * get a random colored bitmap
     * @return
     */
    public static int getRandomColoredBitmapId() {
        int index = mRandomGenerator.nextInt(mGumballIds.size());
        int id = mGumballIds.get(index);
        return id;
    }
    
    /**
     * add a random colored bitmap to the world
     * @param gumball
     */
    public void addGumball(Gumball gumball) {
        gumball.mGumballColorId = getRandomColoredBitmapId();
        mWorld.addGumball(gumball.mXInitPos, gumball.mYInitPos, gumball, 185.77f, 0.258f, 0.2f,
                0.8f, BodyType.DYNAMIC);
    }
}
