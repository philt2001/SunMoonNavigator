package sunmoonnavigator.com;

import java.sql.Date;
import java.util.Calendar;
import java.util.TimeZone;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	CanvasView canvasView;
	public Button Sun_button; 
	
	private final static int degreesPerHour = (360/12);
	private final static int minutesPerHour = (60);
	private final static long MILLISECS_PER_DAY = 24 * 60 * 60 * 1000;
	private final static long MINUTES_PER_HOUR = 60;
	private final static float daysPerLunarCycle = (float)29.53;
	private final static float hoursPerHalfLunarCycle = (float)6;
	public static final String MY_PREFS_NAME = "SunMoonNavigatorPrefs";
	public static final String Pref_NorthHemi = "NorthHemisphere";
	public static final String Pref_SunMode = "SunMode";
	public static final String Pref_ValidUseAccepted = "ValidUseAccepted";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		//setContentView(new CanvasView(this));
		setContentView(R.layout.activity_main);
		
		Sun_button = (Button) findViewById(R.id.sunButton);
		canvasView = (CanvasView) findViewById(R.id.canvasView);
		
		//Set the sun/moon button icon
		UpdateModeImage();
		
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
				
				//Update the setting and re-draw the arrow
				SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
				Editor editor = prefs.edit();
				if ( prefs.getBoolean(Pref_SunMode, true) ) {
					editor.putBoolean(Pref_SunMode, false );
					canvasView.SetRotationAngle( CalculateMoonAngle() );
				}
				else {
					editor.putBoolean(Pref_SunMode, true );
					canvasView.SetRotationAngle( CalculateSunAngle() );
				}
				editor.commit();
				UpdateModeImage();
				
				
			}
		}); 
		
		//See if the user has accepted the valid use conditions
		validUseConfirmation();
		
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
	
	//Setup the default SharedPreferences
	public void SetDefaultSharedPreferences()
	{
		SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
		Editor editor = prefs.edit();
		
		if (!prefs.contains(Pref_SunMode))
	    {
			editor.putBoolean(Pref_SunMode, true );
	    }
		if (!prefs.contains(Pref_ValidUseAccepted))
	    {
			editor.putBoolean(Pref_ValidUseAccepted, false );
	    }
		editor.commit();
	}
	
	//Function to calculate the correct angle for the current time
	//This is relative to the "reference" time, either 12 (noon) for normal time
	//or 1 (pm) for daylight saving time
	//"before" (notionally morning) are negative angles that are need to get to the reference hour
	//e.g. 11am in normal time is -30
	//"after" (pm times) are positive angle past the reference hour
	//e.g. 3pm in daylight saving is +60
	private float ConvertTimeToRelativeAngleToReference()
	{
		Calendar c = Calendar.getInstance(); 
		int hour = c.get(Calendar.HOUR);
		int minutes = c.get(Calendar.MINUTE);
		boolean currentlyAM = ( c.get(Calendar.AM_PM) == Calendar.AM );
		
		return ConvertTimeToRelativeAngleToReference(hour, minutes, currentlyAM);
	}
	
	//Function to calculate the moon phase
	//Returns the number of days through the cycle the moon currently is (0 being the new moon)
	//Reference: http://www.moonconnection.com/moon_phases_calendar.phtml
	private float CalculateMoonPhaseDays()
	{
		//Best solution would be using something like: https://code.google.com/p/moonphase/source/browse/trunk/Moon/MoonPhase.java?r=12
		//or: http://web.mit.edu/javadev/packages/Acme/Phase.java
		
		//Lazy (temp) solution is to have a fixed date and calculate days difference
		//There was a new moon on (Friday) 14th august 2015
		//Note: months start from 0, so August is actually month 7
		Date reference = getDate(2015, 7, 14);
		Calendar c = Calendar.getInstance();
		//Date today = getDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH) );
		Date today = getDate(2016, 7, 22);
		
		//Calculate the number of days different
		long diff_ms = today.getTime() - reference.getTime();
		float numDays = (float) (diff_ms / MILLISECS_PER_DAY);
		
		//Toast.makeText( getApplicationContext(),"diff_ms = " + diff_ms + ", The days through lunar cycle is: "+numDays% daysPerLunarCycle,Toast.LENGTH_LONG).show();
		
		return (numDays % daysPerLunarCycle);
	}
	
	
	//Function to convert arbitrary time to offset from reference angle
	private float ConvertTimeToRelativeAngleToReference(int hour, int minutes, boolean AM_Flag)
	{
		Calendar c = Calendar.getInstance(); 
		
		//Check if in Daylight Saving Time
		//References are now 1 instead of 12
		//Note, new Date() should work, but is throwing an error for some reason
		float referenceHour_deg = 0;
		if ( TimeZone.getDefault().inDaylightTime( new Date( c.getTimeInMillis() ) ) ) {
			referenceHour_deg = (float)degreesPerHour;
		}
		
		//Calculate the angle of the hour hand
		float hourHandAngle_deg = (float)degreesPerHour * ((float)hour + ((float)minutes/(float)minutesPerHour) );
		
		//Calculate the angle to the reference hour with the correct sign
		float angleToRefHour_deg = hourHandAngle_deg - referenceHour_deg;
		if ( AM_Flag ) { //take away 360 to be negative
			angleToRefHour_deg -= 360;
			if ( angleToRefHour_deg < -360 ) { //correct for daylight savings time around midnight
				angleToRefHour_deg += 360;
			}
		}
		
		//Toast.makeText(getApplicationContext(),"Time ("+hour+") to Ref Angle ("+referenceHour_deg+"): hour hand angle = " + hourHandAngle_deg + ", angle to ref hour = "+angleToRefHour_deg,Toast.LENGTH_LONG).show();
		
		return angleToRefHour_deg;	
	}
	
	//Function to get the angle for the current moon phase and time
	private float ConvertTimeAndMoonPhaseToRelativeAngleToReference()
	{
		//Get the moon phase in days
		float moonPhaseDays = CalculateMoonPhaseDays();
		
		//Get the number of hours to adjust by
		//From: https://www.quora.com/How-can-you-navigate-using-the-Moon-as-a-guide
		//From new->full +0->6 hours
		//From full->new -6->0 hours
		float fractionOfMoonPhase = moonPhaseDays / daysPerLunarCycle;
		float hoursToAdjust;
		if ( fractionOfMoonPhase < 0.5 ) //new->full
		{
			hoursToAdjust = hoursPerHalfLunarCycle * ( fractionOfMoonPhase * 2 ); //Scale by 2 as only go to 0.5 for a full 6 hours
		}
		else
		{
			hoursToAdjust = -hoursPerHalfLunarCycle * ( (1- fractionOfMoonPhase) * 2 ); //Scale by 2 as only go to 0.5 for a full 6 hours
		}
		
		//Apply the time offset to the current time - converting to hours and minutes
		int hourToAdjust = (int)hoursToAdjust;
		int minuteToAdjust = (int)Math.floor( (hoursToAdjust-hourToAdjust) * MINUTES_PER_HOUR );
		
		Calendar c = Calendar.getInstance(); 
		c.add( Calendar.HOUR , hourToAdjust);
		c.add( Calendar.MINUTE , minuteToAdjust);
		
		int hour = c.get(Calendar.HOUR);
		int minutes = c.get(Calendar.MINUTE);
		boolean currentlyAM = ( c.get(Calendar.AM_PM) == Calendar.AM );
		
		return ConvertTimeToRelativeAngleToReference(hour, minutes, currentlyAM);
	}
	
	//Function to calculate the angle for the sun mode
	//Methods from: http://www.wikihow.com/Find-True-North-Without-a-Compass
	//and: https://www.quora.com/How-can-you-navigate-using-the-Moon-as-a-guide
	//TODO: GMT only at the moment
	private float CalculateSunAngle()
	{
		SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
		boolean northernHemi_Flag = prefs.getBoolean(Pref_NorthHemi, true);
		
		float halfDiffAngle_deg = ConvertTimeToRelativeAngleToReference() / 2;
		
		//Currently finding the angle between the reference and the hour hand
		//If in the north, then this is south
		//If in the south, then this is north
		if ( northernHemi_Flag )
		{
			halfDiffAngle_deg += 180;
		}
		return halfDiffAngle_deg;
	}
	
	private float CalculateMoonAngle()
	{
		SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
		boolean northernHemi_Flag = prefs.getBoolean(Pref_NorthHemi, true);
		
		float halfDiffAngle_deg = ConvertTimeAndMoonPhaseToRelativeAngleToReference() / 2;
		
		//Currently finding the angle between the reference and the hour hand
		//If in the north, then this is south
		//If in the south, then this is north
		if ( northernHemi_Flag )
		{
			halfDiffAngle_deg += 180;
		}
		return halfDiffAngle_deg;
	}
	
	//Function to return a date object for a given day
	public static Date getDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = new Date( cal.getTimeInMillis() );
        return date;
	}
	
	//Function to update the image
	public void UpdateModeImage()
	{
		//Set the sun/moon button icon
		SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
		if ( prefs.getBoolean(Pref_SunMode, true) ) {
			Sun_button.setBackgroundResource(R.drawable.sun);
		}
		else {
			Sun_button.setBackgroundResource(R.drawable.moon);
		}
		
	}

	
	//Function to get the user confirmation the limitations
	//from: http://www.androidhub4you.com/2012/09/alert-dialog-box-or-confirmation-box-in.html
	public void validUseConfirmation() {
		
		SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
		if ( prefs.getBoolean(Pref_ValidUseAccepted, false) ) {
			return;
		}

		
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                     switch (which) {
                     case DialogInterface.BUTTON_POSITIVE:
                            // OK button clicked
	                    	SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
	                 		Editor editor = prefs.edit();
	                 		editor.putBoolean(Pref_ValidUseAccepted, true );
	                 		editor.commit();
                            //Toast.makeText(getApplicationContext(), "OK Clicked",Toast.LENGTH_LONG).show();
                            break;
                     }
               }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.validUse_statement)
        	.setPositiveButton(R.string.validUse_accept, dialogClickListener).show();
	}
	
}
