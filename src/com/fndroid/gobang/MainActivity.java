package com.fndroid.gobang;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;

/**
 *主界面的activity
 */
public class MainActivity extends Activity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViewById(R.id.btn_human_computer_battle).setOnClickListener(this);
		findViewById(R.id.btn_double_battle).setOnClickListener(this);
		findViewById(R.id.btn_goto_setting).setOnClickListener(this);
		
		//查看屏幕宽高
		Display display = getWindowManager().getDefaultDisplay();
		System.out.println("width:" + display.getWidth() + " height:" + display.getHeight());
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch(v.getId()){
		//人机对战
		case R.id.btn_human_computer_battle:
			intent.setClass(this, SmartPanelActivity.class);
			startActivity(intent);
			break;
		//人人对战
		case R.id.btn_double_battle:
			intent.setClass(MainActivity.this, PanelActivity.class);
			startActivity(intent);
			break;
		//设置
		case R.id.btn_goto_setting:
			intent.setClass(MainActivity.this, SettingActivity.class);
			startActivity(intent);
			break;
		}
	}
}
