package com.shur.newsimooc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Shur on 2016/11/16.
 * 多线程/asynctask加载图片的工具类
 */

public class ImageLoader {

    private ImageView mImageView;
    private String murl;

    //Map？
    private LruCache<String, Bitmap> mLruCache;
    private Set<NewsAsyncTask> mTasks; //保存task
    private ListView mListView;//保存要加载图片区间的那个listview

    public ImageLoader(ListView listView) {
        mListView = listView;
        mTasks = new HashSet<>();
        //获取最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        //缓存为最大可用内存的1/4
        int cacheSize = maxMemory / 4;
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存的时候调用
                return value.getByteCount();
            }
        };
    }

    /**
     * 把对应url的bitmap存入到缓存中
     *
     * @param url
     * @param bitmap
     */
    public void addBitmapToCache(String url, Bitmap bitmap) {
        if (getBitmapFromeCache(url) == null) {
            mLruCache.put(url, bitmap);
        }
    }

    /**
     * 从缓存中根据url取出bitmap
     *
     * @param url
     * @return
     */
    public Bitmap getBitmapFromeCache(String url) {
        Bitmap bitmap = mLruCache.get(url);
        return bitmap;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //通过设置gettag和settag 来解决listview加载图片
            if (mImageView.getTag().equals(murl)) {
                mImageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };

    /**
     * 多线程加载图片
     *
     * @param imageView 要加载图片的iv
     * @param url       图片的url
     */
    public void showImageByThread(ImageView imageView, final String url) {

        mImageView = imageView;
        murl = url;

        new Thread() {
            @Override
            public void run() {
                super.run();
                Bitmap bitmap = getBitmapFromURL(url);
                Message message = Message.obtain();
                message.obj = bitmap;
                mHandler.sendMessage(message);
            }
        }.start();
    }

    public Bitmap getBitmapFromURL(String urlString) {
        Bitmap bitmap;
        InputStream is = null;
        try {
            URL url = new URL(urlString);
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                is = new BufferedInputStream(connection.getInputStream());
                bitmap = BitmapFactory.decodeStream(is);
                connection.disconnect();
                return bitmap;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 通过asynctask异步加载图片
     *
     * @param imageView
     * @param url
     * @return
     */
    public void showImageByAsyncTask(ImageView imageView, String url) {
        //从缓存中取出图片 没有使用网络加载
        Bitmap bitmap = getBitmapFromeCache(url);
        if (bitmap == null) {
            imageView.setImageResource(R.drawable.ic_launcher);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * 当listview滑动时加载可见列表的数据
     *
     * @param start 可见的第一个元素
     * @param end   最后一个可见的元素
     */
    public void loadImages(int start, int end) {
        for (int i = start; i < end; i++) {
            String url = NewsAdapter.URLS[i];
            Bitmap bitmap = getBitmapFromeCache(url);

            if (bitmap == null) {
                NewsAsyncTask task = new NewsAsyncTask(url);
                task.execute(url);
                mTasks.add(task);
            } else {
                ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    /**
     * 取消所有加载的任务
     */
    public void cancelAllTasks() {
        if (mTasks != null) {
            for (NewsAsyncTask task : mTasks) {
                task.cancel(false);
            }
        }
    }

    private class NewsAsyncTask extends AsyncTask<String, Void, Bitmap> {

        //        private ImageView mImageView;
        private String murl;//标签

        public NewsAsyncTask(String url) {
//            mImageView = imageView;
            murl = url;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            //从网络获取图片
            String url = strings[0];
            Bitmap bitmap = getBitmapFromURL(url);
            if (bitmap != null) {
                //加入缓存
                addBitmapToCache(url, bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
//            if (mImageView.getTag().equals(murl)) {
//                mImageView.setImageBitmap(bitmap);
//            }
            ImageView imageView = (ImageView) mListView.findViewWithTag(murl);
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
            mTasks.remove(this);
        }
    }


}
