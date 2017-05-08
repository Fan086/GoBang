package com.fndroid.gobang;

import com.fndroid.gobang.panel.GoBangPanel;
import com.fndroid.gobang.player.Human;
import com.fndroid.gobang.player.Player;
import com.fndroid.gobang.utils.GoBangUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class PanelActivity extends Activity implements OnClickListener {
	GoBangPanel panel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_panel);
		
		panel = (GoBangPanel) findViewById(R.id.panel_gobang);
		
		Player whitePlayer = new Human(panel, true);
		Player blackPlayer = new Human(panel, false);
		
		panel.setWhitePlayer(whitePlayer);
		panel.setBlackPlayer(blackPlayer);
		
		findViewById(R.id.btn_restart).setOnClickListener(this);
		findViewById(R.id.btn_regret).setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_restart:
			GoBangUtils.restart(panel);
			break;
		case R.id.btn_regret:
//			Toast.makeText(MainActivity.this, "用户悔棋了", 0).show();
			GoBangUtils.regret(panel);
			break;
		}
	}


}
