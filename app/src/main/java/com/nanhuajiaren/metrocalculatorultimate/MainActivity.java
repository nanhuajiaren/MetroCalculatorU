package com.nanhuajiaren.metrocalculatorultimate;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;

public class MainActivity extends BaseActivity 
{
	private SHMetro metro;
	private DrawerLayout drawer;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setupToolBar(getString(R.string.app_name));
		drawer = (DrawerLayout) findViewById(R.id.mainDrawerLayout);
		ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this, drawer,(Toolbar) findViewById(R.id.toolbar), R.string.open, R.string.close){};		
		toggle.syncState();
		drawer.setDrawerListener(toggle);
		metro = SHMetro.from(getFromAssets("shmetro.json"),this);
    }

	@Override
	public void onBackPressed()
	{
		if(drawer.isDrawerOpen(Gravity.START)){
			drawer.closeDrawer(Gravity.START);
			return;
		}
		super.onBackPressed();
	}
	
	public String getFromAssets(String fileName){ 
		try { 
			InputStreamReader inputReader = new InputStreamReader( getResources().getAssets().open(fileName) ); 
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line="";
			String result="";
			while((line = bufReader.readLine()) != null)
				result += line;
			return result;
		} catch (Exception e) { 
			e.printStackTrace(); 
			return "";
		}
    }
	
	public void onButtonClick(View view){
		TextView text = (TextView) findViewById(R.id.maincontentTextViewresult);
		String input = ((EditText) findViewById(R.id.maincontentEditTextInput)).getText().toString();
		text.setText(metro.calculate(input));
	}
}
