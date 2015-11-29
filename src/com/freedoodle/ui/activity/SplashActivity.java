package com.freedoodle.ui.activity;
import cn.fjnu.edu.paint.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {
	private LinearLayout ll_splash_main = null;
	private TextView tv_splash_version = null;
	private String versionText = null;
	private ProgressDialog pd = null;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {

		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			loadMainUI();

		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.setContentView(R.layout.splash);
		ll_splash_main = (LinearLayout) findViewById(R.id.ll_splash_main);
		tv_splash_version = (TextView) super
				.findViewById(R.id.tv_splash_version);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		versionText = this.getVersion();
		tv_splash_version.append(versionText);
		new Thread() {
			public void run() {

				try {
					Thread.sleep(4000);
					handler.sendEmptyMessage(0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();

		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);
		aa.setDuration(3000);
		ll_splash_main.startAnimation(aa);

	}


	public String getVersion() {
		try {
			PackageManager manager = getPackageManager();
			PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
			return info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

	}
	
	private void loadMainUI() {

	       	 Intent intent = new Intent(this,PaintMainActivity.class);
			 startActivity(intent);
			 this.finish();
	        
	}

}
