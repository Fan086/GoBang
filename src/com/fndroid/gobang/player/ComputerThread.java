package com.fndroid.gobang.player;

import java.util.ConcurrentModificationException;

import com.fndroid.gobang.MainActivity;
import com.fndroid.gobang.panel.BaseGoBangPanel;
import com.fndroid.gobang.utils.GoBangConstants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

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
				//执行走棋方法
				computer.goPiece();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}catch(ConcurrentModificationException|StackOverflowError e2){
				System.out.println("发生了ConcurrentModificationException,栈移除异常 ");
				BaseGoBangPanel panel = computer.getPanel();
				Activity activity = (Activity) panel.getContext();
				activity.finish();
				break;
			}
		}
	}

}
