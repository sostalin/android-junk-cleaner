package com.soe.junkcleaner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnClean = findViewById(R.id.btnClean);
        tvStatus = findViewById(R.id.tvStatus);

        btnClean.setOnClickListener(v -> cleanJunk());
    }

    private void cleanJunk() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                Toast.makeText(this, "กรุณาเปิดสิทธิ์ All Files Access ก่อน", Toast.LENGTH_LONG).show();
                return;
            }
        } else if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return;
        }

        long bytesFreed = deleteJunkFiles();
        double mbFreed = bytesFreed / (1024.0 * 1024.0);

        String result = String.format("ลบขยะเสร็จสิ้น!\nลบไป %.2f MB", mbFreed);
        tvStatus.setText(result);
        Toast.makeText(this, "ลบขยะเรียกร้อย", Toast.LENGTH_LONG).show();
    }

    private long deleteJunkFiles() {
        long total = 0;

        total += deleteRecursive(getCacheDir());
        if (getExternalCacheDir() != null) total += deleteRecursive(getExternalCacheDir());

        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        total += deleteRecursive(new File(downloadDir, ".cache"));
        total += deleteRecursive(new File(downloadDir, "temp"));

        return total;
    }

    private long deleteRecursive(File fileOrDir) {
        if (fileOrDir == null || !fileOrDir.exists()) return 0;

        long size = 0;
        if (fileOrDir.isDirectory()) {
            for (File child : fileOrDir.listFiles()) {
                size += deleteRecursive(child);
            }
        }
        size += fileOrDir.length();
        fileOrDir.delete();
        return size;
    }
}