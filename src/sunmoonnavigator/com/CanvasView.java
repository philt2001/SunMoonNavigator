package sunmoonnavigator.com;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CanvasView extends View {
	
	Paint paint;
	Path path;
	float rotationAngle_deg;
	 
	public CanvasView(Context context) {
	 super(context);
	 init();
	}
	 
	public CanvasView(Context context, AttributeSet attrs) {
	 super(context, attrs);
	 init();
	}
	 
	public CanvasView(Context context, AttributeSet attrs, int defStyle) {
	 super(context, attrs, defStyle);
	 init();
	}
	 
	private void init(){
		paint = new Paint();
		path = new Path();
		rotationAngle_deg = 45;
	}
	
	//This is the angle that the arrow should be rotated clockwise from facing the top of the phone
	public void SetRotationAngle( float angle_deg )
	{
		this.rotationAngle_deg = angle_deg + 180; //angle of 0 faces the arrow to the bototm of the phone
		invalidate(); //force a re-draw
	}
	 
	@Override
	protected void onDraw(Canvas canvas) {
		 // TODO Auto-generated method stub
		 super.onDraw(canvas);
		 
		 int width = canvas.getWidth();
		 int height = canvas.getHeight();
		 int radius = (int) ((double)(width/2) * 0.9);
		 int halfWidth = width/2;
		 int halfHeight = height/2;
		 
		 //Clear the path (last arrow)
		 path.reset();
		 
		 paint.setColor(Color.BLACK);
		 paint.setStrokeWidth(10);
		 paint.setStyle(Paint.Style.STROKE);
		 
		 paint.setStyle(Paint.Style.STROKE);
		 canvas.drawCircle(width/2, height/2, radius, paint);
		 
		 //paint.setStyle(Paint.Style.FILL);
		 //canvas.drawCircle(300, 300, 200, paint);
		 //drawCircle(cx, cy, radius, paint)
		 
		 //Offsets from centre, x-coordinates ready
		 //y-coordinates ready to be scaled by the radius
		 //The arrow actually points down, not up
		 float[][] arrowPath = new float[][] {
					{ 0,  1}, //top of arrow
			{30, (float)0.9}, //right of arrow head
			{15, (float)0.9}, //right top of line
			{15, -1}, //right bottom of line
			{-15, -1}, //left bottom of line
			{-15, (float)0.9}, //left top of line
			{-30, (float)0.9}, //left top of arrow head
			};
		 
		 float [][] rotateMatrix = new float[][] {
				 { (float) Math.cos( Math.toRadians(rotationAngle_deg) ), (float) -Math.sin( Math.toRadians(rotationAngle_deg)) },
				 { (float) Math.sin( Math.toRadians(rotationAngle_deg) ),  (float) Math.cos( Math.toRadians(rotationAngle_deg)) },
		 };
		 
		 //Scale to the screen size and rotate
		 float [] temp = new float[2];
		 for (int idx = 0; idx < arrowPath.length; idx++)
		 {
			 arrowPath[idx][1] *= radius;
			 temp[0] = arrowPath[idx][0] * rotateMatrix[0][0] + arrowPath[idx][1] * rotateMatrix[1][0];
			 temp[1] = arrowPath[idx][0] * rotateMatrix[0][1] + arrowPath[idx][1] * rotateMatrix[1][1];
			 
			 arrowPath[idx][0] = temp[0];
			 arrowPath[idx][1] = temp[1];
		 }
		 
		 //Draw the arrow
		 path.moveTo( halfWidth + arrowPath[0][0], halfHeight + arrowPath[0][1] );
		 for (int idx = 1; idx < arrowPath.length; idx++)
		 {
			 path.lineTo( halfWidth + arrowPath[idx][0], halfHeight + arrowPath[idx][1] );
		 }
		 path.close();
		 
		 paint.setColor(Color.RED);
		 canvas.drawPath(path, paint);
	 
	 
	}
}
