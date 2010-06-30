/**
 * 
 */
package nu.shacknet.poseidon.pn.signanzorbit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * @author poseidon
 *
 */
public class Signanzorbit extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signanzorbit);
		Button startGameButton = (Button)findViewById(R.id.StartGameButton);
		startGameButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(Signanzorbit.this, LevelActivity.class);
				startActivity(i);
			}
		});
		Button helpButton = (Button)findViewById(R.id.HelpButton);
		helpButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(Signanzorbit.this, HelpActivity.class);
				startActivity(i);
			}
		});
		Button exitButton = (Button)findViewById(R.id.ExitButton);
		exitButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
	}
}
