package com.freedoodle.config;

import android.content.Context;

/**
 * 一些常量
 * @author GaoFei
 *
 */

public class Const {

	public static Context appContext;
	public static int dateNum;
	public static long lastTime;
	public static long currentTime;
	
	public static final long ONE_DAY_MILL=24*60*60*1000;
	public static final String SHARED_NAME="shared_name";
	public static class Key{
		public static final String DATE_NUM="date_num";
		public static final String LAST_TIME="last_time";
		public static final String UPDATE_TIP ="update_tip";
		public static final String DOWNLOAD_ID ="download_id";
		//软件安装更新时间
		public static final String APP_INSTALL_TIME ="app_instanll_time";
	}
	
	
}
