package com.nanhuajiaren.metrocalculatorultimate;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.net.Uri;

public class DonateActivity extends BaseActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.donate);
		setupToolBar(getString(R.string.donate));
		setBackIconEnabled(true);
	}
}
