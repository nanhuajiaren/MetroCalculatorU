package com.nanhuajiaren.metrocalculatorultimate;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.support.annotation.CallSuper;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.widget.EditText;
import android.view.inputmethod.InputMethodManager;

public abstract class BaseActivity extends AppCompatActivity
{
	private AlertDialog loadingDialog;
	protected static final String spName = "settings";
	
	@Override
	public void setContentView(int layoutResID)
	{
		super.setContentView(R.layout.global_frame);
		LinearLayout contentHolder = (LinearLayout) findViewById(R.id.globalframeContentLinearLayout);
		View content = LayoutInflater.from(this).inflate(layoutResID,contentHolder,false);
		contentHolder.addView(content);
	}

	public void setupToolBar(String title)
	{
		Toolbar tool = (Toolbar) findViewById(R.id.toolbar);
		tool.setTitle(R.string.app_name);
		tool.setBackgroundColor(getResources().getColor(R.color.colorMain));
		setSupportActionBar(tool);
	}
	
	protected void setBackIconEnabled(boolean enabled){
		getSupportActionBar().setDisplayHomeAsUpEnabled(enabled);//左侧添加一个默认的返回图标
		getSupportActionBar().setHomeButtonEnabled(enabled); //设置返回键可用
	}
	
	@Override
	@CallSuper
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()){
			case android.R.id.home:
				return onHomePressed();
		}
		return super.onOptionsItemSelected(item);
	}

	protected boolean onHomePressed(){
		finish();
		return true;
	}
	
	protected final void showLoadDialog(){
		if(loadingDialog == null){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(R.layout.loading_dialog);
			builder.setCancelable(false);
			loadingDialog = builder.show();
		}
	}
	
	protected final void hideLoadDialog(){
		if(loadingDialog != null){
			loadingDialog.dismiss();
			loadingDialog = null;
		}
	}
	
	public String getSPItem(String name,String defaultValue){
		return getSharedPreferences(spName,MODE_PRIVATE).getString(name,defaultValue);
	}
	
	public void saveSPItem(String name,String value){
		getSharedPreferences(spName,MODE_PRIVATE).edit().putString(name,value).commit();
	}
	
	public boolean getSPItem(String name,boolean defaultValue){
		return getSharedPreferences(spName,MODE_PRIVATE).getBoolean(name,defaultValue);
	}

	public void saveSPItem(String name,boolean value){
		getSharedPreferences(spName,MODE_PRIVATE).edit().putBoolean(name,value).commit();
	}
	
	public int getSPItem(String name,int defaultValue){
		return getSharedPreferences(spName,MODE_PRIVATE).getInt(name,defaultValue);
	}

	public void saveSPItem(String name,int value){
		getSharedPreferences(spName,MODE_PRIVATE).edit().putInt(name,value).commit();
	}
	
	public float getSPItem(String name,float defaultValue){
		return getSharedPreferences(spName,MODE_PRIVATE).getFloat(name,defaultValue);
	}

	public void saveSPItem(String name,float value){
		getSharedPreferences(spName,MODE_PRIVATE).edit().putFloat(name,value).commit();
	}
	
	public long getSPItem(String name,long defaultValue){
		return getSharedPreferences(spName,MODE_PRIVATE).getLong(name,defaultValue);
	}

	public void saveSPItem(String name,long value){
		getSharedPreferences(spName,MODE_PRIVATE).edit().putLong(name,value).commit();
	}
	
	@Override
	public void startActivity(Intent intent)
	{
		try{
			super.startActivity(intent);
		}catch(Exception e){
			
		}
	}
	
	//点击EditText之外的区域隐藏键盘
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isSoftShowing() && isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return super.dispatchTouchEvent(ev);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    //判断软键盘是否正在展示
    private boolean isSoftShowing() {
        //获取当前屏幕内容的高度
        int screenHeight = getWindow().getDecorView().getHeight();
        //获取View可见区域的bottom
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

        return screenHeight - rect.bottom != 0;
    }


    //是否需要隐藏键盘
    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
				&& event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
	
}
