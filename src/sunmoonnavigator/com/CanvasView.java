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
import android.widget.Toast;

public class CanvasView extends View {
	
	Paint paint;
	Path path;
	float rotationAngle_deg;
	
	boolean clockMode;
	float currHour;
	float offsetHour;
	float refHour;
	float hoursPerRotation;
	String displayText;  
	 
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
		
		clockMode = false;
		currHour = 0;
		offsetHour = 0;
		refHour = 0;
		hoursPerRotation = 12;
	}
	
	//This is the angle that the arrow should be rotated clockwise from facing the top of the phone
	public void SetRotationAngle( float angle_deg, String displayText )
	{
		this.rotationAngle_deg = angle_deg + 180; //angle of 0 faces the arrow to the bottom of the phone
		this.clockMode = false;
		this.displayText = displayText;
		invalidate(); //force a re-draw
	}
	
	//Function to update the clock face
	public void SetClockFace( float currHour, float offsetHour, float refHour )
	{
		this.currHour = -(currHour + 6);
		this.offsetHour = -(offsetHour + 6);
		this.refHour = -(refHour + 6);
		
		this.clockMode = true;
		invalidate(); //force a re-draw
	}
	
	//Create a 2D rotation matrix
	public float [][] CreateRotationMatrix_deg( float rotAngle_deg )
	{
		float [][] rotateMatrix = new float[][] {
				 { (float) Math.cos( Math.toRadians(rotAngle_deg) ), (float) -Math.sin( Math.toRadians(rotAngle_deg)) },
				 { (float) Math.sin( Math.toRadians(rotAngle_deg) ),  (float) Math.cos( Math.toRadians(rotAngle_deg)) },
		 };
		return rotateMatrix;
	}
	
	//Rotate a 2D list of points (in place) by a given rotation matrix and scale the points
	//Size should be N rows by 2 columns
	public void RotateAndScaleMatrix( float [][] matrix, float [][] roationMatrix, float radius )
	{
		float [] temp = new float[2];
		 for (int idx = 0; idx < matrix.length; idx++)
		 {
			 matrix[idx][1] *= radius;
			 temp[0] = matrix[idx][0] * roationMatrix[0][0] + matrix[idx][1] * roationMatrix[1][0];
			 temp[1] = matrix[idx][0] * roationMatrix[0][1] + matrix[idx][1] * roationMatrix[1][1];
			 
			 matrix[idx][0] = temp[0];
			 matrix[idx][1] = temp[1];
		 }
	}
	
	//Function to draw a clock face
	public void drawClock( Canvas canvas )
	{
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
		 
		 //Draw circles for the specific angle
		 int dotRadius = (int) ((double)(width/2) * 0.05);
		 float [][] dotPosition = new float [][] {
				 {0, 1},
		 };
		 
		 //Calculate the circle centres
		 float [][] rotateMatrix = CreateRotationMatrix_deg( 360*currHour/hoursPerRotation );
		 RotateAndScaleMatrix( dotPosition, rotateMatrix, (int) ((double)(width/2) * 0.8) );
		 paint.setColor(Color.RED);
		 canvas.drawCircle(halfWidth +dotPosition[0][0], halfHeight +dotPosition[0][1], dotRadius, paint);
		 
		 dotPosition[0][0] = 0;
		 dotPosition[0][1] = 1;
		 rotateMatrix = CreateRotationMatrix_deg( 360*offsetHour/hoursPerRotation );
		 RotateAndScaleMatrix( dotPosition, rotateMatrix, (int) ((double)(width/2) * 0.8) );
		 paint.setColor(Color.BLUE);
		 dotRadius = (int) ((double)(width/2) * 0.1);
		 canvas.drawCircle(halfWidth +dotPosition[0][0], halfHeight +dotPosition[0][1], dotRadius, paint);
		 
		 dotPosition[0][0] = 0;
		 dotPosition[0][1] = 1;
		 rotateMatrix = CreateRotationMatrix_deg( 360*refHour/hoursPerRotation );
		 RotateAndScaleMatrix( dotPosition, rotateMatrix, (int) ((double)(width/2) * 0.8) );
		 paint.setColor(Color.GREEN);
		 canvas.drawCircle(halfWidth +dotPosition[0][0], halfHeight +dotPosition[0][1], dotRadius, paint);
		 
	}
	 
	@Override
	protected void onDraw(Canvas canvas) {
		 // TODO Auto-generated method stub
		 super.onDraw(canvas);
		 
		 if ( this.clockMode )
		 {
			 drawClock(canvas);
			 //return;
		 }
		 
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
		 
		 float [][] rotateMatrix = CreateRotationMatrix_deg( rotationAngle_deg );
		 
		 //Scale to the screen size and rotate
		 RotateAndScaleMatrix( arrowPath, rotateMatrix, radius );
		 
		 //Draw the arrow
		 path.moveTo( halfWidth + arrowPath[0][0], halfHeight + arrowPath[0][1] );
		 for (int idx = 1; idx < arrowPath.length; idx++)
		 {
			 path.lineTo( halfWidth + arrowPath[idx][0], halfHeight + arrowPath[idx][1] );
		 }
		 path.close();
		 
		 paint.setColor(Color.RED);
		 canvas.drawPath(path, paint);
		 
		 //Add the text
		 paint.setColor(Color.BLACK);
		 paint.setTextSize(25);
		 paint.setStrokeWidth(0); //need to turn this off, otherwise it covers the text
		 canvas.drawText( displayText, 0, 25, paint);
	 
	}
}
