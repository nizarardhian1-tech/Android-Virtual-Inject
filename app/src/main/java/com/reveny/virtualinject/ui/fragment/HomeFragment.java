package com.reveny.virtualinject.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.reveny.virtualinject.BuildConfig;
import com.reveny.virtualinject.R;
import com.reveny.virtualinject.databinding.DialogAboutBinding;
import com.reveny.virtualinject.databinding.FragmentHomeBinding;
import com.reveny.virtualinject.model.VirtualApp;
import com.reveny.virtualinject.ui.adapter.VirtualAppAdapter;
import com.reveny.virtualinject.ui.dialog.BlurBehindDialogBuilder;
import com.reveny.virtualinject.util.chrome.LinkTransformationMethod;
import com.vcore.BlackBoxCore;

import java.util.ArrayList;
import java.util.List;

import rikka.material.app.LocaleDelegate;

public class HomeFragment extends BaseFragment {
    private FragmentHomeBinding binding;
    private VirtualAppAdapter adapter;
    private List<VirtualApp> virtualApps = new ArrayList<>();

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.about).setOnMenuItemClickListener(item -> {
            showAbout();
            return true;
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        setupToolbar(binding.toolbar, null, R.string.app_name, R.menu.menu_home);
        binding.toolbar.setNavigationIcon(null);
        binding.appBar.setLiftable(true);
        
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        setupRecyclerView();
        loadVirtualApps();

        binding.fabAdd.setOnClickListener(v -> showInstallDialog());

        return binding.getRoot();
    }

    private int titleClickCount = 0;
    private long lastClickTime = 0;

    private void setupSecretGesture() {
        binding.toolbar.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 500) {
                titleClickCount++;
            } else {
                titleClickCount = 1;
            }
            lastClickTime = currentTime;

            if (titleClickCount >= 7) {
                titleClickCount = 0;
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new DeveloperFragment())
                        .addToBackStack(null)
                        .commit();
                Toast.makeText(requireContext(), "Developer Mode Active", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        setupSecretGesture();
        adapter = new VirtualAppAdapter(virtualApps, new VirtualAppAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(VirtualApp app) {
                BlackBoxCore.get().launchApk(app.getPackageName(), 0);
            }

            @Override
            public void onItemLongClick(VirtualApp app) {
                showAppMenu(app);
            }
        });
        binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        binding.recyclerView.setAdapter(adapter);
    }

    private void loadVirtualApps() {
        virtualApps.clear();
        List<ApplicationInfo> installedApps = BlackBoxCore.get().getInstalledApplications(0, 0);
        for (ApplicationInfo info : installedApps) {
            virtualApps.add(new VirtualApp(
                info.packageName,
                info.loadLabel(requireContext().getPackageManager()).toString(),
                info.loadIcon(requireContext().getPackageManager())
            ));
        }
        adapter.notifyDataSetChanged();
    }

    private void showAppMenu(VirtualApp app) {
        String[] options = {"Launch", "Clear Data", "Stop", "Uninstall"};
        new AlertDialog.Builder(requireContext())
                .setTitle(app.getLabel())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Launch
                            BlackBoxCore.get().launchApk(app.getPackageName(), 0);
                            break;
                        case 1: // Clear Data
                            BlackBoxCore.get().clearPackage(app.getPackageName(), 0);
                            Toast.makeText(requireContext(), "Data cleared", Toast.LENGTH_SHORT).show();
                            break;
                        case 2: // Stop
                            BlackBoxCore.get().stopPackage(app.getPackageName(), 0);
                            Toast.makeText(requireContext(), "Stopped", Toast.LENGTH_SHORT).show();
                            break;
                        case 3: // Uninstall
                            BlackBoxCore.get().uninstallPackageAsUser(app.getPackageName(), 0);
                            loadVirtualApps();
                            break;
                    }
                }).show();
    }

    private void showInstallDialog() {
        // Simple implementation: show list of system installed apps to clone
        List<ApplicationInfo> apps = requireContext().getPackageManager().getInstalledApplications(0);
        List<String> labels = new ArrayList<>();
        List<String> packages = new ArrayList<>();
        
        for (ApplicationInfo info : apps) {
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                labels.add(info.loadLabel(requireContext().getPackageManager()).toString());
                packages.add(info.packageName);
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Select App to Clone")
                .setItems(labels.toArray(new String[0]), (dialog, which) -> {
                    String pkg = packages.get(which);
                    BlackBoxCore.get().installPackageAsUser(pkg, 0);
                    loadVirtualApps();
                    Toast.makeText(requireContext(), "App Cloned", Toast.LENGTH_SHORT).show();
                }).show();
    }

    public static class AboutDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            DialogAboutBinding binding = DialogAboutBinding.inflate(getLayoutInflater(), null, false);
            setupAboutDialog(binding);
            return new BlurBehindDialogBuilder(requireContext()).setView(binding.getRoot()).create();
        }

        private void setupAboutDialog(DialogAboutBinding binding) {
            binding.designAboutTitle.setText(R.string.app_name);
            binding.designAboutInfo.setMovementMethod(LinkMovementMethod.getInstance());
            binding.designAboutInfo.setTransformationMethod(new LinkTransformationMethod(requireActivity()));
            binding.designAboutInfo.setText(HtmlCompat.fromHtml(getString(
                    R.string.about_view_source_code,
                    "<b><a href=\"https://t.me/revenyy\">Telegram</a></b>",
                    "<b><a href=\"https://github.com/reveny/\">Reveny</a></b>"), HtmlCompat.FROM_HTML_MODE_LEGACY));
            binding.designAboutVersion.setText(String.format(LocaleDelegate.getDefaultLocale(), "%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        }
    }

    private void showAbout() {
        new AboutDialog().show(getChildFragmentManager(), "about");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
