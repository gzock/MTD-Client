package com.example.mtd_client.app;

import java.util.ArrayList;

/**
 * Created by Gzock on 2014/05/02.
 */
public class TargetListData {

    private String  id          = null;
    private String  targetName  = null;
    private String  beforeAfter = null;
    private Integer picture     = null;

    // コンストラクタ
    public TargetListData() {}

    // セッター
    public void setId          (String str)  { this.id          = str; }
    public void setTargetName  (String str)  { this.targetName  = str; }
    public void setBeforeAgter (String str)  { this.beforeAfter = str; }
    public void setPicture     (String str)  { this.picture     = Integer.valueOf(str); }

    // ゲッター
    public String  getId()          { return this.id; }
    public String  getTargetName()  { return this.targetName; }
    public String  getBeforeAfter() { return this.beforeAfter; }
    public Integer getPicture()     { return this.picture; }
}
