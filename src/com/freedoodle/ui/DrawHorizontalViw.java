package com.freedoodle.ui;
import com.freedoodle.engine.DrawView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class DrawHorizontalViw extends HorizontalScrollView{

	public DrawHorizontalViw(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public DrawHorizontalViw(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public DrawHorizontalViw(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	//拦截触屏事件
		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
				// TODO Auto-generated method stub
				//Toast.makeText(getContext(), "love", Toast.LENGTH_SHORT).show();
				//int pointCount=ev.getPointerCount();
				/*if(pointCount>1)
					DrawView.isMove=true;
				else
					DrawView.isMove=false;*/
				if(DrawView.isMove){
				//	DrawScrollView.drawScrollView.requestDisallowInterceptTouchEvent(true);
					//拦截DrawView的触屏事件
					return true;
				}
					 
				else
					return false;
	}

}
