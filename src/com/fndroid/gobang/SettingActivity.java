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

public class SettingActivity extends Activity implements OnClickListener{
	private TextView tv_line_num;
	private RadioGroup rg_human_piece_color;
	private SharedPreferences sp;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		sp = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
		boolean isHumanFirst = sp.getBoolean(HUMAN_FIRST, false);
		
		findViewById(R.id.btn_minus).setOnClickListener(this);
		findViewById(R.id.btn_plus).setOnClickListener(this);
		findViewById(R.id.btn_setting_confirm).setOnClickListener(this);
		
		rg_human_piece_color = (RadioGroup) findViewById(R.id.rg_human_piece_order);
		if(isHumanFirst){
			rg_human_piece_color.check(R.id.rb_human_first);
		}else{
			rg_human_piece_color.check(R.id.rb_human_second);
		}
		
		tv_line_num = (TextView) findViewById(R.id.tv_line_num);
		tv_line_num.setText(sp.getInt(LINE_NUM, 15) + "");
	}

	@Override
	public void onClick(View v) {
		int num = Integer.parseInt(tv_line_num.getText().toString());
		
		switch(v.getId()){
		case R.id.btn_minus:
			num = num > 8 ? num - 1 : 8;
			tv_line_num.setText(num + "");
			break;
		case R.id.btn_plus:
			num = num < 25 ? num + 1 : 25;
			tv_line_num.setText(num + "");
			break;
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
