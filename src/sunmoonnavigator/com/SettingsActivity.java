package sunmoonnavigator.com;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
	public static final String Pref_ValidUseAccepted = "ValidUseAccepted";
	Button NorthSouthHemi_button;
	Button ResetValidUse_button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		NorthSouthHemi_button = (Button) findViewById(R.id.NorthSouthHemisphere_Button);
		ResetValidUse_button = (Button) findViewById(R.id.ResetValidUse_Button);
		
		//Setup the default SharedPreferences
		SetDefaultSharedPreferences();
		
		UpdateButtonText();
		
		//Set the North/South Hemisphere button listener
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
		
		ResetValidUse_button.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				//Change the preference to false and then show the message
				SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
				Editor editor = prefs.edit();
				editor.putBoolean(Pref_ValidUseAccepted, false);
				editor.commit();
				
				//Show the message
				validUseConfirmation();
			}
		}); //End of ResetValidUse_button setOnClickListener
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
	
	//Function to get the user confirmation the limitations - copied from MainActivity
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
