package cn.fjnu.edu.paint.adapter;
import java.util.ArrayList;
import java.util.List;

import cn.fjnu.edu.paint.R;
import cn.fjnu.edu.ui.activity.PaintMainActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
/**
 * 
 * @author GaoFei
 *
 */
public class PastePhotoAdapter extends BaseAdapter{

	private Context context;
	private List<Integer> photoId=new ArrayList<Integer>();
	private int resId;
	public PastePhotoAdapter(PaintMainActivity mainActivity){
		context=mainActivity;
		//299
		for(int i=0;i<319;i++){
				resId=mainActivity.getResources().getIdentifier("p"+i,"drawable", context.getPackageName());
				photoId.add(resId);
		}
		//initGrid();
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return photoId.size();
	}

	@Override
	public Object getItem(int position) {
		
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return photoId.get(position);
	}

	@Override
	public View getView(int position, View contentView, ViewGroup parentView) {
		// TODO Auto-generated method stub
	//	Context.LAYOUT_INFLATER_SERVICE
		LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout linearlayout= (LinearLayout)inflater.inflate(R.layout.paste_img_layout, null);
		ImageView imageView=(ImageView) linearlayout.findViewById(R.id.displaygridphoto);
		imageView.setScaleType(ScaleType.CENTER_INSIDE);
		imageView.setLayoutParams(new LinearLayout.LayoutParams(PaintMainActivity.screenWidth/10, PaintMainActivity.screenWidth/10));
		imageView.setImageResource(photoId.get(position));
		return linearlayout;
	}

}
