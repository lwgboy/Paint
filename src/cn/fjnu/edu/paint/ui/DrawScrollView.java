package cn.fjnu.edu.paint.ui;


import cn.fjnu.edu.paint.engine.DrawView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class DrawScrollView extends ScrollView{

	private GestureDetector mGestureDetector;
	View.OnClickListener mGestureListener;
	public static DrawScrollView drawScrollView;
	public DrawScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	//	mGestureDetector=new GestureDetector(new YS)
		mGestureDetector=new GestureDetector(new YScrollDetector());
		setFadingEdgeLength(0);
		getDrawScrollView();
	}

	public DrawScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mGestureDetector=new GestureDetector(new YScrollDetector());
		setFadingEdgeLength(0);
		getDrawScrollView();
	}

	public DrawScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mGestureDetector=new GestureDetector(new YScrollDetector());
		setFadingEdgeLength(0);
		getDrawScrollView();
	}

	public void getDrawScrollView(){
		drawScrollView=DrawScrollView.this;
	}
	//拦截触屏事件
		@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
			// TODO Auto-generated method stub
			//Toast.makeText(getContext(), "love", Toast.LENGTH_SHORT).show();
			//touch事件是否传递给子view
			return super.onInterceptTouchEvent(ev)&&mGestureDetector.onTouchEvent(ev)&&DrawView.isMove;
			//return false;
		}

	@Override
	public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
		// TODO Auto-generated method stub
		super.requestDisallowInterceptTouchEvent(disallowIntercept);
	}
	class YScrollDetector extends SimpleOnGestureListener{

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			if(Math.abs(distanceY)>Math.abs(distanceX)){
				return true;
			}
			return false;
			//return super.onScroll(e1, e2, distanceX, distanceY);
		}
		
	}

}
