package com.fndroid.gobang.player;

import com.fndroid.gobang.panel.BaseGoBangPanel;

public class Human extends Player {
	
	public Human(BaseGoBangPanel panel, boolean colorFlag){
		this("默认玩家", panel, colorFlag);
	}
	
	public Human(String name, BaseGoBangPanel panel, boolean colorFlag) {
		super(name, panel, colorFlag);
	}

}
