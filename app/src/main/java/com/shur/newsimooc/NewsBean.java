package com.shur.newsimooc;

/**
 * Created by Shur on 2016/11/16.
 * 新闻JSON数据的解析bean
 */

public class NewsBean {
    public String newsIconUrl;//新闻配图
    public String newsTitle;//新闻标题
    public String newsContent;//新闻内容

    public String getNewsIconUrl() {
        return newsIconUrl;
    }

    public void setNewsIconUrl(String newsIconUrl) {
        this.newsIconUrl = newsIconUrl;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }

    public String getNewsContent() {
        return newsContent;
    }

    public void setNewsContent(String newsContent) {
        this.newsContent = newsContent;
    }
}
