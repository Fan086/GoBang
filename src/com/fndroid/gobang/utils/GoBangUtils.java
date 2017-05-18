package com.fndroid.gobang.utils;

import static com.fndroid.gobang.utils.Orientation.HORIZONTAL;
import static com.fndroid.gobang.utils.Orientation.LEFT_DIAGONAL;
import static com.fndroid.gobang.utils.Orientation.RIGHT_DIAGONAL;
import static com.fndroid.gobang.utils.Orientation.VERTICAL;

import java.util.LinkedList;

import com.fndroid.gobang.panel.BaseGoBangPanel;
import com.fndroid.gobang.panel.GoBangPanel;
import com.fndroid.gobang.panel.SmartGoBangPanel;
import com.fndroid.gobang.player.Computer;

import android.app.AlertDialog;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;


public class GoBangUtils {
	private static boolean isWhiteWin;
	private static boolean isBlackWin;
	private static boolean isDoubleWin;
	private static Handler handler = new Handler(Looper.getMainLooper()){
		public void handleMessage(Message msg) {
			AlertDialog dialog = (AlertDialog) msg.obj;
			dialog.show();
		};
	};
	/**
	 * 再来一局
	 */
	public static void restart(BaseGoBangPanel panel){
		
		panel.mIsGameOver = false;
		
		LinkedList<Point> blackSteps = panel.blackPlayer.getSteps();
		LinkedList<Point> whiteSteps = panel.whitePlayer.getSteps();
		
		blackSteps.clear();
		whiteSteps.clear();
		
		if(panel instanceof GoBangPanel){
			
			panel.mIsWhiteGo = true;
			panel.invalidate();
		//当处于人机对战的情况下，如果电脑先手，则让它重新落子，否则，将栈清空，并重新绘制界面	
		}else if(panel instanceof SmartGoBangPanel){
			SmartGoBangPanel smartPanel = (SmartGoBangPanel)panel;
			
			if(!smartPanel.isHumanFirst){
				smartPanel.isHumanGo = false;
				Computer computer = (Computer) smartPanel.whitePlayer;
				//TODO:觉得有问题
				computerGo(computer);
			}else{
				smartPanel.isHumanGo = true;
				panel.invalidate();
			}
		}
	}
	private static void computerGo(Computer computer) {
		synchronized(computer){
			computer.notify();
		}
//		computerPlayer.goPiece();
	}
	/**
	 * 悔棋的操作
	 */
	public static void regret(BaseGoBangPanel panel){
		LinkedList<Point> whiteSteps = panel.whitePlayer.getSteps();
		LinkedList<Point> blackSteps = panel.blackPlayer.getSteps();
		
		//当游戏已经结束后点击悔棋时，需要将标志位取false，
		//并且对于人机模式，要根据人类的先后手顺序，进行特殊处理（胜利时，玩家会多走一步棋）
		if(panel.mIsGameOver){
			
			panel.mIsGameOver = false;
			
			if(panel instanceof SmartGoBangPanel){
				SmartGoBangPanel smartPanel = (SmartGoBangPanel) panel;
				if(isWhiteWin){
					whiteSteps.pop();
				}else if(isBlackWin){
					blackSteps.pop();
					whiteSteps.pop();
				}
				
				smartPanel.invalidate();
				
				return;
			}
		}
		//双人游戏的悔棋规则：每人悔一步
		if(panel instanceof GoBangPanel){
			
			if(whiteSteps.isEmpty() && blackSteps.isEmpty()){
				return;
			}
			//回退到上一个下子的一方
			panel.mIsWhiteGo = !panel.mIsWhiteGo;
			if(panel.mIsWhiteGo){
				whiteSteps.pop();
			}else{
				blackSteps.pop();
			}
		//人机对战的悔棋规则：悔到玩家下子的那一步，当先手是电脑时，即白子是电脑时，留下电脑下的那颗棋子
		}else if(panel instanceof SmartGoBangPanel){
			SmartGoBangPanel smartPanel = (SmartGoBangPanel) panel;
			
			//当电脑先手时，只剩一颗子的情况下无法悔棋；当玩家先手时，棋盘上没有子时，无法悔棋
			if((!smartPanel.isHumanFirst && whiteSteps.size() == 1) ||
					(smartPanel.isHumanFirst && whiteSteps.isEmpty() && blackSteps.isEmpty())
					){
				return;
			}
			blackSteps.pop();
			whiteSteps.pop();
			
			//悔棋之后，总是轮到用户先下棋
			smartPanel.isHumanGo = true;
		}
		
		panel.invalidate();
	}
	
	/**
	 * 判断是否游戏结束
	 * @return true表示游戏结束
	 */
	public static boolean isGameOver(BaseGoBangPanel panel){
		AlertDialog dialog = panel.dialog;
		
		//检测是否白棋获胜，或者黑棋获胜，或者和棋，当满足条件时，游戏结束
		isWhiteWin = checkIsInLine(panel.whitePlayer.getSteps());
		isBlackWin = checkIsInLine(panel.blackPlayer.getSteps());
		isDoubleWin = checkIsDoubleWin(panel);
		
		
		if(isWhiteWin){
			panel.mIsGameOver = true;
			dialog.setMessage("白棋获胜，游戏结束");
			Message msg = new Message();
			msg.obj = dialog;
			handler.sendMessage(msg);
//			dialog.show();
			return true;
		}else if(isBlackWin){
			panel.mIsGameOver = true;
			dialog.setMessage("黑棋获胜，游戏结束");
			Message msg = new Message();
			msg.obj = dialog;
			handler.sendMessage(msg);
//			dialog.show();
			return true;
		}else if(isDoubleWin){
			panel.mIsGameOver = true;
			dialog.setMessage("平局，游戏结束");
			Message msg = new Message();
			msg.obj = dialog;
			handler.sendMessage(msg);
//			dialog.show();
			return true;
		}
		
		return false;
	}

	/**
	 * 检测是否是平局,当所有落子点都被下完还没有胜负时，判断为平局
	 * @return true表示是
	 */
	private static boolean checkIsDoubleWin(BaseGoBangPanel panel) {
		int lineNum = BaseGoBangPanel.getLineNum();
		int allUnit = lineNum * lineNum;
		LinkedList<Point> mBlackSteps = panel.blackPlayer.getSteps();
		LinkedList<Point> mWhiteSteps = panel.whitePlayer.getSteps();
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
		boolean isHorizontalInLine = checkIsInLineByOrientation(point, points, HORIZONTAL);
		boolean isVerticalInLine = checkIsInLineByOrientation(point, points, VERTICAL);
		boolean isLeftDiagonalInLine = checkIsInLineByOrientation(point, points, LEFT_DIAGONAL);
		boolean isRightDiagonalInLine = checkIsInLineByOrientation(point, points, RIGHT_DIAGONAL);
		
		if(isHorizontalInLine || isVerticalInLine || isLeftDiagonalInLine || isRightDiagonalInLine){
			return true;
		}
		
		return false;
	}

	/**
	 * 根据方向，来检测所在方向上同颜色的棋子是否已经连城线
	 * @param point 最新下的那个棋子
	 * @param points 这个棋子的所有情况
	 * @param orientation 方向
	 * @return 是否已经练成线
	 */
	private static boolean checkIsInLineByOrientation(Point point, LinkedList<Point> points, Orientation orientation) {
		//初值为1，代表当前已经有一个棋子
		int cnt = 1;
		int x = point.x;
		int y = point.y;
		
		Point tmpPoint = new Point();
		//上半部分
		for(int i = 1; i < BaseGoBangPanel.MAX_PIECES; ++i){
			
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
		for(int i = 1; i < BaseGoBangPanel.MAX_PIECES; ++i){
			
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
		
		if(cnt >= BaseGoBangPanel.MAX_PIECES){
			return true;
		}
		
		return false;
	}
	
	/**
	 * 检测point是否越界
	 * @return true代表越界了
	 */
	public static boolean checkIndexOutOfBoundary(Point point){
		return point.x < 0 || point.y < 0 ||
				point.x >= BaseGoBangPanel.getLineNum() ||
				point.y >= BaseGoBangPanel.getLineNum();
	}
}
