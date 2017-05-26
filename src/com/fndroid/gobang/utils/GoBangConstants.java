package com.fndroid.gobang.utils;

public final class GoBangConstants {
	/**
	 * 用于sharedPreference中取出设置信息的常量
	 */
	public static final String SETTINGS = "settings";
	
	/**
	 * 存储行数与列数
	 */
	public static final String LINE_NUM = "line_num";
	
	/**
	 * 存储玩家颜色，即落子顺序
	 */
	public static final String HUMAN_FIRST = "human_first";
	/**
	 * 定义电脑下棋的时间
	 */
	public static final int COMPUTER_SLEEP_TIME = 800;
	/**
	 * 定义设置中最小的行列数
	 */
	public static final int SETTING_MIN_NUM = 10;
	/**
	 * 定义设置中最大的行列数
	 */
	public static final int SETTING_MAX_NUM = 21;
}
