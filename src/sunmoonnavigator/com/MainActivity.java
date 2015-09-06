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
	//Methods from: http://www.wikihow.com/Find-True-North-Without-a-Compass
	//and: https://www.quora.com/How-can-you-navigate-using-the-Moon-as-a-guide
	//TODO: GMT only at the moment
	private float CalculateSunAngle()
	{
		Calendar c = Calendar.getInstance(); 
		boolean currentlyAM = ( c.get(Calendar.AM_PM) == Calendar.AM );
		
		SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
		boolean northernHemi_Flag = prefs.getBoolean(Pref_NorthHemi, true);
		
		//Check if in Daylight Saving Time
		//References are now 1 instead of 12
		//Note, new Date() should work, but is throwing an error for some reason
		float referenceHour_deg = 0;
		if ( TimeZone.getDefault().inDaylightTime( new Date( c.getTimeInMillis() ) ) ) {
			referenceHour_deg = (float)degreesPerHour;
		}
		
		float diffAngle_deg, rotationAngle_deg;
		if ( currentlyAM ) {
			diffAngle_deg = ( referenceHour_deg - ConvertTimeToAngle() ) % 360;
		}
		else {
			diffAngle_deg = ( -referenceHour_deg + ConvertTimeToAngle() ) % 360;
		}
		float halfDiffAngle_deg = diffAngle_deg / 2;
		
		//If northern hemisphere, then apply the half angle to the current hour
		//If southern hemisphere, then apply the half angle to "12", the top of the phone
		
		if ( northernHemi_Flag )
		{
			if ( currentlyAM ) {
				rotationAngle_deg = ( referenceHour_deg - halfDiffAngle_deg ) % 360;
			}
			else {
				rotationAngle_deg = ( referenceHour_deg + halfDiffAngle_deg ) % 360;
			}
		}
		else //Southern hemisphere
		{
			if ( currentlyAM ) {
				rotationAngle_deg = ( 180 + halfDiffAngle_deg ) % 360;
			}
			else {
				rotationAngle_deg = ( 180 - halfDiffAngle_deg ) % 360;
			}
		}
		return rotationAngle_deg;
	}
}
