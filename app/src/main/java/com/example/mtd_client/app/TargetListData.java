package com.example.mtd_client.app;

import java.util.ArrayList;

/**
 * Created by Gzock on 2014/05/02.
 */
public class TargetListData {

    private String  id                  = null;
    private String  parent              = null;
    private String  targetName          = null;
    private Integer  photoBeforeAfter   = null;
    private Integer photoCheck          = null;
    private Double bfr_photo_shot_cnt  = null;
    private Double bfr_photo_total_cnt = null;
    private Double aft_photo_shot_cnt  = null;
    private Double aft_photo_total_cnt = null;
    private Integer type                = null;
    private Integer lock                = null;
    // コンストラクタ
    public TargetListData() {}

    // セッター
    public void setId                (String str) { this.id                  = str; }
    public void setParent            (String str) { this.parent              = str; }
    public void setTargetName        (String str) { this.targetName          = str; }
    public void setPhotoBeforeAfter  (String str) { this.photoBeforeAfter    = Integer.valueOf(str); }
    public void setPhotoCheck        (String str) { this.photoCheck          = Integer.valueOf(str); }
    public void setBfrPhotoShotCnt  (String str) { this.bfr_photo_shot_cnt  = Double.valueOf(str); }
    public void setBfrPhotoTotalCnt (String str) { this.bfr_photo_total_cnt = Double.valueOf(str); }
    public void setAftPhotoShotCnt   (String str) { this.aft_photo_shot_cnt  = Double.valueOf(str); }
    public void setAftPhotoTotalCnt  (String str) { this.aft_photo_total_cnt = Double.valueOf(str); }
    public void setType              (String str) { this.type                = Integer.valueOf(str); }
    public void setLock              (String str) { this.lock                = Integer.valueOf(str); }

    // ゲッター
    public String  getId()               { return this.id; }
    public String  getParent()           { return this.parent; }
    public String  getTargetName()       { return this.targetName; }
    public Integer getPhotoBeforeAfter() { return this.photoBeforeAfter; }
    public Integer getPhotoCheck()       { return this.photoCheck; }
    public Double getBfrPhotoShotCnt()  { return this.bfr_photo_shot_cnt; }
    public Double getBfrPhotoTotalCnt() { return this.bfr_photo_total_cnt; }
    public Double getAftPhotoShotCnt()  { return this.aft_photo_shot_cnt; }
    public Double getAftPhotoTotalCnt() { return this.aft_photo_total_cnt; }
    public Integer getType()             { return this.type; }
    public Integer getLock()             { return this.lock; }
}
