package com.fndroid.gobang;

import java.util.LinkedList;

import com.fndroid.gobang.utils.GoBangUtils;
import static com.fndroid.gobang.utils.GoBangConstants.*;
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

public class GoBangPanel extends View{
	
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
	 * 棋盘是否已经绘制，当绘制完成时，之后就不再绘制棋盘
	 */
	private boolean mHasDrawedBoard = false;
	
	/**
	 * 判断是否是白方执子，否则是黑方
	 */
	public boolean mIsWhiteGo;
	
	private Bitmap mWhitePiece;
	private Bitmap mBlackPiece;
	
	/**
	 * 棋子占每一格的比例，默认为3/4
	 */
	private float mRatioOfPieceToSingleWidth = 3.0f / 4;
	
	/**
	 * 用于记录白方所走的位置
	 */
	public LinkedList<Point> mWhiteSteps;
	/**
	 * 用于记录黑方所走的位置
	 */
	public LinkedList<Point> mBlackSteps;
	
	/**
	 * 判断游戏是否结束
	 */
	public boolean mIsGameOver = false;
	
	public GoBangPanel(Context context) {
		this(context, null);
	}
	
	public GoBangPanel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public GoBangPanel(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		setBackgroundColor(Color.WHITE);
		init();
	}
	
	
	/**
	 * 初始化画笔，棋子,以及所走的步子
	 */
	private void init() {
		
		//从配置文件中读取设置信息
		SharedPreferences sp = getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
		mLineNum = sp.getInt(LINE_NUM, 15);
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setStyle(Style.STROKE);
		
		mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
		mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);
		
		mWhiteSteps = new LinkedList<>();
		mBlackSteps = new LinkedList<>();
		
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
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//当游戏结束时，不执行点击事件
		if(mIsGameOver){
			return false;
		}
		int action = event.getAction();
		//之所以用up不用down是为了当上一级控件是scrollView这样的控件时，由它来处理；否则会出现滑动时落子的情况-
		if(action == MotionEvent.ACTION_UP){
			
			float x = event.getX();
			float y = event.getY();
			
			Point point = getValidPoint(x, y);
			
			//当黑白双方的步子集合中已存在这个坐标时，本次点击事件不处理
			if(mWhiteSteps.contains(point) || mBlackSteps.contains(point)){
				return false;
			}
			
			//哪个子走棋，则将它放入对应的集合中
			if(mIsWhiteGo){
				mWhiteSteps.push(point);
			}else{
				mBlackSteps.push(point);
			}
			
			invalidate();
			
			//检测是否游戏结束
			GoBangUtils.isGameOver(this);
			
			mIsWhiteGo = !mIsWhiteGo;
		}
		//这里需要返回true，即告诉父控件这块区域的点击事件由我处理。这样才可获得ACTION_UP
		return true;
	}
	
	private Point getValidPoint(float x, float y) {
		return new Point((int)(x / LINE_WIDTH), (int)(y / LINE_WIDTH));
	}

	@Override
	protected void onDraw(Canvas canvas) {
//		if(!mHasDrawedBoard){
//			drawBoard(canvas);
//		}
		
		drawBoard(canvas);
		drawPieces(canvas);
	}
	
	private void drawPieces(Canvas canvas) {
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
		//TODO:将划线生成的背景保存成图片
//		Drawable drawable = canvas.
//		Bitmap backgroundBitmap = getDrawingCache(true);
//		Drawable background = new BitmapDrawable(getResources(), backgroundBitmap);
//		setBackground(background);
//		mHasDrawedBoard = true;
	}
	
//	@Override
//	protected Parcelable onSaveInstanceState() {
//		Bundle bundle = new Bundle();
//		bundle.putBoolean("isGameOver", mIsGameOver);
//		bundle.putSerializable("whiteSteps", mWhiteSteps);
//		bundle.putSerializable("blackSteps", mBlackSteps);
//		return bundle;
//	}
//	
//	@Override
//	protected void onRestoreInstanceState(Parcelable state) {
//		if(state instanceof Bundle){
//			System.out.println("onRestore");
//		}
//		super.onRestoreInstanceState(state);
//	}
}
