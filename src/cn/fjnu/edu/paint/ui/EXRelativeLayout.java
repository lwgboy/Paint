package cn.fjnu.edu.paint.ui;
import cn.fjnu.edu.paint.engine.DrawView;
import cn.fjnu.edu.ui.activity.PaintMainActivity;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
public class EXRelativeLayout  extends RelativeLayout {

	//触摸点的横坐标
//	private float rX;
	private float mX;
	private boolean isContinue;//判断ActionDown之后是否继续执行ActionMove
	public EXRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}


	public EXRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}


	public EXRelativeLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


	//拦截触屏事件
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		//Toast.makeText(getContext(), "love", Toast.LENGTH_SHORT).show();
	//	rX=ev.getX();
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			//得到屏幕宽度
		
			/*if(Math.abs(rX-getWidth())<10){
				
				isContinue=true;
				DrawView.listenMode=DrawView.SLIDE_MODE;
			}
			
			else
				isContinue=false;
*/
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			if(PaintMainActivity.isReduce)
				DrawView.listenMode=DrawView.PAINT_MODE;
			break;
		default:
			break;
		}
		//touch事件传到view
		return false;
	}


	//MianView触屏事件
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		mX=event.getX();
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			//得到屏幕宽度
			if(Math.abs(mX-getWidth())<10){
				isContinue=true;
				DrawView.listenMode=DrawView.SLIDE_MODE;
			}
				
			else
				isContinue=false;
			break;
		
		case MotionEvent.ACTION_MOVE:
			if(isContinue){
				if(Math.abs(mX-getWidth())>20){
				//	MainActivity.MActivity.showSlide();
				
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if(PaintMainActivity.isReduce)
				DrawView.listenMode=DrawView.PAINT_MODE;
			break;
		default:
			break;
		}
	  return true;
	}
	
}
