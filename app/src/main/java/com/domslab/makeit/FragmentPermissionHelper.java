package com.domslab.makeit;

import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.FragmentActivity;


public class FragmentPermissionHelper {
    public void startPermissionRequest(FragmentActivity fa, FragmentPermissionInterface fs, String manifest) {
        ActivityResultLauncher<String> requestPermissionLauncher =
                fa.registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> fs.onGranted(isGranted));
        requestPermissionLauncher.launch(manifest);
    }
}
