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
import android.app.AlertDialog;

public class MainActivity extends BaseActivity 
{
	public static final String UPDATE_URL = "https://gitee.com/nanhuajiaren/shmetro.json/raw/master/shmetro.json";
	
	private static final int REQUIRE_EXTERNAL_STORAGE = 1;
	
	private volatile SHMetro metro;
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
		requireExternalStorage();
		loadMetro();
	}
	
	public void loadMetro(){
		showLoadDialog();
		new Thread(){
			@Override
			public void run(){
				File dir = new File(getWorkDir());
				dir.mkdirs();
				File onlineFileCopy = new File(getWorkDir() + "shmetro.json");
				metro = SHMetro.from(getFromAssets("shmetro.json"),MainActivity.this);
				if(onlineFileCopy.exists()){
					metro = SHMetro.from(readFileByLines(onlineFileCopy),MainActivity.this);
				}
				if(getConnectedType() != -1){
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
									public void run(){
										AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
										ab.setMessage(metro.updateInfo);
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
					public void run(){
						hideLoadDialog();
					}
				});
			}
		}.start();
    }

	public String getSDCard(){
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}
	
	public String getWorkDir(){
		return getSDCard() + "/MetroCalculatorU/";
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
	
	protected void requireExternalStorage()
	{
		if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUIRE_EXTERNAL_STORAGE);
		}
	}
	
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
			finish();
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
	
	public static String readFileByLines(BufferedReader reader) {
		try {
			String tempString = null;
			StringBuilder sb = new StringBuilder();
			// 一次读一行，读入null时文件结束
			while ((tempString = reader.readLine()) != null) {
				sb.append(tempString);
				sb.append("\n");
			}
			reader.close();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String readFileByLines(File file){
		try
		{
			return readFileByLines(new BufferedReader(new FileReader(file)));
		}
		catch (FileNotFoundException e)
		{}
		return "";
	}
	
	public static void writeLocalStr(String str,File file){
		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			file.createNewFile();
			if(str != null && !"".equals(str)){
				FileWriter fw = new FileWriter(file, true);
				fw.write(str);//写入本地文件中
				fw.flush();
				fw.close();
				System.out.println("执行完毕!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
