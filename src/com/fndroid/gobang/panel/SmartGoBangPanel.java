package com.fndroid.gobang.panel;

import static com.fndroid.gobang.utils.GoBangConstants.HUMAN_FIRST;
import static com.fndroid.gobang.utils.GoBangConstants.SETTINGS;

import java.util.LinkedList;

import com.fndroid.gobang.player.Computer;
import com.fndroid.gobang.player.Human;
import com.fndroid.gobang.player.Player;
import com.fndroid.gobang.utils.GoBangUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 *人机对战的棋盘
 */
public class SmartGoBangPanel extends BaseGoBangPanel {

	/**
	 * 判断是否是用户回合
	 */
	public boolean isHumanGo;
	/**
	 * 判断是否是用户先手
	 */
	public boolean isHumanFirst;
	
	public Player humanPlayer;
	public Computer computerPlayer;
	
	public LinkedList<Point> humanSteps;
	public LinkedList<Point> computerSteps;
	
	public SmartGoBangPanel(Context context) {
		this(context, null);
	}
	public SmartGoBangPanel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public SmartGoBangPanel(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		init();
	}
	
	@Override
	protected void init() {
		super.init();
		
		SharedPreferences sp = getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
		isHumanFirst = sp.getBoolean(HUMAN_FIRST, true);
	}
	
	@Override
	public void setWhitePlayer(Player whitePlayer) {
		super.setWhitePlayer(whitePlayer);
		if(whitePlayer instanceof Human){
			isHumanGo = true;
			humanPlayer = whitePlayer;
			humanSteps = mWhiteSteps;
		}else{
			isHumanGo = false;
			computerPlayer = (Computer) whitePlayer;
			computerSteps = mWhiteSteps;
			computerGo();
		}
	}
	@Override
	public void setBlackPlayer(Player blackPlayer) {
		super.setBlackPlayer(blackPlayer);
		if(blackPlayer instanceof Human){
			humanPlayer = blackPlayer;
			humanSteps = mBlackSteps;
		}else{
			computerPlayer = (Computer) blackPlayer;
			computerSteps = mBlackSteps;
		}
	}
	
	@Override
	//相当于是用户在走棋
	public boolean onTouchEvent(MotionEvent event) {
		//当不是用户回合或者游戏结束时，不执行点击事件
		if(!isHumanGo || mIsGameOver){
			return false;
		}
		int action = event.getAction();
		//之所以用up不用down是为了当上一级控件是scrollView这样的控件时，由它来处理；否则会出现滑动时落子的情况-
		if(action == MotionEvent.ACTION_UP){
			
			float x = event.getX();
			float y = event.getY();
			
			Point point = getValidPoint(x, y);
			
			//当黑白双方的步子集合中已存在这个坐标时，本次点击事件不处理
			if(humanSteps.contains(point) || computerSteps.contains(point)){
				return false;
			}
			
			//放入人类的落子栈中
			humanSteps.push(point);
			
			//更新界面
			invalidate();
			
			isHumanGo = false;
			//检测是否游戏结束并将结束位改变
			GoBangUtils.isGameOver(this);

			//电脑走棋
			computerGo();
			
		}
		//这里需要返回true，即告诉父控件这块区域的点击事件由我处理。这样才可获得ACTION_UP
		return true;
	}
	
	
	private void computerGo() {
		synchronized(computerPlayer){
			//唤醒电脑线程
			computerPlayer.notify();
		}
//		computerPlayer.goPiece();
	}
	
	
}
