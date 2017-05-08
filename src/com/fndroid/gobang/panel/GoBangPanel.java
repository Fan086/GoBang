package com.fndroid.gobang.panel;

import com.fndroid.gobang.utils.GoBangUtils;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class GoBangPanel extends BaseGoBangPanel{

	public GoBangPanel(Context context) {
		this(context, null);
	}
	public GoBangPanel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public GoBangPanel(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//当游戏结束时，不执行点击事件
		if(mIsGameOver){
			return false;
		}
		int action = event.getAction();
		//之所以用up不用down是为了当上一级控件是scrollView这样的控件时，由它来处理；否则会出现滑动时落子的情况-
		if(action == MotionEvent.ACTION_UP){
			
			float x = event.getX();
			float y = event.getY();
			
			Point point = getValidPoint(x, y);
			
			//当黑白双方的步子集合中已存在这个坐标时，本次点击事件不处理
			if(mWhiteSteps.contains(point) || mBlackSteps.contains(point)){
				return false;
			}
			
			//哪个子走棋，则将它放入对应的集合中
			if(mIsWhiteGo){
				mWhiteSteps.push(point);
			}else{
				mBlackSteps.push(point);
			}
			
			invalidate();
			
			//检测是否游戏结束
			GoBangUtils.isGameOver(this);
			
			mIsWhiteGo = !mIsWhiteGo;
		}
		//这里需要返回true，即告诉父控件这块区域的点击事件由我处理。这样才可获得ACTION_UP
		return true;
	}

}
