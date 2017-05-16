package com.fndroid.gobang.player;

import android.os.Handler;
import android.os.Looper;

public class ComputerThread extends Thread{
	private Computer computer;
	public ComputerThread(Player computer){
		this.computer = (Computer) computer;
	}
	
	@Override
	public void run() {
		while(true){
			try {
				Thread.sleep(800);
				computer.goPiece();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
