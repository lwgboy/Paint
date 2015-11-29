package cn.fjnu.edu.ui.activity;
import java.util.List;
import cn.edu.fjnu.utils.PackageUtils;
import cn.edu.fjnu.utils.ViewUtils;
import cn.fjnu.edu.paint.R;
import cn.fjnu.edu.paint.adapter.APPListAdapter;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;


/**
 * @author  GaoFei
 * @version 2015.3.27
 * 应用推荐页面
 *
 */
public class RecomActivity extends Activity {

	private ViewUtils viewUtils;
	private ListView appListView;
	private String[] packNames;
	private List<PackageInfo> packageInfos;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		initData();
		initView();
		setContentView(viewUtils.getRelativeLayout());
		initEvent();
	}

	public void initView(){
		viewUtils=new ViewUtils(this);
		
		appListView=new ListView(this);
		appListView.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
		viewUtils.addView(appListView);
		APPListAdapter appListAdapter=new APPListAdapter(this);
		appListView.setAdapter(appListAdapter);
		
	}
	
	public void initEvent(){
		appListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub;
				int index=0;
				for(PackageInfo packageInfo: packageInfos){
					//Log.i(packageInfo.packageName,packageInfo.packageName);
					index++;
					if(packageInfo.packageName.equals(packNames[position]))
						break;
					
					
				}
				
				if(index==packageInfos.size()){
					Uri uri=Uri.parse("market://search?q=pname:"+packNames[position]);
					Intent intent=new Intent(Intent.ACTION_VIEW);
					intent.setData(uri);
					
					PackageManager pManager=getPackageManager();
					if(intent.resolveActivity(pManager)!=null)
						startActivity(intent);
				}
			}
		});
	}
	
	public void initData(){
		packageInfos=PackageUtils.getAllApp(this);
		packNames=getResources().getStringArray(R.array.packagenames);
	}
}
