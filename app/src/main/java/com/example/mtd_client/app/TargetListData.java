package com.example.mtd_client.app;

import java.util.ArrayList;

/**
 * Created by Gzock on 2014/05/02.
 */
public class TargetListData {

    private String  id                = null;
    private String  parent            = null;
    private String  targetName        = null;
    private Integer  photoBeforeAfter  = null;
    private Integer photoCheck        = null;
    private Integer bfr_photo_percent = null;
    private Integer aft_photo_percent = null;
    private Integer type              = null;

    // コンストラクタ
    public TargetListData() {}

    // セッター
    public void setId               (String str) { this.id          = str; }
    public void setParent           (String str) { this.parent      = str; }
    public void setTargetName       (String str) { this.targetName  = str; }
    public void setPhotoBeforeAfter (String str) { this.photoBeforeAfter = Integer.valueOf(str); }
    public void setPhotoCheck       (String str) { this.photoCheck = Integer.valueOf(str); }
    public void setBfrPhotoPercent  (String str) { this.bfr_photo_percent = Integer.valueOf(str); }
    public void setAftPhotoPercent  (String str) { this.aft_photo_percent = Integer.valueOf(str); }
    public void setType             (String str) { this.type = Integer.valueOf(str); }

    // ゲッター
    public String  getId()               { return this.id; }
    public String  getParent()           { return this.parent; }
    public String  getTargetName()       { return this.targetName; }
    public Integer  getPhotoBeforeAfter() { return this.photoBeforeAfter; }
    public Integer getPhotoCheck()       { return this.photoCheck; }
    public Integer getBfrPhotoPercent()  { return this.bfr_photo_percent; }
    public Integer getAftPhotoPercent()  { return this.aft_photo_percent; }
    public Integer getType()             { return this.type; }
}
