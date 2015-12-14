package sunmoonnavigator.com;

import java.sql.Date;
import java.util.Calendar;
import java.util.TimeZone;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import Moon.MoonPhase;

public class MainActivity extends Activity {
	
	CanvasView canvasView;
	public Button Sun_button; 
	private Handler handler = new Handler();
	
	private final static int degreesPerHour = (360/12);
	private final static int minutesPerHour = (60);
	private final static long MILLISECS_PER_DAY = 24 * 60 * 60 * 1000;
	private final static long MINUTES_PER_HOUR = 60;
	private final static float daysPerLunarCycle = (float)29.53058868;
	private final static float daysPerLunarQuarterTolerance = (float)1; //if within 1 day, then report the new/full/first/last quarter 
	private final static float hoursPerHalfLunarCycle = (float)6;
	public static final String MY_PREFS_NAME = "SunMoonNavigatorPrefs";
	public static final String Pref_NorthHemi = "NorthHemisphere";
	public static final String Pref_SunMode = "SunMode";
	public static final String Pref_ValidUseAccepted = "ValidUseAccepted";
	public static final String Pref_InstructionsRead = "InstructionsRead";
	private static final int updateRate_min = 15; //Update angle every 15 minutes
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Display this activity when the screen unlocks if it was already open
		//From: https://stackoverflow.com/questions/3629179/android-activity-over-default-lock-screen/5727133#5727133
		//Alternative: https://stackoverflow.com/questions/19074466/android-how-to-show-dialog-or-activity-over-lock-screen-not-unlock-the-screen/25707716#25707716
		//List of options: https://developer.android.com/reference/android/view/WindowManager.LayoutParams.html
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		
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
				editor.putBoolean(Pref_SunMode, !prefs.getBoolean(Pref_SunMode, true) );
				editor.commit();
				
				DrawCorrectNorthArrow();
				
				UpdateModeImage();
			}
		}); 
		
		//Show the instructions
		instructionsDialog(false);
		
		DrawCorrectNorthArrow();
		
		//Start the update loop
		handler.postDelayed(updateArrowTask, updateRate_min*60*1000);
		
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
		case R.id.instructions:
			//Display the instructions dialog
			SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
			Editor editor = prefs.edit();
			editor.putBoolean(Pref_InstructionsRead, false);
			editor.commit();
			instructionsDialog(true);
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
		if (!prefs.contains(Pref_InstructionsRead))
	    {
			editor.putBoolean(Pref_InstructionsRead, false );
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
		
		Calendar c = Calendar.getInstance();
		
		//Using https://code.google.com/p/moonphase/source/browse/trunk/Moon/MoonPhase.java?r=12
		MoonPhase moonPhase = new MoonPhase(c);
		moonPhase.getPhase(); //update the class the the current date
		
		//Toast.makeText( getApplicationContext(),"Moon Phase in days = " + moonPhase.getMoonAgeAsDays(),Toast.LENGTH_LONG).show();
		
		return (float)moonPhase.getMoonAgeAsDays();
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
	
	//Function to calculate the moon phase offset in fractional hours
	private float ConvertMoonPhaseToFractionalHours()
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
		
		//Toast.makeText( getApplicationContext(),"ConvertMoonPhaseToFractionalHours\n " + 
		//		"fractionOfMoonPhase = " + fractionOfMoonPhase + "\n" + 
		//		"moonPhaseDays = " + moonPhaseDays,Toast.LENGTH_LONG).show();
		
		return -hoursToAdjust;
	}
	
	//Function to return the moon phase name, giving a tolerance for first/third quarter, full and new
	private String GetMoonPhaseString()
	{
		float moonPhaseDays = CalculateMoonPhaseDays();
		float lunarDaysFirstQuarter = daysPerLunarCycle/4;
		float lunarDaysFull = 2*daysPerLunarCycle/4;
		float lunarDaysThirdQuarter = 3*daysPerLunarCycle/4;
		
		//Special cases
		if ( moonPhaseDays < (daysPerLunarQuarterTolerance/2) || (daysPerLunarCycle-moonPhaseDays) < (daysPerLunarQuarterTolerance/2) )
		{
			return getResources().getString(R.string.moonPhase_new);
		}
		if ( (lunarDaysFirstQuarter-daysPerLunarQuarterTolerance/2) < moonPhaseDays && moonPhaseDays < (lunarDaysFirstQuarter+daysPerLunarQuarterTolerance/2) )
		{
			return getResources().getString(R.string.moonPhase_firstQ);
		}
		if ( (lunarDaysFull-daysPerLunarQuarterTolerance/2) < moonPhaseDays && moonPhaseDays < (lunarDaysFull+daysPerLunarQuarterTolerance/2) )
		{
			return getResources().getString(R.string.moonPhase_full);
		}
		if ( (lunarDaysThirdQuarter-daysPerLunarQuarterTolerance/2) < moonPhaseDays && moonPhaseDays < (lunarDaysThirdQuarter+daysPerLunarQuarterTolerance/2) )
		{
			return getResources().getString(R.string.moonPhase_thirdQ);
		}
		
		//General
		if ( moonPhaseDays < lunarDaysFirstQuarter )
		{
			return getResources().getString(R.string.moonPhase_waxingC);
		}
		if ( moonPhaseDays < lunarDaysFull )
		{
			return getResources().getString(R.string.moonPhase_waxingG);
		}
		if ( moonPhaseDays < lunarDaysThirdQuarter )
		{
			return getResources().getString(R.string.moonPhase_waningG);
		}
		else 
		{
			return getResources().getString(R.string.moonPhase_waningC);
		}
		
	}
	
	//Function to calculate the angle for the sun mode
	//Methods from: http://www.wikihow.com/Find-True-North-Without-a-Compass
	//and: https://www.quora.com/How-can-you-navigate-using-the-Moon-as-a-guide
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
	
	//Moon calculations - better explanation than quora.com:
	//Calculate the virtual hour hand (based on the moon phase)
	//Point the virtual hour at the moon
	//Halfway between the "actual" hour hand and the reference hour is South
	private float CalculateMoonAngle()
	{
		SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
		boolean northernHemi_Flag = prefs.getBoolean(Pref_NorthHemi, true);
		
		//Do the ordinary time calculation
		float halfDiffAngle_deg = ConvertTimeToRelativeAngleToReference() / 2;
		
		//Apply the moon phase offset
		float hoursToAdjust = ConvertMoonPhaseToFractionalHours();
		float moonPhaseAdjustment_deg = hoursToAdjust * degreesPerHour;
		
		halfDiffAngle_deg += moonPhaseAdjustment_deg;
		
		//Currently finding the angle between the reference and the hour hand
		//If in the north, then this is south
		//If in the south, then this is north
		if ( northernHemi_Flag )
		{
			halfDiffAngle_deg += 180;
		}
		
		return halfDiffAngle_deg;
	}
	
	//Function to draw the correct angle of arrow
	private void DrawCorrectNorthArrow()
	{
		SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
		if ( prefs.getBoolean(Pref_SunMode, true) ) {
			canvasView.SetRotationAngle( CalculateSunAngle(), "" );
			//canvasView.SetRotationAngle( 90 );
		}
		else 
		{
			/*
			Calendar c = Calendar.getInstance(); 
			
			float currHour = c.get(Calendar.HOUR) + ( (float)c.get(Calendar.MINUTE) / (float)60);
			float offsetHour = currHour + ConvertMoonPhaseToFractionalHours();
			float refHour = 0; //in GMT
			if ( TimeZone.getDefault().inDaylightTime( new Date( c.getTimeInMillis() ) ) ) {
				refHour = (float)1;
			}
			
			Toast.makeText( getApplicationContext(),"Clock Hour (R)= " + currHour + ", Offset hour (B) = " + offsetHour 
				+ ", Ref Hour (G) = "+refHour,Toast.LENGTH_LONG).show(); */
			
			canvasView.SetRotationAngle( CalculateMoonAngle(), GetMoonPhaseString() );
			//canvasView.SetClockFace( currHour, offsetHour, refHour );
		}
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
	
	//Function to display the instructions
	//from: http://www.androidhub4you.com/2012/09/alert-dialog-box-or-confirmation-box-in.html
	public void instructionsDialog(boolean forceFlag) {
		
		SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
		if ( !forceFlag && prefs.getBoolean(Pref_InstructionsRead, false) ) {
			validUseConfirmation();
			return;
		}
		
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                     switch (which) {
                     case DialogInterface.BUTTON_POSITIVE:
                            // OK button clicked
	                    	SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
	                 		Editor editor = prefs.edit();
	                 		editor.putBoolean(Pref_InstructionsRead, true );
	                 		editor.commit();
	                 		
	                 		validUseConfirmation();
                            break;
                     }
               }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.instructions_text)
        	.setPositiveButton(R.string.validUse_accept, dialogClickListener).show();
	}
	
	//Function to update the arrow position over time
	//From: https://web.archive.org/web/20100126090836/http://developer.android.com/intl/zh-TW/resources/articles/timed-ui-updates.html
	private Runnable updateArrowTask = new Runnable() {
	   public void run() {
		   DrawCorrectNorthArrow();
		   //Toast.makeText( getApplicationContext(),"updateArrowTask fired at" ,Toast.LENGTH_LONG).show();
	     
		   handler.postDelayed(this, updateRate_min*60*1000);
	   }
	};
	
}
