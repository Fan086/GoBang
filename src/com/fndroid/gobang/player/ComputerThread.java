package com.fndroid.gobang.player;

import com.fndroid.gobang.utils.GoBangConstants;

public class ComputerThread extends Thread{
	private Computer computer;
	public ComputerThread(Player computer){
		this.computer = (Computer) computer;
	}
	
	@Override
	public void run() {
		while(true){
			try {
				Thread.sleep(GoBangConstants.COMPUTER_SLEEP_TIME);
				computer.goPiece();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
