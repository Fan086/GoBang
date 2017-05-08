package com.fndroid.gobang.player;

import java.util.LinkedList;

import com.fndroid.gobang.panel.BaseGoBangPanel;

import android.graphics.Point;

public abstract class Player {
	
	private String name;
	/**
	 * 颜色标记，true表示白色，false表示黑色，白色先走棋
	 */
	private boolean colorFlag;
	/**
	 * 棋盘对象
	 */
	private BaseGoBangPanel panel;
	
	/**
	 * 当前玩家所走的棋子
	 */
	private LinkedList<Point> steps;
	
	
	public Player(String name, BaseGoBangPanel panel, boolean colorFlag){
		this.name = name;
		this.panel = panel;
		this.colorFlag = colorFlag;
		
		steps = new LinkedList<Point>();
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public boolean isColorFlag() {
		return colorFlag;
	}


	public void setColorFlag(boolean colorFlag) {
		this.colorFlag = colorFlag;
	}


	public BaseGoBangPanel getPanel() {
		return panel;
	}


	public void setPanel(BaseGoBangPanel panel) {
		this.panel = panel;
	}


	public LinkedList<Point> getSteps() {
		return steps;
	}


	public void setSteps(LinkedList<Point> steps) {
		this.steps = steps;
	}
	
	
}
