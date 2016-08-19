package com.ysr.ftpdemo.beans;

/**
 * Created by Administrator on 2016/8/19.
 */
public class FilesBean {
    public String title;
    public String time;
    private boolean selected;
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
