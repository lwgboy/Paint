package com.freedoodle.system;
import java.util.Date;

import com.freedoodle.config.Config;
import com.freedoodle.config.Const;

import cn.edu.fjnu.uitls.system.BaseApplication;
import cn.edu.fjnu.utils.OPUtils;

public class PaintApplication extends BaseApplication {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		Const.appContext=getApplicationContext();
		
		//获取天数和上次广告投放时间
		String dateNum= Config.getValue(Const.Key.DATE_NUM);
		//String lastTime=Config.getValue(Const.Key.LAST_TIME);
		if("".equals(dateNum)){
			Config.saveValue(Const.Key.DATE_NUM, "0");
			Config.saveValue(Const.Key.LAST_TIME, String.valueOf(System.currentTimeMillis()));
		}
		
		Const.dateNum=Integer.parseInt( Config.getValue(Const.Key.DATE_NUM));
		Const.lastTime=Long.parseLong(Config.getValue(Const.Key.LAST_TIME));
		Const.currentTime=System.currentTimeMillis();
		
		//记录软件安装时间
		OPUtils.saveValToSharedpreferences(Const.Key.APP_INSTALL_TIME,new Date().getTime()+"");
	}
	
	
	
}
