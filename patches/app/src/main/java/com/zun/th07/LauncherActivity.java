package com.zun.th07;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LauncherActivity extends Activity {
    private static final int PICK_TH07 = 1001;
    private static final int PICK_BGM = 1002;
    private static final int PICK_FONT = 1003;

    private TextView status;
    private Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        refreshStatus();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        int p = dp(20);
        root.setPadding(p, p, p, p);

        TextView title = new TextView(this);
        title.setText("TH07 Android\n东方妖妖梦 ～ Perfect Cherry Blossom");
        title.setTextSize(22f);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView info = new TextView(this);
        info.setText("此 APK 不包含原版游戏素材。请从你合法拥有的日文版 1.00b 中导入下面三个文件。\n\n首次导入后，文件会复制到本应用自己的目录中。以后可直接启动游戏。");
        info.setTextSize(15f);
        info.setPadding(0, dp(18), 0, dp(18));
        root.addView(info);

        root.addView(makeImportButton("导入 th07.dat", "th07.dat", PICK_TH07));
        root.addView(makeImportButton("导入 thbgm.dat", "thbgm.dat", PICK_BGM));
        root.addView(makeImportButton("导入 msgothic.ttc", "msgothic.ttc", PICK_FONT));

        status = new TextView(this);
        status.setTextSize(14f);
        status.setPadding(0, dp(18), 0, dp(18));
        root.addView(status);

        playButton = new Button(this);
        playButton.setText("启动游戏");
        playButton.setOnClickListener(v -> launchGame());
        root.addView(playButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        Button folderButton = new Button(this);
        folderButton.setText("查看资源目录位置");
        folderButton.setOnClickListener(v -> {
            File dir = getExternalFilesDir(null);
            String path = dir == null ? "不可用" : dir.getAbsolutePath();
            Toast.makeText(this, path, Toast.LENGTH_LONG).show();
        });
        root.addView(folderButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        setContentView(root);
    }

    private Button makeImportButton(String label, String targetName, int requestCode) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setOnClickListener(v -> pickFile(targetName, requestCode));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(8));
        button.setLayoutParams(lp);
        return button;
    }

    private void pickFile(String expectedName, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, expectedName);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            return;
        }

        String targetName;
        if (requestCode == PICK_TH07) {
            targetName = "th07.dat";
        } else if (requestCode == PICK_BGM) {
            targetName = "thbgm.dat";
        } else if (requestCode == PICK_FONT) {
            targetName = "msgothic.ttc";
        } else {
            return;
        }

        Uri uri = data.getData();
        String pickedName = displayName(uri);
        if (pickedName != null && !targetName.equalsIgnoreCase(pickedName)) {
            Toast.makeText(this,
                    "你选择的是 “" + pickedName + "”，这里需要 “" + targetName + "”。",
                    Toast.LENGTH_LONG).show();
            return;
        }

        try {
            copyToGameDir(uri, targetName);
            Toast.makeText(this, targetName + " 导入成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "导入失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        refreshStatus();
    }

    private String displayName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    private void copyToGameDir(Uri uri, String targetName) throws IOException {
        File dir = getExternalFilesDir(null);
        if (dir == null) throw new IOException("无法访问应用文件目录");
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("无法创建应用文件目录");

        File tmp = new File(dir, targetName + ".part");
        File target = new File(dir, targetName);

        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(tmp)) {
            if (in == null) throw new IOException("无法读取所选文件");
            byte[] buffer = new byte[1024 * 256];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }

        if (target.exists() && !target.delete()) {
            throw new IOException("无法覆盖旧文件 " + targetName);
        }
        if (!tmp.renameTo(target)) {
            throw new IOException("无法保存 " + targetName);
        }
    }

    private void refreshStatus() {
        File dir = getExternalFilesDir(null);
        boolean a = dir != null && new File(dir, "th07.dat").isFile();
        boolean b = dir != null && new File(dir, "thbgm.dat").isFile();
        boolean c = dir != null && new File(dir, "msgothic.ttc").isFile();

        status.setText("资源状态：\n" +
                (a ? "✓ " : "✗ ") + "th07.dat\n" +
                (b ? "✓ " : "✗ ") + "thbgm.dat\n" +
                (c ? "✓ " : "✗ ") + "msgothic.ttc");
        playButton.setEnabled(a && b && c);
    }

    private void launchGame() {
        refreshStatus();
        if (!playButton.isEnabled()) {
            Toast.makeText(this, "请先导入三个资源文件", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, PerfectCherryBlossom.class);
        startActivity(intent);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
