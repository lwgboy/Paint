package cn.fjnu.edu.ui.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Types.BoomType;
import com.nightonke.boommenu.Types.ButtonType;
import com.nightonke.boommenu.Types.PlaceType;
import com.nightonke.boommenu.Util;
import com.qhad.ads.sdk.adcore.Qhad;
import com.qhad.ads.sdk.interfaces.IQhAdEventListener;
import com.qhad.ads.sdk.interfaces.IQhBannerAd;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Random;

import cn.edu.fjnu.utils.DeviceInfoUtils;
import cn.edu.fjnu.utils.OPUtils;
import cn.fjnu.edu.paint.R;
import cn.fjnu.edu.paint.adapter.PastePhotoAdapter;
import cn.fjnu.edu.paint.config.Const;
import cn.fjnu.edu.paint.data.Shape_Type;
import cn.fjnu.edu.paint.engine.Background;
import cn.fjnu.edu.paint.engine.DrawView;
import cn.fjnu.edu.paint.ui.ColorPicker;
import cn.fjnu.edu.paint.ui.DisplayPenSizeView;
import cn.fjnu.edu.paint.ui.OpacityBar;

@SuppressLint("SimpleDateFormat")
public class PaintMainActivity extends AppBaseActivity implements BoomMenuButton.OnSubButtonClickListener,BoomMenuButton.AnimatorListener{

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
    private SeekBar penSeekBar;
    private TextView penTextView;
    private DisplayPenSizeView processImageView;
    private BoomMenuButton mButtonMenuMain;
    private boolean isInitMain = false;
    private int penSize;
    private Button process_okButton;
    private Button process_cancelButton;
    private Dialog setsizeDialog;
    private View mainView;
    private ZoomControls zoomCanvas;
    private float oreignWidthScalex;
    private float oreignHeightScalex;
    private int penProcess = 5;
    public static int drawWidth;
    public static int drawHeight;
    public static int screenWidth;
    public static int screenHeight;
    public static DrawView canvasView;
    private boolean isBlackColor = false;
    private String paintText = null;
    private String[] mColors = {"#F44336", "#E91E63", "#9C27B0", "#2196F3", "#03A9F4", "#00BCD4",
            "#009688", "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722",
            "#795548", "#9E9E9E", "#607D8B"};

    //广告容器页面
    private RelativeLayout mLayoutAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_paint);
        Log.i(TAG, "after setContentView");
        init();
        //初始化主功能
        initMain();
        //初始化广告
        initAd();
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
        mButtonMenuMain = (BoomMenuButton) findViewById(R.id.btn_boom_main);
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
    public void initAd() {
        if (OPUtils.getValFromSharedpreferences(Const.Key.CLICK_AD).equals("true"))
            return;
        final IQhBannerAd iQhBannerAd = Qhad.showBanner(mLayoutAd, this, "aa5vaot012", false);
        //广告点击事件
        iQhBannerAd.setAdEventListener(new IQhAdEventListener() {
            @Override
            public void onAdviewGotAdSucceed() {

            }

            @Override
            public void onAdviewGotAdFail() {

            }

            @Override
            public void onAdviewRendered() {
                //此时广告渲染完成
                //OPUtils.saveValToSharedpreferences(Const.Key.SHOW_AD, "true");
            }

            @Override
            public void onAdviewIntoLandpage() {

            }

            @Override
            public void onAdviewDismissedLandpage() {

            }

            @Override
            public void onAdviewClicked() {
                //此时广告被点击,存储被点击的状态
                OPUtils.saveValToSharedpreferences(Const.Key.CLICK_AD, "true");
                //关闭广告
                iQhBannerAd.closeAds();
                OPUtils.saveValToSharedpreferences(Const.Key.CLICK_AD, "true");
            }

            @Override
            public void onAdviewClosed() {

            }

            @Override
            public void onAdviewDestroyed() {

            }
        });
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
                    String[] projStrings = {MediaStore.Images.Media.DATA};
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
                    .setMessage("确认退出?")
                    .setPositiveButton("是",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                    finish();
                                    System.exit(0);

                                }
                            })
                    .setNegativeButton("否", null).show();

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
                //shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                shareIntent.setType("image/*");
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat(
                            "yyyy_MM_dd_kk_mm_ss");
                    String date = formatter.format(new java.util.Date());
                    String dirPath = Environment.getExternalStorageDirectory() + "/drawphoto";
                    File dirFile = new File(dirPath);
                    if(!dirFile.exists())
                        dirFile.mkdirs();
                    File pathFile = new File(dirFile, date+".png");
                    canvansImageView.saveImage(pathFile.getAbsolutePath(), SHARE_MODE);
                    Uri uri = Uri.fromFile(pathFile);
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
                String dirPath = Environment.getExternalStorageDirectory() + "/drawphoto";
                File dirFile = new File(dirPath);
                if(!dirFile.exists())
                    dirFile.mkdirs();
                File pathFile = new File(dirFile, date+".png");
                canvansImageView.saveImage(pathFile.getAbsolutePath(), SAVE_MODE);
                break;
            case R.id.closecolor:
                disClsColDialog();
                break;
            case R.id.areaselect:
                canvansImageView.setPaintMode(DrawView.CUT_MODE);
                break;
            case R.id.moreapp:
                OPUtils.startActivity(this, RecomActivity.class);
                break;
            case R.id.about_ad:
                new AlertDialog.Builder(this)
                        .setTitle("关于广告")
                        .setMessage("您只需要点击一次即可永久关闭广告")
                        .setPositiveButton("确定", null)
                        .show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
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


    public void disClsColDialog() {
        final Dialog colorDialog = new Dialog(PaintMainActivity.this, android.R.style.Theme_Holo_Light_Dialog);
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


    /**
     * 初始化主界面功能
     **/
    public void initMain() {
        Drawable[] mainDrawables = { ContextCompat.getDrawable(this, R.drawable.paste_main), ContextCompat.getDrawable(this, R.drawable.shape_main), ContextCompat.getDrawable(this, R.drawable.paint_main),
                ContextCompat.getDrawable(this, R.drawable.size_main),ContextCompat.getDrawable(this, R.drawable.background_main), ContextCompat.getDrawable(this, R.drawable.erase_main),
                ContextCompat.getDrawable(this, R.drawable.color_main), ContextCompat.getDrawable(this, R.drawable.empty_main), ContextCompat.getDrawable(this, R.drawable.text_main)};
        int[][] mainColors = new int[9][2];
        Random random = new Random();
        for (int i = 0; i != 9; ++i) {
            mainColors[i][0] = Color.parseColor(mColors[random.nextInt(9)]);
            mainColors[i][1] = mainColors[i][0];
        }
        String contents[] = {"贴图", "形状", "画笔", "粗细", "背景", "橡皮", "颜色", "清空", "文字"};
       // mButtonMenuMain.init(mainDrawables, contents, mainColors, ButtonType.CIRCLE, BoomType.PARABOLA, PlaceType.SHARE_9_1, null, null, null, null, null, null, 0);
        new BoomMenuButton.Builder()
                .subButtons(mainDrawables, mainColors, contents)
                .button(ButtonType.CIRCLE)
                .boom(BoomType.LINE)
                .place(PlaceType.CIRCLE_9_1)
                .animator(this)
                .duration(500)
                .boomButtonShadow(Util.getInstance().dp2px(2), Util.getInstance().dp2px(2))
                .subButtonsShadow(Util.getInstance().dp2px(2), Util.getInstance().dp2px(2))
                .onSubButtonClick(this)
                .init(mButtonMenuMain);

    }

    @Override
    public void toShow() {

    }

    @Override
    public void showing(float fraction) {

    }

    @Override
    public void showed() {

    }

    @Override
    public void toHide() {

    }

    @Override
    public void hiding(float fraction) {

    }

    @Override
    public void hided() {

    }

    @Override
    public void onClick(int buttonIndex) {
        switch (buttonIndex) {
            case 0:
                final Dialog displayPPDialog = new Dialog(PaintMainActivity.this, android.R.style.Theme_Holo_Light_Dialog);
                displayPPDialog.setTitle("选择贴图");
                displayPPDialog.setContentView(R.layout.pastephoto_layout);
                //displayPPDialog.getWindow().setLayout();
                GridView photoGridView = (GridView) displayPPDialog.findViewById(R.id.paste_grid);
                //photoGridView.remove
                PastePhotoAdapter adapter = new PastePhotoAdapter(PaintMainActivity.this);
                photoGridView.setAdapter(adapter);
                photoGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent, View view, int position, long id) {
                        canvansImageView.setPaintMode(DrawView.COPY_MODE);
                        Bitmap copyBitmap = BitmapFactory.decodeResource(getResources(), (int) id);
                        canvansImageView.setCopyBitmap(copyBitmap);
                        displayPPDialog.dismiss();
                    }

                });
                displayPPDialog.show();
                break;
            case 1:
                AlertDialog alertDialog = new AlertDialog.Builder(PaintMainActivity.this, android.R.style.Theme_Holo_Light_Dialog)
                        .setTitle("选择绘图类型")
                        .setSingleChoiceItems(
                                new String[]{"直线", "折线", "矩形",
                                        "六边形", "椭圆", "自由手绘"},
                                canvansImageView.getCurrentShape(),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        switch (which) {

                                            case Shape_Type.STRAIGIT:
                                                canvansImageView
                                                        .setShape(Shape_Type.STRAIGIT);
                                                break;
                                            case Shape_Type.BROKEN:
                                                canvansImageView.setShape(Shape_Type.BROKEN);
                                                break;
                                            case Shape_Type.RECT:
                                                canvansImageView
                                                        .setShape(Shape_Type.RECT);
                                                break;
                                            case Shape_Type.MUTIL:
                                                canvansImageView.setShape(Shape_Type.MUTIL);
                                                break;
                                            case Shape_Type.OVAL:
                                                canvansImageView.setShape(Shape_Type.OVAL);
                                                break;
                                            case Shape_Type.FREE:
                                                canvansImageView
                                                        .setShape(Shape_Type.FREE);
                                                break;
                                            default:
                                                break;
                                        }
                                        canvansImageView.setCurrentShape();
                                        if (canvansImageView.getPaintMode() != DrawView.COMMON_MODE) {
                                            // fillcolorImageView.setBackgroundColor(Color.TRANSPARENT);
                                            // cutImageView.setBackgroundColor(Color.TRANSPARENT);
                                            canvansImageView.setPaintMode(DrawView.COMMON_MODE);
                                        }

                                        dialog.dismiss();
                                    }

                                }).create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();
                break;
            case 2:
                if (canvansImageView.getPaintMode() != DrawView.COMMON_MODE) {
                    canvansImageView.setPaintMode(DrawView.COMMON_MODE);
                }
                break;
            case 3:
                setsizeDialog = new Dialog(PaintMainActivity.this, android.R.style.Theme_Holo_Light_Dialog);
                setsizeDialog.setTitle("画笔粗细");
                setsizeDialog.setContentView(getLayoutInflater().inflate(R.layout.shape_paint, null));
                setsizeDialog.getWindow().setLayout(DeviceInfoUtils.getScreenWidth(PaintMainActivity.this), ViewGroup.LayoutParams.WRAP_CONTENT);
                setsizeDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        processImageView.displayPenSize(Integer.parseInt(OPUtils.getValFromSharedpreferences(Const.Key.PEN_COLOR_SIZE)));
                    }
                });
                penTextView = (TextView) setsizeDialog.getWindow().findViewById(R.id.process_text);
                processImageView = (DisplayPenSizeView) setsizeDialog.getWindow().findViewById(R.id.pen_shape);
                process_okButton = (Button) setsizeDialog.getWindow().findViewById(R.id.pen_ok);
                process_cancelButton = (Button) setsizeDialog.getWindow().findViewById(R.id.pen_cancel);
                process_okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OPUtils.saveValToSharedpreferences(Const.Key.PEN_COLOR_SIZE, "" + penProcess);
                        canvansImageView.setPenSize(penProcess);
                        setsizeDialog.dismiss();
                    }
                });
                process_cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setsizeDialog.dismiss();
                    }
                });
                penSeekBar = (SeekBar) setsizeDialog.getWindow().findViewById(R.id.pen_seekbar);
                penSeekBar.setMax(50);
                penSeekBar.setProgress(Integer.parseInt(OPUtils.getValFromSharedpreferences(Const.Key.PEN_COLOR_SIZE)));
                penTextView.setText(OPUtils.getValFromSharedpreferences(Const.Key.PEN_COLOR_SIZE));
                penSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        penProcess = seekBar.getProgress();
                        processImageView.displayPenSize(penProcess);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        penTextView.setText("" + progress);
                        processImageView.displayPenSize(progress);
                    }
                });
                setsizeDialog.show();
                break;
            case 4:
                Background backDialog = new Background(PaintMainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog);
                backDialog.setTitle("更换背景图");
                backDialog.show();
                break;
            case 5:
                if (canvansImageView.getPaintMode() != DrawView.ERASER_MODE) {
                    canvansImageView.setPaintMode(DrawView.ERASER_MODE);
                }
                break;
            case 6:
                final Dialog colorDialog = new Dialog(PaintMainActivity.this, android.R.style.Theme_Holo_Light_Dialog);
                colorDialog.setTitle("颜色选择");
                colorDialog.setContentView(R.layout.dialog_for_selectcolor);
                final ColorPicker colorPicker = (ColorPicker) colorDialog
                        .findViewById(R.id.picker);
                colorPicker.setOldCenterColor(canvansImageView.getColor());
                colorPicker.setColor(canvansImageView.getColor());
                final OpacityBar opacityBar = (OpacityBar) colorDialog
                        .findViewById(R.id.opacitybar);
                final CheckBox blackCheckBox = (CheckBox) colorDialog.findViewById(R.id.black_checkbox);
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
                        if (blackCheckBox.isChecked()) {
                            isBlackColor = true;
                            canvansImageView.setColor(Color.BLACK);
                        } else {
                            isBlackColor = false;
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
                break;
            case 7:
                canvansImageView.clear();
                break;
            case 8:
                final EditText textEditText = new EditText(PaintMainActivity.this);
                textEditText.setHint("输入自定义文字");
                AlertDialog textDialog = new AlertDialog.Builder(PaintMainActivity.this, android.R.style.Theme_Holo_Light_Dialog)
                        .setTitle("自定义文字").setView(textEditText).
                                setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        paintText = textEditText.getText().toString();
                                        if (paintText.equals("")) {
                                            Toast.makeText(PaintMainActivity.this, "请输入自定义文字", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        canvansImageView.setPaintMode(DrawView.PASTETEXT_MODE);
                                        canvansImageView.setPaintText(paintText);
                                        dialog.dismiss();
                                    }
                                }).setNegativeButton("取消", null).create();
                textDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                textDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                textDialog.show();
                break;
        }
    }

}
