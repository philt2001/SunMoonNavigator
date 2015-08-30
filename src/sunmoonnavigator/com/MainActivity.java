package sunmoonnavigator.com;

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
				
				canvasView.SetRotationAngle( 90 );
				
				Toast.makeText(getApplicationContext(),"Selected Sun mode",Toast.LENGTH_LONG).show();
				
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
	
}
