package sunmoonnavigator.com;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	
	public static final String MY_PREFS_NAME = "SunMoonNavigatorPrefs";
	public static final String Pref_NorthHemi = "NorthHemisphere";
	Button NorthSouthHemi_button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		NorthSouthHemi_button = (Button) findViewById(R.id.NorthSouthHemisphere_Button);
		
		//Setup the default SharedPreferences
		SetDefaultSharedPreferences();
		
		UpdateButtonText();
		
		//Set the New Meter Reading Button Action
		NorthSouthHemi_button.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				//Change the preference and update the display
				SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
				boolean northHemi = prefs.getBoolean(Pref_NorthHemi, true);
				northHemi = !northHemi;
				
				Editor editor = prefs.edit();
				editor.putBoolean(Pref_NorthHemi, northHemi);
				editor.commit();
				
				//Update the displayed text
				UpdateButtonText();
				
				Toast.makeText(getApplicationContext(),"North hemi = "+northHemi,Toast.LENGTH_LONG).show();
			}
		}); //End of NorthSouthHemi_button setOnClickListener
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	
	//Setup the default SharedPreferences
	public void SetDefaultSharedPreferences()
	{
		SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
		Editor editor = prefs.edit();
		
		/*if (!prefs.contains(Reminder_DayOfTheWeek))
	    {
			editor.putString(Reminder_DayOfTheWeek, "Sunday");
	    }*/
		
		if (!prefs.contains(Pref_NorthHemi))
	    {
			editor.putBoolean(Pref_NorthHemi, true );
	    }
		/*if (!prefs.contains(Reminder_Minute))
	    {
			editor.putInt(Reminder_Minute, 00 );
	    }
		if (!prefs.contains(Reminder_OnFlag))
	    {
			editor.putInt(Reminder_OnFlag, 00 );
	    }*/
		editor.commit();
	}

	//Update the displayed text on the button
	public void UpdateButtonText()
	{
		SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
		boolean northHemi = prefs.getBoolean(Pref_NorthHemi, true);
		
		if ( northHemi ){
			NorthSouthHemi_button.setText(R.string.northHemi_settings);
		}
		else {
			NorthSouthHemi_button.setText(R.string.southHemi_settings);
		}
		
	}
	
}
