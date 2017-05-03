package com.fndroid.gobang;

import java.util.LinkedList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;
import android.widget.Toast;


public class GoBangUtils {
	
	//四个方向的常量
	enum Orientation{
		HORIZONTAL, VERTICAL, LEFT_DIAGONAL, RIGHT_DIAGONAL
	}
	/**
	 * 再来一局
	 */
	public static void restart(GoBangPanel panel){
		LinkedList<Point> blackSteps = panel.mBlackSteps;
		LinkedList<Point> whiteSteps = panel.mWhiteSteps;
		
		blackSteps.clear();
		whiteSteps.clear();
		
		panel.mIsGameOver = false;
		
		panel.invalidate();
		
		panel.mIsWhiteGo = true;
	}
	
	/**
	 * 悔棋的操作
	 */
	public static void regret(GoBangPanel panel){
		LinkedList<Point> whiteSteps = panel.mWhiteSteps;
		LinkedList<Point> blackSteps = panel.mBlackSteps;
		
		if(whiteSteps.isEmpty() && blackSteps.isEmpty()){
			return;
		}
		//当游戏还没结束时，可以悔棋
		if(panel.mIsGameOver){
			panel.mIsGameOver = false;
		}
			//回退到上一个下子的一方
			panel.mIsWhiteGo = !panel.mIsWhiteGo;
			if(panel.mIsWhiteGo){
				whiteSteps.pop();
			}else{
				blackSteps.pop();
			}
			
			panel.invalidate();
//		}
	}
	
	/**
	 * 判断是否游戏结束
	 * @return true表示游戏结束
	 */
	public static boolean isGameOver(GoBangPanel panel){
		AlertDialog dialog = panel.dialog;
		
		//检测是否白棋获胜，或者黑棋获胜，或者和棋，当满足条件时，游戏结束
		boolean isWhiteWin = checkIsInLine(panel.mWhiteSteps);
		boolean isBlackWin = checkIsInLine(panel.mBlackSteps);
		boolean isDoubleWin = checkIsDoubleWin(panel);
		if(isWhiteWin){
			panel.mIsGameOver = true;
			dialog.setMessage("白棋获胜，游戏结束");
			dialog.show();
			return true;
		}else if(isBlackWin){
			panel.mIsGameOver = true;
			dialog.setMessage("黑棋获胜，游戏结束");
			dialog.show();
			return true;
		}else if(isDoubleWin){
			panel.mIsGameOver = true;
			dialog.setMessage("平局，游戏结束");
			dialog.show();
			return true;
		}
		return false;
	}

	/**
	 * 检测是否是平局,当所有落子点都被下完还没有胜负时，判断为平局
	 * @return true表示是
	 */
	private static boolean checkIsDoubleWin(GoBangPanel panel) {
		int lineNum = GoBangPanel.LINE_NUM;
		int allUnit = lineNum * lineNum;
		LinkedList<Point> mBlackSteps = panel.mBlackSteps;
		LinkedList<Point> mWhiteSteps = panel.mWhiteSteps;
		return mBlackSteps.size() + mWhiteSteps.size() == allUnit;
	}

	/**
	 * 检测是否已经五子连珠。检测规则：取出最后一个放入集合中的point，判断它的横向，竖向，左斜，右斜是否构成五子连珠
	 */
	private static boolean checkIsInLine(LinkedList<Point> points) {
		Point point = points.peek();
		if(point == null){
			return false;
		}
		boolean isHorizontalInLine = checkIsInLineByOrientation(point, points, Orientation.HORIZONTAL);
		boolean isVerticalInLine = checkIsInLineByOrientation(point, points, Orientation.VERTICAL);
		boolean isLeftDiagonalInLine = checkIsInLineByOrientation(point, points, Orientation.LEFT_DIAGONAL);
		boolean isRightDiagonalInLine = checkIsInLineByOrientation(point, points, Orientation.RIGHT_DIAGONAL);
		
		if(isHorizontalInLine || isVerticalInLine || isLeftDiagonalInLine || isRightDiagonalInLine){
			return true;
		}
		
		return false;
	}

	private static boolean checkIsInLineByOrientation(Point point, LinkedList<Point> points, Orientation orientation) {
		int cnt = 1;
		int x = point.x;
		int y = point.y;
		
		Point tmpPoint = new Point();
		//上半部分
		for(int i = 1; i < GoBangPanel.MAX_PIECES; ++i){
			
			//根据方向来取不同的坐标点
			switch(orientation){
			case HORIZONTAL:
				tmpPoint.set(x - i, y);
				break;
			case VERTICAL:
				tmpPoint.set(x, y - i);
				break;
			case LEFT_DIAGONAL:
				tmpPoint.set(x - i, y - i);
				break;
			case RIGHT_DIAGONAL:
				tmpPoint.set(x + i, y - i);
				break;
			}
			
			//判断集合中是否包含此坐标，不包含时，结束本次循环
			if(points.contains(tmpPoint)){
				++cnt;
			}else{
				break;
			}
		}
		//下半部分
		for(int i = 1; i < GoBangPanel.MAX_PIECES; ++i){
			
			//根据方向来取不同的坐标点
			switch(orientation){
			case HORIZONTAL:
				tmpPoint.set(x + i, y);
				break;
			case VERTICAL:
				tmpPoint.set(x, y + i);
				break;
			case LEFT_DIAGONAL:
				tmpPoint.set(x + i, y + i);
				break;
			case RIGHT_DIAGONAL:
				tmpPoint.set(x - i, y + i);
				break;
			}
			
			//判断集合中是否包含此坐标，不包含时，结束本次循环
			if(points.contains(tmpPoint)){
				++cnt;
			}else{
				break;
			}
		}
		
		if(cnt == GoBangPanel.MAX_PIECES){
			return true;
		}
		
		return false;
	}
}
