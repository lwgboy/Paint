package com.freedoodle.service;

import java.io.File;

import cn.edu.fjnu.uitls.domain.DownloadInfo;
import cn.edu.fjnu.utils.DownloadUtils;
import cn.edu.fjnu.utils.OPUtils;

import com.freedoodle.config.Const;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * @author GaoFei
 *
 */
public class DownloadReceiver extends BroadcastReceiver {

	private static final String TAG = "DownloadReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		
		//监听下载是否完成
		if(intent.getAction() == DownloadManager.ACTION_DOWNLOAD_COMPLETE){
			
			long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
		 	String downloadID = OPUtils.getValFromSharedpreferences(Const.Key.DOWNLOAD_ID);
		 	if(Long.parseLong(downloadID)==reference){
		 		Log.i(TAG,"downloadID:"+downloadID);
		 		DownloadInfo info = DownloadUtils.getDownloadInfoById(reference);
		 		Log.i(TAG,""+info.getDescription());
		 		File downloadFile = new File(info.getLocalFileName());
		 		Intent installIntent = new Intent(Intent.ACTION_VIEW);
		 		installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 		installIntent.setDataAndType(Uri.fromFile(downloadFile),"application/vnd.android.package-archive");
		 		context.startActivity(installIntent);
		 		
		 		//InnerActivityUtils.accessUrl(context, "http://www.baidu.com");
		 	}
		}

	}

}
