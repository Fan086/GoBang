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

public class SmartGoBangPanel extends BaseGoBangPanel {
	
	/**
	 * 判断是否是用户回合
	 */
	public boolean isHumanGo;
	/**
	 * 判断是否是用户先手
	 */
	private boolean isHumanFirst;
	
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
		if(action == MotionEvent.ACTION_DOWN){
			
			float x = event.getX();
			float y = event.getY();
			
			Point point = getValidPoint(x, y);
			
			//当黑白双方的步子集合中已存在这个坐标时，本次点击事件不处理
			if(humanSteps.contains(point) || computerSteps.contains(point)){
				return false;
			}
			
			//哪个子走棋，则将它放入对应的集合中
//			if(mIsWhiteGo){
//				humanSteps.push(point);
//			}else{
//				computerSteps.push(point);
//			}
			humanSteps.push(point);
			
			
			invalidate();
			
			isHumanGo = !isHumanGo;
			//检测是否游戏结束
//			if(GoBangUtils.isGameOver(this)){
//				return false;
//			};
			computerGo();
			
		}
		//这里需要返回true，即告诉父控件这块区域的点击事件由我处理。这样才可获得ACTION_UP
		return true;
	}
	
	
	
	private void computerGo() {
		computerPlayer.goPiece();
	}
	
	
}
