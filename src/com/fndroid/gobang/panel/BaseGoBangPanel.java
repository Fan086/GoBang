package com.fndroid.gobang.panel;

import static com.fndroid.gobang.utils.GoBangConstants.LINE_NUM;
import static com.fndroid.gobang.utils.GoBangConstants.SETTINGS;

import java.util.LinkedList;

import com.fndroid.gobang.R;
import com.fndroid.gobang.R.drawable;
import com.fndroid.gobang.player.Human;
import com.fndroid.gobang.player.Player;
import com.fndroid.gobang.utils.GoBangUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public abstract class BaseGoBangPanel extends View{
	enum Type{
		HUMAN2HUMAN, HUMAN2COMPUTER
	}
	
	/**
	 * 结束提示框
	 */
	public AlertDialog dialog;
	
	/**
	 * 五子棋的行数和列数，默认为15
	 */
	public static int mLineNum;
	
	/**
	 * 当连城多少个时，可以结束游戏
	 */
	public static final int MAX_PIECES = 5;
	
	/**
	 * 每行之间的间距
	 */
	private static float LINE_WIDTH; 

	/**
	 * 屏幕的宽度
	 */
	private static int PANEL_WIDTH;
	
	private Paint mPaint;
	
	/**
	 * 判断是否是白方执子，否则是黑方
	 */
	public boolean mIsWhiteGo = true;
	
	private Bitmap mWhitePiece;
	private Bitmap mBlackPiece;
	
	/**
	 * 棋子占每一格的比例，默认为3/4
	 */
	private float mRatioOfPieceToSingleWidth = 3.0f / 4;
	
	/**
	 * 白色玩家
	 */
	public Player whitePlayer;
	/**
	 * 黑色玩家
	 */
	public Player blackPlayer;
	/**
	 * 用于记录白方所走的位置
	 */
	protected LinkedList<Point> mWhiteSteps;
	/**
	 * 用于记录黑方所走的位置
	 */
	protected LinkedList<Point> mBlackSteps;
	
	
	/**
	 * 判断游戏是否结束
	 */
	public boolean mIsGameOver = false;
	
	public BaseGoBangPanel(Context context) {
		this(context, null);
	}
	
	public BaseGoBangPanel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public BaseGoBangPanel(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		setBackgroundColor(Color.WHITE);
		init();
	}
	
	
	/**
	 * 初始化画笔，棋子,以及所走的步子
	 */
	protected void init() {
		
		//从配置文件中读取设置信息
		SharedPreferences sp = getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
		mLineNum = sp.getInt(LINE_NUM, 15);
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setStyle(Style.STROKE);
		
		mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
		mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);
		
		dialog = new AlertDialog.Builder(getContext())
					.setTitle("提示")
					.setMessage("游戏结束")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.setCancelable(false)
					.create();
	}
	
	public void setWhitePlayer(Player whitePlayer){
		this.whitePlayer = whitePlayer;
		mWhiteSteps = whitePlayer.getSteps();
	}
	
	public void setBlackPlayer(Player blackPlayer){
		this.blackPlayer = blackPlayer;
		mBlackSteps = blackPlayer.getSteps();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		
		//真实的宽高
		int size = Math.min(widthSize, heightSize);
		
		//防止此控件放在scrollView之类的控件中
		if(widthMode == MeasureSpec.UNSPECIFIED){
			size = heightSize;
		}
		if(heightMode == MeasureSpec.UNSPECIFIED){
			size = widthSize;
		}
		
		setMeasuredDimension(size, size);
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		PANEL_WIDTH = w;
		
		LINE_WIDTH = PANEL_WIDTH * 1.0f / mLineNum;
		
		int width = (int) (LINE_WIDTH * mRatioOfPieceToSingleWidth);
		mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, width, width, false);
		mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, width, width, false);
	}
	
	
	
	protected Point getValidPoint(float x, float y) {
		return new Point((int)(x / LINE_WIDTH), (int)(y / LINE_WIDTH));
	}

	@Override
	protected void onDraw(Canvas canvas) {

		drawBoard(canvas);
		drawPieces(canvas);
	}
	
	protected void drawPieces(Canvas canvas) {
		if(whitePlayer == null || blackPlayer == null){
			throw new RuntimeException("player has not set, please call setWhitePlayer and setBlackPlayer before call this method");
		}
		//绘制白棋
		for(int i = 0; i < mWhiteSteps.size(); ++i){
			Point point = mWhiteSteps.get(i);
			
			int x = point.x;
			int y = point.y;
			
			float left = x * LINE_WIDTH + LINE_WIDTH / 8;
			float top = y * LINE_WIDTH + LINE_WIDTH / 8;
			
			canvas.drawBitmap(mWhitePiece, left, top, mPaint);
		}
		//绘制黑棋
		for(int i = 0; i < mBlackSteps.size(); ++i){
			Point point = mBlackSteps.get(i);
			
			int x = point.x;
			int y = point.y;
			
			float left = x * LINE_WIDTH + LINE_WIDTH / 8;
			float top = y * LINE_WIDTH + LINE_WIDTH / 8;
			
			canvas.drawBitmap(mBlackPiece, left, top, mPaint);
		}
	}

	/**
	 * 绘制棋盘
	 * @param canvas
	 */
	private void drawBoard(Canvas canvas) {
		//绘制横线和纵线，两者合并在了一起
		for(int i = 0; i < mLineNum; ++i){
			//计算出每行的y的位置
			float y = i * LINE_WIDTH + LINE_WIDTH / 2;
			float startX = LINE_WIDTH / 2;
			float stopX = PANEL_WIDTH - startX;
			canvas.drawLine(startX, y, stopX, y, mPaint);
			
			canvas.drawLine(y, startX, y, stopX, mPaint);
		}

	}
	
}
