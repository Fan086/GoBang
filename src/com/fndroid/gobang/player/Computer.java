package com.fndroid.gobang.player;

import java.util.LinkedList;
import java.util.Random;

import com.fndroid.gobang.panel.BaseGoBangPanel;
import com.fndroid.gobang.panel.SmartGoBangPanel;
import com.fndroid.gobang.utils.GoBangUtils;

import android.graphics.Point;

public class Computer extends Player{
	private Random random;
	private SmartGoBangPanel panel;
	public Computer(BaseGoBangPanel panel,boolean colorFlag){
		this("默认电脑", panel ,colorFlag);
	}
	public Computer(String name, BaseGoBangPanel panel, boolean colorFlag) {
		super(name, panel, colorFlag);
		this.panel = (SmartGoBangPanel) panel;
		random = new Random();
	}
	public void goPiece(){
		if(panel.isHumanGo || panel.mIsGameOver){
			return;
		}
		//先获得一个随机下棋的电脑
		//获得棋盘的行数与列数
		int lineNum = BaseGoBangPanel.mLineNum;
		
		int x = random.nextInt(lineNum);
		int y = random.nextInt(lineNum);
		Point point = new Point(x, y);
		
		LinkedList<Point> humanSteps = panel.humanSteps;
		LinkedList<Point> computerSteps = panel.computerSteps;
		
		if(!humanSteps.contains(point) && !computerSteps.contains(point)){
			computerSteps.push(point);
			panel.invalidate();
			
			GoBangUtils.isGameOver(panel);
			
			panel.isHumanGo = !panel.isHumanGo;
			
		}else{
			goPiece();
		}
	}
}
