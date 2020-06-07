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
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.io.File;
import android.os.Environment;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileWriter;
import android.os.Handler;
import android.view.animation.TranslateAnimation;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;

public class MainActivity extends BaseActivity 
{
	public static final String UPDATE_URL = "https://gitee.com/nanhuajiaren/shmetro.json/raw/master/shmetro.json";

	private static final int REQUIRE_EXTERNAL_STORAGE = 1;
	
	private volatile SHMetro metro;
	private volatile boolean isInAnimation = false;
	private volatile String waitingText = "a";
	private DrawerLayout drawer;
	private TextView outputView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setupToolBar(getString(R.string.app_name));
		drawer = (DrawerLayout) findViewById(R.id.mainDrawerLayout);
		ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawer,(Toolbar) findViewById(R.id.toolbar),R.string.open,R.string.close){};		
		toggle.syncState();
		drawer.setDrawerListener(toggle);
		outputView = (TextView) findViewById(R.id.maincontentTextViewresult);
		if(showAgreement()){
			if(requireExternalStorage()){
				loadMetro();
			}
		}
		updateDarkModeState();
	}

	public void loadMetro()
	{
		showLoadDialog();
		new Thread(){
			@Override
			public void run()
			{
				File dir = new File(getWorkDir());
				dir.mkdirs();
				File onlineFileCopy = new File(getWorkDir() + "shmetro.json");
				metro = SHMetro.from(getFromAssets("shmetro.json"),MainActivity.this);
				if (onlineFileCopy.exists())
				{
					metro = SHMetro.from(readFileByLines(onlineFileCopy),MainActivity.this);
				}
				if (getConnectedType() != -1)
				{
					try
					{
						URL url = new URL(UPDATE_URL);
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						if (conn.getResponseCode() == 200)
						{
							InputStream is = conn.getInputStream();
							BufferedReader br = new BufferedReader(new InputStreamReader(is));
							String online = readFileByLines(br);
							SHMetro onlineMetro = SHMetro.from(online,MainActivity.this);
							if (onlineMetro.getDataVersion() > metro.getDataVersion())
							{
								metro = onlineMetro;
								writeLocalStr(online,onlineFileCopy);
								runOnUiThread(new Runnable(){
										@Override
										public void run()
										{
											AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
											ab.setTitle(R.string.data_updated);
											StringBuilder sb = new StringBuilder();
											sb.append(getString(R.string.update_info));
											sb.append(metro.updateInfo);
											sb.append("\n");
											sb.append(getString(R.string.update_time));
											sb.append(metro.updateTime);
											ab.setMessage(sb.toString());
											ab.setPositiveButton(R.string.ok,null);
											ab.show();
										}
									});
							}
						}
					}
					catch (IOException e)
					{}
				}
				runOnUiThread(new Runnable(){
						@Override
						public void run()
						{
							hideLoadDialog();
						}
					});
			}
		}.start();
    }

	public String getSDCard()
	{
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}

	public String getWorkDir()
	{
		return getSDCard() + "/MetroCalculatorU/";
	}

	@Override
	public void onBackPressed()
	{
		if (drawer.isDrawerOpen(Gravity.START))
		{
			drawer.closeDrawer(Gravity.START);
			return;
		}
		super.onBackPressed();
	}

	public void onButtonClick(View view)
	{
		String input = ((EditText) findViewById(R.id.maincontentEditTextInput)).getText().toString();
		if(input.length() > 0){
			postTextToOutput(metro.calculate(input));
		}
	}

	protected boolean requireExternalStorage()
	{
		if (ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},REQUIRE_EXTERNAL_STORAGE);
			return false;
		}else{
			return true;
		}
	}

	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode,permissions,grantResults);
		boolean flag = grantResults.length > 0;
		for (int i = 0;i < grantResults.length;i ++)
		{
			if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
			{
				flag = false;
			}
		}
		if (!flag)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.storage_permission_info);
			builder.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1,int p2){
					requireExternalStorage();
				}
			});
			builder.setCancelable(false);
			builder.show();
		}else{
			loadMetro();
		}
	}

	public int getConnectedType()
	{
        ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null && mNetworkInfo.isAvailable())
		{
			return mNetworkInfo.getType();
		}
        return -1;
    }

	public static String readFileByLines(BufferedReader reader)
	{
		try
		{
			String tempString = null;
			StringBuilder sb = new StringBuilder();
			// 一次读一行，读入null时文件结束
			while ((tempString = reader.readLine()) != null)
			{
				sb.append(tempString);
				sb.append("\n");
			}
			reader.close();
			return sb.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return "";
	}

	public static String readFileByLines(File file)
	{
		try
		{
			return readFileByLines(new BufferedReader(new FileReader(file)));
		}
		catch (FileNotFoundException e)
		{}
		return "";
	}

	public static void writeLocalStr(String str, File file)
	{
		try
		{
			if (!file.getParentFile().exists())
			{
				file.getParentFile().mkdirs();
			}
			file.createNewFile();
			if (str != null && !"".equals(str))
			{
				FileWriter fw = new FileWriter(file,true);
				fw.write(str);//写入本地文件中
				fw.flush();
				fw.close();
				System.out.println("执行完毕!");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void postTextToOutput(String str)
	{
		if (isInAnimation)
		{
			return;
		}
		Handler handler = new Handler();
		isInAnimation = true;
		if (!waitingText.equals(""))
		{
			waitingText = str;
			TranslateAnimation animation = new TranslateAnimation(0,0,0,getWindowManager().getDefaultDisplay().getHeight());
			animation.setDuration(250);
			outputView.startAnimation(animation);
			handler.postDelayed(new Runnable(){
					@Override
					public void run()
					{
						outputView.setText(waitingText);
						TranslateAnimation animation = new TranslateAnimation(0,0,getWindowManager().getDefaultDisplay().getHeight(),0);
						animation.setDuration(250);
						outputView.startAnimation(animation);
					}
				},250);
			handler.postDelayed(new Runnable(){
					@Override
					public void run()
					{
						isInAnimation = false;
					}
				},500);
		}
		else
		{
			outputView.setText(waitingText);
			TranslateAnimation animation = new TranslateAnimation(0,0,getWindowManager().getDefaultDisplay().getHeight(),0);
			animation.setDuration(250);
			outputView.startAnimation(animation);
			handler.postDelayed(new Runnable(){
					@Override
					public void run()
					{
						isInAnimation = false;
					}
				},250);
		}
	}

	public void onNavTextClick(View view)
	{
		switch (view.getId())
		{
			case R.id.mainnavTextViewAbout:
				startActivity(new Intent(this,AboutActivity.class));
				break;
			case R.id.mainnavTextViewDarkMode:
				saveDarkMode(!getDarkMode());
				updateDarkModeState();
				break;
			case R.id.mainnavTextViewDonate:
				startActivity(new Intent(this,DonateActivity.class));
				break;
			case R.id.mainnavTextViewAgreement:
				showAgreement(true);
				break;
			default:

				break;
		}
	}

	public void updateDarkModeState()
	{
		TextView text = (TextView) findViewById(R.id.mainnavTextViewDarkMode);
		text.setText(getString(R.string.dark_mode) + getString(getDarkMode() ? R.string.open : R.string.close));
	}
	
	private boolean showAgreement(){
		return showAgreement(false);
	}
	
	private boolean showAgreement(boolean forced){
		if(!getSPItem("agreement",false) || forced){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			try
			{
				String agreement = readFileByLines(new BufferedReader(new InputStreamReader(getAssets().open("agreement.txt"))));
				builder.setTitle(R.string.agreement);
				builder.setMessage(agreement);
				builder.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface p1,int p2){
						saveSPItem("agreement",true);
						requireExternalStorage();
					}
				});
			}
			catch (IOException e)
			{
				builder.setMessage(R.string.agreement_error);
				builder.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface p1,int p2){
						finish();
					}
				});
			}
			
			builder.setCancelable(false);
			builder.show();
			return false;
		}else{
			return true;
		}
	}
}
