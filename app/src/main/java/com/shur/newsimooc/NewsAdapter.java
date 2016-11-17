package com.shur.newsimooc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Shur on 2016/11/16.
 * listview异步加载提高效率
 * ①滑动时停止后才加载可见项
 * ②滑动时时，取消所有加载项
 */

class NewsAdapter extends BaseAdapter implements AbsListView.OnScrollListener {

    private List<NewsBean> mList;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    private int mStart, mEnd;
    static String[] URLS;//存要加载的url到数组里
    private boolean mFirstIn;//首次启动加载

    NewsAdapter(Context context, List<NewsBean> list, ListView listView) {
        mList = list;
        mInflater = LayoutInflater.from(context);
        mImageLoader = new ImageLoader(listView);//构造方法初始化保证只有一个imageloader对象
        //把url存到数组里
        URLS = new String[mList.size()];
        for (int i = 0; i < mList.size(); i++) {
            URLS[i] = mList.get(i).newsIconUrl;
        }
        mFirstIn = true;
        listView.setOnScrollListener(this);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = mInflater.inflate(R.layout.item_layout, null);
            viewHolder.ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
            viewHolder.tvTitle = (TextView) view.findViewById(R.id.tv_title);
            viewHolder.tvContent = (TextView) view.findViewById(R.id.tv_content);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.ivIcon.setImageResource(R.drawable.ic_launcher);

        String url = mList.get(i).newsIconUrl;
        viewHolder.ivIcon.setTag(url);
//        new ImageLoader().showImageByThread(viewHolder.ivIcon, url);

//        new ImageLoader().showImageByAsyncTask(viewHolder.ivIcon,url);

        mImageLoader.showImageByAsyncTask(viewHolder.ivIcon, url);

        viewHolder.tvTitle.setText(mList.get(i).newsTitle);
        viewHolder.tvContent.setText(mList.get(i).newsContent);
        return view;
    }

    /**
     * 滑动状态改变
     *
     * @param absListView
     * @param i           状态
     */
    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        if (i == SCROLL_STATE_IDLE) {
            //加载可见
            mImageLoader.loadImages(mStart, mEnd);
        } else {
            //取消所有加载任务
            mImageLoader.cancelAllTasks();
        }
    }

    /**
     * 滑动中
     *
     * @param absListView
     * @param i           第一个可见元素
     * @param i1          可见元素总数
     * @param i2          listview元素总数
     */
    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
        mStart = i;
        mEnd = i + i1;
        //第一次显示的时候调用
        if (mFirstIn && i1 > 0) {
            mImageLoader.loadImages(mStart, mEnd);
            mFirstIn = false;
        }
    }


    class ViewHolder {
        TextView tvTitle, tvContent;
        ImageView ivIcon;
    }
}
