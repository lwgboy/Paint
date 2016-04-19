package cn.fjnu.edu.paint.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import cn.fjnu.edu.paint.R;
import cn.fjnu.edu.ui.activity.PaintMainActivity;


/**
 * 
 * @author GaoFei
 *
 */
public class CustomPhotoAdapter extends BaseAdapter{
	private Context context;
	private List<Integer> photoId=new ArrayList<Integer>();
	private int resId;
	public CustomPhotoAdapter(PaintMainActivity mActivity){
		context=mActivity;
		for(int i=0;i<46;i++){
				resId=mActivity.getResources().getIdentifier("b"+i,"drawable", context.getPackageName());
				photoId.add(resId);
		}
	}
	@Override
	public int getCount() {
		return photoId.size();
	}

	@Override
	public Object getItem(int position) {
		
		return position;
	}

	@Override
	public long getItemId(int position) {
		return photoId.get(position);
	}

	@Override
	public View getView(int position, View contentView, ViewGroup parentView) {
		LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout linearlayout= (LinearLayout)inflater.inflate(R.layout.paste_img_layout, null);
		ImageView imageView=(ImageView) linearlayout.findViewById(R.id.displaygridphoto);
		imageView.setScaleType(ScaleType.CENTER_INSIDE);
		imageView.setLayoutParams(new LinearLayout.LayoutParams(PaintMainActivity.screenWidth/4, LinearLayout.LayoutParams.WRAP_CONTENT));
		resId=context.getResources().getIdentifier("b"+position,"drawable", context.getPackageName());
		imageView.setImageResource(resId);
		return linearlayout;
	}
}
