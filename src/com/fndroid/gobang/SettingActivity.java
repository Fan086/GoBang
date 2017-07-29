package com.fndroid.gobang;

import static com.fndroid.gobang.utils.GoBangConstants.*;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

/**
 *设置界面的activity
 */
public class SettingActivity extends Activity implements OnClickListener{
	private TextView tv_line_num;
	private RadioGroup rg_human_piece_color;
	private SharedPreferences sp;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		//获取sharedPreference中的配置信息
		sp = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
		boolean isHumanFirst = sp.getBoolean(HUMAN_FIRST, false);
		
		findViewById(R.id.btn_minus).setOnClickListener(this);
		findViewById(R.id.btn_plus).setOnClickListener(this);
		findViewById(R.id.btn_setting_confirm).setOnClickListener(this);
		
		//如果是白色，则是先手；黑色为后手
		rg_human_piece_color = (RadioGroup) findViewById(R.id.rg_human_piece_order);
		if(isHumanFirst){
			rg_human_piece_color.check(R.id.rb_human_first);
		}else{
			rg_human_piece_color.check(R.id.rb_human_second);
		}
		
		//行数设置
		tv_line_num = (TextView) findViewById(R.id.tv_line_num);
		tv_line_num.setText(sp.getInt(LINE_NUM, 15) + "");
	}

	@Override
	public void onClick(View v) {
		int num = Integer.parseInt(tv_line_num.getText().toString());
		
		switch(v.getId()){
		//点击加号或减号，更改行数
		case R.id.btn_minus:
			num = num > SETTING_MIN_NUM ? num - 1 : SETTING_MIN_NUM;
			tv_line_num.setText(num + "");
			break;
		case R.id.btn_plus:
			num = num < SETTING_MAX_NUM ? num + 1 : SETTING_MAX_NUM;
			tv_line_num.setText(num + "");
			break;
		//点击确定按钮，将配置信息保存到sharedPreference中
		case R.id.btn_setting_confirm:
			
			int rbId = rg_human_piece_color.getCheckedRadioButtonId();
			boolean humanFirst = (rbId == R.id.rb_human_first) ? true : false;
			
			Editor edit = sp.edit();
			edit.putInt(LINE_NUM, num);
			edit.putBoolean(HUMAN_FIRST, humanFirst);
			edit.apply();
			finish();
			break;
		}
	}
}
