package sunmoonnavigator.com;

import java.sql.Date;
import java.util.Calendar;
import java.util.TimeZone;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	CanvasView canvasView;
	private static int degreesPerHour = (360/12);
	private static int minutesPerHour = (60);
	public static final String MY_PREFS_NAME = "SunMoonNavigatorPrefs";
	public static final String Pref_NorthHemi = "NorthHemisphere";
	
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
				
				//Toast.makeText(getApplicationContext(),"Selected Sun mode with "+CalculateSunAngle()+"deg, timeToAngle = "+ConvertTimeToAngle(),Toast.LENGTH_LONG).show();
				
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
				canvasView.SetRotationAngle( 0 );
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings_menu:
			// Create an intent stating which Activity you would like to start
			Intent startSettingsActivity = new Intent(MainActivity.this, SettingsActivity.class);
			startActivity(startSettingsActivity); // Launch the Activity using the intent
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	
	//Function to calculate the correct angle for the current time
	//This is relative to the "reference" time, either 12 (noon) for normal time
	//or 1 (pm) for daylight saving time
	//"before" (notionally morning) are negative angles that are need to get to the reference hour
	//e.g. 11am in normal time is -30
	//"after" (pm times) are positive angle past the reference hour
	//e.g. 3pm in daylight saving is +60
	private float ConvertTimeToRelativeAngleToRefernece()
	{
		Calendar c = Calendar.getInstance(); 
		int hour = c.get(Calendar.HOUR);
		int minute = c.get(Calendar.MINUTE);
		boolean currentlyAM = ( c.get(Calendar.AM_PM) == Calendar.AM );
		
		//Check if in Daylight Saving Time
		//References are now 1 instead of 12
		//Note, new Date() should work, but is throwing an error for some reason
		float referenceHour_deg = 0;
		if ( TimeZone.getDefault().inDaylightTime( new Date( c.getTimeInMillis() ) ) ) {
			referenceHour_deg = (float)degreesPerHour;
		}
		
		//Calculate the angle of the hour hand
		float hourHandAngle_deg = (float)degreesPerHour * ((float)hour + ((float)minute/(float)minutesPerHour) );
		
		//Calculate the angle to the reference hour with the correct sign
		float angleToRefHour_deg = hourHandAngle_deg - referenceHour_deg;
		if ( currentlyAM ) { //take away 360 to be negative
			angleToRefHour_deg -= 360;
			if ( angleToRefHour_deg < -360 ) { //correct for daylight savings time around midnight
				angleToRefHour_deg += 360;
			}
		}
		
		Toast.makeText(getApplicationContext(),"Time ("+hour+") to Ref Angle ("+referenceHour_deg+"): hour hand angle = " + hourHandAngle_deg + ", angle to ref hour = "+angleToRefHour_deg,Toast.LENGTH_LONG).show();
		
		return angleToRefHour_deg;
	}
	
	//Function to calculate the angle for the sun mode
	//Methods from: http://www.wikihow.com/Find-True-North-Without-a-Compass
	//and: https://www.quora.com/How-can-you-navigate-using-the-Moon-as-a-guide
	//TODO: GMT only at the moment
	private float CalculateSunAngle()
	{
		SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
		boolean northernHemi_Flag = prefs.getBoolean(Pref_NorthHemi, true);
		
		float halfDiffAngle_deg = ConvertTimeToRelativeAngleToRefernece() / 2;
		
		//Currently finding the angle between the reference and the hour hand
		//If in the north, then this is south
		//If in the south, then this is north
		if ( northernHemi_Flag )
		{
			halfDiffAngle_deg += 180;
		}
		return halfDiffAngle_deg;
	}
}
