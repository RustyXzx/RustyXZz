package com.rustyx.mobile.optimizer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

public final class MainActivity extends Activity {
    private TextView statusText;
    private TextView memoryText;
    private TextView storageText;
    private TextView batteryText;
    private TextView networkText;
    private TextView cacheText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(5, 5, 5));
        getWindow().setNavigationBarColor(Color.rgb(5, 5, 5));
        buildInterface();
        refreshStatus();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        return view;
    }

    private LinearLayout card(String title, TextView value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.setBackgroundColor(Color.rgb(18, 18, 20));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(params);

        card.addView(text(title, 12, Color.rgb(150, 150, 158), true));
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(-1, -2);
        valueParams.topMargin = dp(6);
        value.setLayoutParams(valueParams);
        card.addView(value);
        return card;
    }

    private Button action(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(Color.WHITE);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setBackgroundColor(Color.rgb(28, 28, 31));
        button.setOnClickListener(listener);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(54));
        params.setMargins(0, 0, 0, dp(10));
        button.setLayoutParams(params);
        return button;
    }

    private void buildInterface() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(5, 5, 5));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(22), dp(18), dp(30));
        scroll.addView(root);

        TextView brand = text("RUSTYX", 30, Color.WHITE, true);
        brand.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(brand);

        TextView subtitle = text("Mobile Optimizer 0.3.1", 13, Color.rgb(150, 150, 158), false);
        subtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.setMargins(0, dp(2), 0, dp(22));
        subtitle.setLayoutParams(subtitleParams);
        root.addView(subtitle);

        statusText = text("Analisando aparelho...", 14, Color.WHITE, true);
        memoryText = text("--", 20, Color.WHITE, true);
        storageText = text("--", 20, Color.WHITE, true);
        batteryText = text("--", 20, Color.WHITE, true);
        networkText = text("--", 20, Color.WHITE, true);
        cacheText = text("--", 20, Color.WHITE, true);

        root.addView(card("STATUS", statusText));
        root.addView(card("MEMÓRIA DISPONÍVEL", memoryText));
        root.addView(card("ARMAZENAMENTO LIVRE", storageText));
        root.addView(card("BATERIA", batteryText));
        root.addView(card("REDE", networkText));
        root.addView(card("CACHE DO RUSTYX", cacheText));

        TextView title = text("Ferramentas de otimização", 17, Color.WHITE, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, -2);
        titleParams.setMargins(0, dp(16), 0, dp(12));
        title.setLayoutParams(titleParams);
        root.addView(title);

        root.addView(action("Otimização rápida do RustyX", v -> optimizeRustyX()));
        root.addView(action("Limpeza de armazenamento do Android", v -> openSetting(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)));
        root.addView(action("Economia de bateria", v -> openSetting(Settings.ACTION_BATTERY_SAVER_SETTINGS)));
        root.addView(action("Informações do RustyX", v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }));
        root.addView(action("Atualizar diagnóstico", v -> refreshStatus()));

        TextView warning = text(
            "O Android não permite que aplicativos comuns encerrem outros apps ou apaguem o cache deles. O RustyX usa somente ações oficiais, sem root e sem promessas falsas.",
            12,
            Color.rgb(145, 145, 152),
            false);
        warning.setLineSpacing(0, 1.25f);
        LinearLayout.LayoutParams warningParams = new LinearLayout.LayoutParams(-1, -2);
        warningParams.setMargins(0, dp(12), 0, 0);
        warning.setLayoutParams(warningParams);
        root.addView(warning);

        setContentView(scroll);
    }

    private void refreshStatus() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        if (manager != null) {
            manager.getMemoryInfo(info);
        }

        StatFs storage = new StatFs(Environment.getDataDirectory().getPath());
        BatteryManager battery = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batteryPercent = battery == null
            ? -1
            : battery.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        statusText.setText(info.lowMemory
            ? "Atenção: pouca memória disponível"
            : "Aparelho em condição normal");
        statusText.setTextColor(info.lowMemory
            ? Color.rgb(255, 95, 100)
            : Color.rgb(70, 215, 125));

        memoryText.setText(formatBytes(info.availMem) + " de " + formatBytes(info.totalMem));
        storageText.setText(formatBytes(storage.getAvailableBytes()) + " de " + formatBytes(storage.getTotalBytes()));
        batteryText.setText(batteryPercent < 0 ? "Não disponível" : batteryPercent + "%");
        networkText.setText(isOnline() ? "Conectado à internet" : "Sem conexão");
        cacheText.setText(formatBytes(directorySize(getCacheDir())));
    }

    private boolean isOnline() {
        ConnectivityManager manager = getSystemService(ConnectivityManager.class);
        if (manager == null) {
            return false;
        }

        Network network = manager.getActiveNetwork();
        if (network == null) {
            return false;
        }

        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
        return capabilities != null
            && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private void optimizeRustyX() {
        long before = directorySize(getCacheDir());
        boolean success = deleteChildren(getCacheDir());
        onTrimMemory(TRIM_MEMORY_COMPLETE);
        System.gc();
        refreshStatus();

        String result = before > 0
            ? formatBytes(before) + " liberados"
            : "O cache do RustyX já estava limpo";
        Toast.makeText(
            this,
            success ? result : "A limpeza não foi concluída",
            Toast.LENGTH_LONG).show();
    }

    private void openSetting(String action) {
        try {
            startActivity(new Intent(action));
        } catch (Exception error) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    private long directorySize(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        if (file.isFile()) {
            return file.length();
        }

        long total = 0;
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                total += directorySize(child);
            }
        }
        return total;
    }

    private boolean deleteChildren(File directory) {
        if (directory == null || !directory.exists()) {
            return true;
        }

        boolean success = true;
        File[] children = directory.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    success &= deleteChildren(child);
                }
                success &= child.delete();
            }
        }
        return success;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        double value = bytes;
        String[] units = {"KB", "MB", "GB", "TB"};
        int unit = -1;
        do {
            value /= 1024.0;
            unit++;
        } while (value >= 1024 && unit < units.length - 1);

        return String.format(Locale.getDefault(), "%.1f %s", value, units[unit]);
    }
}
