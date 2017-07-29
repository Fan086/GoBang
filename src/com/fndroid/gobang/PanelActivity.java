package com.fndroid.gobang;

import com.fndroid.gobang.panel.GoBangPanel;
import com.fndroid.gobang.player.Human;
import com.fndroid.gobang.player.Player;
import com.fndroid.gobang.utils.GoBangUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 *人人对战棋盘的activity
 */
public class PanelActivity extends Activity implements OnClickListener {
	/**
	 * 自定义棋盘控件
	 */
	GoBangPanel panel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_panel);
		
		panel = (GoBangPanel) findViewById(R.id.panel_gobang);
		
		//构建两个人类玩家，让玩家和棋盘相互添加引用
		Player whitePlayer = new Human(panel, true);
		Player blackPlayer = new Human(panel, false);
		
		panel.setWhitePlayer(whitePlayer);
		panel.setBlackPlayer(blackPlayer);
		
		//为按钮添加监听
		findViewById(R.id.btn_home).setOnClickListener(this);
		findViewById(R.id.btn_restart).setOnClickListener(this);
		findViewById(R.id.btn_regret).setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		//返回主界面
		case R.id.btn_home:
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
			break;
		//重新再来
		case R.id.btn_restart:
			GoBangUtils.restart(panel);
			break;
		//悔棋
		case R.id.btn_regret:
			GoBangUtils.regret(panel);
			break;
		}
	}


}
