package cn.fjnu.edu.ui.activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import cn.edu.fjnu.utils.PackageUtils;
import cn.edu.fjnu.utils.ViewUtils;
import cn.fjnu.edu.paint.R;
import cn.fjnu.edu.paint.adapter.APPListAdapter;
import cn.fjnu.edu.paint.config.Const;
public class RecomActivity extends AppBaseActivity {

	private ViewUtils viewUtils;
	private ListView appListView;
	private String[] packNames;
	private String[] mDownloadUrls;
	private List<PackageInfo> packageInfos;
	private ProgressDialog mDownloadDialog;
	private DownloadAppTask mDownloadTask;
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
				int index=0;
				for(PackageInfo packageInfo: packageInfos){
					index++;
					if(packageInfo.packageName.equals(packNames[position]))
						break;
				}
				
				if(index==packageInfos.size()){
					mDownloadDialog = new ProgressDialog(RecomActivity.this);
					mDownloadDialog.setCancelable(false);
					mDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					mDownloadDialog.setProgress(0);
					mDownloadDialog.setMax(100);
					mDownloadDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(mDownloadTask != null && mDownloadTask.getStatus() == AsyncTask.Status.RUNNING)
								mDownloadTask.cancel(true);
						}
					});
					mDownloadDialog.show();
					mDownloadTask = new DownloadAppTask(new DownloadAppTask.CallBack() {
						@Override
						public void onSuccess(String downloadPath) {
							Toast.makeText(getApplicationContext(), "下载完成", Toast.LENGTH_SHORT).show();
							mDownloadDialog.dismiss();
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.fromFile(new File(downloadPath)), "application/vnd.android.package-archive");
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}

						@Override
						public void onFailed() {
							Toast.makeText(getApplicationContext(), "下载失败，请重试", Toast.LENGTH_SHORT).show();
							mDownloadDialog.dismiss();
						}

						@Override
						public void onUpdate(Integer progress) {
							mDownloadDialog.setProgress(progress);
						}
					});
					mDownloadTask.execute(mDownloadUrls[position]);

				}
			}
		});
	}
	
	public void initData(){
		packageInfos=PackageUtils.getAllApp(this);
		packNames=getResources().getStringArray(R.array.packagenames);
		mDownloadUrls = getResources().getStringArray(R.array.downloadURL);
	}

	public static class DownloadAppTask extends AsyncTask<String,Integer,Integer>{
		public interface CallBack{
			void onSuccess(String downloadPath);
			void onFailed();
			void onUpdate(Integer progress);
		}
		public DownloadAppTask(CallBack callBack){
			this.mCallBack = callBack;
		}
		private CallBack mCallBack;
		public  static final Integer SUCC = 1;
		public  static final Integer FAIL = -1;
		private String mDownloadPath;
		@Override
		protected Integer doInBackground(String... params) {

			HttpURLConnection connection = null;
			try {
				connection = (HttpURLConnection) (new URL(params[0]).openConnection());
				connection.connect();
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					return FAIL;
				}
				File appDirFile = new File(Const.AppDownloadDir);
				if(!appDirFile.exists())
					appDirFile.mkdirs();
				File appFile =  new File(appDirFile, UUID.randomUUID().toString() + ".apk");
				mDownloadPath = appFile.getAbsolutePath();
				FileOutputStream appOutStream = new FileOutputStream(appFile);
				int fileLength = connection.getContentLength();
			//	Log.i(TAG, "文件长度:" + fileLength);
				InputStream inputStream = connection.getInputStream();
				byte[] readBuffer = new byte[2048];
				int readBytes = inputStream.read(readBuffer,0, readBuffer.length);
			//	Log.i(TAG, "开始写入文件:" + fileLength);
				int totalReadCount = 0;
				while(readBytes > 0){
					appOutStream.write(readBuffer, 0, readBytes);
					totalReadCount += readBytes;
					publishProgress((int)(100 * (totalReadCount * 1.0 / fileLength)));
					readBytes = inputStream.read(readBuffer, 0, readBuffer.length);
				}
				return SUCC;
			//	Log.i(TAG, "写入文件完成");
			} catch (IOException e) {
				e.printStackTrace();
				return FAIL;
			}

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			mCallBack.onUpdate(values[0]);
		}

		@Override
		protected void onPostExecute(Integer integer) {
			if(integer == SUCC)
				mCallBack.onSuccess(mDownloadPath);
			else
				mCallBack.onFailed();
		}
	}
}
