package com.reveny.virtualinject.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.reveny.virtualinject.R;
import com.reveny.virtualinject.databinding.FragmentDeveloperBinding;
import com.vcore.BlackBoxCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DeveloperFragment extends BaseFragment {
    private FragmentDeveloperBinding binding;
    private String selectedApp;
    private String libraryPath;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDeveloperBinding.inflate(inflater, container, false);
        // Perbaikan: gunakan overload yang menerima String title
        setupToolbar(binding.toolbar, null, "Developer Tools", 0, null);

        setupAppList();

        binding.libPathChoose.setEndIconOnClickListener(v -> {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/octet-stream"});
            startActivityForResult(Intent.createChooser(chooseFile, "Choose .so file"), 100);
        });

        binding.injectLaunchButton.setOnClickListener(v -> {
            if (selectedApp != null && libraryPath != null) {
                Toast.makeText(requireContext(), "Injecting & Launching...", Toast.LENGTH_SHORT).show();
                BlackBoxCore.get().launchApk(selectedApp, 0);
            } else {
                Toast.makeText(requireContext(), "Select app and .so file", Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    private void setupAppList() {
        // Perbaikan: getInstalledPackages me-return List<PackageInfo>
        List<PackageInfo> installed = BlackBoxCore.get().getInstalledPackages(0, 0);
        List<String> pkgNames = new ArrayList<>();
        for (PackageInfo info : installed) {
            pkgNames.add(info.packageName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, pkgNames);
        binding.appSelectorText.setAdapter(adapter);
        binding.appSelectorText.setOnItemClickListener((parent, view, position, id) -> selectedApp = (String) parent.getItemAtPosition(position));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                File dest = new File(requireContext().getCacheDir(), "libinject.so");
                try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
                     OutputStream out = new FileOutputStream(dest)) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                    libraryPath = dest.getAbsolutePath();
                    binding.libPath.setText(uri.getPath());
                    Toast.makeText(requireContext(), "Library Prepared", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e("DevTools", "Copy failed", e);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}