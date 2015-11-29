package com.freedoodle.engine;

import java.io.File;
import com.freedoodle.adapter.CustomPhotoAdapter;
import com.freedoodle.ui.activity.PaintMainActivity;
import com.freedoodle.ui.activity.MultiPhotoSelectActivity;

import cn.fjnu.edu.paint.R;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
//窗口类  背景图的3种选择方式
public class Background extends Dialog {

		public Background(Context context, int theme) {
			super(context);
			setContentView(R.layout.dialog_for_newcreate);
			ListView listView = (ListView) findViewById(R.id.newlist);
			String[] way = { "相机", "文件", "自带" ,"拼图"};
			ArrayAdapter<String> adapter=new ArrayAdapter<String>(context,R.layout.list_text_center,way);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					//Toast.makeText(getContext(),""+position,Toast.LENGTH_SHORT).show();
					switch(position){
					case 0:
						try{
							File file=new File(Environment.getExternalStorageDirectory(),"test.jpg");
							Uri outPutUri=Uri.fromFile(file);
							PaintMainActivity.photopath=file.getAbsolutePath();
							Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							/*Intent intent=new Intent(MainActivity.MActivity, CameraActivity.class);
							MainActivity.MActivity.startActivity(intent);*/
							intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
							PaintMainActivity.MActivity.startActivityForResult(intent, 1);
						}
						catch(Exception e){
							e.printStackTrace();
						}
						dismiss();
						break;
					case 1:
						Intent intent = new Intent();
						intent.setType("image/*");//打开图片方式
						intent.setAction(Intent.ACTION_GET_CONTENT); 
						//
						//MainActivity.saveType=
				//		MainActivity.saveType=MainActivity.BACK_MODE;
						PaintMainActivity.MActivity.startActivityForResult(intent,2);
						dismiss();
						break;
					case 2:
						dismiss();
						GridView gridView;;
						final Dialog backDialog=new Dialog(getContext(), android.R.style.Theme_Holo_Light_Dialog);
						backDialog.setTitle("选择背景图");
						backDialog.setContentView(R.layout.pastephoto_layout);
						gridView=(GridView)backDialog.findViewById(R.id.paste_grid);
						gridView.setGravity(Gravity.CENTER);
						gridView.setColumnWidth(PaintMainActivity.screenWidth/3);
						gridView.setHorizontalSpacing(5);
						gridView.setVerticalSpacing(5);
						CustomPhotoAdapter customPhotoAdapter=new CustomPhotoAdapter(PaintMainActivity.MActivity);
						gridView.setAdapter(customPhotoAdapter);
						gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {
								// TODO Auto-generated method stub
								//MainActivity.canvasView.setScaleType(ScaleType.MATRIX);
								Bitmap bitmap=BitmapFactory.decodeResource(PaintMainActivity.MActivity.getResources(), PaintMainActivity.bigBackId[position]);
								int widthCount=PaintMainActivity.drawWidth/bitmap.getWidth();
								int heightCount=PaintMainActivity.drawHeight/bitmap.getHeight();
								int paintWidth=widthCount*bitmap.getWidth();
								int paintHeight=heightCount*bitmap.getHeight();
								Bitmap drawBitmap=Bitmap.createBitmap(paintWidth, paintHeight, Config.RGB_565);
								Canvas mCanvas=new Canvas(drawBitmap);
								for(int i=0;i<heightCount;i++)
									for(int j=0;j<widthCount;j++){
										mCanvas.drawBitmap(bitmap,j*bitmap.getWidth(), i*bitmap.getHeight(),null);
									}
								PaintMainActivity.canvasView.setImageBitmap(drawBitmap);
								backDialog.dismiss();
							}
						});
						//gridView.setAdapter(adapter);
						backDialog.show();
				
						break;
					case 3:
						dismiss();
						Intent mulIntent=new Intent(PaintMainActivity.MActivity,MultiPhotoSelectActivity.class);
						PaintMainActivity.MActivity.startActivity(mulIntent);
						break;
					}
				}
			});
		}

	}