package com.fndroid.gobang.player;

import com.fndroid.gobang.panel.BaseGoBangPanel;

/**
 *代表人类玩家的类
 */
public class Human extends Player {
	
	/**
	 * 可以为玩家命名，但目前没有实现显示玩家名
	 * @param panel 对应哪个棋盘
	 * @param colorFlag 颜色旗标，白色为true；黑色为false
	 */
	public Human(BaseGoBangPanel panel, boolean colorFlag){
		this("默认玩家", panel, colorFlag);
	}
	
	public Human(String name, BaseGoBangPanel panel, boolean colorFlag) {
		super(name, panel, colorFlag);
	}

}
