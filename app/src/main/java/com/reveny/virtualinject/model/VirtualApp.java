package com.reveny.virtualinject.model;

import android.graphics.drawable.Drawable;

public class VirtualApp {
    private String packageName;
    private String label;
    private Drawable icon;

    public VirtualApp(String packageName, String label, Drawable icon) {
        this.packageName = packageName;
        this.label = label;
        this.icon = icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getLabel() {
        return label;
    }

    public Drawable getIcon() {
        return icon;
    }
}
