package cn.fjnu.edu.paint.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import cn.fjnu.edu.paint.R;
import cn.fjnu.edu.paint.data.BitmapType;
import cn.fjnu.edu.paint.data.Shape_Type;
import cn.fjnu.edu.paint.domain.BrokenPoint;
import cn.fjnu.edu.paint.domain.DrawPath;
import cn.fjnu.edu.paint.ui.ColorPicker;
import cn.fjnu.edu.paint.ui.OpacityBar;
import cn.fjnu.edu.ui.activity.PaintMainActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;


@SuppressLint("ClickableViewAccessibility")
public class DrawView extends ImageView {
	
	public static final int COMMON_MODE=0;//通常模式
	public static final int  ERASER_MODE=1;//橡皮擦模式
	public static final int CUT_MODE=2;//图片裁剪模式
	public static final int COPY_MODE=3;//位图复制模式
	public static final int FILLCOLOR_MODE=4;//填色模式
	public static final int PASTETEXT_MODE=5;//黏贴文字模式
	public static final int SAVE_MODE=1;//保存模式
	public static final int PAINT_MODE=0;//画笔模式
	public static final int SLIDE_MODE=1;//滑动模式
	public static final int DRAW_MODE=0;//画线模式
	public static final int BITMAP_MODE=1;//画位图模式
	public static final int DRAW_TEXT_MODE=2;//绘制文字模式
	public static boolean isIntercept=false;//是否拦截DrawView的触屏事件
	public static boolean isMove=false;//判断画布是否移动
	private int fillcolor;//填充颜色
	private int cutFillColor;//剪切填充模式
	public static int listenMode=PAINT_MODE;//设置为画笔模式
	private int paintMode=COMMON_MODE;//设置为通常模式
	private  int shapeMode=Shape_Type.FREE;//设置为自由手绘
	private int currentShape=shapeMode;//记录当前图像类型
	private float currentPenSize=5;//记录当前画笔宽度
	private BrokenPoint brokenPoint;//折线转折点
	private float mulX;//六边形中心点横坐标
	private float mulY;//六边形中心点纵坐标
	private float widthScale;//横向缩放比例
	private float heightScale;//纵向缩放比例
	private Matrix matrix;//缩放/旋转矩阵
	private Bitmap resizeBitmap;//缩放之后的bitmap
	private Bitmap roatBitmap;//旋转之后的bitmap
	private Bitmap copyBitmap;//要复制的bitmap
	private Bitmap tempBitmap;
	private boolean isInitDrawBroken=true;
	private boolean isInitZoom=true;//是否初次缩放
	private boolean isInitRote=true;//是否初次旋转
	private boolean isshapeSave=false;//currentShape是否保存
	public static boolean isFirstDraw=true;//是否初次绘画
	private RectF rectF;//矩形
	private Bitmap mBitmap;
	private Paint translatePaint;//透明画笔
	private Canvas mCanvas;
	private Canvas tempCanvas;//临时画布
	private Path mPath;
	public  Paint mBitmapPaint;// 画布的画笔
	public  Paint mPaint;// 真实的画笔
	//public static Paint exPaint;//供其他类使用
	private float mX, mY;// 临时点坐标
	private float endX,endY;//手指离开屏幕的坐标
	private static final float TOUCH_TOLERANCE = 4;
	// 保存Path路径的集合,用List集合来模拟栈，用于后退步骤
	private  List<DrawPath> savePath;
	// 保存Path路径的集合,用List集合来模拟栈,用于前进步骤
	private  List<DrawPath> canclePath;
	// 记录Path路径的对象
	private DrawPath dp;
	private DrawPath bmpDrawPath;
	private DrawPath lastDrawPath;
	private SeekBar zoomSeekBar;//缩放图片控制条
	private SeekBar roatSeekBar;//旋转图片控制条
	//绘制颜色
	public  int color =Color.BLACK;
	public  float srokeWidth = 5;
	//待绘制的文字
	private String paintText=null;
	//用于绘制文字的画笔
	private Paint textPaint=null;
		public DrawView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}
		
		public DrawView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}
		
		public Paint getPaint() {
			return mPaint;
		}
		public int getColor(){
			return  color;
		}
		public void setPaint(Paint mPaint) {
			this.mPaint = mPaint;
		}
		public void setBitPaint(Paint paint){
			this.mBitmapPaint=paint;
		}
		//设置画笔颜色
		public void setColor(int ex_color){
			
			color=ex_color;
			
		}
		//设置画笔粗细
		public void setPenSize(float size){
			srokeWidth=size;
			currentPenSize=size;
		}
		//获得画笔粗细
		public float getPenSize(){
			return srokeWidth;
		}
		//清空画布
		public void clear(){
			mBitmap = Bitmap.createBitmap(PaintMainActivity.drawWidth, PaintMainActivity.drawHeight,
					Bitmap.Config.ARGB_8888);
			if(mCanvas!=null)
				mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布
			if(canclePath!=null&&canclePath.size()>0){
				canclePath.clear();
			}
			if(savePath!=null&&savePath.size()>0){
				savePath.clear();
			}
		//	onDraw(mCanvas);
			invalidate();
		}
		
		//保存图片
		public void saveImage(String path,int mode){

			File saveFile=new File(path);
			if(saveFile.exists())
				saveFile.delete();
			int photoWidth;
			int photoHeight;
			try {
				
				FileOutputStream fileOutputStream=new FileOutputStream(saveFile);
				//对大于500的图片压缩
				if(getWidth()>480||getHeight()>800){
					//计算压缩比例
					float scale=getWidth()/480f;
					if(getHeight()/scale<=800){
						photoWidth=480;
						photoHeight=(int)(getHeight()/scale);
					}else{
						scale=getHeight()/800;
						photoHeight=800;
						photoWidth=(int)(getWidth()/scale);
					}
					
				}else{
					photoWidth=getWidth();
					photoHeight=getHeight();
				}
	     		Bitmap bitmap=Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.RGB_565);
				draw(new Canvas(bitmap));
				Bitmap saveBitmap=Bitmap.createScaledBitmap(bitmap,photoWidth,photoHeight, true);
				//压缩成原图质量的50%
				saveBitmap.compress(CompressFormat.PNG, 50, fileOutputStream);
				fileOutputStream.flush();
				fileOutputStream.close();
				if(mode==SAVE_MODE)
					Toast.makeText(getContext(), "文件保存在"+path,Toast.LENGTH_SHORT).show();
				
			} catch (Exception e) {
				// TODO: handle exception
				Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
			}
		}
		
		//设置绘图类型
		public void setShape(int shape){
			shapeMode=shape;
			
		}
		
		//保存当前绘图类型
		public  void setCurrentShape(){
			currentShape=getShape();
			currentPenSize=getPenSize();
		}
		//得到绘图类型
		public int getShape(){
			return shapeMode;
		}
		//获取当前绘图类型
		public int getCurrentShape(){
			return currentShape;
		}
		//设置绘图模式
		public void setPaintMode(int ex_paindMode){
			paintMode=ex_paindMode;
			switch (ex_paindMode) {
			case COMMON_MODE:
				setShape(currentShape);
				setPenSize(currentPenSize);
				break;
			case CUT_MODE:
				//保存绘图类型
				//保存画笔粗细
				if(!isshapeSave){
						currentShape=getShape();    
						currentPenSize=getPenSize();
						isshapeSave=true;
				}
				setShape(Shape_Type.RECT);
				break;
			case ERASER_MODE:
				if(!isshapeSave){
					currentShape=getShape();
					currentPenSize=getPenSize();
					isshapeSave=true;
				}
				setShape(Shape_Type.FREE);
				break;
			case FILLCOLOR_MODE:
				break;
			case PASTETEXT_MODE:
				//initTextPaint();
				break;
			default:
				break;
			}
		}
		//获取当前绘图模式
		public int getPaintMode(){
			return paintMode;
		}
	
		//画笔粗细显示
		public void displayPenSize(int size){
			
			mBitmap=Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
			if(mCanvas==null)
				mCanvas=new Canvas();
			mCanvas.setBitmap(mBitmap);
			if(mPaint==null){
				mPaint=new Paint();
				mPaint.setAntiAlias(true);
				mPaint.setStyle(Paint.Style.STROKE);
				mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
				mPaint.setStrokeCap(Paint.Cap.ROUND);// 形状
			}
			mPaint.setColor(Color.BLACK);
			mPaint.setStrokeWidth((float)size);
	//		measure(MeasureSpec.EXACTLY, MeasureSpec.EXACTLY);
			measure(View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY), 
					View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY));
			mCanvas.drawLine(0, getMeasuredHeight()/2, getMeasuredWidth(),getMeasuredHeight()/2, mPaint);
			invalidate();
		}
		//设置填色模式
		public void setFillColor(int color ){
			fillcolor=color;
			paintMode=FILLCOLOR_MODE;
		}
		//设置剪切填色模式
		public void setCutFillColor(int color){
			cutFillColor=color;
		}
		//设置背景图
		public void setBackImage(int res){
			setImageResource(res);
		}
		
		//设置监听模式
		public void setListentMode(int mode){
			listenMode=mode;
		}

		private void initPaint(){
				if(isFirstDraw){
					mBitmap = Bitmap.createBitmap(PaintMainActivity.drawWidth, PaintMainActivity.drawHeight,
							Bitmap.Config.ARGB_8888);
					// 保存一次一次绘制出来的图形
					mCanvas = new Canvas(mBitmap);
					mBitmapPaint = new Paint(Paint.DITHER_FLAG);
					mPaint = new Paint();
					mPaint.setAntiAlias(true);
					mPaint.setStyle(Paint.Style.STROKE);
					mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
					mPaint.setStrokeCap(Paint.Cap.ROUND);// 形状
					mPaint.setStrokeWidth(srokeWidth);// 画笔宽度
					mPaint.setColor(color);
					savePath = new ArrayList<DrawPath>();
					canclePath = new ArrayList<DrawPath>();
					isFirstDraw=false;
				}
				mPaint = new Paint();
				mPaint.setAntiAlias(true);
				mPaint.setStyle(Paint.Style.STROKE);
				mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
				mPaint.setStrokeCap(Paint.Cap.ROUND);// 形状
				mPaint.setStrokeWidth(srokeWidth);// 画笔宽度
				mPaint.setColor(color);
			
			if(paintMode==ERASER_MODE){
				mPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
				mPaint.setAlpha(0);
	//			mPaint.setColor(Color.RED);
			}
			if(paintMode==CUT_MODE){
				mPaint.setColor(Color.WHITE);
				mPaint.setStrokeWidth(5);
		//		setShape(Shape_Type.RECT);
			}
			
		}
		//设置要复制的Bitmap
	public void setCopyBitmap(Bitmap ex_bitBitmap){
		copyBitmap=ex_bitBitmap;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
	//	Toast.makeText(getContext(), "触发onDraw事件", Toast.LENGTH_SHORT).show();
		super.onDraw(canvas);
		if(mBitmap!=null)
			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
		
		if (mPath != null&&paintMode==COMMON_MODE) {
			    canvas.drawPath(mPath, mPaint);
		}
		if(mPath!=null&&paintMode==CUT_MODE){
			 canvas.drawPath(mPath, mPaint);
		}
		if(PaintMainActivity.isLoad){
			PaintMainActivity.drawWidth=getWidth();
			PaintMainActivity.drawHeight=getHeight();
			clear();
			PaintMainActivity.isLoad=false;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
	//	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int measureHeight=measureLength(heightMeasureSpec);
		int measureWidth=measureLength(widthMeasureSpec);
		setMeasuredDimension(measureWidth, measureHeight);
	}
	private int measureLength(int length){
		//int specMode=MeasureSpec.getMode(length);
		int specSize=MeasureSpec.getSize(length);
		int result=specSize;
		return result;
	}
	private void touch_start(float x, float y) {
		
		switch(paintMode){
		case FILLCOLOR_MODE:
			FloodFill(mBitmap,new Point((int)x, (int)y), Color.TRANSPARENT, fillcolor);
			break;
		case ERASER_MODE:
//			setShape(Shape_Type.FREE);
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
			break;
		case COPY_MODE:
			mCanvas.drawBitmap(copyBitmap, x-0.5f*copyBitmap.getWidth(),y-0.5f*copyBitmap.getHeight(), mBitmapPaint);
			DrawPath bitmapDrawPath=new DrawPath();
			bitmapDrawPath.bitmap=copyBitmap;
			bitmapDrawPath.bitmapType=BitmapType.COMMON_BITMAP;
			bitmapDrawPath.oreignBitmapTyep=BitmapType.COMMON_BITMAP;
			bitmapDrawPath.bx=x-0.5f*copyBitmap.getWidth();
			bitmapDrawPath.by=y-0.5f*copyBitmap.getHeight();
			bitmapDrawPath.mode=BITMAP_MODE;
			savePath.add(bitmapDrawPath);
			break;
		case CUT_MODE:
		//	setShape(Shape_Type.RECT);
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
			break;
		case COMMON_MODE:
			switch(shapeMode){
			case Shape_Type.FREE:case Shape_Type.OVAL:
			case Shape_Type.RECT:case Shape_Type.STRAIGIT:
				//重新设置初始化折线
				isInitDrawBroken=true;
				mPath.moveTo(x, y);
				mX = x;
				mY = y;
				break;
			case Shape_Type.BROKEN:
			
				//第一次画点
				if(isInitDrawBroken){
					mPath.addCircle(x, y, 0.5f, Path.Direction.CW);
					 brokenPoint=new BrokenPoint();
					isInitDrawBroken=false;
					brokenPoint.x=x;
					brokenPoint.y=y;
				//	countPoint++;
				}
				else{
					
					mPath.moveTo(brokenPoint.x, brokenPoint.y);
					mPath.lineTo(x, y);
					brokenPoint.x=x;
					brokenPoint.y=y;
				}
				break;
			case Shape_Type.MUTIL:
				//重新设置初始化折线
				isInitDrawBroken=true;
				//获取六边形初始点坐标
				mulX=x;
				mulY=y;
				break;
			}
			break;
		case PASTETEXT_MODE:
			initTextPaint();
			//textPaint.setTextSize(20);
			//float paintTextSize=textPaint.measureText(paintText);
			float textX=x;
			float textY=y;
			String drawText=new String(paintText);
			mCanvas.drawText(paintText,textX, textY, textPaint);
			DrawPath textDrawPath=new DrawPath();
			textDrawPath.mode=DRAW_TEXT_MODE;
			textDrawPath.bx=textX;
			textDrawPath.by=textY;
			textDrawPath.paint=textPaint;
			textDrawPath.drawText=drawText;
			savePath.add(textDrawPath);
			break;
		default:
				break;		
		}
		
	
	}

	private void touch_move(float x, float y) {
		switch(shapeMode){
		case Shape_Type.STRAIGIT:
				mPath.reset();
				mPath.moveTo(mX, mY);
				mPath.lineTo(x, y);
			break;
		case Shape_Type.BROKEN:
			break;
		case Shape_Type.RECT:
				mPath.reset();
				if(x>mX&&y>mY)
					mPath.addRect(mX, mY, x, y, Path.Direction.CW);
				else if(x>mX&&y<mY)
					mPath.addRect(mX, y,x, mY, Path.Direction.CW);
				else if(x<mX&&y<mY)
					mPath.addRect(x,y, mX, mY,Path.Direction.CW);
				else
					mPath.addRect(x,mY,mX,y,Path.Direction.CW);
				mPath.moveTo(mX, mY);
			break;
		case Shape_Type.MUTIL:
			mPath.reset();
			float r=(float)Math.sqrt((mulX-x)*(mulX-x)+(mulY-y)*(mulY-y));//获取半径
			mPath.moveTo(mulX+r, mulY);
			mPath.lineTo((float)(mulX+0.5*r), (float)(mulY+0.86603*r));
			mPath.lineTo((float)(mulX-0.5*r), (float)(mulY+0.86603*r));
			mPath.lineTo((float)(mulX-r), mulY);
			mPath.lineTo((float)(mulX-0.5*r), (float)(mulY-0.86603*r));
			mPath.lineTo((float)(mulX+0.5*r), (float)(mulY-0.86603*r));
			mPath.close();
			break;
		case Shape_Type.OVAL:
				mPath.reset();
				mPath.addOval(new RectF(mX, mY, x, y), Path.Direction.CW);
				mPath.moveTo(mX, mY);
			break;
		case Shape_Type.FREE:
			if(paintMode==ERASER_MODE)
				mCanvas.drawPath(mPath, mPaint);
			float dx = Math.abs(x - mX);
			float dy = Math.abs(mY - y);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				// 从x1,y1到x2,y2画一条贝塞尔曲线，更平滑(直接用mPath.lineTo也是可以的)
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;
		
			}
			break;
		default:
				break;
		}
	
	}

	private void touch_up() {
		switch(paintMode){
		case COMMON_MODE:
			switch(shapeMode){
			case Shape_Type.FREE:case Shape_Type.OVAL:case Shape_Type.RECT:
			case Shape_Type.STRAIGIT:
				mPath.lineTo(mX, mY);
				mCanvas.drawPath(mPath, mPaint);
				// 将一条完整的路径保存下来(相当于入栈操作)
				dp.path = mPath;
				dp.paint = mPaint;
				savePath.add(dp);
				mPath = null;// 重新置空
				break;
			case Shape_Type.BROKEN:
				mCanvas.drawPath(mPath, mPaint);
				// 将一条完整的路径保存下来(相当于入栈操作)
				dp.path = mPath;
				dp.paint = mPaint;
				savePath.add(dp);
				mPath = null;// 重新置空
				break;
			case Shape_Type.MUTIL:
				mCanvas.drawPath(mPath, mPaint);
				// 将一条完整的路径保存下来(相当于入栈操作)
				dp.path = mPath;
				dp.paint = mPaint;
				savePath.add(dp);
				mPath = null;// 重新置空
				break;
			default:
					break;
			}
			break;
		case ERASER_MODE:
			mPath.lineTo(mX, mY);
			mCanvas.drawPath(mPath, mPaint);
			// 将一条完整的路径保存下来(相当于入栈操作)
			dp.path = mPath;
			dp.paint = mPaint;
			savePath.add(dp);
			mPath = null;// 重新置空
			break;
		case CUT_MODE:
			new AlertDialog.Builder(getContext())
			.setTitle("操作方式")
			.setItems(new String[]{"复制","剪切","缩放","删除","旋转","填色"}, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(mX==endX||mY==endY)
						return;
					if(PaintMainActivity.isReduce){
						if(getWidth()*getScaleX()<endX)
							endX=getWidth()*getScaleX();
						if(getHeight()*getScaleY()<endY)
							endY=getHeight()*getScaleY();
					} 
					switch(which){
					case 0:
						copyBitmap=Bitmap.createBitmap
						(mBitmap, (int)mX,(int)mY, (int)Math.abs(endX-mX),(int) Math.abs(endY-mY));
						paintMode=COPY_MODE;
						break;
					case 1:
						copyBitmap=Bitmap.createBitmap
						(mBitmap, (int)mX,(int) mY, (int)Math.abs(endX-mX),(int) Math.abs(endY-mY));
						bmpDrawPath=new DrawPath();
						bmpDrawPath.bitmap=copyBitmap;
						bmpDrawPath.bitmapType=BitmapType.COMMON_BITMAP;
						bmpDrawPath.oreignBitmapTyep=BitmapType.COMMON_BITMAP;
						bmpDrawPath.bx=mX;
						bmpDrawPath.by=mY;
						bmpDrawPath.mode=BITMAP_MODE;
						savePath.add(bmpDrawPath);
						translatePaint=new Paint();
						translatePaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
						mCanvas.drawRect(mX, mY, endX, endY, translatePaint);
						paintMode=COPY_MODE;
						invalidate();
						break;
					case 2:
					
						copyBitmap=Bitmap.createBitmap
						(mBitmap, (int)mX,(int) mY, (int)Math.abs(endX-mX),(int) Math.abs(endY-mY));
						bmpDrawPath=new DrawPath();
						bmpDrawPath.bitmap=copyBitmap;
						bmpDrawPath.bx=mX;
						bmpDrawPath.by=mY;
						bmpDrawPath.mode=BITMAP_MODE;
						bmpDrawPath.oreignBitmapTyep=BitmapType.ZOOM_BITMAP;
						bmpDrawPath.bitmapType=BitmapType.ZOOM_BITMAP;
						savePath.add(bmpDrawPath);
						zoomSeekBar=(SeekBar)PaintMainActivity.MActivity.findViewById(R.id.zoom_seekbar);
						zoomSeekBar.setVisibility(VISIBLE);
						zoomSeekBar.setProgress(50);
						zoomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
							
							@Override
							public void onStopTrackingTouch(SeekBar seekBar) {
								translatePaint=new Paint();
								translatePaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
								int roatProcess=seekBar.getProgress();
								if(roatProcess!=0)
									widthScale=heightScale=roatProcess/50f;
								matrix=new Matrix();
								matrix.postScale(widthScale, heightScale);
								if(isInitZoom){
								mCanvas.drawRect(mX, mY, mX+copyBitmap.getWidth(),mY+copyBitmap.getHeight(),translatePaint);
								resizeBitmap=Bitmap.createBitmap(copyBitmap,0, 0, copyBitmap.getWidth(), copyBitmap.getHeight(),matrix, true);
								mCanvas.drawBitmap(resizeBitmap, mX+0.5f*copyBitmap.getWidth()-0.5f*resizeBitmap.getWidth(),
										mY+0.5f*copyBitmap.getHeight()-0.5f*resizeBitmap.getHeight(), mBitmapPaint);
								bmpDrawPath=new DrawPath();
								bmpDrawPath.bitmap=resizeBitmap;
								bmpDrawPath.bx=mX+0.5f*copyBitmap.getWidth()-0.5f*resizeBitmap.getWidth();
								bmpDrawPath.by=mY+0.5f*copyBitmap.getHeight()-0.5f*resizeBitmap.getHeight();
								bmpDrawPath.mode=BITMAP_MODE;
								bmpDrawPath.oreignBitmapTyep=BitmapType.COMMON_BITMAP;
								bmpDrawPath.bitmapType=BitmapType.COMMON_BITMAP;
								savePath.add(bmpDrawPath);
								isInitZoom=false;
								invalidate();
							}
							else{
								
								lastDrawPath= savePath.get(savePath.size()-1);
								lastDrawPath.oreignBitmapTyep=BitmapType.ZOOM_BITMAP;
								lastDrawPath.bitmapType=BitmapType.ZOOM_BITMAP;
								mCanvas.drawRect(lastDrawPath.bx, lastDrawPath.by,
										lastDrawPath.bx+lastDrawPath.bitmap.getWidth(),
										lastDrawPath.by+lastDrawPath.bitmap.getHeight(), translatePaint);
								resizeBitmap=Bitmap.createBitmap(copyBitmap,0, 0, copyBitmap.getWidth(), copyBitmap.getHeight(),matrix, true);
								mCanvas.drawBitmap(resizeBitmap, mX+0.5f*copyBitmap.getWidth()-0.5f*resizeBitmap.getWidth(),
										mY+0.5f*copyBitmap.getHeight()-0.5f*resizeBitmap.getHeight(), mBitmapPaint);
								bmpDrawPath=new DrawPath();
								bmpDrawPath.bitmap=resizeBitmap;
								bmpDrawPath.bx=mX+0.5f*copyBitmap.getWidth()-0.5f*resizeBitmap.getWidth();
								bmpDrawPath.by=mY+0.5f*copyBitmap.getHeight()-0.5f*resizeBitmap.getHeight();
								bmpDrawPath.mode=BITMAP_MODE;
								bmpDrawPath.oreignBitmapTyep=BitmapType.COMMON_BITMAP;
								bmpDrawPath.bitmapType=BitmapType.COMMON_BITMAP;
								savePath.add(bmpDrawPath);
								invalidate();
							}
								
							}
							
							@Override
							public void onStartTrackingTouch(SeekBar seekBar) {
								// TODO Auto-generated method stub
								
					
							}
							
							@Override
							public void onProgressChanged(SeekBar seekBar, int progress,
									boolean fromUser) {
								// TODO Auto-generated method stub
					
				
							}
						});
				//		paintMode=COPY_MODE;
						//第一缩放矩阵
						//定义缩放比例
				//		mCanvas.scale(2f, 2f);
						break;
					case 3:
						copyBitmap=Bitmap.createBitmap
						(mBitmap, (int)mX,(int) mY, (int)Math.abs(endX-mX),(int) Math.abs(endY-mY));
						bmpDrawPath=new DrawPath();
						bmpDrawPath.bitmap=copyBitmap;
						bmpDrawPath.bx=mX;
						bmpDrawPath.by=mY;
						bmpDrawPath.mode=BITMAP_MODE;
						bmpDrawPath.oreignBitmapTyep=BitmapType.DELETE_BITMAP;
						bmpDrawPath.bitmapType=BitmapType.DELETE_BITMAP;
						savePath.add(bmpDrawPath);
						translatePaint=new Paint();
						translatePaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
						rectF=new RectF(mX, mY, endX, endY);
						mCanvas.drawRect(rectF, translatePaint);
						invalidate();
						break;
					case 4:
						copyBitmap=Bitmap.createBitmap
						(mBitmap, (int)mX,(int) mY, (int)Math.abs(endX-mX),(int) Math.abs(endY-mY));
						bmpDrawPath=new DrawPath();
						bmpDrawPath.bitmap=copyBitmap;
						bmpDrawPath.oreignBitmapTyep=BitmapType.ROAT_BITMAP;
						bmpDrawPath.bitmapType=BitmapType.ROAT_BITMAP;
						bmpDrawPath.bx=mX;
						bmpDrawPath.by=mY;
						bmpDrawPath.mode=BITMAP_MODE;
						savePath.add(bmpDrawPath);
					    roatSeekBar=(SeekBar)PaintMainActivity.MActivity.findViewById(R.id.roat_seekbar);
					    roatSeekBar.setVisibility(VISIBLE);
					    roatSeekBar.setProgress(0);
					    roatSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
							
							@Override
							public void onStopTrackingTouch(SeekBar seekBar) {
								// TODO Auto-generated method stub
								//把前一张图片设置为RoatBitmap(旋转类型)
								lastDrawPath=savePath.get(savePath.size()-1);
								lastDrawPath.oreignBitmapTyep=BitmapType.ROAT_BITMAP;
								lastDrawPath.bitmapType=BitmapType.ROAT_BITMAP;
								bmpDrawPath=new DrawPath();
								bmpDrawPath.bitmap=roatBitmap;
								bmpDrawPath.oreignBitmapTyep=BitmapType.COMMON_BITMAP;
								bmpDrawPath.bitmapType=BitmapType.COMMON_BITMAP;
								bmpDrawPath.bx=mX;
								bmpDrawPath.by=mY;
								bmpDrawPath.mode=BITMAP_MODE;
								savePath.add(bmpDrawPath);
							
							}
							
							@Override
							public void onStartTrackingTouch(SeekBar seekBar) {
								// TODO Auto-generated method stub
								translatePaint=new Paint();
								translatePaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OUT));
								mCanvas.drawRect(mX, mY, endX, endY, translatePaint);
								invalidate();
								
							}
							
							@Override
							public void onProgressChanged(SeekBar seekBar, int progress,
									boolean fromUser) {
								// TODO Auto-generated method stub
								//设置旋转矩阵
								matrix=new Matrix();
								matrix.postRotate(progress, (mX+copyBitmap.getWidth())/2,(mY+copyBitmap.getHeight())/2);
								translatePaint=new Paint();
								translatePaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OUT));
								//是否初始旋转
								if(isInitRote){
									roatBitmap=Bitmap.createBitmap(copyBitmap,0, 0, copyBitmap.getWidth(), copyBitmap.getHeight(),matrix, true);
									mCanvas.drawBitmap(roatBitmap, mX, mY, mBitmapPaint);
									isInitRote=false;
									invalidate();
								}
								else{
									mCanvas.drawRect(mX, mY, mX+roatBitmap.getWidth(), mY+roatBitmap.getHeight(), translatePaint);
									roatBitmap=Bitmap.createBitmap(copyBitmap,0, 0, copyBitmap.getWidth(), copyBitmap.getHeight(),matrix, true);
									mCanvas.drawBitmap(roatBitmap, mX, mY, mBitmapPaint);
									invalidate();
								}
							}
						});
					//	matrix=new Matrix();
					//	matrix.postRotate(Integer.parseInt(roatEditText.getText().toString()), mX+copyBitmap.getWidth()/2, mY+copyBitmap.getHeight()/2);
						
						break;
					case 5:
						 //矩形区域填色对话框
					 final Dialog colorDialog=new Dialog(getContext());
						colorDialog.setTitle("填色颜色");
						colorDialog.setContentView(R.layout.dialog_for_selectcolor);
						final ColorPicker colorPicker=(ColorPicker)colorDialog.findViewById(R.id.picker);
						colorPicker.setColor(getColor());
						final OpacityBar opacityBar=(OpacityBar)colorDialog.findViewById(R.id.opacitybar);
						colorPicker.addOpacityBar(opacityBar);
						opacityBar.setOpacity(((PaintMainActivity)getContext()).getOpacity());
						Button colorOKButton=(Button)colorDialog.findViewById(R.id.colorok);
						Button colorCancelButton=(Button)colorDialog.findViewById(R.id.colorcancel);
						colorOKButton.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								setCutFillColor(colorPicker.getColor());
								copyBitmap=Bitmap.createBitmap
										(mBitmap, (int)mX,(int)mY, (int)Math.abs(endX-mX),(int) Math.abs(endY-mY));
										tempCanvas=new Canvas(copyBitmap);
										tempCanvas.drawColor(cutFillColor);
										mCanvas.drawBitmap(copyBitmap, (int)mX, (int)mY, mBitmapPaint);
										DrawPath drawPath=new DrawPath();
										drawPath.bitmap=copyBitmap;
										drawPath.bx=mX;
										drawPath.by=mY;
										drawPath.mode=BITMAP_MODE;
										savePath.add(drawPath);
										invalidate();
										
								colorDialog.dismiss();
							}
						});
						colorCancelButton.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								colorDialog.dismiss();
							}
						});
					//	colorDialog.findViewById(R.id.sv)
						colorDialog.show();
						 
						break;
					default:
							break;
					}
					dialog.dismiss();
				}
			})
			.show();
			mPath.reset();
			mPath=null;
			break;
		case COPY_MODE:
			mPath=null;
			break;
		default:
				break;
		}
		
	}
	
	public int undo() {
		
		mBitmap =Bitmap.createBitmap(PaintMainActivity.drawWidth, PaintMainActivity.drawHeight,
				Bitmap.Config.ARGB_8888);
		if(mCanvas!=null)
			mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布
		// 清空画布，但是如果图片有背景的话，则使用上面的重新初始化的方法，用该方法会将背景清空掉…
		if (savePath != null && savePath.size() > 0) {
			
			DrawPath dPath =savePath.get(savePath.size() - 1);
			canclePath.add(dPath);
			// 移除最后一个path,相当于出栈操作
			savePath.remove(savePath.size() - 1);
			if(savePath.size()>0){
				if(savePath.get(savePath.size()-1).bitmapType==BitmapType.ZOOM_BITMAP){
					savePath.get(savePath.size()-1).bitmapType=BitmapType.COMMON_BITMAP;
			//		cancelType=BitmapType.ZOOM_BITMAP;
				}
				else if(savePath.get(savePath.size()-1).bitmapType==BitmapType.ROAT_BITMAP){
					savePath.get(savePath.size()-1).bitmapType=BitmapType.COMMON_BITMAP;
			//		cancelType=BitmapType.ROAT_BITMAP;
				}
			}
			
			Iterator<DrawPath> iter = savePath.iterator();
			while(iter.hasNext()){
				DrawPath drawPath = iter.next();
				if(drawPath.mode==DRAW_MODE){
					Log.i("mCanvas is null?",""+(mCanvas==null));
					Log.i("drawPath.path is null?",""+(drawPath.path==null));
					Log.i("drawPath.paint is null?",""+(drawPath.paint==null));
					mCanvas.drawPath(drawPath.path, drawPath.paint);
				}else if(drawPath.mode==DRAW_TEXT_MODE){
					mCanvas.drawText(drawPath.drawText, drawPath.bx, drawPath.by, drawPath.paint);
				}
				else{
					Paint drawPaint=new Paint();
					drawPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
					if(drawPath.bitmapType==BitmapType.DELETE_BITMAP)
						mCanvas.drawRect(drawPath.bx, drawPath.by, drawPath.bx+drawPath.bitmap.getWidth(),
								drawPath.by+drawPath.bitmap.getHeight(), drawPaint);
					//首次出现ZOOM_BITMAP
					else if(drawPath.bitmapType==BitmapType.ZOOM_BITMAP){
						mCanvas.drawRect(drawPath.bx, drawPath.by, drawPath.bx+drawPath.bitmap.getWidth(),
								drawPath.by+drawPath.bitmap.getHeight(), drawPaint);
						invalidate();
						lastDrawPath=drawPath;
						if(iter.hasNext())
							drawPath=iter.next();
						while(drawPath.bitmapType==BitmapType.ZOOM_BITMAP){
								mCanvas.drawRect(lastDrawPath.bx, lastDrawPath.by, lastDrawPath.bx+lastDrawPath.bitmap.getWidth(),
								lastDrawPath.by+lastDrawPath.bitmap.getHeight(), drawPaint);
					//			mCanvas.drawBitmap(drawPath.bitmap, drawPath.bx, drawPath.by, mBitmapPaint);
								invalidate();
								lastDrawPath=drawPath;
								drawPath=iter.next();
							}
						mCanvas.drawBitmap(drawPath.bitmap, drawPath.bx, drawPath.by, mBitmapPaint);
						invalidate();
					}
					//旋转之后的位图
					else if(drawPath.bitmapType==BitmapType.ROAT_BITMAP){
						mCanvas.drawRect(drawPath.bx, drawPath.by, drawPath.bx+drawPath.bitmap.getWidth(),
								drawPath.by+drawPath.bitmap.getHeight(), drawPaint);
						invalidate();
						lastDrawPath=drawPath;
						if(iter.hasNext())
							drawPath=iter.next();
						while(drawPath.bitmapType==BitmapType.ROAT_BITMAP){
								mCanvas.drawRect(lastDrawPath.bx, lastDrawPath.by, lastDrawPath.bx+lastDrawPath.bitmap.getWidth(),
								lastDrawPath.by+lastDrawPath.bitmap.getHeight(), drawPaint);
								invalidate();
								lastDrawPath=drawPath;
								drawPath=iter.next();
							}
						mCanvas.drawBitmap(drawPath.bitmap, drawPath.bx, drawPath.by, mBitmapPaint);
						invalidate();
					}
					else
					{
					//	Toast.makeText(getContext(), ""+savePath.size(), Toast.LENGTH_SHORT).show();
						mCanvas.drawBitmap(drawPath.bitmap, drawPath.bx, drawPath.by,mBitmapPaint);
					//	invalidate();
					}
						
						
				}
			}
			invalidate();// 刷新
			
		}else{
			return -1;
		}
		return savePath.size();
	}
	/**
	 * 重做的核心思想就是将撤销的路径保存到另外一个集合里面(栈)， 然后从redo的集合里面取出最顶端对象， 画在画布上面即可。
	 */
	public int redo() {
		// 如果撤销你懂了的话，那就试试重做吧。
		if(canclePath.size()<1)
			return canclePath.size();
		
		mBitmap = Bitmap.createBitmap(PaintMainActivity.drawWidth, PaintMainActivity.drawHeight,
				Bitmap.Config.ARGB_8888);
		mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布
		// 清空画布，但是如果图片有背景的话，则使用上面的重新初始化的方法，用该方法会将背景清空掉…
		if (canclePath != null && canclePath.size() > 0) {
			// 移除最后一个path,相当于出栈操作
			DrawPath  dPath = canclePath.get(canclePath.size() - 1);
			savePath.add(dPath);
	//		Toast.makeText(getContext(), ""+canclePath.size() +"   "+savePath.size(), Toast.LENGTH_SHORT).show();
			canclePath.remove(canclePath.size() - 1);
			Iterator<DrawPath> iter = savePath.iterator();
			while(iter.hasNext()){
				DrawPath drawPath = iter.next();
				if(drawPath.mode==DRAW_MODE){
					mCanvas.drawPath(drawPath.path, drawPath.paint);
				}else if(drawPath.mode==DRAW_TEXT_MODE){
					mCanvas.drawText(drawPath.drawText, drawPath.bx, drawPath.by, drawPath.paint);
				}
				else{
					
					Paint drawPaint=new Paint();
					drawPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
					if(drawPath.bitmapType==BitmapType.DELETE_BITMAP)
						mCanvas.drawRect(drawPath.bx, drawPath.by, drawPath.bx+drawPath.bitmap.getWidth(),
								drawPath.by+drawPath.bitmap.getHeight(), drawPaint);
					//首次出现ZOOM_BITMAP
					else if(drawPath.oreignBitmapTyep==BitmapType.ZOOM_BITMAP){
						drawPath.bitmapType=BitmapType.ZOOM_BITMAP;
						mCanvas.drawRect(drawPath.bx, drawPath.by, drawPath.bx+drawPath.bitmap.getWidth(),
								drawPath.by+drawPath.bitmap.getHeight(), drawPaint);
						invalidate();
						lastDrawPath=drawPath;
						if(iter.hasNext())
							drawPath=iter.next();
						while(drawPath.oreignBitmapTyep==BitmapType.ZOOM_BITMAP){
								drawPath.bitmapType=BitmapType.ZOOM_BITMAP;
								mCanvas.drawRect(lastDrawPath.bx, lastDrawPath.by, lastDrawPath.bx+lastDrawPath.bitmap.getWidth(),
								lastDrawPath.by+lastDrawPath.bitmap.getHeight(), drawPaint);
					//			mCanvas.drawBitmap(drawPath.bitmap, drawPath.bx, drawPath.by, mBitmapPaint);
								invalidate();
								lastDrawPath=drawPath;
								if(iter.hasNext())
									drawPath=iter.next();
								else 
									break;
							}
						mCanvas.drawBitmap(drawPath.bitmap, drawPath.bx, drawPath.by, mBitmapPaint);
						invalidate();
						
					}
					//旋转之后的位图
					else if(drawPath.oreignBitmapTyep==BitmapType.ROAT_BITMAP){
						drawPath.bitmapType=BitmapType.ROAT_BITMAP;
						mCanvas.drawRect(drawPath.bx, drawPath.by, drawPath.bx+drawPath.bitmap.getWidth(),
								drawPath.by+drawPath.bitmap.getHeight(), drawPaint);
						invalidate();
						lastDrawPath=drawPath;
						if(iter.hasNext())
							drawPath=iter.next();
						while(drawPath.oreignBitmapTyep==BitmapType.ROAT_BITMAP){
								drawPath.bitmapType=BitmapType.ROAT_BITMAP;
								mCanvas.drawRect(lastDrawPath.bx, lastDrawPath.by, lastDrawPath.bx+lastDrawPath.bitmap.getWidth(),
								lastDrawPath.by+lastDrawPath.bitmap.getHeight(), drawPaint);
								invalidate();
								lastDrawPath=drawPath;
								if(iter.hasNext())
									drawPath=iter.next();
								else 
									break;
							}
						mCanvas.drawBitmap(drawPath.bitmap, drawPath.bx, drawPath.by, mBitmapPaint);
						invalidate();
					}
					else
						mCanvas.drawBitmap(drawPath.bitmap, drawPath.bx, drawPath.by,mBitmapPaint);
						
				}
			}
			invalidate();// 刷新
			
		}else{
			return -1;
		}
		
		invalidate();// 刷新
		//}
		return canclePath.size();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	//	getParent().requestDisallowInterceptTouchEvent(true);
		float x = event.getX();
		float y = event.getY();
		//侧边滑动监听
		switch (event.getAction()) {
		
		case MotionEvent.ACTION_DOWN:

			if(zoomSeekBar!=null&&zoomSeekBar.getVisibility()==VISIBLE){
					zoomSeekBar.setVisibility(INVISIBLE);
					//isInitInZoom=true;
			}
			if(roatSeekBar!=null&&roatSeekBar.getVisibility()==VISIBLE)
			{
				roatSeekBar.setVisibility(INVISIBLE);
				isInitRote=true;
			}
			if(PaintMainActivity.MActivity.getZoomCanvans().getVisibility()==View.VISIBLE)
				PaintMainActivity.MActivity.getZoomCanvans().setVisibility(View.INVISIBLE);	
			if(listenMode==PAINT_MODE){
				initPaint();
				//重置下一步操作,前进步骤
				canclePath = new ArrayList<DrawPath>();
				// 每次down下去重新new一个Path
				mPath = new Path();
				// 每一次记录的路径对象是不一样的
				dp = new DrawPath();
				dp.mode=DRAW_MODE;
				touch_start(x, y);
				invalidate();
				
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(listenMode==PAINT_MODE){
				touch_move(x, y);
				invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			if(listenMode==PAINT_MODE){
				endX=event.getX();
				endY=event.getY();
				if(mPath!=null)
					touch_up();
				invalidate();
			}
			else{
				listenMode=PAINT_MODE;
		}
			break;
			}
		return true;
	}
	//初始化文字绘制画笔
	public void initTextPaint(){
		textPaint=new Paint();
		textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		textPaint.setStrokeWidth(getPenSize());
		textPaint.setTextSize(20*getPenSize());
		textPaint.setAntiAlias(true);
		textPaint.setStrokeCap(Paint.Cap.ROUND);
		textPaint.setStrokeJoin(Paint.Join.ROUND);
	//	textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(getColor());
		//textPaint.
	}
	
	public void setPaintText(String paintText){
		this.paintText=paintText;
	}
	/*Flood Fill Algorithm*/
	private void FloodFill(Bitmap bmp, Point pt, int targetColor, int replacementColor) 
	{
	    FloodFillAlgorithm floodFillAlgorithm=new FloodFillAlgorithm(bmp);
	    floodFillAlgorithm.floodFillScanLineWithStack(pt.x, pt.y, replacementColor, targetColor);
	    tempBitmap=Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight());
	    //将填色之后的Bitmap加入savePath中
	    if(floodFillAlgorithm.isFill()){
	    	bmpDrawPath=new DrawPath();
	  	    bmpDrawPath.mode=BITMAP_MODE;
	  	    bmpDrawPath.bitmap=tempBitmap;
	  	    bmpDrawPath.bitmapType=BitmapType.COMMON_BITMAP;
	  	    bmpDrawPath.oreignBitmapTyep=BitmapType.COMMON_BITMAP;
	  	    bmpDrawPath.bx=0;
	  	    bmpDrawPath.by=0;
	  	    savePath.add(bmpDrawPath);
	    }
	  
	}
	
	//FloodFill类
	class FloodFillAlgorithm {

		private Bitmap fillBitmap;
	//	private int[] inPixels;
		private int width;
		private int height;
		private boolean isFillColor=true;
		// 	stack data structure
		private int maxStackSize = 500; // will be increased as needed
		private int[] xstack = new int[maxStackSize];
		private int[] ystack = new int[maxStackSize];
		private int stackSize;

		public FloodFillAlgorithm(Bitmap fillBitmap) {
			this.fillBitmap = fillBitmap;
			width = fillBitmap.getWidth();
	        height = fillBitmap.getHeight();
	   //     inPixels = new int[width*height];
	   //     getColor(point.x, point.y);
		}

		public int getColor(int x, int y)
		{
			
			return fillBitmap.getPixel(x, y);
		}
		
		public void setColor(int x, int y, int newColor)
		{
			fillBitmap.setPixel(x, y, newColor);
		}
		/**
		 * 判断是封闭区域是否填充
		 * @return 
		 */
		public boolean isFill(){
			return isFillColor;
		}
		public void floodFillScanLineWithStack(int x, int y, int newColor, int oldColor)
		{
			if(oldColor!=Color.TRANSPARENT){
				isFillColor=false;
				return ;
			}
				
			
			if(oldColor == newColor) {
				System.out.println("do nothing !!!, filled area!!");
				return;
			}
		    emptyStack();
		    
		    int y1; 
		    boolean spanLeft, spanRight;
		    push(x, y);
		    
		    while(true)
		    {    
		    	x = popx();
		    	if(x == -1) return;
		    	y = popy();
		        y1 = y;
		        while(y1 >= 0 && getColor(x, y1) == oldColor) y1--; // go to line top/bottom
		        y1++; // start from line starting point pixel
		        spanLeft = spanRight = false;
		        while(y1 < height && getColor(x, y1) == oldColor)
		        {
		        	setColor(x, y1, newColor);
		            if(!spanLeft && x > 0 && getColor(x - 1, y1) == oldColor)// just keep left line once in the stack
		            {
		                push(x - 1, y1);
		                spanLeft = true;
		            }
		            else if(spanLeft && x > 0 && getColor(x - 1, y1) != oldColor)
		            {
		                spanLeft = false;
		            }
		            if(!spanRight && x < width - 1 && getColor(x + 1, y1) == oldColor) // just keep right line once in the stack
		            {
		                push(x + 1, y1);
		                spanRight = true;
		            }
		            else if(spanRight && x < width - 1 && getColor(x + 1, y1) != oldColor)
		            {
		                spanRight = false;
		            } 
		            y1++;
		        }
		    }
			
		}
		
		private void emptyStack() {
			while(popx() != - 1) {
				popy();
			}
			stackSize = 0;
		}

		final void push(int x, int y) {
			stackSize++;
			if (stackSize==maxStackSize) {
				int[] newXStack = new int[maxStackSize*2];
				int[] newYStack = new int[maxStackSize*2];
				System.arraycopy(xstack, 0, newXStack, 0, maxStackSize);
				System.arraycopy(ystack, 0, newYStack, 0, maxStackSize);
				xstack = newXStack;
				ystack = newYStack;
				maxStackSize *= 2;
			}
			xstack[stackSize-1] = x;
			ystack[stackSize-1] = y;
		}
		
		final int popx() {
			if (stackSize==0)
				return -1;
			else
	            return xstack[stackSize-1];
		}

		final int popy() {
	        int value = ystack[stackSize-1];
	        stackSize--;
	        return value;
		}

	}

}
