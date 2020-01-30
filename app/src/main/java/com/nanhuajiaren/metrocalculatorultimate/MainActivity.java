package com.nanhuajiaren.metrocalculatorultimate;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;

public class MainActivity extends BaseActivity 
{
	
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
}
