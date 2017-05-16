package com.fndroid.gobang;

import static com.fndroid.gobang.utils.GoBangConstants.HUMAN_FIRST;
import static com.fndroid.gobang.utils.GoBangConstants.SETTINGS;

import com.fndroid.gobang.panel.BaseGoBangPanel;
import com.fndroid.gobang.panel.SmartGoBangPanel;
import com.fndroid.gobang.player.Computer;
import com.fndroid.gobang.player.ComputerThread;
import com.fndroid.gobang.player.Human;
import com.fndroid.gobang.player.Player;
import com.fndroid.gobang.utils.GoBangUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class SmartPanelActivity extends Activity implements OnClickListener{
	BaseGoBangPanel panel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_smart_panel);
		
		panel = (SmartGoBangPanel) findViewById(R.id.panel_smart_gobang);
		
		SharedPreferences sp = this.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
		boolean isHumanFirst = sp.getBoolean(HUMAN_FIRST, true);
		Player humanPlayer = new Human(panel, isHumanFirst);
		Player computerPlayer = new Computer(panel, !isHumanFirst);
		if(isHumanFirst){
			panel.setWhitePlayer(humanPlayer);
			panel.setBlackPlayer(computerPlayer);
		}else{
			panel.setBlackPlayer(humanPlayer);
			panel.setWhitePlayer(computerPlayer);
		}
		
		findViewById(R.id.btn_smart_restart).setOnClickListener(this);
		findViewById(R.id.btn_smart_regret).setOnClickListener(this);
		
		//开启电脑线程
		new ComputerThread(computerPlayer).start();
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_smart_restart:
			GoBangUtils.restart(panel);
			break;
		case R.id.btn_smart_regret:
			GoBangUtils.regret(panel);
			break;
		}
	}
}
