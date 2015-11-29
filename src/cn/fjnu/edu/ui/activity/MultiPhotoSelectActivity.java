package cn.fjnu.edu.ui.activity;
/*图片选择类*/
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import cn.fjnu.edu.paint.R;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
 
/**
 * @author 默默等待
 */
public class MultiPhotoSelectActivity extends BaseActivity {
 
    private ArrayList<String> imageUrls;
    private DisplayImageOptions options;
    private ImageAdapter imageAdapter;
    //选择图片的个数
    private int checkCount=0;
    //选择图片的路径
    private List<String> selectPath;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_image_grid);
        final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        Cursor imagecursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy + " DESC");
 
        this.imageUrls = new ArrayList<String>();
 
        for (int i = 0; i < imagecursor.getCount(); i++) {
            imagecursor.moveToPosition(i);;
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
            imageUrls.add(imagecursor.getString(dataColumnIndex));
 
       //     System.out.println("=====> Array path => "+imageUrls.get(i));
        }
 
        options = new DisplayImageOptions.Builder()
            .showStubImage(R.drawable.stub_image)
            .showImageForEmptyUri(R.drawable.image_for_empty_url)
            .cacheInMemory()
            .cacheOnDisc()
            .build();
 
        imageAdapter = new ImageAdapter(this, imageUrls);
 
        GridView gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(imageAdapter);
        /*gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startImageGalleryActivity(position);
            }
        });*/
    }
 
    @Override
    protected void onStop() {
        imageLoader.stop();
        super.onStop();
    }
 
    public void btnChoosePhotosClick(View v){
    	
    	if(checkCount!=2&&checkCount!=4){
    		Toast.makeText(this, "只能选择两张或四张图片", Toast.LENGTH_SHORT).show();
    		return;
    	}else{
    		selectPath=imageAdapter.getCheckedItems();
    		plicePhoto(selectPath);
    		this.finish();
    	}
    
    }
 
    /*private void startImageGalleryActivity(int position) {
        Intent intent = new Intent(this, ImagePagerActivity.class);
        intent.putExtra(Extra.IMAGES, imageUrls);
        intent.putExtra(Extra.IMAGE_POSITION, position);
        startActivity(intent);
    }*/
    //拼接图片
    public void plicePhoto(List<String> selectList){
    	//创建一张透明的bitmap
    	Bitmap drawBitmap=Bitmap.createBitmap(PaintMainActivity.screenWidth, PaintMainActivity.screenHeight,
    			Bitmap.Config.ARGB_8888);
    	//以bitmap为背景绘制
    	Canvas canvas=new Canvas(drawBitmap);
    	//用于读取外部图片的信息
    	BitmapFactory.Options options=new BitmapFactory.Options();
    	//图片的实际宽度
    	int imageWidth;
    	//图片的实际高度
    	int imageHeight;
    	//计算X方向和Y方向上图片的压缩比例
    	int scaleX,scaleY;
    	if(selectList.size()==2){

    		Bitmap bitmap0,bitmap1;
    		//选择的第一张图片
    		//仅读取图片信息，不加载图片至内存
    		options.inJustDecodeBounds=true;
    		BitmapFactory.decodeFile(selectList.get(0),options);
    		imageWidth=options.outWidth;
    		imageHeight=options.outHeight;
    		scaleX=imageWidth/(PaintMainActivity.screenWidth);
    		scaleY=imageHeight/(PaintMainActivity.screenHeight/2);
    		if(scaleX>1&&scaleX>scaleY)
    			options.inSampleSize=scaleX;
    		 if(scaleX<scaleY&&scaleY>1)
    			options.inSampleSize=scaleY;
    		 options.inJustDecodeBounds=false;
    		 bitmap0=BitmapFactory.decodeFile(selectList.get(0), options);
    		//对图片的压缩处理
    		Bitmap scaleBitmap0=Bitmap.createScaledBitmap(bitmap0, PaintMainActivity.screenWidth, 
    				PaintMainActivity.screenHeight/2,true);
    	//	MainActivity.MActivity.
    		//选择的第二张图片
    		options.inJustDecodeBounds=true;
    		BitmapFactory.decodeFile(selectList.get(1),options);
    		imageWidth=options.outWidth;
    		imageHeight=options.outHeight;
    		scaleX=imageWidth/(PaintMainActivity.screenWidth);
    		scaleY=imageHeight/(PaintMainActivity.screenHeight/2);
    		if(scaleX>1&&scaleX>scaleY)
    			options.inSampleSize=scaleX;
    		 if(scaleX<scaleY&&scaleY>1)
    			options.inSampleSize=scaleY;
    		 options.inJustDecodeBounds=false;
    		 bitmap1=BitmapFactory.decodeFile(selectList.get(1),options);
    		//对图片的压缩处理
    		Bitmap scaleBitmap1=Bitmap.createScaledBitmap(bitmap1, PaintMainActivity.screenWidth, 
    				PaintMainActivity.screenHeight/2,true);
    		canvas.drawBitmap(scaleBitmap0, 0,0,null);
    		canvas.drawBitmap(scaleBitmap1, 0,PaintMainActivity.screenHeight/2,null);
    		//backImageBitmap=Bitmap.createBitmap(MainActivity.screenWidth, MainActivity.screenHeight, Config.ARGB_8888);
    		
    		
    	}else{
    		Bitmap bitmap0,bitmap1,bitmap2,bitmap3;
    		//选择的第一张图片
    		//BitmapFactory.Options imageOptions=new BitmapFactory.Options();
    		options.inJustDecodeBounds=true;
    		 BitmapFactory.decodeFile(selectList.get(0),options);
    		 imageWidth=options.outWidth;
    		 imageHeight=options.outHeight;
    		 scaleX=imageWidth/(PaintMainActivity.screenWidth/2);
    		 scaleY=imageHeight/(PaintMainActivity.screenHeight/2);
    		 if(scaleX>1&&scaleX>scaleY)
    			 options.inSampleSize=scaleX;
    		 if(scaleY>1&&scaleY>scaleX)
    			 options.inSampleSize=scaleY;
    		//对图片的压缩处理
    		 options.inJustDecodeBounds=false;
    		 bitmap0=BitmapFactory.decodeFile(selectList.get(0), options);
    		Bitmap scaleBitmap0=Bitmap.createScaledBitmap(bitmap0, PaintMainActivity.screenWidth/2, 
    				PaintMainActivity.screenHeight/2,true);
    		//选择的第二张图片
	    	options.inJustDecodeBounds=true;
	   		 BitmapFactory.decodeFile(selectList.get(1),options);
	   		 imageWidth=options.outWidth;
	   		 imageHeight=options.outHeight;
	   		 scaleX=imageWidth/(PaintMainActivity.screenWidth/2);
	   		 scaleY=imageHeight/(PaintMainActivity.screenHeight/2);
	   		 if(scaleX>1&&scaleX>scaleY)
	   			 options.inSampleSize=scaleX;
	   		 if(scaleY>1&&scaleY>scaleX)
	   			 options.inSampleSize=scaleY;
	   		//对图片的压缩处理
	   		 options.inJustDecodeBounds=false;
    		bitmap1=BitmapFactory.decodeFile(selectList.get(1));
    		//对图片的压缩处理
    		Bitmap scaleBitmap1=Bitmap.createScaledBitmap(bitmap1, PaintMainActivity.screenWidth/2, 
    				PaintMainActivity.screenHeight/2,true);
    		//选择的第三张图片
    		options.inJustDecodeBounds=true;
	   		 BitmapFactory.decodeFile(selectList.get(2),options);
	   		 imageWidth=options.outWidth;
	   		 imageHeight=options.outHeight;
	   		 scaleX=imageWidth/(PaintMainActivity.screenWidth/2);
	   		 scaleY=imageHeight/(PaintMainActivity.screenHeight/2);
	   		 if(scaleX>1&&scaleX>scaleY)
	   			 options.inSampleSize=scaleX;
	   		 if(scaleY>1&&scaleY>scaleX)
	   			 options.inSampleSize=scaleY;
	   		//对图片的压缩处理
	   		 options.inJustDecodeBounds=false;
    		//对图片的压缩处理
	   		 bitmap2=BitmapFactory.decodeFile(selectList.get(2),options);
    		Bitmap scaleBitmap2=Bitmap.createScaledBitmap(bitmap2, PaintMainActivity.screenWidth/2, 
    				PaintMainActivity.screenHeight/2,true);
    		options.inJustDecodeBounds=true;
	   		 BitmapFactory.decodeFile(selectList.get(3),options);
	   		 imageWidth=options.outWidth;
	   		 imageHeight=options.outHeight;
	   		 scaleX=imageWidth/(PaintMainActivity.screenWidth/2);
	   		 scaleY=imageHeight/(PaintMainActivity.screenHeight/2);
	   		 if(scaleX>1&&scaleX>scaleY)
	   			 options.inSampleSize=scaleX;
	   		 if(scaleY>1&&scaleY>scaleX)
	   			 options.inSampleSize=scaleY;
	   		//对图片的压缩处理
	   		 options.inJustDecodeBounds=false;
    		bitmap3=BitmapFactory.decodeFile(selectList.get(3),options);
    		//对图片的压缩处理
    		Bitmap scaleBitmap3=Bitmap.createScaledBitmap(bitmap3, PaintMainActivity.screenWidth/2, 
    				PaintMainActivity.screenHeight/2,true);
    		canvas.drawBitmap(scaleBitmap0, 0, 0,null);
    		canvas.drawBitmap(scaleBitmap1, PaintMainActivity.screenWidth/2,0,null);
    		canvas.drawBitmap(scaleBitmap2, 0, PaintMainActivity.screenHeight/2,null);
    		canvas.drawBitmap(scaleBitmap3, PaintMainActivity.screenWidth/2, PaintMainActivity.screenHeight/2,null);
    		//backImageBitmap=Bitmap.createBitmap(MainActivity.screenWidth, MainActivity.screenHeight, Config.ARGB_8888);
    		
    	}
    		//DrawView.drawView.setImageBitmap(drawBitmap);
    	PaintMainActivity.canvasView.setImageBitmap(drawBitmap);
    	
    }
    public class ImageAdapter extends BaseAdapter {
 
        ArrayList<String> mList;
        LayoutInflater mInflater;
        Context mContext;
        SparseBooleanArray mSparseBooleanArray;
 
        public ImageAdapter(Context context, ArrayList<String> imageList) {
            // TODO Auto-generated constructor stub
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            mSparseBooleanArray = new SparseBooleanArray();
            mList = new ArrayList<String>();
            this.mList = imageList;
 
        }
 
        public ArrayList<String> getCheckedItems() {
            ArrayList<String> mTempArry = new ArrayList<String>();
 
            for(int i=0;i<mList.size();i++) {
                if(mSparseBooleanArray.get(i)) {
                    mTempArry.add(mList.get(i));
                }
            }
 
            return mTempArry;
        }
 
        @Override
        public int getCount() {
            return imageUrls.size();
        }
 
        @Override
        public Object getItem(int position) {
            return null;
        }
 
        @Override
        public long getItemId(int position) {
            return position;
        }
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
 
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.row_multiphoto_item, null);
            }
 
            CheckBox mCheckBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
            final ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);
 
            imageLoader.displayImage("file://"+imageUrls.get(position), imageView, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(Bitmap loadedImage) {
                    Animation anim = AnimationUtils.loadAnimation(MultiPhotoSelectActivity.this, R.anim.fade_in);
                    imageView.setAnimation(anim);
                    anim.start();
                }
            });
 
            mCheckBox.setTag(position);
            mCheckBox.setChecked(mSparseBooleanArray.get(position));
            mCheckBox.setOnCheckedChangeListener(mCheckedChangeListener);
 
            return convertView;
        }
 
        OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {
 
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
            	if(isChecked)
            		checkCount++;
            	else
            		checkCount--;
                mSparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);
              //  checkCount++;
            }
        };
       
    }
   
}