package com.fndroid.gobang;

import java.util.LinkedList;
import java.util.Random;

import com.fndroid.gobang.panel.BaseGoBangPanel;

import android.graphics.Point;

public class Computer4GoBang {
	private String name;
	private BaseGoBangPanel panel;
	private Random random;
	
	public Computer4GoBang(BaseGoBangPanel panel){
		this("默认电脑", panel);
	}
	
	public Computer4GoBang(String name, BaseGoBangPanel panel){
		this.name = name;
		this.panel = panel;
		
		random = new Random();
	}
	
	public boolean goPiece(){
		//先获得一个随机下棋的电脑
		//获得棋盘的行数与列数
		int lineNum = panel.getLineNum();
		
		int x = random.nextInt(lineNum);
		int y = random.nextInt(lineNum);
		Point point = new Point(x, y);
		
//		LinkedList<Point> whiteSteps = panel.mWhiteSteps;
//		LinkedList<Point> blackSteps = panel.mBlackSteps;
//		
//		if(!whiteSteps.contains(point) && !blackSteps.contains(point)){
//			
//			return true;
//		}
		
		return false;
	}
	
	public BaseGoBangPanel getPanel() {
		return panel;
	}

	public void setPanel(BaseGoBangPanel panel) {
		this.panel = panel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
