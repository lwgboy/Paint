package cn.fjnu.edu.paint.domain;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;
/*绘制路径类*/
public class DrawPath {
	public Path path;// 路径
	public Paint paint;// 画笔
	public Bitmap bitmap;//贴图
	public int mode;//贴图模式还是画图模式
	public float bx;//位图初始x坐标
	public float by;//位图y坐标
	public int bitmapType;//位图类型
	public int oreignBitmapTyep;//原位图类型
	public String drawText;//绘制文字
}
