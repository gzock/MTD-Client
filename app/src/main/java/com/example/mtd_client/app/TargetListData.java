package com.example.mtd_client.app;

import java.util.ArrayList;

/**
 * Created by Gzock on 2014/05/02.
 */
public class TargetListData {
    private String targetName  = null;
    private String beforeAfter = null;
    private Integer picture    = null;

    public TargetListData() {}

    public void setTargetName(String str)  { this.targetName  = str; }
    public void setBeforeAgter(String str) { this.beforeAfter = str; }
    public void setPicture(String str)     { this.picture     = Integer.valueOf(str); }

    public String  getTargetName()  { return this.targetName; }
    public String  getBeforeAfter() { return this.beforeAfter; }
    public Integer getPicture()     { return this.picture; }
}
