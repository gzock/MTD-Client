/**
 * http://alldaysyu-ya.blogspot.jp/2013/09/android_12.html
 * 大感謝!!!
 */

package com.example.mtd_client.app;
 
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
 
/**
 * �t�@�C���I���_�C�A���O
 */
public class FileSelectDialog extends Activity implements OnClickListener {
 
    //アクティビティ
    private Activity activity = null;
 
    //リスナー
    private OnFileSelectDialogListener listener = null;
 
    //拡張子
    private String extension = "";
 
    //ファイルリスト
    private List<File> viewFileDataList = null;
 
    //履歴
    private List<String> viewPathHistory = null;
 
    //コンストラクタ
    public FileSelectDialog(Activity activity) {
 
        this.activity = activity;
        this.viewPathHistory = new ArrayList<String>();
    }
 
    //コンストラクタ
    public FileSelectDialog(Activity activity, String extension) {
 
        this.activity = activity;
        this.extension = extension;
        this.viewPathHistory = new ArrayList<String>();
    }
 
    /**
     * �I���C�x���g
     *
     * @param dialog �_�C�A���O
     * @param which �I���ʒu
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
 
        File File = this.viewFileDataList.get(which);
 
        // �f�B���N�g���̏ꍇ
        if (File.isDirectory()) {
 
            show(File.getAbsolutePath() + "/");
 
        } else {
 
            this.listener.onClickFileSelect(File);
        }
    }
 
    /**
     * �_�C�A���O��\��
     *
     * @param dirPath �f�B���N�g���̃p�X
     */
    public void show(final String dirPath) {
 
        // �ύX����̏ꍇ
        if (this.viewPathHistory.size() == 0 || !dirPath.equals(this.viewPathHistory.get(this.viewPathHistory.size() - 1))) {
 
            // ������ǉ�
            this.viewPathHistory.add(dirPath);
        }
 
        // �t�@�C�����X�g
        File[] FileArray = new File(dirPath).listFiles();
 
        // ���O���X�g
        List<String> nameList = new ArrayList<String>();
 
        if (FileArray != null) {
 
            // �t�@�C�����}�b�v
            Map<String, File> map = new HashMap<String, File>();
 
            for (File File : FileArray) {
 
                // �f�B���N�g���̏ꍇ
                if (File.isDirectory()) {
 
                    nameList.add(File.getName() + "/");
                    map.put(nameList.get(map.size()), File);
 
                // �ΏۂƂȂ�g���q�̏ꍇ
                } else if ("".equals(this.extension) || File.getName().matches("^.*" + this.extension + "$")) {
 
                    nameList.add(File.getName());
                    map.put(nameList.get(map.size()), File);
                }
            }
 
            // �\�[�g
            Collections.sort(nameList);
 
            // �t�@�C����񃊃X�g
            this.viewFileDataList = new ArrayList<File>();
 
            for (String name : nameList) {
 
                this.viewFileDataList.add(map.get(name));
            }
        }
 
        // �_�C�A���O�𐶐�
        AlertDialog.Builder dialog = new AlertDialog.Builder(this.activity);
        dialog.setTitle(dirPath);
        //dialog.setIcon(R.drawable.);
        dialog.setItems(nameList.toArray(new String[0]), this);
 
        dialog.setPositiveButton("上へ", new DialogInterface.OnClickListener() {
 
            @Override
            public void onClick(DialogInterface dialog, int value) {

            if (!"/".equals(dirPath)) {
 
                    String dirPathNew = dirPath.substring(0, dirPath.length() - 1);
                    dirPathNew = dirPathNew.substring(0, dirPathNew.lastIndexOf("/") + 1);
 
                    // ������ǉ�
                    FileSelectDialog.this.viewPathHistory.add(dirPathNew);
 
                    // 1���
                    show(dirPathNew);
 
                } else {
 
                    // ����ێ�
                    show(dirPath);
                }
            }
        });
 
        dialog.setNeutralButton("戻る", new DialogInterface.OnClickListener() {
 
            @Override
            public void onClick(DialogInterface dialog, int value) {
 
                int index = FileSelectDialog.this.viewPathHistory.size() - 1;
 
                if (index > 0) {
 
                    // �������폜
                    FileSelectDialog.this.viewPathHistory.remove(index);
 
                    // 1�O�ɖ߂�
                    show(FileSelectDialog.this.viewPathHistory.get(index - 1));
 
                } else {
 
                    // ����ێ�
                    show(dirPath);
                }
            }
        });
 
        dialog.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
 
            @Override
            public void onClick(DialogInterface dialog, int value) {
 
                FileSelectDialog.this.listener.onClickFileSelect(null);
            }
        });
 
        dialog.show();
    }
 
    /**
     * ���X�i�[��ݒ�
     *
     * @param listener �I���C�x���g���X�i�[
     */
    public void setOnFileSelectDialogListener(OnFileSelectDialogListener listener) {
 
        this.listener = listener;
    }
 
    /**
     * �{�^�������C���^�[�t�F�[�X
     */
    public interface OnFileSelectDialogListener {
 
        /**
         * �I���C�x���g
         *
         * @param File �t�@�C��
         */
        public void onClickFileSelect(File File);
    }
}
 

