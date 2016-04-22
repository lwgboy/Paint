package cn.fjnu.edu.ui.activity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.qhad.ads.sdk.adcore.Qhad;
import com.qhad.ads.sdk.interfaces.IQhBannerAd;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

import cn.edu.fjnu.utils.OPUtils;
import cn.fjnu.edu.paint.R;
import cn.fjnu.edu.paint.adapter.PastePhotoAdapter;
import cn.fjnu.edu.paint.data.Shape_Type;
import cn.fjnu.edu.paint.engine.Background;
import cn.fjnu.edu.paint.engine.DrawView;
import cn.fjnu.edu.paint.ui.ColorPicker;
import cn.fjnu.edu.paint.ui.DisplayPenSizeView;
import cn.fjnu.edu.paint.ui.OpacityBar;

@SuppressLint("SimpleDateFormat")
public class PaintMainActivity extends Activity {

	private static final String TAG = "PaintMainActivity";
	public static final int SHARE_MODE = 0;
	public static final int SAVE_MODE = 1;
	public static final int CUT_MODE = 2;
	public static int saveType = SAVE_MODE;
	public static String photopath;
	public static PaintMainActivity MActivity;
	public static int[] backID = new int[46];
	public static int[] bigBackId = new int[46];
	public static boolean isReduce = false;
	public static boolean isLoad = true;
	public boolean isMeasure = true;
	private final int DEFAULT_WIDTH = 480;
	private final int DEFAULT_HEIGHT = 800;
	private int opacity = 0xff;
	private DrawView canvansImageView;
	private ImageView main_shapeImageView;
	private ImageView main_clearImageView;
	private ImageView main_eraserImageView;
	private ImageView main_backgroundImageView;
	private ImageView main_drawtypeImageView;
	private ImageView main_newcreateImageView;
	private ImageView main_penImageView;
	private ImageView main_colorImageView;
	private SeekBar penSeekBar;
	private TextView penTextView;
	private DisplayPenSizeView processImageView;
	private int penSize;
	private Button process_okButton;
	private Button process_cancelButton;
	private Dialog setsizeDialog;
	private View mainView;
	private ZoomControls zoomCanvas;
	private float oreignWidthScalex;
	private float oreignHeightScalex;
	private int penProcess = 10;
	private int orignPenProcess;
	public static int drawWidth;
	public static int drawHeight;
	public static int screenWidth;
	public static int screenHeight;
	public static DrawView canvasView;
	private int createWidth;
	private int createHeight;
	private android.widget.LinearLayout.LayoutParams createLayoutParams;
	private boolean isBlackColor=false;
	private String paintText=null;
	//广告容器页面
	private RelativeLayout mLayoutAd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_paint);
		init();
		//初始化广告
		initAd();
		initMainImage();
		initIMageLoader();
		canvansImageView.setImageResource(R.drawable.app_rm);
	}
	
	
	
	@Override
	protected void onStart() {
		super.onStart();
		
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
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
		mLayoutAd = (RelativeLayout) findViewById(R.id.layout_ad);
		MActivity = PaintMainActivity.this;
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
		zoomCanvas.setOnZoomInClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
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
		zoomCanvas.setOnZoomOutClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
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


	/**
	 * 将广告显示在页面的底部
	 */
	public void initAd(){
		Qhad.showBanner(mLayoutAd,this,  "aa5vaot012", false);
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
				.enableLogging()
				.build();
		ImageLoader.getInstance().init(config);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

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
					Bitmap btp = BitmapFactory.decodeFile(photopath, options);
					canvansImageView.setImageBitmap(btp);
				} catch (Exception e) {
					Toast.makeText(this, "文件保存失败，请重试...", Toast.LENGTH_SHORT)
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
					Toast.makeText(this, "选择图片失败...", Toast.LENGTH_SHORT)
							.show();
					e.printStackTrace();
				}

			} else if (requestCode == 3) {
				if (data != null) {
					Bundle extras = data.getExtras();
					Bitmap thePic = extras.getParcelable("data");
					if (saveType == CUT_MODE) {

						SimpleDateFormat formatter = new SimpleDateFormat(
								"yyyy_MM_dd_kk_mm_ss");
						String date = formatter.format(new java.util.Date());
						String pathString = Environment
								.getExternalStorageDirectory()
								+ "/drawphoto/"
								+ date + ".png";
						try {
							FileOutputStream fileOutputStream = new FileOutputStream(
									pathString);
							thePic.compress(CompressFormat.PNG, 90,
									fileOutputStream);
							fileOutputStream.flush();
							fileOutputStream.close();
							Toast.makeText(this, "文件保存成功" + pathString,
									Toast.LENGTH_SHORT).show();

						} catch (Exception e) {
							Toast.makeText(this, "文件保存失败", Toast.LENGTH_SHORT)
									.show();
						}
					} else
						canvansImageView.setImageBitmap(thePic);
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
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(PaintMainActivity.this)
					.setTitle("温馨提示")
					.setMessage("是否保存")
					.setPositiveButton("是",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									SimpleDateFormat formatter = new SimpleDateFormat(
											"yyyy_MM_dd_kk_mm_ss");
									String date = formatter
											.format(new java.util.Date());
									String pathString = Environment
											.getExternalStorageDirectory()
											+ "/drawphoto/" + date + ".png";
									canvansImageView.saveImage(pathString,
											SAVE_MODE);
									dialog.dismiss();
									finish();
									System.exit(0);

								}
							})
					.setNegativeButton("否",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									finish();
									System.exit(0);

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
				
				Toast.makeText(PaintMainActivity.this, "不存在可分享应用", Toast.LENGTH_SHORT)
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
			final Dialog displayPPDialog = new Dialog(this);
			displayPPDialog.setTitle("选择贴图");
			displayPPDialog.setContentView(R.layout.pastephoto_layout);
			GridView photoGridView = (GridView) displayPPDialog.findViewById(R.id.paste_grid);
			//photoGridView.remove
			PastePhotoAdapter adapter = new PastePhotoAdapter(PaintMainActivity.this);
			photoGridView.setAdapter(adapter);
			photoGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(
								AdapterView<?> parent,View view, int position, long id) {
							canvansImageView.setPaintMode(DrawView.COPY_MODE);
							Bitmap copyBitmap = BitmapFactory.decodeResource(getResources(),(int) id);
							canvansImageView.setCopyBitmap(copyBitmap);
							main_penImageView.setBackgroundColor(Color.TRANSPARENT);
							main_eraserImageView.setBackgroundColor(Color.TRANSPARENT);
							displayPPDialog.dismiss();
						}

					});
			displayPPDialog.show();
			break;
		case R.id.closecolor:
			// ��ʾ��ɫ��ɫѡ��Ի���
			disClsColDialog();
			break;
		case R.id.areaselect:
			canvansImageView.setPaintMode(DrawView.CUT_MODE);
			main_penImageView.setBackgroundColor(Color.TRANSPARENT);
			main_eraserImageView.setBackgroundColor(Color.TRANSPARENT);
			break;
		case R.id.pastetext:
			final EditText textEditText=new EditText(PaintMainActivity.this);
			textEditText.setHint("输入自定义名字");
			new AlertDialog.Builder(PaintMainActivity.this)
			.setTitle("自定义文字").setView(textEditText).
			setPositiveButton("确定",new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					paintText=textEditText.getText().toString();
					if(paintText.equals("")){
						Toast.makeText(PaintMainActivity.this,"请输入自定义文字",Toast.LENGTH_SHORT).show();
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

	public ZoomControls getZoomCanvans() {
		return zoomCanvas;
	}

	public int getOpacity() {
		return opacity;
	}

	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
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
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// ����intent�ķ������࣬����Ч�����ã�����ֱ�ӵ���Ӳ������ͷcamera
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
			PaintMainActivity.MActivity.startActivityForResult(intent, 1);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openpicture() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);

		startActivityForResult(intent, 2);
	}

	public void cutpicture() {
		try {
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			cropIntent.putExtra("crop", "true");
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			cropIntent.putExtra("outputX", 256);
			cropIntent.putExtra("outputY", 256);
			cropIntent.putExtra("return-data", true);
			startActivityForResult(cropIntent, 3);
		} catch (ActivityNotFoundException anfe) {
			String errorMessage = "Whoops - your device doesn't support the crop action!";
			Toast.makeText(PaintMainActivity.this, errorMessage, Toast.LENGTH_SHORT)
					.show();

		}
	}

	public void disClsColDialog() {
		final Dialog colorDialog = new Dialog(PaintMainActivity.this);
		colorDialog.setTitle("颜色选择");
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
				colorDialog.dismiss();
			}
		});
		// colorDialog.findViewById(R.id.sv)
		colorDialog.show();
	}

	@SuppressLint({ "ResourceAsColor", "InflateParams" })
	public void initMainImage() {
		
		int measureScreenWidth = getWindowManager().getDefaultDisplay()
				.getWidth();
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
				new AlertDialog.Builder(PaintMainActivity.this)
						.setTitle("温馨提示")
						.setMessage("是否保存当前页面")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
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
																			.toString());// ��ȡ����ĸ߶�

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
																			.toString());// ��ȡ����Ŀ��

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
																			.toString());

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
																			.toString());

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
				setsizeDialog = new Dialog(PaintMainActivity.this);
				setsizeDialog.setTitle("画笔粗细");
				setsizeDialog.setContentView(getLayoutInflater().inflate(
						R.layout.shape_paint, null));
				setsizeDialog
						.setOnShowListener(new DialogInterface.OnShowListener() {

							@Override
							public void onShow(DialogInterface dialog) {
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
						penSize = (int) (0.3 * penProcess + 2);
						canvansImageView.setPenSize(penSize);
						setsizeDialog.dismiss();
					}
				});
				process_cancelButton
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
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
								penProcess = seekBar.getProgress();
								penSize = (int) (0.3 * penProcess + 2);
								processImageView.displayPenSize(penSize);
							}

							@Override
							public void onStartTrackingTouch(SeekBar seekBar) {
								orignPenProcess=penProcess;
							}

							@Override
							public void onProgressChanged(SeekBar seekBar,
									int progress, boolean fromUser) {
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
				main_penImageView.setBackgroundColor(R.color.selectColor);
				if (canvansImageView.getPaintMode() != DrawView.COMMON_MODE) {
					main_eraserImageView.setBackgroundColor(Color.TRANSPARENT);
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
				final ColorPicker colorPicker = (ColorPicker) colorDialog
						.findViewById(R.id.picker);
				colorPicker.setOldCenterColor(canvansImageView.getColor());
				colorPicker.setColor(canvansImageView.getColor());
				final OpacityBar opacityBar = (OpacityBar) colorDialog
						.findViewById(R.id.opacitybar);
				final CheckBox blackCheckBox=(CheckBox)colorDialog.findViewById(R.id.black_checkbox);
				blackCheckBox.setChecked(isBlackColor);
				colorPicker.addOpacityBar(opacityBar);
				opacityBar.setOpacity(opacity);
				Button colorOKButton = (Button) colorDialog
						.findViewById(R.id.colorok);
				Button colorCancelButton = (Button) colorDialog
						.findViewById(R.id.colorcancel);
				colorOKButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
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
								colorDialog.dismiss();
							}
						});
				colorDialog.show();
			}
		});
		main_backgroundImageView.setLayoutParams(layoutParams);
		main_backgroundImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
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
				if (canvansImageView.getPaintMode() != DrawView.ERASER_MODE) {
					main_penImageView.setBackgroundColor(Color.TRANSPARENT);
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
				canvansImageView.clear();
			}
		});
	}
	


}
