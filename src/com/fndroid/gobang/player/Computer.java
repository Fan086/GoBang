package com.fndroid.gobang.player;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.Random;

import com.fndroid.gobang.panel.BaseGoBangPanel;
import com.fndroid.gobang.panel.SmartGoBangPanel;
import com.fndroid.gobang.utils.GoBangUtils;
import com.fndroid.gobang.utils.Orientation;
import static com.fndroid.gobang.utils.Orientation.*;
import android.graphics.Point;
import android.os.SystemClock;

public class Computer extends Player{
	private Random random;
	private SmartGoBangPanel panel;
	private Brain brain;
	/**
	 * 递归次数
	 */
	private int recursionTime;
	
	
	public Computer(BaseGoBangPanel panel,boolean colorFlag){
		this("默认电脑", panel ,colorFlag);
	}
	public Computer(String name, BaseGoBangPanel panel, boolean colorFlag) {
		super(name, panel, colorFlag);
		this.panel = (SmartGoBangPanel) panel;
		random = new Random();
		brain = new Brain();
	}
	/**
	 * 电脑下子的方法，下子的优先级：
	 * 当人类或者电脑出现四字相连，或者202,301,103这样的情况时优先级最高
	 * 	四字相连则拦截两端，其他情况堵住缺口
	 * 当人类或电脑出现三子相连的情况，或者201，102这样的情况时，优先级第二
	 * 	三子相连拦截两端，其他情况堵缺口
	 * 当电脑出现俩子相连，或者101的情况时（俩子先不管人类怎么下），优先级第三
	 * 	俩子相连补两端，101补中间
	 * 当电脑出现单独一个子的时候，下在那个子的四周
	 * 当没有下子的时候，把子落在棋盘的中间区域
	 */
	public synchronized void goPiece() throws ConcurrentModificationException, StackOverflowError{
		if(panel.isHumanGo || panel.mIsGameOver){
			return;
		}
//		SystemClock.sleep(1000);
		
		LinkedList<Point> humanSteps = panel.humanSteps;
		LinkedList<Point> computerSteps = panel.computerSteps;
		
		Point point = null;
		//传入自己，代表先进攻，传入人类，代表后防守
		//检测电脑冲5的情况
		if(brain.checkGotoFiveInLine(this)){
			checkOverAndUpdateUI();
		//检测人类冲5的情况
		}else if(brain.checkGotoFiveInLine(panel.humanPlayer)){
			checkOverAndUpdateUI();
		//检测电脑活3的情况
		}else if(brain.checkComputerLiveThree(this)){
			checkOverAndUpdateUI();
		//检测电脑活201的情况，在只考虑一步的情况下，与半活的201没什么区别，不考虑放两边的情况
		}else if(brain.checkComputerLiveTwoOne(this)){
			checkOverAndUpdateUI();
			
		//检测人类活三的情况
		}else if(brain.checkHumanLiveThree((Human) panel.humanPlayer)){
			checkOverAndUpdateUI();
		//检测人类活201的情况
		}else if(brain.checkHumanLiveTwoOne((Human) panel.humanPlayer)){
			checkOverAndUpdateUI();
			
		//检测电脑半活三的情况，可以用来冲4
		}else if(brain.checkComputerHalfLiveThree(this)){
			checkOverAndUpdateUI();
			//检测电脑半活201的情况，可以用来冲4
		}else if(brain.checkComputerHalfLiveTwoOne(this)){
			checkOverAndUpdateUI();
		//检测电脑活二的情况
		}else if(brain.checkComputerLiveTwo(this)){
			checkOverAndUpdateUI();
			//检测电脑活101的情况,如果有，则将棋子放入中间
		}else if(brain.checkComputerLiveOneOne(this)){
			checkOverAndUpdateUI();
			//检测电脑活1的情况
		}else if(brain.checkComputerLiveOne(this)){
			checkOverAndUpdateUI();
		//中间区域随机落子
		}else if(recursionTime < 15){
			point = brain.centerBlockGo();
			if(!humanSteps.contains(point) && !computerSteps.contains(point)){
				System.out.println("在中间区域落子" + point);
				computerSteps.push(point);
				checkOverAndUpdateUI();
			}else{
				++recursionTime;
				goPiece();
				//这里必须要加return，否则当进入这里的时候，代表发生了递归，如果没有return，则即使notify了这一次，上一次的调用还是会锁住（进入wait）
				return;
			}
		//随机落子
		}else{
			point = brain.randomGo();
			if(!humanSteps.contains(point) && !computerSteps.contains(point)){
				System.out.println("随机落子" + point);
				computerSteps.push(point);
				checkOverAndUpdateUI();
			}else{
				goPiece();
				return;
			}
		}
		
		//让所在线程进入等待状态，等待用户下完棋之后唤醒
		try {
			wait();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private void checkOverAndUpdateUI() {
		recursionTime = 0;
		panel.isHumanGo = !panel.isHumanGo;
		GoBangUtils.isGameOver(panel);
		panel.post(new Runnable(){
			@Override
			public void run() {
				panel.invalidate();
			}
		});
	}
	
	public void goPiece2(){
		if(panel.isHumanGo || panel.mIsGameOver){
			return;
		}
		
		LinkedList<Point> humanSteps = panel.humanSteps;
		LinkedList<Point> computerSteps = panel.computerSteps;
		
		Point point = brain.centerBlockGo();
		
		if(!humanSteps.contains(point) && !computerSteps.contains(point)){
			computerSteps.push(point);
			panel.invalidate();
			
			GoBangUtils.isGameOver(panel);
			
			panel.isHumanGo = !panel.isHumanGo;
			
		}else{
			goPiece();
		}
	}

	private class Brain{
		
		private int centerRange = 2;
		private int lineNum;
		
		public Brain(){
			lineNum = BaseGoBangPanel.getLineNum();
			
			if(lineNum < 12){
				centerRange = 2;
			}else if(lineNum < 14){
				centerRange = 3;
			}else if(lineNum < 16){
				centerRange = 4;
			}else if(lineNum < 18){
				centerRange = 5;
			}else{
				centerRange = 6;
			}
		}
		/**
		 * 随机落子
		 */
		public Point randomGo(){
			
			int x = random.nextInt(lineNum);
			int y = random.nextInt(lineNum);
			Point point = new Point(x, y);
			
			return point;
		}
		
		/**
		 * 检测五连的可能性,true表示可以连城5连
		 */
		public boolean checkComputerPosibleToFive(Point point, Orientation orientation){
			LinkedList<Point> humanSteps = panel.humanSteps;
			LinkedList<Point> computerSteps = panel.computerSteps;
			
			//用于记录当前棋子指定方向成为五连的可能性
			int potential = 1;
			int x = point.x;
			int y = point.y;
			
			Point tmpPoint = new Point();
			
			//从左边开始计数

			switch(orientation){
			case HORIZONTAL:
				for(int i = x - 1; i >= 0; --i){
					tmpPoint.set(i, y);
					
					if(humanSteps.contains(tmpPoint)){
						break;
					}else{
						++potential;
					}
				}
				break;
			case VERTICAL:
				for(int j = y - 1; j >= 0; --j){
					tmpPoint.set(x, j);
					
					if(humanSteps.contains(tmpPoint)){
						break;
					}else{
						++potential;
					}
				}
				break;
			case LEFT_DIAGONAL:
				for(int i = x - 1,j = y - 1; i >= 0 && j >= 0; --i, --j){
					tmpPoint.set(i, j);
					
					if(humanSteps.contains(tmpPoint)){
						break;
					}else{
						++potential;
					}
				}
				break;
			case RIGHT_DIAGONAL:
				for(int i = x + 1,j = y - 1; i < BaseGoBangPanel.getLineNum() && j >= 0; ++i, --j){
					tmpPoint.set(i, j);
					
					if(humanSteps.contains(tmpPoint)){
						break;
					}else{
						++potential;
					}
				}
				break;
			}
			
			//在从右边数
			switch(orientation){
			case HORIZONTAL:
				for(int i = x + 1; i < BaseGoBangPanel.getLineNum(); ++i){
					tmpPoint.set(i, y);
					
					if(humanSteps.contains(tmpPoint)){
						break;
					}else{
						++potential;
					}
				}
				break;
			case VERTICAL:
				for(int j = y + 1; j < BaseGoBangPanel.getLineNum(); ++j){
					tmpPoint.set(x, j);
					
					if(humanSteps.contains(tmpPoint)){
						break;
					}else{
						++potential;
					}
				}
				break;
			case LEFT_DIAGONAL:
				for(int i = x + 1,j = y + 1; i < BaseGoBangPanel.getLineNum() && j < BaseGoBangPanel.getLineNum(); ++i, ++j){
					tmpPoint.set(i, j);
					
					if(humanSteps.contains(tmpPoint)){
						break;
					}else{
						++potential;
					}
				}
				break;
			case RIGHT_DIAGONAL:
				for(int i = x - 1,j = y + 1; i >= 0 && j < BaseGoBangPanel.getLineNum(); --i, ++j){
					tmpPoint.set(i, j);
					
					if(humanSteps.contains(tmpPoint)){
						break;
					}else{
						++potential;
					}
				}
				break;
			}
			
			if(potential >= 5){
				return true;
			}
			return false;
		}
		
		/**
		 * 检测电脑只有一子的情况
		 */
		public boolean checkComputerLiveOne(Computer computer){
			LinkedList<Point> computerSteps = computer.getSteps();
			
			for(Point point: computerSteps){
				boolean horizontal = checkComputerLiveOneByOrientation(point, HORIZONTAL);
				if(horizontal){
					return true;
				}
				
				boolean vertical = checkComputerLiveOneByOrientation(point, VERTICAL);
				if(vertical){
					return true;
				}
				
				boolean left_diagonal = checkComputerLiveOneByOrientation(point, LEFT_DIAGONAL);
				if(left_diagonal){
					return true;
				}
				
				boolean right_diagonal = checkComputerLiveOneByOrientation(point, RIGHT_DIAGONAL);
				if(right_diagonal){
					return true;
				}
			}
			return false;
		}
		private boolean checkComputerLiveOneByOrientation(Point point, Orientation orientation) {
			
			if(!checkComputerPosibleToFive(point, orientation)){
				return false;
			}
			
			LinkedList<Point> computerSteps = panel.computerSteps;
			LinkedList<Point> humanSteps = panel.humanSteps;
			
			int x = point.x;
			int y = point.y;
			
			Point leftPoint = null;
			Point tmpPoint = new Point();
			
			//以横向来说明，先检测它的左边时候有阻挡物（敌方有子或者边界的情况）
			switch(orientation){
			case HORIZONTAL:
				tmpPoint.set(x - 1, y);
				break;
			case VERTICAL:
				tmpPoint.set(x, y - 1);
				break;
			case LEFT_DIAGONAL:
				tmpPoint.set(x - 1, y - 1);
				break;
			case RIGHT_DIAGONAL:
				tmpPoint.set(x + 1, y - 1);
				break;
			}
			//如果左边被堵住，还需要看右边的情况；否则，用一个变量记住这个位置的前一个位置
			if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
				return false;
			}
			
			leftPoint = new Point(tmpPoint);
			int cnt = 1;
			
			switch(orientation){
			case HORIZONTAL:
				tmpPoint.set(x + 1, y);
				break;
			case VERTICAL:
				tmpPoint.set(x, y + 1);
				break;
			case LEFT_DIAGONAL:
				tmpPoint.set(x + 1, y + 1);
				break;
			case RIGHT_DIAGONAL:
				tmpPoint.set(x - 1, y + 1);
				break;
			}
			//如果右边被堵住，返回false
			if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
				return false;
			//否则，在左右两边，随机选择一个方向将其加入
			}else{
				computerSteps.push(random.nextBoolean() ? leftPoint : tmpPoint);
				return true;
			}
		}

		/**
		 * 检测电脑活101的情况，可以用来冲三或者201的情况。
		 */
		public boolean checkComputerLiveOneOne(Computer computer){
			LinkedList<Point> computerSteps = computer.getSteps();
			
			for(Point point: computerSteps){
				boolean horizontal = checkComputerLiveOneOneByOrientation(point, HORIZONTAL);
				if(horizontal){
					return true;
				}
				
				boolean vertical = checkComputerLiveOneOneByOrientation(point, VERTICAL);
				if(vertical){
					return true;
				}
				
				boolean left_diagonal = checkComputerLiveOneOneByOrientation(point, LEFT_DIAGONAL);
				if(left_diagonal){
					return true;
				}
				
				boolean right_diagonal = checkComputerLiveOneOneByOrientation(point, RIGHT_DIAGONAL);
				if(right_diagonal){
					return true;
				}
			}
			return false;
		}
		
		
		private boolean checkComputerLiveOneOneByOrientation(Point point, Orientation orientation) {
			
			if(!checkComputerPosibleToFive(point, orientation)){
				return false;
			}
			
			boolean isLeftLive = true;
			
			LinkedList<Point> computerSteps = panel.computerSteps;
			LinkedList<Point> humanSteps = panel.humanSteps;
			
			int x = point.x;
			int y = point.y;
			
			Point centerPoint = null;
			Point tmpPoint = new Point();
			Point rightPoint = null;
			
			//以横向来说明，先检测它的左边时候有阻挡物（敌方有子或者边界的情况）
			switch(orientation){
			case HORIZONTAL:
				tmpPoint.set(x - 1, y);
				break;
			case VERTICAL:
				tmpPoint.set(x, y - 1);
				break;
			case LEFT_DIAGONAL:
				tmpPoint.set(x - 1, y - 1);
				break;
			case RIGHT_DIAGONAL:
				tmpPoint.set(x + 1, y - 1);
				break;
			}
			//如果左边被堵住，不用在考虑下面了
			if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
				return false;
			}
			
			int cnt = 1;
			
			for(int i = 1; i < 3; ++i){
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
				
				if(computerSteps.contains(tmpPoint)){
					++cnt;
				}else{
					//当右边被堵时，此时数量不满足要求，返回false；否则代表还没有下子，用一个引用指向这个位置
					if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint)){
						return false;
					}else{
						centerPoint = new Point(tmpPoint);
					}
				}
			}
			
			if(cnt == 2){
				switch(orientation){
				case HORIZONTAL:
					tmpPoint.set(x + 3, y);
					break;
				case VERTICAL:
					tmpPoint.set(x, y + 3);
					break;
				case LEFT_DIAGONAL:
					tmpPoint.set(x + 3, y + 3);
					break;
				case RIGHT_DIAGONAL:
					tmpPoint.set(x - 3, y + 3);
					break;
				}
				//如果右边被堵住，查看左边是否被堵住，若是，说明已经没有发展，返回false；否则，将中间位置加入步子中
				if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
					return false;
				//否则，右边没被堵住，可以将中间放入
				}else{
					computerSteps.push(centerPoint);
					return true;
				}
			}
			return false;
		}

		/**
		 * TODO:检测电脑活二，可以用来冲活三或者201，这里用来冲活三
		 */
		public boolean checkComputerLiveTwo(Computer computer){
			LinkedList<Point> computerSteps = computer.getSteps();
			
			for(Point point: computerSteps){
				boolean horizontal = checkComputerLiveTwoByOrientation(point, HORIZONTAL);
				if(horizontal){
					return true;
				}
				
				boolean vertical = checkComputerLiveTwoByOrientation(point, VERTICAL);
				if(vertical){
					return true;
				}
				
				boolean left_diagonal = checkComputerLiveTwoByOrientation(point, LEFT_DIAGONAL);
				if(left_diagonal){
					return true;
				}
				
				boolean right_diagonal = checkComputerLiveTwoByOrientation(point, RIGHT_DIAGONAL);
				if(right_diagonal){
					return true;
				}
			}
			return false;
		}
		
		private boolean checkComputerLiveTwoByOrientation(Point point, Orientation orientation) {
			
			if(!checkComputerPosibleToFive(point, orientation)){
				return false;
			}
			
			LinkedList<Point> computerSteps = panel.computerSteps;
			LinkedList<Point> humanSteps = panel.humanSteps;
			
			int x = point.x;
			int y = point.y;
			
			Point leftPoint = null;
			Point tmpPoint = new Point();
			
			//以横向来说明，先检测它的左边时候有阻挡物（敌方有子或者边界的情况）
			switch(orientation){
			case HORIZONTAL:
				tmpPoint.set(x - 1, y);
				break;
			case VERTICAL:
				tmpPoint.set(x, y - 1);
				break;
			case LEFT_DIAGONAL:
				tmpPoint.set(x - 1, y - 1);
				break;
			case RIGHT_DIAGONAL:
				tmpPoint.set(x + 1, y - 1);
				break;
			}
			//如果左边被堵住，还需要看右边的情况；否则，用一个变量记住这个位置的前一个位置
			if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
				return false;
			}
			
			leftPoint = new Point(tmpPoint);
			int cnt = 1;
			
			for(int i = 1; i < 2; ++i){
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
				
				if(computerSteps.contains(tmpPoint)){
					++cnt;
				}else{
					return false;
				}
			}
			
			if(cnt == 2){
				switch(orientation){
				case HORIZONTAL:
					tmpPoint.set(x + 2, y);
					break;
				case VERTICAL:
					tmpPoint.set(x, y + 2);
					break;
				case LEFT_DIAGONAL:
					tmpPoint.set(x + 2, y + 2);
					break;
				case RIGHT_DIAGONAL:
					tmpPoint.set(x - 2, y + 2);
					break;
				}
				//如果右边被堵住，返回false
				if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
					return false;
				//否则，在左右两边，随机选择一个方向将其加入
				}else{
					computerSteps.push(random.nextBoolean() ? leftPoint : tmpPoint);
					return true;
				}
			}
			return false;
		}

		/**
		 * 检测电脑半活201的情况，即左右两边有一边有东西围堵，这个与半活三一样只需检测电脑冲4的情况，人类的不用考虑
		 */
		public boolean checkComputerHalfLiveTwoOne(Computer computer){
			LinkedList<Point> computerSteps = computer.getSteps();
			
			for(Point point: computerSteps){
				boolean horizontal = checkComputerHalfLiveTwoOneByOrientation(point, HORIZONTAL);
				if(horizontal){
					return true;
				}
				
				boolean vertical = checkComputerHalfLiveTwoOneByOrientation(point, VERTICAL);
				if(vertical){
					return true;
				}
				
				boolean left_diagonal = checkComputerHalfLiveTwoOneByOrientation(point, LEFT_DIAGONAL);
				if(left_diagonal){
					return true;
				}
				
				boolean right_diagonal = checkComputerHalfLiveTwoOneByOrientation(point, RIGHT_DIAGONAL);
				if(right_diagonal){
					return true;
				}
			}
			return false;
		}
		private boolean checkComputerHalfLiveTwoOneByOrientation(Point point, Orientation orientation) {
			
			if(!checkComputerPosibleToFive(point, orientation)){
				return false;
			}
			
			boolean isLeftLive = true;
			
			LinkedList<Point> computerSteps = panel.computerSteps;
			LinkedList<Point> humanSteps = panel.humanSteps;
			
			int x = point.x;
			int y = point.y;
			
			Point centerPoint = null;
			Point tmpPoint = new Point();
			Point rightPoint = null;
			
			//以横向来说明，先检测它的左边时候有阻挡物（敌方有子或者边界的情况）
			switch(orientation){
			case HORIZONTAL:
				tmpPoint.set(x - 1, y);
				break;
			case VERTICAL:
				tmpPoint.set(x, y - 1);
				break;
			case LEFT_DIAGONAL:
				tmpPoint.set(x - 1, y - 1);
				break;
			case RIGHT_DIAGONAL:
				tmpPoint.set(x + 1, y - 1);
				break;
			}
			//如果左边被堵住，不用在考虑下面了
			if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
				isLeftLive = false;
			}else{
				isLeftLive = true;
			}
			
			int cnt = 1;
			
			for(int i = 1; i < 4; ++i){
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
				
				if(computerSteps.contains(tmpPoint)){
					++cnt;
				}else{
					//当右边被堵时，此时数量不满足要求，返回false；否则代表还没有下子，用一个引用指向这个位置
					if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint)){
						return false;
					}else{
						centerPoint = new Point(tmpPoint);
					}
				}
			}
			
			if(cnt == 3){
				switch(orientation){
				case HORIZONTAL:
					tmpPoint.set(x + 4, y);
					break;
				case VERTICAL:
					tmpPoint.set(x, y + 4);
					break;
				case LEFT_DIAGONAL:
					tmpPoint.set(x + 4, y + 4);
					break;
				case RIGHT_DIAGONAL:
					tmpPoint.set(x - 4, y + 4);
					break;
				}
				//如果右边被堵住，查看左边是否被堵住，若是，说明已经没有发展，返回false；否则，将中间位置加入步子中
				if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
					if(!isLeftLive){
						return false;
					}else{
						computerSteps.push(centerPoint);
						return true;
					}
				//否则，右边没被堵住，可以将中间放入
				}else{
					computerSteps.push(centerPoint);
					return true;
				}
			}
			return false;
		}
		/**
		 * 检测人类活201的情况
		 */
		public boolean checkHumanLiveTwoOne(Human human){
			LinkedList<Point> humanSteps = human.getSteps();
			
			for(Point point: humanSteps){
				boolean horizontal = checkHumanLiveTwoOneByOrientation(point, HORIZONTAL);
				if(horizontal){
					return true;
				}
				
				boolean vertical = checkHumanLiveTwoOneByOrientation(point, VERTICAL);
				if(vertical){
					return true;
				}
				
				boolean left_diagonal = checkHumanLiveTwoOneByOrientation(point, LEFT_DIAGONAL);
				if(left_diagonal){
					return true;
				}
				
				boolean right_diagonal = checkHumanLiveTwoOneByOrientation(point, RIGHT_DIAGONAL);
				if(right_diagonal){
					return true;
				}
			}
			return false;
		}
		
		private boolean checkHumanLiveTwoOneByOrientation(Point point, Orientation orientation) {
			LinkedList<Point> computerSteps = panel.computerSteps;
			LinkedList<Point> humanSteps = panel.humanSteps;
			
			int x = point.x;
			int y = point.y;
			
			Point centerPoint = null;
			Point tmpPoint = new Point();
			Point rightPoint = null;
			
			//以横向来说明，先检测它的左边时候有阻挡物（敌方有子或者边界的情况）
			switch(orientation){
			case HORIZONTAL:
				tmpPoint.set(x - 1, y);
				break;
			case VERTICAL:
				tmpPoint.set(x, y - 1);
				break;
			case LEFT_DIAGONAL:
				tmpPoint.set(x - 1, y - 1);
				break;
			case RIGHT_DIAGONAL:
				tmpPoint.set(x + 1, y - 1);
				break;
			}
			//如果左边被堵住，不用在考虑下面了
			if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
				return false;
			}
			
			int cnt = 1;
			
			for(int i = 1; i < 4; ++i){
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
				
				if(humanSteps.contains(tmpPoint)){
					++cnt;
				}else{
					if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || computerSteps.contains(tmpPoint)){
						return false;
					}else{
						centerPoint = new Point(tmpPoint);
					}
				}
			}
			
			if(cnt == 3){
				switch(orientation){
				case HORIZONTAL:
					tmpPoint.set(x + 4, y);
					break;
				case VERTICAL:
					tmpPoint.set(x, y + 4);
					break;
				case LEFT_DIAGONAL:
					tmpPoint.set(x + 4, y + 4);
					break;
				case RIGHT_DIAGONAL:
					tmpPoint.set(x - 4, y + 4);
					break;
				}
				//如果右边被堵住，返回false
				if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
					return false;
				//否则，在左右两边，随机选择一个方向将其加入
				}else{
					computerSteps.push(centerPoint);
					return true;
				}
			}
			return false;
		}

		/**
		 * TODO:检测电脑活201,102的情况，即两边没有受阻，目前只放中间，不考虑两边
		 */
		public boolean checkComputerLiveTwoOne(Computer computer){
			LinkedList<Point> computerSteps = computer.getSteps();
			
			for(Point point: computerSteps){
				boolean horizontal = checkComputerLiveTwoOneByOrientation(point, HORIZONTAL);
				if(horizontal){
					return true;
				}
				
				boolean vertical = checkComputerLiveTwoOneByOrientation(point, VERTICAL);
				if(vertical){
					return true;
				}
				
				boolean left_diagonal = checkComputerLiveTwoOneByOrientation(point, LEFT_DIAGONAL);
				if(left_diagonal){
					return true;
				}
				
				boolean right_diagonal = checkComputerLiveTwoOneByOrientation(point, RIGHT_DIAGONAL);
				if(right_diagonal){
					return true;
				}
			}
			return false;
		}
		
		private boolean checkComputerLiveTwoOneByOrientation(Point point, Orientation orientation) {
			
			if(!checkComputerPosibleToFive(point, orientation)){
				return false;
			}
			
			LinkedList<Point> computerSteps = panel.computerSteps;
			LinkedList<Point> humanSteps = panel.humanSteps;
			
			int x = point.x;
			int y = point.y;
			
			Point centerPoint = null;
			Point tmpPoint = new Point();
			Point rightPoint = null;
			
			//以横向来说明，先检测它的左边时候有阻挡物（敌方有子或者边界的情况）
			switch(orientation){
			case HORIZONTAL:
				tmpPoint.set(x - 1, y);
				break;
			case VERTICAL:
				tmpPoint.set(x, y - 1);
				break;
			case LEFT_DIAGONAL:
				tmpPoint.set(x - 1, y - 1);
				break;
			case RIGHT_DIAGONAL:
				tmpPoint.set(x + 1, y - 1);
				break;
			}
			//如果左边被堵住，不用在考虑下面了
			if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
				return false;
			}
			
			int cnt = 1;
			
			for(int i = 1; i < 4; ++i){
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
				
				if(computerSteps.contains(tmpPoint)){
					++cnt;
				}else{
					if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint)){
						return false;
					}else{
						centerPoint = new Point(tmpPoint);
					}
				}
			}
			
			if(cnt == 3){
				switch(orientation){
				case HORIZONTAL:
					tmpPoint.set(x + 4, y);
					break;
				case VERTICAL:
					tmpPoint.set(x, y + 4);
					break;
				case LEFT_DIAGONAL:
					tmpPoint.set(x + 4, y + 4);
					break;
				case RIGHT_DIAGONAL:
					tmpPoint.set(x - 4, y + 4);
					break;
				}
				//如果右边被堵住，返回false
				if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
					return false;
				//否则，在左右两边，随机选择一个方向将其加入
				}else{
					computerSteps.push(centerPoint);
					return true;
				}
			}
			return false;
		}

		/**
		 * 检测电脑半活三的情况，可以用来冲4
		 */
		public boolean checkComputerHalfLiveThree(Computer computer){
			LinkedList<Point> computerSteps = computer.getSteps();
			
			for(Point point: computerSteps){
				boolean horizontal = checkComputerHalfLiveThreeByOrientation(point, HORIZONTAL);
				if(horizontal){
					return true;
				}
				
				boolean vertical = checkComputerHalfLiveThreeByOrientation(point, VERTICAL);
				if(vertical){
					return true;
				}
				
				boolean left_diagonal = checkComputerHalfLiveThreeByOrientation(point, LEFT_DIAGONAL);
				if(left_diagonal){
					return true;
				}
				
				boolean right_diagonal = checkComputerHalfLiveThreeByOrientation(point, RIGHT_DIAGONAL);
				if(right_diagonal){
					return true;
				}
			}
			return false;
		}
		
		private boolean checkComputerHalfLiveThreeByOrientation(Point point, Orientation orientation) {
			
			if(!checkComputerPosibleToFive(point, orientation)){
				return false;
			}
			
			LinkedList<Point> computerSteps = panel.computerSteps;
			LinkedList<Point> humanSteps = panel.humanSteps;
			
			boolean isLeftLive = true;
			
			int x = point.x;
			int y = point.y;
			
			Point leftPoint = null;
			Point tmpPoint = new Point();
			
			//以横向来说明，先检测它的左边时候有阻挡物（敌方有子或者边界的情况）
			switch(orientation){
			case HORIZONTAL:
				tmpPoint.set(x - 1, y);
				break;
			case VERTICAL:
				tmpPoint.set(x, y - 1);
				break;
			case LEFT_DIAGONAL:
				tmpPoint.set(x - 1, y - 1);
				break;
			case RIGHT_DIAGONAL:
				tmpPoint.set(x + 1, y - 1);
				break;
			}
			//如果左边被堵住，还需要看右边的情况；否则，用一个变量记住这个位置
			if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
				isLeftLive = false;
			}else{
				isLeftLive = true;
				leftPoint = new Point(tmpPoint);
			}
			
			
			int cnt = 1;
			//用于记录最多可以形成多少连
//			TODO:在两边被堵住的情况下，没有判断是否有形成5连的可能int potential = 1;
			
			for(int i = 1; i < 3; ++i){
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
				
				if(computerSteps.contains(tmpPoint)){
					++cnt;
				}else{
					return false;
				}
			}
			
			if(cnt == 3){
				switch(orientation){
				case HORIZONTAL:
					tmpPoint.set(x + 3, y);
					break;
				case VERTICAL:
					tmpPoint.set(x, y + 3);
					break;
				case LEFT_DIAGONAL:
					tmpPoint.set(x + 3, y + 3);
					break;
				case RIGHT_DIAGONAL:
					tmpPoint.set(x - 3, y + 3);
					break;
				}
				//如果右边被堵住，判断左边是否被堵住，若是，返回false,否则，左边可以下子
				if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
					if(isLeftLive){
						computerSteps.push(leftPoint);
						return true;
					}
				//右边是活的，左边其实可以不用判断了
				}else{
					computerSteps.push(tmpPoint);
					return true;
				}
			}
			return false;
		}

		/**
		 * 检测人类活三的情况
		 */
		public boolean checkHumanLiveThree(Human human){
			LinkedList<Point> humanSteps = human.getSteps();
			
			for(Point point: humanSteps){
				boolean horizontal = checkHumanLiveThreeByOrientation(point, HORIZONTAL);
				if(horizontal){
					return true;
				}
				
				boolean vertical = checkHumanLiveThreeByOrientation(point, VERTICAL);
				if(vertical){
					return true;
				}
				
				boolean left_diagonal = checkHumanLiveThreeByOrientation(point, LEFT_DIAGONAL);
				if(left_diagonal){
					return true;
				}
				
				boolean right_diagonal = checkHumanLiveThreeByOrientation(point, RIGHT_DIAGONAL);
				if(right_diagonal){
					return true;
				}
			}
			return false;
		}
		
		private boolean checkHumanLiveThreeByOrientation(Point point, Orientation orientation) {
			LinkedList<Point> computerSteps = panel.computerSteps;
			LinkedList<Point> humanSteps = panel.humanSteps;
			
			int x = point.x;
			int y = point.y;
			
			Point leftPoint = null;
			Point tmpPoint = new Point();
			
			//以横向来说明，先检测它的左边时候有阻挡物（敌方有子或者边界的情况）
			switch(orientation){
			case HORIZONTAL:
				tmpPoint.set(x - 1, y);
				break;
			case VERTICAL:
				tmpPoint.set(x, y - 1);
				break;
			case LEFT_DIAGONAL:
				tmpPoint.set(x - 1, y - 1);
				break;
			case RIGHT_DIAGONAL:
				tmpPoint.set(x + 1, y - 1);
				break;
			}
			//如果左边被堵住，还需要看右边的情况；否则，用一个变量记住这个位置的前一个位置
			if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
				return false;
			}
			
			leftPoint = new Point(tmpPoint);
			int cnt = 1;
			
			for(int i = 1; i < 3; ++i){
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
				
				if(humanSteps.contains(tmpPoint)){
					++cnt;
				}else{
					return false;
				}
			}
			
			if(cnt == 3){
				switch(orientation){
				case HORIZONTAL:
					tmpPoint.set(x + 3, y);
					break;
				case VERTICAL:
					tmpPoint.set(x, y + 3);
					break;
				case LEFT_DIAGONAL:
					tmpPoint.set(x + 3, y + 3);
					break;
				case RIGHT_DIAGONAL:
					tmpPoint.set(x - 3, y + 3);
					break;
				}
				//如果右边被堵住，返回false
				if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
					return false;
				//否则，在左右两边，随机选择一个方向将其加入
				}else{
					computerSteps.push(random.nextBoolean() ? leftPoint : tmpPoint);
					return true;
				}
			}
			return false;
		}

		/**
		 * 检测电脑活三的情况
		 */
		public boolean checkComputerLiveThree(Computer computer){
			LinkedList<Point> computerSteps = computer.getSteps();
			
			for(Point point: computerSteps){
				boolean horizontal = checkComputerLiveThreeByOrientation(point, HORIZONTAL);
				if(horizontal){
					return true;
				}
				
				boolean vertical = checkComputerLiveThreeByOrientation(point, VERTICAL);
				if(vertical){
					return true;
				}
				
				boolean left_diagonal = checkComputerLiveThreeByOrientation(point, LEFT_DIAGONAL);
				if(left_diagonal){
					return true;
				}
				
				boolean right_diagonal = checkComputerLiveThreeByOrientation(point, RIGHT_DIAGONAL);
				if(right_diagonal){
					return true;
				}
			}
			return false;
		}
		private boolean checkComputerLiveThreeByOrientation(Point point, Orientation orientation) {
			
			if(!checkComputerPosibleToFive(point, orientation)){
				return false;
			}
			
			LinkedList<Point> computerSteps = panel.computerSteps;
			LinkedList<Point> humanSteps = panel.humanSteps;
			
			int x = point.x;
			int y = point.y;
			
			Point leftPoint = null;
			Point tmpPoint = new Point();
			
			//以横向来说明，先检测它的左边时候有阻挡物（敌方有子或者边界的情况）
			switch(orientation){
			case HORIZONTAL:
				tmpPoint.set(x - 1, y);
				break;
			case VERTICAL:
				tmpPoint.set(x, y - 1);
				break;
			case LEFT_DIAGONAL:
				tmpPoint.set(x - 1, y - 1);
				break;
			case RIGHT_DIAGONAL:
				tmpPoint.set(x + 1, y - 1);
				break;
			}
			//如果左边被堵住，还需要看右边的情况；否则，用一个变量记住这个位置的前一个位置
			if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
				return false;
			}
			
			leftPoint = new Point(tmpPoint);
			int cnt = 1;
			
			for(int i = 1; i < 3; ++i){
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
				
				if(computerSteps.contains(tmpPoint)){
					++cnt;
				}else{
					return false;
				}
			}
			
			if(cnt == 3){
				switch(orientation){
				case HORIZONTAL:
					tmpPoint.set(x + 3, y);
					break;
				case VERTICAL:
					tmpPoint.set(x, y + 3);
					break;
				case LEFT_DIAGONAL:
					tmpPoint.set(x + 3, y + 3);
					break;
				case RIGHT_DIAGONAL:
					tmpPoint.set(x - 3, y + 3);
					break;
				}
				//如果右边被堵住，返回false
				if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint)){
					return false;
				//否则，在左右两边，随机选择一个方向将其加入
				}else{
					computerSteps.push(random.nextBoolean() ? leftPoint : tmpPoint);
					return true;
				}
			}
			return false;
		}

		/**
		 * 冲4的情况
		 * @param player 待检测的人类或电脑
		 * @return
		 */
		public boolean checkGotoFourInLine(Player player) {
			//电脑优先进攻，再防守
			if(player instanceof Computer){
				LinkedList<Point> computerSteps = panel.computerSteps;
				
				for(Point point: computerSteps){
					
					boolean horizontal = checkGotoFourInLineByOrientation(point, player, true, HORIZONTAL);
					if(horizontal){
						return true;
					}
					
					boolean vertical = checkGotoFourInLineByOrientation(point, player, true, VERTICAL);
					if(vertical){
						return true;
					}
					
					boolean left_diagonal = checkGotoFourInLineByOrientation(point, player, true, LEFT_DIAGONAL);
					if(left_diagonal){
						return true;
					}
					
					boolean right_diagonal = checkGotoFourInLineByOrientation(point, player, true, RIGHT_DIAGONAL);
					if(right_diagonal){
						return true;
					}
				}
			}else{
				LinkedList<Point> humanSteps = panel.humanSteps;
				
				for(Point point: humanSteps){
					
					boolean horizontal = checkGotoFourInLineByOrientation(point, player, false, HORIZONTAL);
					if(horizontal){
						return true;
					}
					
					boolean vertical = checkGotoFourInLineByOrientation(point, player, false, VERTICAL);
					if(vertical){
						return true;
					}
					
					boolean left_diagonal = checkGotoFourInLineByOrientation(point, player, false, LEFT_DIAGONAL);
					if(left_diagonal){
						return true;
					}
					
					boolean right_diagonal = checkGotoFourInLineByOrientation(point, player, false, RIGHT_DIAGONAL);
					if(right_diagonal){
						return true;
					}
				}
			}
			return false;
		}
		/**
		 * 冲4有这么几种情况：活三，201且两边没有障碍
		 * @param point
		 * @param player
		 * @param isAttack
		 * @param orientation
		 * @return
		 */
		private boolean checkGotoFourInLineByOrientation(Point point, Player player, boolean isAttack,
				Orientation orientation) {
			LinkedList<Point> computerSteps, humanSteps;
			Point leftPoint1 = null, leftPoint2 = null, centerPoint = null;
			if(player instanceof Computer){
				computerSteps = player.getSteps();
				humanSteps = panel.humanSteps;
			}else{
				computerSteps = panel.computerSteps;
				humanSteps = player.getSteps();
			}
			//进攻的时候，查看电脑这边的棋子，寻找冲4的情况
			if(isAttack){
				int cnt = 1;
				//这个用于记录是否有可能发展成5子连珠,空白和已经下过的子都算，被围堵的不算
				int potential = 1;
				//以横向为例，先判断左边有没有被拦截
				int x = point.x;
				int y = point.y;
				
				Point tmpPoint = new Point(point);
				for(int i = 1; i < 3; ++i){
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
					
					if(computerSteps.contains(tmpPoint)){
						++cnt;
						++potential;
					}else if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint)){
						break;
					//否则这个位置还没有下过子
					}else{
						leftPoint1 = leftPoint2;
						leftPoint2 = new Point(tmpPoint);
						++potential;
					}

				}
				
				for(int i = 1; i < 4; ++i){
					if(cnt == 3){
						break;
					}
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
					
					//判断集合中是否包含此坐标，不包含时，再判断敌方是否包含这个棋子或边界是否越界，若是，则退出循环。否则，说明这个位置为空白，potential加1，并用一个变量指向这个位置
					if(computerSteps.contains(tmpPoint)){
						++cnt;
						++potential;
					}else if(humanSteps.contains(tmpPoint) || GoBangUtils.checkIndexOutOfBoundary(tmpPoint) ){
						
						break;

					//走到这里，说明要么是中间为空白，要么就是前面有四个子，第五个为空白。至于这个子是否是冲5的子，还需要留到后面判断
					}else{
						++potential;
						centerPoint = new Point(tmpPoint);
					}
				}
				
				if(cnt == 3){
					if(potential < 5){
						return false;
					}else{
						if(centerPoint != null){
//							if(leftPoint2 != null){
//								computerSteps.push(random.nextBoolean() ? leftPoint2 : centerPoint);
//							}else{
								computerSteps.push(centerPoint);
//							}
						//说明右边被挡住了，只能往左边下棋
						}else{
							computerSteps.push(random.nextBoolean() ? leftPoint1 : leftPoint2);
						}
						return true;
					}
				}
			}else{
				int cnt = 1;
				//这个用于记录是否有可能发展成5子连珠,空白和已经下过的子都算，被围堵的不算
				int potential = 1;
				//以横向为例，先判断左边有没有被拦截
				int x = point.x;
				int y = point.y;
				
				Point tmpPoint = new Point(point);
				for(int i = 1; i < 3; ++i){
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
					
					if(humanSteps.contains(tmpPoint)){
						++cnt;
						++potential;
					}else if(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || computerSteps.contains(tmpPoint)){
						break;
					//否则这个位置还没有下过子
					}else{
						leftPoint1 = leftPoint2;
						leftPoint2 = new Point(tmpPoint);
						++potential;
					}

				}
				
				for(int i = 1; i < 4; ++i){

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
					
					//判断集合中是否包含此坐标，不包含时，再判断敌方是否包含这个棋子或边界是否越界，若是，则退出循环。否则，说明这个位置为空白，potential加1，并用一个变量指向这个位置
					if(humanSteps.contains(tmpPoint)){
						++cnt;
						++potential;
					}else if(computerSteps.contains(tmpPoint) || GoBangUtils.checkIndexOutOfBoundary(tmpPoint) ){
						
						break;

					//走到这里，说明要么是中间为空白，要么就是前面有四个子，第五个为空白。至于这个子是否是冲5的子，还需要留到后面判断
					}else{
						++potential;
						centerPoint = new Point(tmpPoint);
					}
				}
				
				if(cnt == 3){
					if(potential < 5){
						return false;
					}else{
						if(centerPoint != null){
							
							computerSteps.push(centerPoint);
							
						//说明右边被挡住了，只能往左边下棋
						}else{
							computerSteps.push(random.nextBoolean() ? leftPoint1 : leftPoint2);
						}
						return true;
					}
				}
			}
			return false;
		}
		/**
		 * TODO:检测是否有202或者103
		 * @return
		 */
		public boolean checkTwoTwo(){
			return false;
		}
		/**
		 * 冲5的情况，包括202，301， 四连三种情况
		 */
		public boolean checkGotoFiveInLine(Player player){
			//电脑优先进攻，再防守
			if(player instanceof Computer){
				LinkedList<Point> computerSteps = panel.computerSteps;
				
				for(Point point: computerSteps){
					
					boolean horizontal = checkLiveInLineByOrientation(5, point, player, true, HORIZONTAL);
					if(horizontal){
						return true;
					}
					
					boolean vertical = checkLiveInLineByOrientation(5, point, player, true, VERTICAL);
					if(vertical){
						return true;
					}
					
					boolean left_diagonal = checkLiveInLineByOrientation(5, point, player, true, LEFT_DIAGONAL);
					if(left_diagonal){
						return true;
					}
					
					boolean right_diagonal = checkLiveInLineByOrientation(5, point, player, true, RIGHT_DIAGONAL);
					if(right_diagonal){
						return true;
					}
				}
			}else{
				LinkedList<Point> humanSteps = panel.humanSteps;
				
				for(Point point: humanSteps){
					
					boolean horizontal = checkLiveInLineByOrientation(5, point, player, false, HORIZONTAL);
					if(horizontal){
						return true;
					}
					
					boolean vertical = checkLiveInLineByOrientation(5, point, player, false, VERTICAL);
					if(vertical){
						return true;
					}
					
					boolean left_diagonal = checkLiveInLineByOrientation(5, point, player, false, LEFT_DIAGONAL);
					if(left_diagonal){
						return true;
					}
					
					boolean right_diagonal = checkLiveInLineByOrientation(5, point, player, false, RIGHT_DIAGONAL);
					if(right_diagonal){
						return true;
					}
				}
			}
			return false;
		}
		/**
		 * 根据方向来进行冲5
		 * @param num 需要达成的数量
		 * @param isAttack true表示进攻，false表示防守
		 * @param point 当前需要判定的棋子
		 * @param player 当前需要判定的玩家，可能是人类，也可能是电脑
		 * @param orientation
		 * @return 冲5失败返回false
		 */
		private boolean checkLiveInLineByOrientation(int num, Point point, Player player, boolean isAttack, Orientation orientation) {
			//这里用几个变量去记住一些关键地方
			Point leftPoint = null, rightPoint = null, centerPoint = null;
			LinkedList<Point> computerSteps;
			LinkedList<Point> humanSteps;
			if(player instanceof Human){
				humanSteps = player.getSteps();
				computerSteps = panel.computerSteps;
			}else{
				humanSteps = panel.humanSteps;
				computerSteps = player.getSteps();
			}
			
			if(isAttack){
				//初值为1，代表当前已经有一个棋子
				int cnt = 1;
				int x = point.x;
				int y = point.y;
				
				Point tmpPoint = new Point();
				
				//以横向来说明，先检测它的左边时候有阻挡物（敌方有子或者边界的情况）
				switch(orientation){
				case HORIZONTAL:
					tmpPoint.set(x - 1, y);
					break;
				case VERTICAL:
					tmpPoint.set(x, y - 1);
					break;
				case LEFT_DIAGONAL:
					tmpPoint.set(x - 1, y - 1);
					break;
				case RIGHT_DIAGONAL:
					tmpPoint.set(x + 1, y - 1);
					break;
				}
				//如果左边被堵住，还需要看右边的情况；否则，用一个变量记住这个位置的前一个位置
				if(!(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || humanSteps.contains(tmpPoint) || computerSteps.contains(tmpPoint))){
					leftPoint = new Point(tmpPoint);
				}
				
				//开始进行统计
				for(int i = 1; i < num; ++i){
					
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
					
					//判断集合中是否包含此坐标，不包含时，再判断敌方是否包含这个棋子或边界是否越界，若是，还需进一步判断。否则，说明这个位置为空白，先用一个变量记录这个位置，等之后再讨论
					if(computerSteps.contains(tmpPoint)){
						++cnt;
					}else if(humanSteps.contains(tmpPoint) || GoBangUtils.checkIndexOutOfBoundary(tmpPoint) ){
						//在敌方包含这枚棋子的情况下，如果我方还没有四连，说明中途被敌方截断，冲5失败；如果已经四连，则需要判断左边界是否可以下子；如果可以，则将左边加入步子中，冲5成功
						//否则说明这四个子已经被堵死，冲5失败
						if(cnt == num - 1){
							if(leftPoint != null){
								computerSteps.push(leftPoint);
								return true;
							}else{
								return false;
							}
						}else{
							return false;
						}
					//走到这里，说明要么是中间为空白，要么就是前面有四个子，第五个为空白。至于这个子是否是冲5的子，还需要留到后面判断
					}else{
						centerPoint = new Point(tmpPoint);
					}
				}
				
				//在这次循环结束后，已经排除了4子被堵死，4子右边被堵住和中间截断的情况，（五子相连的情况是不需要在这里考虑的）但还有301，202和活四和左边被堵住的情况
				//当达成目标子数时,如冲5时，必须要有4子，才可以进行下一步判断
//				if(cnt == num - 1){
//					//先处理301和202和左边被堵住的情况
//					if(leftPoint == null && centerPoint != null){
//						computerSteps.push(centerPoint);
//					//再处理活四的情况，随机将子下在两边
//					}else if(leftPoint != null && centerPoint != null){
//						boolean left = random.nextBoolean();
//						if(left){
//							computerSteps.push(leftPoint);
//						}else{
//							computerSteps.push(centerPoint);
//						}
//					}
//					return true;
//				}
				
				if(cnt == num - 1){
					if(centerPoint.equals(rightPoint)){
						//先处理右半活四的情况
						if(leftPoint == null){
							computerSteps.push(centerPoint);
						//活四的情况
						}else{
							boolean left = random.nextBoolean();
							if(left){
								computerSteps.push(leftPoint);
							}else{
								computerSteps.push(centerPoint);
							}
						}
						//301和202的情况
					}else{
						computerSteps.push(centerPoint);
					}
					return true;
				}
			//防守的情况
			}else{
				//初值为1，代表当前已经有一个棋子
				int cnt = 1;
				int x = point.x;
				int y = point.y;
				
				Point tmpPoint = new Point();
				
				//以横向来说明，先检测它的左边时候有阻挡物（敌方有子或者边界的情况）
				switch(orientation){
				case HORIZONTAL:
					tmpPoint.set(x - 1, y);
					break;
				case VERTICAL:
					tmpPoint.set(x, y - 1);
					break;
				case LEFT_DIAGONAL:
					tmpPoint.set(x - 1, y - 1);
					break;
				case RIGHT_DIAGONAL:
					tmpPoint.set(x + 1, y - 1);
					break;
				}
				//如果左边被堵住，还需要看右边的情况；否则，用一个变量记住这个位置的前一个位置
				if(!(GoBangUtils.checkIndexOutOfBoundary(tmpPoint) || computerSteps.contains(tmpPoint) || humanSteps.contains(tmpPoint))){
					leftPoint = new Point(tmpPoint);
				}
				
				//开始进行统计
				for(int i = 1; i < num; ++i){
					
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
					//右指针在没有越界的情况下，一直指向第五个元素
					rightPoint = tmpPoint;
					//判断集合中是否包含此坐标，不包含时，再判断敌方是否包含这个棋子或边界是否越界，若是，还需进一步判断。否则，说明这个位置为空白，先用一个变量记录这个位置，等之后再讨论
					if(humanSteps.contains(tmpPoint)){
						++cnt;
					}else if(computerSteps.contains(tmpPoint) || GoBangUtils.checkIndexOutOfBoundary(tmpPoint) ){
						//在敌方包含这枚棋子的情况下，如果我方还没有四连，说明中途被敌方截断，冲5失败；如果已经四连，则需要判断左边界是否可以下子；如果可以，则将左边加入步子中，冲5成功
						//否则说明这四个子已经被堵死，冲5失败
						if(cnt == num - 1){

							//左边半活4的情况
							if(leftPoint != null){
								computerSteps.push(leftPoint);
								return true;
							}else{
								return false;
							}
						}else{
							return false;
						}
					//走到这里，说明要么是中间为空白，要么就是前面有四个子，第五个为空白。至于这个子是否是冲5的子，还需要留到后面判断
					}else{
						centerPoint = new Point(tmpPoint);
					}
				}
				
				//在这次循环结束后，已经排除了4子被堵死，4子右边被堵住和中间截断的情况，（五子相连的情况是不需要在这里考虑的）但还有301，202和活四和左边被堵住的情况
				//当达成目标子数时,如冲5时，必须要有4子，才可以进行下一步判断
//					if(leftPoint == null && centerPoint != null){
				if(cnt == num - 1){
					if(centerPoint.equals(rightPoint)){
						//先处理右半活四的情况
						if(leftPoint == null){
							computerSteps.push(centerPoint);
						//活四的情况
						}else{
							boolean left = random.nextBoolean();
							if(left){
								computerSteps.push(leftPoint);
							}else{
								computerSteps.push(centerPoint);
							}
						}
						//301和202的情况
					}else{
						computerSteps.push(centerPoint);
					}
					return true;
				}
			}
			
			return false;
		}
		
		
		/**
		 * 出现一个棋子的情况
		 */
		public boolean onePieceGo(){
			
			LinkedList<Point> computerSteps = panel.computerSteps;
			
			for(Point point : computerSteps){
				Point tmpPoint = new Point(point);
			}
			
			return false;
		}
		
		/**
		 * 检测中间区域，用于给下第一个子，划定范围
		 */
		public Point centerBlockGo(){
			//取棋盘的中间位置
			Point centerPoint = new Point(lineNum / 2, lineNum / 2);
			
//			centerPoint.
			//向中心的周围两格区域内随机取子，避免它取特别偏的位置
			int offsetX = random.nextInt(centerRange);
			int offsetY = random.nextInt(centerRange);
			//true代表取正数，false代表取负数
			boolean isPositiveNumX = random.nextBoolean();
			boolean isPositiveNumY = random.nextBoolean();
			
			offsetX = isPositiveNumX ? offsetX : -offsetX;
			offsetY = isPositiveNumY ? offsetY : -offsetY;
			
			centerPoint.offset(offsetX, offsetY);
			
			return centerPoint;
		}
	}
}
