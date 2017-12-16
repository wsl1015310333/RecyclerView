package com.example.wangsl.androidfragment;

/**
 * Created by wangsl on 2017/11/29.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.lang.ref.WeakReference;
/**
 * Created by zyh on 2015/9/15.
 */
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {
    private String [] mImages;
    private Context mContext;
    private LayoutInflater mInflater;
    private Bitmap mBitmap;
    private LruCache<String ,BitmapDrawable>mMemoryCache;
    public MyRecyclerViewAdapter(Context context,String [] imagesUrls){
        this.mContext= context;
        mImages = imagesUrls;
        mInflater = LayoutInflater.from(context);

        mBitmap=BitmapFactory.decodeResource(context.getResources(),R.drawable.default_img);

        int maxMemory=(int)Runtime.getRuntime().maxMemory();
        int cachesize=maxMemory/6;
        mMemoryCache=new LruCache<String ,BitmapDrawable>(cachesize){
            @Override
            protected int sizeOf(String key, BitmapDrawable value) {
                return value.getBitmap().getByteCount();
            }
        };
    }

    @Override
    public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_view,parent,false);
        ViewHolder holder = new ViewHolder(view);
        holder.imageView = (ImageView)view.findViewById(R.id.id_imageView);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyRecyclerViewAdapter.ViewHolder holder, int position) {
        String imageUrl= mImages[position];

        BitmapDrawable drawable=getBitmapDrawableFromMemoryCache(imageUrl);
        if(drawable!=null){
            holder.imageView.setImageDrawable(drawable);
        }else if(cancelPotentialTask(imageUrl,holder.imageView)){

            //执行下载操作
            DownLoadTask task = new DownLoadTask(holder.imageView);
            AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(),mBitmap,task);
            holder.imageView.setImageDrawable(asyncDrawable);
            task.execute(imageUrl);
        }


    }
    /**
     * 添加图片到缓存中
     * @param imageUrl
     * @param drawable
     */
    private void addBitmapDrawableToMemoryCache(String imageUrl,BitmapDrawable drawable){
        if (getBitmapDrawableFromMemoryCache(imageUrl) == null ){
            mMemoryCache.put(imageUrl, drawable);


        }
    }

    /**
     * 检查复用的ImageView中是否存在其他图片的下载任务，如果存在就取消并且返回ture 否则返回 false
     * @param imageUrl
     * @param imageView
     * @return
     */
    private boolean cancelPotentialTask(String imageUrl, ImageView imageView) {
        DownLoadTask task = getDownLoadTask(imageView);
        if (task != null) {
            String url = task.url;
            if (url == null || !url .equals(imageUrl)){
                task.cancel(true);
            }else{
                return false;
            }
        }
        return true;
    }

    /**
     * 新建一个类 继承BitmapDrawable
     * 目的： BitmapDrawable 和DownLoadTask建立弱引用关联
     */
    class AsyncDrawable extends  BitmapDrawable{
        private WeakReference<DownLoadTask> downLoadTaskWeakReference;

        public AsyncDrawable(Resources resources, Bitmap bitmap, DownLoadTask downLoadTask){
            super(resources,bitmap);
            downLoadTaskWeakReference = new WeakReference<DownLoadTask>(downLoadTask);
        }

        private DownLoadTask getDownLoadTaskFromAsyncDrawable(){
            return downLoadTaskWeakReference.get();
        }
    }

    /**
     * 获取当前ImageView 的图片下载任务
     * @param imageView
     * @return
     */
    private DownLoadTask getDownLoadTask(ImageView imageView){
        if (imageView != null){
            Drawable drawable  = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable ){
                return  ((AsyncDrawable) drawable).getDownLoadTaskFromAsyncDrawable();
            }
        }
        return null;
    }

    /**
     * 從缓存中获取已存在的图片
     * @param imageUrl
     * @return
     */
    private BitmapDrawable getBitmapDrawableFromMemoryCache(String imageUrl) {
        return mMemoryCache.get(imageUrl);
    }

    @Override
    public int getItemCount() {
        return mImages.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * 异步加载图片
     */
    class DownLoadTask extends AsyncTask<String ,Void,BitmapDrawable>{

      ///  private ImageView mImageView;
        String url;
        private WeakReference<ImageView> imageViewWeakReference;
        public DownLoadTask(ImageView imageView){
            imageViewWeakReference = new WeakReference<ImageView>(imageView);        }
        @Override
        protected BitmapDrawable doInBackground(String... params) {
            url = params[0];
            Bitmap bitmap = downLoadBitmap(url);
            BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(),bitmap);
            addBitmapDrawableToMemoryCache(url,drawable);
            return  drawable;
        }

        private Bitmap downLoadBitmap(String url) {
            Bitmap bitmap = null;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            try {
                Response response = client.newCall(request).execute();
                bitmap = BitmapFactory.decodeStream(response.body().byteStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        /**
         * 验证ImageView 中的下载任务是否相同 如果相同就返回
         * @return
         */
        private ImageView getAttachedImageView() {
            ImageView imageView = imageViewWeakReference.get();
            if (imageView != null){
                DownLoadTask task = getDownLoadTask(imageView);
                if (this == task ){
                    return  imageView;
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(BitmapDrawable drawable) {
            super.onPostExecute(drawable);
ImageView mImageView=getAttachedImageView();
            if ( mImageView != null && drawable != null){
                mImageView.setImageDrawable(drawable);
            }
        }
    }
}