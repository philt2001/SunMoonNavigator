package sunmoonnavigator.com;

import java.util.Calendar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	CanvasView canvasView;
	private static int degreesPerHour = (360/12);
	private static int minutesPerHour = (60);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		//setContentView(new CanvasView(this));
		setContentView(R.layout.activity_main);
		
		Button Sun_button = (Button) findViewById(R.id.sunButton);
		Button Moon_button = (Button) findViewById(R.id.moonButton);
		canvasView = (CanvasView) findViewById(R.id.canvasView);
		
		
		//Set the Sun Button Action
		Sun_button.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				// Hint: use Context's startActivity() method
				// Create an intent stating which Activity you would like to start
				//Intent startNewMeterReading = new Intent(StartPage.this, NewMeterReading.class);
				// Launch the Activity using the intent
				//startActivity(startNewMeterReading);
				
				canvasView.SetRotationAngle( CalculateSunAngle() );
				
				Toast.makeText(getApplicationContext(),"Selected Sun mode with "+CalculateSunAngle()+"deg, timeToAngle = "+ConvertTimeToAngle(),Toast.LENGTH_LONG).show();
				
			}
		}); 
		
		//Set the Moon Button Action
		Moon_button.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				// Hint: use Context's startActivity() method
				// Create an intent stating which Activity you would like to start
				//Intent startNewMeterReading = new Intent(StartPage.this, NewMeterReading.class);
				// Launch the Activity using the intent
				//startActivity(startNewMeterReading);
				
				Toast.makeText(getApplicationContext(),"Selected Moon mode",Toast.LENGTH_LONG).show();
				canvasView.SetRotationAngle( 225 );
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//Function to calculate the correct angle to set
	//Returns 0deg at 12, 90deg at 3
	private float ConvertTimeToAngle()
	{
		Calendar c = Calendar.getInstance(); 
		int hour = c.get(Calendar.HOUR);
		int minute = c.get(Calendar.MINUTE);
		
		float hourHandAngle_deg = (float)degreesPerHour * ((float)hour + ((float)minute/(float)minutesPerHour) );
		return hourHandAngle_deg % 360;
	}
	
	//Function to calculate the angle for the sun mode
	//TODO: GMT only at the moment
	private float CalculateSunAngle()
	{
		Calendar c = Calendar.getInstance(); 
		boolean currentlyAM = ( c.get(Calendar.AM_PM) == Calendar.AM );
		float referenceHour_deg = 30; //for Daylight Savings Time, set to 30
		float diffAngle_deg, rotationAngle_deg;
		if ( currentlyAM ) {
			diffAngle_deg = ( referenceHour_deg - ConvertTimeToAngle() ) % 360;
		}
		else {
			diffAngle_deg = ( -referenceHour_deg + ConvertTimeToAngle() ) % 360;
		}
		float halfDiffAngle_deg = diffAngle_deg / 2;
		if ( currentlyAM ) {
			rotationAngle_deg = ( referenceHour_deg - halfDiffAngle_deg ) % 360;
		}
		else {
			rotationAngle_deg = ( referenceHour_deg + halfDiffAngle_deg ) % 360;
		}
		return rotationAngle_deg;
	}
}
