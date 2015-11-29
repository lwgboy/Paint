/**
 * 
 */
package com.freedoodle.adapter;

import java.util.List;
import cn.edu.fjnu.utils.DeviceInfoUtils;
import cn.edu.fjnu.utils.PackageUtils;
import cn.edu.fjnu.utils.ViewUtils;
import cn.fjnu.edu.paint.R;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * @author Administrator
 *
 */
public class APPListAdapter extends BaseAdapter {
	
	private List<PackageInfo> packageInfos;
	private String[] desNames;
	private String[] appNames;
	private String[] packNames;
	private Context context;
	private int itemHeight;
	private float viewX;
	private float viewY;
	private int viewWidth;
	private int viewHeight;
	
 	public APPListAdapter(Context context){
		this.context=context;
		ViewUtils measureUtils=new ViewUtils(context);
		itemHeight=measureUtils.getPixelFromDp(50);
		packageInfos=PackageUtils.getAllApp(context);
		desNames=context.getResources().getStringArray(R.array.desnames);
		appNames=context.getResources().getStringArray(R.array.appnames);
		packNames=context.getResources().getStringArray(R.array.packagenames);
	}
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return appNames.length;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return appNames[position];
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view=createView(position);
		return view;
		//return createView(position);
	}

	public View createView(final int position){
		
		ViewUtils viewUtils=new ViewUtils(context);
		
		ImageView appImageView=new ImageView(context);
		int imgID=context.getResources().getIdentifier("rm"+position, "drawable",context.getPackageName());
		appImageView.setImageResource(imgID);
		viewX=1.0f/10*itemHeight;
		viewY=viewX;
		viewWidth=(int)(4.0f/5*itemHeight);
		viewHeight=viewWidth;
		viewUtils.addView(appImageView, viewX, viewY, viewWidth, viewHeight);
		
		
		TextView appTextView=new TextView(context);
		viewX=viewUtils.getViewX(appImageView)+viewUtils.getViewWidth(appImageView);
		viewY=viewUtils.getViewY(appImageView);
		viewWidth=RelativeLayout.LayoutParams.WRAP_CONTENT;
		viewHeight=(int)(1.0f/2*viewUtils.getViewHeight(appImageView));
		appTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 4.0f/5*viewHeight);
		appTextView.setGravity(Gravity.TOP);
		viewUtils.addView(appTextView, viewX, viewY, viewWidth, viewHeight);
		
		
		TextView desTextView=new TextView(context);
		viewX=viewUtils.getViewX(appTextView);
		viewY=viewUtils.getViewY(appImageView)+3.0f/4*viewUtils.getViewHeight(appImageView);
		viewWidth=RelativeLayout.LayoutParams.WRAP_CONTENT;
		viewHeight=(int)(2.0f/3*viewUtils.getViewHeight(appTextView));
		desTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 4.0f/5*viewHeight);
		desTextView.setGravity(Gravity.BOTTOM);
		viewUtils.addView(desTextView, viewX, viewY, viewWidth, viewHeight);
		
		
		appTextView.setText(appNames[position]);
		desTextView.setText(desNames[position]);
		//packageInfos.contains(object)
		//appTextView.
		
		ImageView installImageView=new ImageView(context);
		viewX=DeviceInfoUtils.getScreenWidth(context)-viewUtils.getViewX(appImageView)-8.0f/5*itemHeight;
		viewY=viewUtils.getViewY(appImageView);
		viewHeight=(int)(4.0f/5*itemHeight);
		viewWidth=2*viewHeight;
		installImageView.setBackgroundResource(R.drawable.install_state);
		viewUtils.addView(installImageView, viewX, viewY, viewWidth, viewHeight);
		installImageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Uri uri=Uri.parse("market://search?q=pname:"+packNames[position]);
				Intent intent=new Intent(Intent.ACTION_VIEW);
				intent.setData(uri);
				
				PackageManager pManager=context.getPackageManager();
				if(intent.resolveActivity(pManager)!=null)
					context.startActivity(intent);
			}
		});
		
		
		//OPUtils.showToast(packageInfos.toString(), Toast.LENGTH_SHORT);
		for(PackageInfo packageInfo:packageInfos){
			//Log.i("packName",packageInfo.packageName);
			if(packageInfo.packageName.equals(packNames[position])){
				viewUtils.removeView(installImageView);
				TextView installTextView=new TextView(context);
				viewX=DeviceInfoUtils.getScreenWidth(context)-viewUtils.getViewX(appImageView)-8.0f/5*itemHeight;
				viewY=viewUtils.getViewY(appImageView);
				viewHeight=(int)(4.0f/5*itemHeight);
				viewWidth=2*viewHeight;
				installTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,1.0f/3*viewHeight);
				installTextView.setGravity(Gravity.CENTER);
				installTextView.setTextColor(Color.RED);
				installTextView.setText("ÒÑ°²×°");
				viewUtils.addView(installTextView, viewX, viewY, viewWidth, viewHeight);
				break;
			}
		}
		viewUtils.getRelativeLayout().setLayoutParams(new AbsListView.LayoutParams(-1, itemHeight));
		return viewUtils.getRelativeLayout();
	}
}
