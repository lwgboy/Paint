package com.freedoodle.ui.activity;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.freedoodle.adapter.PastePhotoAdapter;
import com.freedoodle.config.Config;
import com.freedoodle.config.Const;
import com.freedoodle.data.Shape_Type;
import com.freedoodle.engine.Background;
import com.freedoodle.engine.DrawView;
import com.freedoodle.ui.ColorPicker;
import com.freedoodle.ui.DisplayPenSizeView;
import com.freedoodle.ui.OpacityBar;
import com.gfinsert.YManager;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import cn.edu.fjnu.utils.DownloadUtils;
import cn.edu.fjnu.utils.OPUtils;
import cn.edu.fjnu.utils.ResourceUtils;
import cn.fjnu.edu.paint.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.text.TextPaint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

@SuppressLint("SimpleDateFormat")
public class PaintMainActivity extends Activity {

	private static final String TAG = "PaintMainActivity";
	public static final int SHARE_MODE = 0;// 分享模式
	public static final int SAVE_MODE = 1;// 保存模式
	public static final int CUT_MODE = 2;// 图片剪切模式
	public static final int BACK_MODE = 3;// 背景图模式
	public static int saveType = SAVE_MODE;// 图片保存类型
	public static String photopath;// 照片之后获取的路径
	public static PaintMainActivity MActivity;// 得到MainAcitivity对象本身
	public static int[] backID = new int[46];
	public static int[] bigBackId = new int[46];
	public static boolean isReduce = false;// 判断画布是否缩小
	public static boolean isLoad = true;// 是否初次载入
	public boolean isMeasure = true;// 是否初次测量屏幕的大小
	private final int DEFAULT_WIDTH = 480;
	private final int DEFAULT_HEIGHT = 800;
	private int opacity = 0xff;// 画笔颜色透明度
	private DrawView canvansImageView;// 用于绘图的imageview
	private ImageView main_shapeImageView;// 用于画笔粗细显示
	private ImageView main_clearImageView;// 清空画布
	private ImageView main_eraserImageView;// 橡皮擦
	private ImageView main_backgroundImageView;// 背景图
	private ImageView main_drawtypeImageView;// 绘图类型
	private ImageView main_newcreateImageView;// 新建
	private ImageView main_penImageView;// 画笔
	private ImageView main_colorImageView;// 画笔颜色
	private SeekBar penSeekBar;// 拖动条控制画笔粗细
	private TextView penTextView;// 指示当前画笔粗细的值
	private DisplayPenSizeView processImageView;// 指示当前画笔粗细
	private int penSize;// 显示的画笔粗细大小
	private Button process_okButton;// 画笔粗细设置的确定按钮
	private Button process_cancelButton;// 画笔粗细设置的取消按钮
	private Dialog setsizeDialog;// 设置画笔粗细的对话框
	private View mainView;// RelativeLayout布局
	private ZoomControls zoomCanvas;// 控制画布大小
	private float oreignWidthScalex;
	private float oreignHeightScalex;
	private int penProcess = 10;// 画笔大小设置的进度条值
	private int orignPenProcess;
	public static int drawWidth;// 绘画的宽度
	public static int drawHeight;// 绘画的高度
	public static int screenWidth;// 屏幕宽度
	public static int screenHeight;// 屏幕高度
	public static DrawView canvasView;// 用于绘制的DrawView
	private int createWidth;
	private int createHeight;
	private android.widget.LinearLayout.LayoutParams createLayoutParams;
	private boolean isBlackColor=false;
	//待绘制的文字
	private String paintText=null;
	//用于绘制文字的画笔
	private TextPaint textPaint=null;
	/**广告显示*/
	private YManager myPPCC;
	/**更新提示*/
	private String updateTip = "";
	/**更新的版本*/
	private String updateVersion = "";
	/**新版本下载路径*/
	private String downloadPath = "";
	private  Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what==1){
				myPPCC.showInsert(-1, -1, null, 1);
				
				
			}else if(msg.what==2){
				myPPCC.showGfSlider();
				myPPCC.enableGfBackground();
			}else if(msg.what==3){
				//获取当前时间,和软件安装更新时间对比,5天之后才会更新
				long currentTime = new Date().getTime();
				long installTime = Long.parseLong(OPUtils.getValFromSharedpreferences(Const.Key.APP_INSTALL_TIME));
				if(installTime - currentTime<5*Const.ONE_DAY_MILL){
					
					return ;
				}
				
				String updateTipResult = OPUtils.getValFromSharedpreferences(Const.Key.UPDATE_TIP);
				if("true".equals(updateTipResult)){
					
					return ;
				}
				//if(OPUtils.isEmpty(updateTipResult))
				//提示版本更新
				new AlertDialog.Builder(PaintMainActivity.this)
				.setTitle("版本更新v"+updateVersion).setMessage(updateTip)
				.setPositiveButton("立即更新",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						File downloadDirFile = new File(Environment.getExternalStorageDirectory(),"Paint/download");
						if(!downloadDirFile.exists())
							downloadDirFile.mkdirs();
						File nameFile = new File(downloadDirFile,"涂鸦画图v"+updateVersion+".apk");
						try {
							if(!nameFile.exists())
								nameFile.createNewFile();
							
						} catch (Exception e) {
							
							Log.i(TAG,""+e);
						}
						
						Log.i(TAG,"下载路径:"+downloadPath);
						long downloadId = DownloadUtils.downloadFile(downloadPath,DownloadManager.Request.VISIBILITY_VISIBLE,ResourceUtils.getString(R.string.app_name)+"v"+updateVersion,"版本更新", nameFile);
						OPUtils.saveValToSharedpreferences(Const.Key.DOWNLOAD_ID,""+downloadId);
					}
				}).setNegativeButton("取消",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						dialog.dismiss();
						
					}
				}).setNeutralButton("不再提示", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						OPUtils.saveValToSharedpreferences(Const.Key.UPDATE_TIP, "true");
						dialog.dismiss();
						
					}
				}).show();
				
				
				
			}
		}
		
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_paint);
		initAD();
		init();
		initMainImage();
		initIMageLoader();
		updateApp();
		canvansImageView.setImageResource(R.drawable.app_rm);
	}
	
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stubei
		super.onWindowFocusChanged(hasFocus);
		if (isLoad) {
			drawWidth = mainView.getWidth();
			drawHeight = mainView.getHeight();
			canvansImageView
					.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
							mainView.getWidth(), mainView.getHeight()));
			canvansImageView.setImageResource(R.drawable.whitebackground);
			isLoad = false;
			if (isMeasure) {
				screenWidth = mainView.getWidth();
				screenHeight = mainView.getHeight();
				isMeasure = false;
			}
		}

	}

	public void init() {
		MActivity = PaintMainActivity.this;
		// 创建目录
		String saveDir = Environment.getExternalStorageDirectory()
				+ "/drawphoto";
		File saveDirFile = new File(saveDir);
		if (!saveDirFile.exists())
			saveDirFile.mkdirs();
		for (int i = 0; i < 46; i++) {
			int resID = getResources().getIdentifier("b" + i, "drawable",
					getPackageName());
			if (resID != 0) {
				backID[i] = resID;
			}
			resID = getResources().getIdentifier("b" + i, "drawable",
					getPackageName());
			if (resID != 0) {
				bigBackId[i] = resID;
			}

		}
		canvansImageView = (DrawView) findViewById(R.id.img_canvans);
		canvasView = canvansImageView;
		mainView = findViewById(R.id.rlay);
		zoomCanvas = (ZoomControls) findViewById(R.id.zoom_control);
		zoomCanvas.setVisibility(View.INVISIBLE);
		oreignWidthScalex = canvansImageView.getScaleX();
		oreignHeightScalex = canvansImageView.getScaleY();
		// 放大画布监听
		zoomCanvas.setOnZoomInClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				float widthScalex = canvansImageView.getScaleX();
				float heightScalex = canvansImageView.getScaleY();
				if (widthScalex + 0.2 < 5 && heightScalex + 0.2 < 5) {
					canvansImageView.setScaleX((float) (widthScalex + 0.2));
					canvansImageView.setScaleY((float) (heightScalex + 0.2));
				}
				if (oreignHeightScalex < heightScalex
						|| oreignWidthScalex < widthScalex) {
					isReduce = false;
				}
			}
		});
		// 缩小画布监听
		zoomCanvas.setOnZoomOutClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				float widthScalex = canvansImageView.getScaleX();
				float heightScalex = canvansImageView.getScaleY();
				if (widthScalex - 0.2 > 0 && heightScalex - 0.2 > 0) {
					canvansImageView.setScaleX((float) (widthScalex - 0.2));
					canvansImageView.setScaleY((float) (heightScalex - 0.2));
				}
				if (oreignHeightScalex > heightScalex
						|| oreignWidthScalex > widthScalex) {
					isReduce = true;
				}
			}
		});

	}

	public void initIMageLoader() {

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplication().getBaseContext())
				.threadPoolSize(3)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCacheSize(1500000)
				// 1.5 Mb
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.enableLogging() // Not necessary in common
				.build();
		ImageLoader.getInstance().init(config);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (RESULT_OK == resultCode) {
			if (requestCode == 1) {
				try {

					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(photopath, options);
					int scaleX, scaleY, imageWidth, imageHeight;
					imageWidth = options.outWidth;
					imageHeight = options.outHeight;
					scaleX = imageWidth / DEFAULT_WIDTH;
					scaleY = imageHeight / DEFAULT_HEIGHT;
					options.inSampleSize = Math.max(scaleX, scaleY);
					options.inJustDecodeBounds = false;
					options.inPurgeable=true;
					// options.inPurgeable=true;
					Bitmap btp = BitmapFactory.decodeFile(photopath, options);
					canvansImageView.setImageBitmap(btp);
				} catch (Exception e) {
					Toast.makeText(this, "出问题了,请多试几次...", Toast.LENGTH_SHORT)
							.show();
					e.printStackTrace();
				}

			} else if (requestCode == 2) {
				try {
					Uri uri = data.getData();
					String[] projStrings = { MediaStore.Images.Media.DATA };
					Cursor cursor = managedQuery(uri, projStrings, null, null,
							null);
					int cloum_index = cursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					cursor.moveToFirst();
					String pathString = cursor.getString(cloum_index);
					/* cursor.close(); */
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(pathString, options);
					int imageWidth, imageHeight;
					imageWidth = options.outWidth;
					imageHeight = options.outHeight;
					int scaleX, scaleY;
					scaleX = imageWidth / DEFAULT_WIDTH;
					scaleY = imageHeight / DEFAULT_HEIGHT;
					options.inSampleSize = Math.max(scaleX, scaleY);
					options.inJustDecodeBounds = false;
					Bitmap bm = BitmapFactory.decodeFile(pathString, options);
					canvansImageView.setImageBitmap(bm);
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(this, "出问题了,请多试几次...", Toast.LENGTH_SHORT)
							.show();
					e.printStackTrace();
				}

				// startPhotoZoom(uri);

			} else if (requestCode == 3) {
				// startPhotoZoom 调用这个函数的时候会到达这个分支 裁剪完后会显示到imageView上面
				if (data != null) {
					Bundle extras = data.getExtras();
					// 获得实际剪裁的区域的bitmap图形
					Bitmap thePic = extras.getParcelable("data");
					// 获得imageview控件的引用

					// 在imageview控件中显示图片
					// backgroundImageView.setImageBitmap(thePic);
					// MainActivity.this.view.pickerBackground(thePic);
					if (saveType == CUT_MODE) {

						SimpleDateFormat formatter = new SimpleDateFormat(
								"yyyy_MM_dd_kk_mm_ss");
						String date = formatter.format(new java.util.Date());
						String pathString = Environment
								.getExternalStorageDirectory()
								+ "/drawphoto/"
								+ date + ".png";
						// String
						// pathString=Environment.getExternalStorageDirectory()+"/"+date+".png";
						try {
							FileOutputStream fileOutputStream = new FileOutputStream(
									pathString);
							thePic.compress(CompressFormat.PNG, 90,
									fileOutputStream);
							fileOutputStream.flush();
							fileOutputStream.close();
							Toast.makeText(this, "文件保存在" + pathString,
									Toast.LENGTH_SHORT).show();

						} catch (Exception e) {
							// TODO: handle exception
							Toast.makeText(this, "保存文件出错", Toast.LENGTH_SHORT)
									.show();
						}
					} else
						canvansImageView.setImageBitmap(thePic);
				}

			} else if (requestCode == 4) {
				ArrayList<String> matches = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				Log.v("1", matches.get(0));
				// System.out.println(matches.get(0));

				if (matches.get(0).equals("拍照") || matches.get(0).equals("相机")) {
					takepicture();
				} else if (matches.get(0).equals("打开图片")) {
					openpicture();
				} else if (matches.get(0).equals("保存图片")) {
					SimpleDateFormat formatter = new SimpleDateFormat(
							"yyyy_MM_dd_kk_mm_ss");
					String date = formatter.format(new java.util.Date());
					String pathString = Environment
							.getExternalStorageDirectory()
							+ "/drawphoto/"
							+ date + ".png";
					// String
					// pathString=Environment.getExternalStorageDirectory()+"/"+date+".png";
					canvansImageView.saveImage(pathString, SAVE_MODE);
				} else if (matches.get(0).equals("剪切图片")) {
					SimpleDateFormat formatter = new SimpleDateFormat(
							"yyyy_MM_dd_kk_mm_ss");
					String date = formatter.format(new java.util.Date());
					String pathString = Environment
							.getExternalStorageDirectory()
							+ "/drawphoto/"
							+ date + ".png";
					// String
					// pathString=Environment.getExternalStorageDirectory()+"/"+date+".png";
					canvansImageView.saveImage(pathString, CUT_MODE);
					File file = new File(pathString);
					Uri uri = Uri.fromFile(file);
					saveType = CUT_MODE;
					startPhotoZoom(uri);
				} else {
					Toast toast = Toast.makeText(PaintMainActivity.this,
							"发音不正确或者无对应语音操作！", Toast.LENGTH_LONG);
					toast.show();
				}

			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(PaintMainActivity.this)
					.setTitle("温馨提示")
					.setMessage("是否保存?")
					.setPositiveButton("是",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									SimpleDateFormat formatter = new SimpleDateFormat(
											"yyyy_MM_dd_kk_mm_ss");
									String date = formatter
											.format(new java.util.Date());
									String pathString = Environment
											.getExternalStorageDirectory()
											+ "/drawphoto/" + date + ".png";
									// String
									// pathString=Environment.getExternalStorageDirectory()+"/"+date+".png";
									canvansImageView.saveImage(pathString,
											SAVE_MODE);
									dialog.dismiss();
									/*
									 * if(adThread!=null&&adThread.isAlive())
									 * adThread.destroy();
									 * msp.r2(MainActivity.this,false ,false ,0
									 * );
									 */
									finish();
									System.exit(0);
									// 退出后广告显示
									// msp.exit(MainActivity.this);

								}
							})
					.setNegativeButton("否",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									dialog.dismiss();
									/*
									 * if(adThread!=null&&adThread.isAlive())
									 * adThread.destroy();
									 * msp.r2(MainActivity.this,false ,false ,0
									 * );
									 */
									finish();
									System.exit(0);
									// 退出后广告显示ss
									// msp.exit(MainActivity.this);
									// System.exit(0);
								}
							}).setNeutralButton("取消", null).show();

		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.undo:
			canvansImageView.undo();
			break;
		case R.id.reply:
			canvansImageView.redo();
			break;
		case R.id.share:
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			shareIntent.setType("image/*");
			try {
				
				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyy_MM_dd_kk_mm_ss");
				String date = formatter.format(new java.util.Date());
				String pathString = Environment.getExternalStorageDirectory()
						+ "/drawphoto/" + date + ".png";
				canvansImageView.saveImage(pathString, SHARE_MODE);
				Uri uri = Uri.fromFile(new File(pathString));
				shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
				PaintMainActivity.this.startActivity(Intent.createChooser(
						shareIntent, "请选择"));
			} catch (Exception e) {
				
				Toast.makeText(PaintMainActivity.this, "出问题了", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.save:
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy_MM_dd_kk_mm_ss");
			String date = formatter.format(new java.util.Date());
			String pathString = Environment.getExternalStorageDirectory()
					+ "/drawphoto/" + date + ".png";
			canvansImageView.saveImage(pathString, SAVE_MODE);
			break;
		/*
		 * case R.id.about: Intent intent=new
		 * Intent(MainActivity.this,DisplayAbout.class);
		 * MainActivity.this.startActivity(intent); break;
		 */
		case R.id.zoom_canvans:
			zoomCanvas.setVisibility(View.VISIBLE);
			break;
		case R.id.move_canvans:
			if (DrawView.isMove) {
				DrawView.isMove = false;
				item.setTitle("移动画布");
			} else {
				DrawView.isMove = true;
				item.setTitle("停止移动");
			}
			break;
		case R.id.pastephoto:
			// 显示对话框
			final Dialog displayPPDialog = new Dialog(this);
			displayPPDialog.setTitle("选择贴图");
			//WindowManager.LayoutParams layoutParams=new WindowManager.LayoutParams((int)(0.8f*screenWidth), (int)(0.5f*screenHeight));
			//LayoutInflater layoutInflater=getLayoutInflater();
			//displayPPDialog.setContentView(layoutInflater.inflate(R.layout.pastephoto_layout,),layoutParams);
			displayPPDialog.setContentView(R.layout.pastephoto_layout);
			GridView photoGridView = (GridView) displayPPDialog.findViewById(R.id.paste_grid);
			//photoGridView.remove
			PastePhotoAdapter adapter = new PastePhotoAdapter(PaintMainActivity.this);
			photoGridView.setAdapter(adapter);
			photoGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(
								AdapterView<?> parent,View view, int position, long id) {
							// TODO Auto-generated method stub
							// canvansImageView.setImageResource((int)id);
							// 设置copy模式
							canvansImageView.setPaintMode(DrawView.COPY_MODE);
							// 设置复制bitmap
							Bitmap copyBitmap = BitmapFactory.decodeResource(getResources(),(int) id);
							canvansImageView.setCopyBitmap(copyBitmap);
							// 画笔,橡皮擦背景设为透明
							main_penImageView.setBackgroundColor(Color.TRANSPARENT);
							main_eraserImageView.setBackgroundColor(Color.TRANSPARENT);
							displayPPDialog.dismiss();
						}

					});
			displayPPDialog.show();
			break;
		case R.id.closecolor:
			// 显示填色颜色选择对话框
			disClsColDialog();
			break;
		case R.id.areaselect:
			canvansImageView.setPaintMode(DrawView.CUT_MODE);
			main_penImageView.setBackgroundColor(Color.TRANSPARENT);
			main_eraserImageView.setBackgroundColor(Color.TRANSPARENT);
			break;
		case R.id.pastetext:
			final EditText textEditText=new EditText(PaintMainActivity.this);
			textEditText.setHint("输入自定义文字");
			new AlertDialog.Builder(PaintMainActivity.this)
			.setTitle("自定义文字").setView(textEditText).
			setPositiveButton("确定",new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					paintText=textEditText.getText().toString();
					if(paintText.equals("")){
						Toast.makeText(PaintMainActivity.this,"输入文字不能为空",Toast.LENGTH_SHORT).show();
						return;
					}
					main_penImageView.setBackgroundColor(Color.TRANSPARENT);
					canvansImageView.setPaintMode(DrawView.PASTETEXT_MODE);
					canvansImageView.setPaintText(paintText);
					dialog.dismiss();
				}
			}).setNegativeButton("取消",null).show();
			
			break;
		case R.id.moreapp:
			OPUtils.startActivity(this, RecomActivity.class);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// 获取zoomCanvas对象
	public ZoomControls getZoomCanvans() {
		return zoomCanvas;
	}

	// 获取透明度
	public int getOpacity() {
		return opacity;
	}

	// 裁剪图片
	public void startPhotoZoom(Uri uri) {
		// 裁剪图片
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 256);
		intent.putExtra("outputY", 256);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, 3);
	}

	public void takepicture() {
		try {
			File file = new File(Environment.getExternalStorageDirectory(),
					"test.jpg");
			PaintMainActivity.photopath = file.getAbsolutePath();
			Uri outputFileUri = Uri.fromFile(file);
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 调用intent的方法照相，但是效果不好，考虑直接调用硬件摄像头camera
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
			PaintMainActivity.MActivity.startActivityForResult(intent, 1);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openpicture() {
		Intent intent = new Intent();
		intent.setType("image/*");// 打开图片方式
		intent.setAction(Intent.ACTION_GET_CONTENT);

		startActivityForResult(intent, 2);
	}

	public void cutpicture() {
		try {
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			// 设置剪裁剪属性
			cropIntent.putExtra("crop", "true");
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			// 输出的坐标
			cropIntent.putExtra("outputX", 256);
			cropIntent.putExtra("outputY", 256);
			// 返回剪裁的图片数据
			cropIntent.putExtra("return-data", true);
			startActivityForResult(cropIntent, 3);
		} catch (ActivityNotFoundException anfe) {
			// display an error message
			String errorMessage = "Whoops - your device doesn't support the crop action!";
			Toast.makeText(PaintMainActivity.this, errorMessage, Toast.LENGTH_SHORT)
					.show();

		}
	}

	// 封闭填色对话框
	public void disClsColDialog() {
		final Dialog colorDialog = new Dialog(PaintMainActivity.this);
		colorDialog.setTitle("填色颜色");
		colorDialog.setContentView(R.layout.dialog_for_selectcolor);
		final ColorPicker colorPicker = (ColorPicker) colorDialog
				.findViewById(R.id.picker);
		colorPicker.setColor(canvansImageView.getColor());
		final OpacityBar opacityBar = (OpacityBar) colorDialog
				.findViewById(R.id.opacitybar);
		colorPicker.addOpacityBar(opacityBar);
		opacityBar.setOpacity(opacity);
		Button colorOKButton = (Button) colorDialog.findViewById(R.id.colorok);
		Button colorCancelButton = (Button) colorDialog
				.findViewById(R.id.colorcancel);
		colorOKButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// colorPicker.getColor()
				// opacity=opacityBar.getOpacity();
				// canvansImageView.setColor(colorPicker.getColor());
				canvansImageView.setFillColor(colorPicker.getColor());
				canvansImageView.setPaintMode(DrawView.FILLCOLOR_MODE);
				main_penImageView.setBackgroundColor(Color.TRANSPARENT);
				main_eraserImageView.setBackgroundColor(Color.TRANSPARENT);
				colorDialog.dismiss();
			}
		});
		colorCancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				colorDialog.dismiss();
			}
		});
		// colorDialog.findViewById(R.id.sv)
		colorDialog.show();
	}

	@SuppressLint({ "ResourceAsColor", "InflateParams" })
	public void initMainImage() {
		
		// 获取屏幕的宽度
		int measureScreenWidth = getWindowManager().getDefaultDisplay()
				.getWidth();
		// 获取每个图标的宽度和高度
		int singleLength = measureScreenWidth / 8;
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				singleLength, singleLength);
		main_newcreateImageView = (ImageView) findViewById(R.id.main_newcreate);
		main_newcreateImageView.setBackgroundResource(R.drawable.img_state);
		
		main_drawtypeImageView = (ImageView) findViewById(R.id.main_drawfree);
		main_drawtypeImageView.setBackgroundResource(R.drawable.img_state);
		
		
		main_shapeImageView = (ImageView) findViewById(R.id.main_pensize);
		main_shapeImageView.setBackgroundResource(R.drawable.img_state);
		
		main_penImageView = (ImageView) findViewById(R.id.main_pen);
		main_penImageView.setBackgroundColor(R.color.selectColor);
		
		
		main_colorImageView = (ImageView) findViewById(R.id.main_pencolor);
		main_colorImageView.setBackgroundResource(R.drawable.img_state);
		
		main_backgroundImageView = (ImageView) findViewById(R.id.main_background);
		main_backgroundImageView.setBackgroundResource(R.drawable.img_state);
		
		
		main_eraserImageView = (ImageView) findViewById(R.id.main_eraser);
		
		main_clearImageView = (ImageView) findViewById(R.id.main_empty);
		main_clearImageView.setBackgroundResource(R.drawable.img_state);
		
		main_newcreateImageView.setLayoutParams(layoutParams);
		main_newcreateImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(PaintMainActivity.this)
						.setTitle("温馨提示")
						.setMessage("是否保存当前页面")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										SimpleDateFormat formatter = new SimpleDateFormat(
												"yyyy_MM_dd_kk_mm_ss");
										String date = formatter
												.format(new java.util.Date());
										String pathString = Environment
												.getExternalStorageDirectory()
												+ "/drawphoto"
												+ "/"
												+ date
												+ ".png";
										canvansImageView.saveImage(pathString,
												SAVE_MODE);
										dialog.dismiss();
										final Dialog createDialog = new Dialog(
												PaintMainActivity.this,
												android.R.style.Theme_Holo_Light_Dialog);
										createDialog.setTitle("新建画布");
										createDialog
												.setContentView(R.layout.new_create_layout);
										createDialog.show();
										Button customCanvasButton = (Button) createDialog
												.getWindow().findViewById(
														R.id.canvas_ok);
										customCanvasButton
												.setOnClickListener(new View.OnClickListener() {

													@Override
													public void onClick(View v) {
														// TODO Auto-generated
														// method stub
														// dismissDialog(id);
														// 设置高度
														EditText widthEditText = (EditText) createDialog
																.getWindow()
																.findViewById(
																		R.id.canvas_width);
														EditText heightEditText = (EditText) createDialog
																.getWindow()
																.findViewById(
																		R.id.canvas_height);
														if (heightEditText
																.getText()
																.toString()
																.isEmpty())
															createHeight = mainView
																	.getHeight();
														else
															createHeight = Integer
																	.parseInt(heightEditText
																			.getText()
																			.toString());// 获取输入的高度

														if (widthEditText
																.getText()
																.toString()
																.isEmpty())
															createWidth = mainView
																	.getWidth();
														else
															createWidth = Integer
																	.parseInt(widthEditText
																			.getText()
																			.toString());// 获取输入的宽度

														createLayoutParams = new android.widget.LinearLayout.LayoutParams(
																createWidth,
																createHeight);
														canvansImageView
																.setLayoutParams(createLayoutParams);
														canvansImageView
																.setImageResource(R.drawable.whitebackground);
														isLoad = true;
														DrawView.isFirstDraw = true;
														createDialog.dismiss();
													}
												});

									}
								})
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.dismiss();
										final Dialog createDialog = new Dialog(
												PaintMainActivity.this,
												android.R.style.Theme_Holo_Light_Dialog);
										createDialog.setTitle("新建画布");
										createDialog
												.setContentView(R.layout.new_create_layout);
										createDialog.show();
										Button customCanvasButton = (Button) createDialog
												.getWindow().findViewById(
														R.id.canvas_ok);
										customCanvasButton
												.setOnClickListener(new View.OnClickListener() {

													@Override
													public void onClick(View v) {
														// TODO Auto-generated
														// method stub
														// dismissDialog(id);
														// 设置高度
														EditText widthEditText = (EditText) createDialog
																.getWindow()
																.findViewById(
																		R.id.canvas_width);
														EditText heightEditText = (EditText) createDialog
																.getWindow()
																.findViewById(
																		R.id.canvas_height);
														if (heightEditText
																.getText()
																.toString()
																.isEmpty())
															createHeight = mainView
																	.getHeight();
														else
															createHeight = Integer
																	.parseInt(heightEditText
																			.getText()
																			.toString());// 获取输入的高度

														if (widthEditText
																.getText()
																.toString()
																.isEmpty())
															createWidth = mainView
																	.getWidth();
														else
															createWidth = Integer
																	.parseInt(widthEditText
																			.getText()
																			.toString());// 获取输入的宽度

														createLayoutParams = new android.widget.LinearLayout.LayoutParams(
																createWidth,
																createHeight);
														canvansImageView
																.setLayoutParams(createLayoutParams);
														canvansImageView
																.setImageResource(R.drawable.whitebackground);
														isLoad = true;
														DrawView.isFirstDraw = true;
														createDialog.dismiss();
													}
												});
									}
								}).show();

			}
		});
		main_drawtypeImageView.setLayoutParams(layoutParams);
		main_drawtypeImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(PaintMainActivity.this)
						.setTitle("选择绘图类型")
						.setSingleChoiceItems(
								new String[] { "直线", "折线", "矩形",
										"六边形", "椭圆", "自由手绘" },
								canvansImageView.getCurrentShape(),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										switch (which) {

										case Shape_Type.STRAIGIT:
											canvansImageView
													.setShape(Shape_Type.STRAIGIT);
											main_drawtypeImageView
													.setImageResource(R.drawable.draw_line);
											break;
										case Shape_Type.BROKEN:
											canvansImageView
													.setShape(Shape_Type.BROKEN);
											main_drawtypeImageView
													.setImageResource(R.drawable.draw_broken);
											break;
										case Shape_Type.RECT:
											canvansImageView
													.setShape(Shape_Type.RECT);
											main_drawtypeImageView
													.setImageResource(R.drawable.draw_rect);
											break;
										case Shape_Type.MUTIL:
											canvansImageView
													.setShape(Shape_Type.MUTIL);
											main_drawtypeImageView
													.setImageResource(R.drawable.draw_mul);
											break;
										case Shape_Type.OVAL:
											canvansImageView
													.setShape(Shape_Type.OVAL);
											main_drawtypeImageView
													.setImageResource(R.drawable.draw_oval);
											break;
										case Shape_Type.FREE:
											canvansImageView
													.setShape(Shape_Type.FREE);
											main_drawtypeImageView
													.setImageResource(R.drawable.draw_free);
											break;
										default:
											break;
										}
										canvansImageView.setCurrentShape();
										main_penImageView
												.setBackgroundColor(R.color.selectColor);
										if (canvansImageView.getPaintMode() != DrawView.COMMON_MODE) {
											main_eraserImageView
													.setBackgroundColor(Color.TRANSPARENT);
											// fillcolorImageView.setBackgroundColor(Color.TRANSPARENT);
											// cutImageView.setBackgroundColor(Color.TRANSPARENT);
											canvansImageView
													.setPaintMode(DrawView.COMMON_MODE);
										}

										dialog.dismiss();
									}

								}).show();
			}
		});
		main_shapeImageView.setLayoutParams(layoutParams);
		main_shapeImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setsizeDialog = new Dialog(PaintMainActivity.this);
				setsizeDialog.setTitle("画笔粗细");
				setsizeDialog.setContentView(getLayoutInflater().inflate(
						R.layout.shape_paint, null));
				setsizeDialog
						.setOnShowListener(new DialogInterface.OnShowListener() {

							@Override
							public void onShow(DialogInterface dialog) {
								// TODO Auto-generated method stub
								processImageView.displayPenSize(penSize);
							}
						});
				penTextView = (TextView) setsizeDialog.getWindow()
						.findViewById(R.id.process_text);
				processImageView = (DisplayPenSizeView) setsizeDialog
						.getWindow().findViewById(R.id.pen_shape);
				// Toast.makeText(MainActivity.this,
				// ""+processImageView.getWidth(),Toast.LENGTH_SHORT).show();
				process_okButton = (Button) setsizeDialog.getWindow()
						.findViewById(R.id.pen_ok);
				process_cancelButton = (Button) setsizeDialog.getWindow()
						.findViewById(R.id.pen_cancel);
				process_okButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						penSize = (int) (0.3 * penProcess + 2);
						canvansImageView.setPenSize(penSize);
						setsizeDialog.dismiss();
					}
				});
				process_cancelButton
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								//penSize=orignPenSize;
								penProcess=orignPenProcess;
								setsizeDialog.dismiss();
							}
						});
				penSeekBar = (SeekBar) setsizeDialog.getWindow().findViewById(
						R.id.pen_seekbar);
				penSeekBar.setProgress(penProcess);
				penTextView.setText("" + penProcess);
				penSize = (int) (0.3 * penProcess + 2);
				penSeekBar
						.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

							@Override
							public void onStopTrackingTouch(SeekBar seekBar) {
								// TODO Auto-generated method stub
								// processImageView.clear();
								penProcess = seekBar.getProgress();
								penSize = (int) (0.3 * penProcess + 2);
								// Toast.makeText(MainActivity.this,
								// ""+processImageView.getWidth(),
								// Toast.LENGTH_SHORT).show();
								processImageView.displayPenSize(penSize);
							}

							@Override
							public void onStartTrackingTouch(SeekBar seekBar) {
								// TODO Auto-generated method stub
								orignPenProcess=penProcess;
							}

							@Override
							public void onProgressChanged(SeekBar seekBar,
									int progress, boolean fromUser) {
								// TODO Auto-generated method stub
								// processImageView.draw(canvas);
								penTextView.setText("" + progress);
								// processImageView.set
								penSize = (int) (0.3 * progress + 2);
								processImageView.displayPenSize(penSize);

							}
						});
				setsizeDialog.show();
			}
		});
		main_penImageView.setLayoutParams(layoutParams);
		main_penImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				main_penImageView.setBackgroundColor(R.color.selectColor);
				if (canvansImageView.getPaintMode() != DrawView.COMMON_MODE) {
					// cutImageView.setBackgroundColor(Color.TRANSPARENT);
					main_eraserImageView.setBackgroundColor(Color.TRANSPARENT);
					// fillcolorImageView.setBackgroundColor(Color.TRANSPARENT);
					canvansImageView.setPaintMode(DrawView.COMMON_MODE);
				}
			}
		});
		main_colorImageView.setLayoutParams(layoutParams);
		main_colorImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				final Dialog colorDialog = new Dialog(PaintMainActivity.this);
				colorDialog.setTitle("颜色选择");
				colorDialog.setContentView(R.layout.dialog_for_selectcolor);
				//colorDialog.getWindow().setLayout(200, 320);
				final ColorPicker colorPicker = (ColorPicker) colorDialog
						.findViewById(R.id.picker);
				//colorPicker.set
				colorPicker.setOldCenterColor(canvansImageView.getColor());
				colorPicker.setColor(canvansImageView.getColor());
				//colorPicker.setAlpha(alpha)
				final OpacityBar opacityBar = (OpacityBar) colorDialog
						.findViewById(R.id.opacitybar);
				final CheckBox blackCheckBox=(CheckBox)colorDialog.findViewById(R.id.black_checkbox);
				blackCheckBox.setChecked(isBlackColor);
				colorPicker.addOpacityBar(opacityBar);
				opacityBar.setOpacity(opacity);
				//colorPicker.set
				//colorPicker.setAlpha(penAlpha);
				Button colorOKButton = (Button) colorDialog
						.findViewById(R.id.colorok);
				Button colorCancelButton = (Button) colorDialog
						.findViewById(R.id.colorcancel);
				colorOKButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						// colorPicker.getColor()
						if(blackCheckBox.isChecked()){
							isBlackColor=true;
							canvansImageView.setColor(Color.BLACK);
						}else{
							isBlackColor=false;
							opacity = opacityBar.getOpacity();
							canvansImageView.setColor(colorPicker.getColor());
						}
						//penAlpha=colorPicker.getAlpha();
						colorDialog.dismiss();
					}
				});
				colorCancelButton
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								colorDialog.dismiss();
							}
						});
				// colorDialog.findViewById(R.id.sv)
				colorDialog.show();
			}
		});
		main_backgroundImageView.setLayoutParams(layoutParams);
		main_backgroundImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Background backDialog = new Background(PaintMainActivity.this,
						R.style.CneterTitleHolo);
				backDialog.setTitle("更换背景图");
				backDialog.show();
			}
		});
		main_eraserImageView.setLayoutParams(layoutParams);
		main_eraserImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (canvansImageView.getPaintMode() != DrawView.ERASER_MODE) {
					main_penImageView.setBackgroundColor(Color.TRANSPARENT);
					// cutImageView.setBackgroundColor(Color.TRANSPARENT);
					// fillcolorImageView.setBackgroundColor(Color.TRANSPARENT);
					canvansImageView.setPaintMode(DrawView.ERASER_MODE);
					main_eraserImageView
							.setBackgroundColor(R.color.selectColor);
				}
			}
		});
		main_clearImageView.setLayoutParams(layoutParams);
		main_clearImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				canvansImageView.clear();
			}
		});
	}
	
	public void initAD(){
		myPPCC=YManager.getInsertInstance(PaintMainActivity.this, "ed211db6f83545be84f251915bb124af","360-12");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					if((Const.currentTime-Const.lastTime)>Const.dateNum*Const.ONE_DAY_MILL){
						Config.saveValue(Const.Key.DATE_NUM,String.valueOf(Const.dateNum+2));
						Config.saveValue(Const.Key.LAST_TIME, String.valueOf(Const.currentTime));
						Thread.sleep(30000);
						handler.sendEmptyMessage(1);
					}
				
					handler.sendEmptyMessage(2);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	
	
	/**
	 * GaoFei Note
	 * Save a newPhoto from originPhoto path
	 * @param rawPath      原来图片的路径
	 * @param newPhoto     新图片的路径
	 * @param quality      压缩质量
	 * @param targetWidth  新图片的宽度
	 * @param targetHeight 新图片的高度
	 * @return
	 */
	
	public static boolean savePhotoFromRaw(String rawPath,String newPhotoPath,int quality,
			int targetWidth,int targetHeight){
		Log.i("rawPath",rawPath);
		
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inJustDecodeBounds=true;
		BitmapFactory.decodeFile(rawPath, options);
		/**原始图片的宽和高*/
		int originWidth=options.outWidth;
		int originHeight=options.outHeight;
		/**
		 * 压缩比例
		 */
		int scaleX=originWidth/targetWidth;
		int scaleY=originHeight/targetHeight;
		int scale=Math.min(scaleX, scaleY);
		scale*=2;
		options.inSampleSize=scale;
		options.inJustDecodeBounds=false;
		Bitmap originBitmap=BitmapFactory.decodeFile(rawPath,options);
		FileOutputStream newFileOutputStream;
		try {
			newFileOutputStream = new FileOutputStream(rawPath);
			originBitmap.compress(CompressFormat.JPEG, quality, newFileOutputStream);
			newFileOutputStream.flush();
			newFileOutputStream.close();
			OPUtils.showToast("成功压缩图片", Toast.LENGTH_SHORT);
		//	Toast.makeText(ISPApplication.getApplication().getApplicationContext(),"成功压缩图片", Toast.LENGTH_SHORT).show();
			return true;
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			OPUtils.showToast(e.getMessage(), Toast.LENGTH_SHORT);
		//	Toast.makeText(ISPApplication.getApplication().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			//Util.addToast(ISPApplication.getApplication().getApplicationContext(), e.getMessage());
			return false;
		}
		//return true;
	}
	
	/**
	 * GaoFei Note
	 * load bitmap from local path with special width and height
	 * @param photoPath     本地存储的图片路径
	 * @param targetWidth   加载内存之后Bitmap的宽度
	 * @param targetHeight  加载内存之后Bitmap的高度
	 * @return
	 */
	public static Bitmap loadBitmapFromPath(String photoPath,int targetWidth,int targetHeight){
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inJustDecodeBounds=true;
		BitmapFactory.decodeFile(photoPath, options);
		/**原始图片的宽和高*/
		int originWidth=options.outWidth;
		int originHeight=options.outHeight;
		/**
		 * 压缩比例
		 */
		int scaleX=originWidth/targetWidth;
		int scaleY=originHeight/targetHeight;
		int scale=Math.min(scaleX, scaleY);
		scale*=2;
		options.inSampleSize=scale;
		options.inJustDecodeBounds=false;
		options.inPurgeable=true;
		Bitmap originBitmap=BitmapFactory.decodeFile(photoPath, options);
		return originBitmap;
	}
	
	
	/**
	 *检查更新应用
	 */
	public void updateApp(){
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();  
		            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder(); 
		            Document doc = dBuilder.parse("http://120.24.210.186:8080/PaintService/app_config.xml");
					if (doc != null) {
						
						NodeList codeList = doc.getElementsByTagName("version_code");
						if(codeList!=null&&codeList.getLength()>0){
							
							Element codeElement = (Element)codeList.item(0);
							String  codeContent = codeElement.getTextContent();
							int nViersionCode = Integer.parseInt(codeContent);
							int currVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
							NodeList updateNodeList = (NodeList)doc.getElementsByTagName("update_descrip");
							updateTip = updateNodeList.item(0).getTextContent();
							updateTip = Html.fromHtml(updateTip).toString();
							NodeList versionNameNodeList = (NodeList)doc.getElementsByTagName("version_name");
							updateVersion = versionNameNodeList.item(0).getTextContent();
							NodeList updateUrList = (NodeList)doc.getElementsByTagName("download_path");
							downloadPath = updateUrList.item(0).getTextContent();
							if(currVersionCode<nViersionCode){
								
								handler.sendEmptyMessage(3);
								
							}else{
								
								Log.i(TAG,"currversionCode:"+currVersionCode);
								Log.i(TAG,"nVersionCode:"+nViersionCode);
								Log.i(TAG,"updateTip:"+updateTip);
								Log.i(TAG,"updateVersion:"+updateVersion);
							}
						}
						
						
					}
					
				} catch (Exception e) {
				
					Log.i(TAG,""+e);
					
				}
				
			}
		}).start();
		
	   
	}
}
