package com.fndroid.gobang;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViewById(R.id.btn_double_battle).setOnClickListener(this);
		findViewById(R.id.btn_goto_setting).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch(v.getId()){
		case R.id.btn_double_battle:
			intent.setClass(MainActivity.this, PanelActivity.class);
			startActivity(intent);
			break;
		case R.id.btn_goto_setting:
			intent.setClass(MainActivity.this, SettingActivity.class);
			startActivity(intent);
			break;
		}
	}
}