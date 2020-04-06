package com.nanhuajiaren.metrocalculatorultimate;
import android.os.Bundle;

public class AboutActivity extends BaseActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		setupToolBar(getString(R.string.about));
		setBackIconEnabled(true);
	}
}
