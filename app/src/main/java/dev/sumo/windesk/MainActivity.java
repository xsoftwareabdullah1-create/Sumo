package dev.sumo.windesk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int bg = Color.rgb(5, 10, 23);
    private final int panel = Color.rgb(14, 27, 47);
    private final int cyan = Color.rgb(70, 225, 255);
    private FrameLayout desktop;
    private View pointer;
    private TextView status;
    private LinearLayout library;
    private File libraryDir;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        libraryDir = new File(getFilesDir(), "incoming-exe");
        buildDesktop();
        handleIncomingIntent(getIntent());
        refreshLibrary();
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);
        refreshLibrary();
    }

    private void buildDesktop() {
        desktop = new FrameLayout(this);
        desktop.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(4, 14, 38), Color.rgb(9, 39, 68), Color.rgb(3, 8, 19)}));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(15), dp(24), dp(12));
        desktop.addView(root, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(android.view.Gravity.CENTER_VERTICAL);
        root.addView(top, new LinearLayout.LayoutParams(-1, -2));
        TextView logo = label("S U M O", 21, cyan, true);
        top.addView(logo, new LinearLayout.LayoutParams(0, -2, 1));
        top.addView(label("WINDESK  •  ANDROID DESKTOP", 11, Color.rgb(151, 190, 220), true));

        TextView title = label("Your Windows workspace", 28, Color.WHITE, true);
        title.setPadding(0, dp(12), 0, dp(3));
        root.addView(title);
        root.addView(label("Import .EXE files into your private SUMO library", 13, Color.rgb(171, 201, 224), false));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(16), 0, dp(12));
        root.addView(actions, new LinearLayout.LayoutParams(-1, -2));
        actions.addView(actionCard("01  /  IMPORT", "Choose a Windows installer or program from Downloads.", "IMPORT EXE", v -> pickExe()),
                new LinearLayout.LayoutParams(0, dp(120), 1));
        View gap = new View(this);
        actions.addView(gap, new LinearLayout.LayoutParams(dp(12), 1));
        actions.addView(actionCard("02  /  LIBRARY", "Your imported files stay in SUMO app storage.", "REFRESH LIST", v -> refreshLibrary()),
                new LinearLayout.LayoutParams(0, dp(120), 1));
        View gap2 = new View(this);
        actions.addView(gap2, new LinearLayout.LayoutParams(dp(12), 1));
        actions.addView(actionCard("03  /  ENGINE", "Windows compatibility runtime is not integrated yet.", "ENGINE STATUS", v ->
                showStatus("Runner engine pending: SUMO can import and keep EXE files, but cannot install or execute Windows programs yet.")),
                new LinearLayout.LayoutParams(0, dp(120), 1));

        LinearLayout libraryPanel = new LinearLayout(this);
        libraryPanel.setOrientation(LinearLayout.VERTICAL);
        libraryPanel.setPadding(dp(15), dp(10), dp(15), dp(8));
        libraryPanel.setBackground(round(panel, dp(13), Color.rgb(34, 69, 99)));
        root.addView(libraryPanel, new LinearLayout.LayoutParams(-1, 0, 1));
        TextView libTitle = label("EXE LIBRARY", 12, cyan, true);
        libraryPanel.addView(libTitle);
        ScrollView scroll = new ScrollView(this);
        libraryPanel.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        library = new LinearLayout(this);
        library.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(library);

        status = label("Ready • Import a trusted .exe file to get started.", 12, Color.WHITE, false);
        status.setPadding(dp(12), dp(10), dp(12), dp(10));
        status.setBackground(round(Color.rgb(9, 20, 35), dp(8), Color.rgb(33, 68, 96)));
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.topMargin = dp(10);
        root.addView(status, statusParams);
        root.addView(label("SUMO is an Android app, not Windows itself. EXE execution requires a compatible runtime.", 10,
                Color.rgb(140, 174, 202), false));

        pointer = new View(this);
        pointer.setBackground(round(cyan, dp(4), Color.WHITE));
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(dp(9), dp(9));
        pp.leftMargin = dp(18);
        pp.topMargin = dp(70);
        desktop.addView(pointer, pp);
        desktop.setOnTouchListener((v, e) -> {
            if (pointer != null && (e.getAction() == MotionEvent.ACTION_MOVE || e.getAction() == MotionEvent.ACTION_DOWN)) {
                FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) pointer.getLayoutParams();
                p.leftMargin = Math.max(0, (int)e.getX());
                p.topMargin = Math.max(0, (int)e.getY());
                pointer.setLayoutParams(p);
            }
            return false;
        });
        setContentView(desktop);
    }

    private View actionCard(String head, String body, String buttonText, View.OnClickListener listener) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(10), dp(12), dp(9));
        card.setBackground(round(panel, dp(12), Color.rgb(39, 77, 107)));
        card.addView(label(head, 11, cyan, true));
        TextView description = label(body, 12, Color.WHITE, false);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(-1, 0, 1);
        descParams.topMargin = dp(5);
        card.addView(description, descParams);
        Button button = new Button(this);
        button.setText(buttonText);
        button.setTextSize(11);
        button.setAllCaps(false);
        button.setTextColor(Color.BLACK);
        button.setBackground(round(cyan, dp(8), cyan));
        button.setOnClickListener(listener);
        card.addView(button, new LinearLayout.LayoutParams(-1, dp(38)));
        return card;
    }

    private void pickExe() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        try { startActivityForResult(intent, 42); }
        catch (Exception e) { showStatus("No Android file picker is available on this device."); }
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != 42 || result != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        String name = uri.getLastPathSegment();
        if (name == null) name = "imported.exe";
        int slash = name.lastIndexOf('/');
        if (slash >= 0) name = name.substring(slash + 1);
        if (!name.toLowerCase(Locale.ROOT).endsWith(".exe")) {
            showStatus("Not imported: choose a file whose name ends in .exe.");
            return;
        }
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) throw new IOException("Android could not open this file");
            if (!libraryDir.exists() && !libraryDir.mkdirs()) throw new IOException("Could not create SUMO library");
            String safe = name.replaceAll("[^A-Za-z0-9._-]", "_");
            File target = uniqueFile(safe);
            try (FileOutputStream out = new FileOutputStream(target)) {
                byte[] buffer = new byte[65536];
                int count;
                while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
                out.getFD().sync();
            }
            showStatus("Imported " + target.getName() + " • " + formatSize(target.length()) +
                    ". Stored in SUMO. Execution is not available until a Windows runtime is integrated.");
            refreshLibrary();
        } catch (Exception e) {
            showStatus("Import failed: " + (e.getMessage() == null ? "file could not be read" : e.getMessage()));
        }
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent == null) return;
        Uri uri = intent.getData();
        if (uri == null && Intent.ACTION_SEND.equals(intent.getAction())) {
            Object extra = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (extra instanceof Uri) uri = (Uri) extra;
        }
        if (uri == null || !(Intent.ACTION_VIEW.equals(intent.getAction()) || Intent.ACTION_SEND.equals(intent.getAction()))) return;
        String name = uri.getLastPathSegment();
        if (name == null) name = "shared.exe";
        int slash = name.lastIndexOf('/');
        if (slash >= 0) name = name.substring(slash + 1);
        if (!name.toLowerCase(Locale.ROOT).endsWith(".exe")) {
            showStatus("Received file ignored: its name does not end in .exe.");
            return;
        }
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) throw new IOException("Cannot read the shared file");
            if (!libraryDir.exists() && !libraryDir.mkdirs()) throw new IOException("Cannot create SUMO library");
            File target = uniqueFile(name.replaceAll("[^A-Za-z0-9._-]", "_"));
            try (FileOutputStream out = new FileOutputStream(target)) {
                byte[] buffer = new byte[65536];
                int count;
                while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
                out.getFD().sync();
            }
            showStatus("Received and saved " + target.getName() + ". It has not been executed.");
            refreshLibrary();
        } catch (Exception e) {
            showStatus("Could not import shared EXE: " + (e.getMessage() == null ? "read error" : e.getMessage()));
        }
    }

    private File uniqueFile(String name) {
        File target = new File(libraryDir, name);
        if (!target.exists()) return target;
        String stem = name.substring(0, name.length() - 4);
        for (int i = 2; i < 10000; i++) {
            target = new File(libraryDir, stem + " (" + i + ").exe");
            if (!target.exists()) return target;
        }
        return new File(libraryDir, System.currentTimeMillis() + ".exe");
    }

    private void refreshLibrary() {
        if (library == null) return;
        library.removeAllViews();
        File[] files = libraryDir != null ? libraryDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".exe")) : null;
        if (files == null || files.length == 0) {
            TextView empty = label("No imported EXE files yet. Tap IMPORT EXE above.", 13, Color.rgb(177, 201, 221), false);
            empty.setPadding(0, dp(12), 0, dp(12));
            library.addView(empty);
            return;
        }
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        for (File file : files) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(dp(8), dp(7), dp(8), dp(7));
            TextView details = label(file.getName() + "\n" + formatSize(file.length()) + " • saved in private app storage", 12, Color.WHITE, false);
            row.addView(details, new LinearLayout.LayoutParams(0, -2, 1));
            Button remove = new Button(this);
            remove.setText("REMOVE");
            remove.setTextSize(10);
            remove.setAllCaps(false);
            remove.setOnClickListener(v -> new android.app.AlertDialog.Builder(this)
                    .setTitle("Remove EXE?")
                    .setMessage("Delete " + file.getName() + " from SUMO's private library?")
                    .setNegativeButton("CANCEL", null)
                    .setPositiveButton("DELETE", (dialog, which) -> {
                        if (file.delete()) showStatus("Removed " + file.getName());
                        else showStatus("Could not remove " + file.getName());
                        refreshLibrary();
                    }).show());
            row.addView(remove, new LinearLayout.LayoutParams(dp(92), dp(42)));
            library.addView(row);
            View divider = new View(this);
            divider.setBackgroundColor(Color.rgb(35, 59, 81));
            library.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));
        }
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024L * 1024) return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024));
        return String.format(Locale.ROOT, "%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private void showStatus(String message) {
        if (status != null) status.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private TextView label(String text, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(color);
        view.setTextSize(size);
        if (bold) view.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        return view;
    }

    private GradientDrawable round(int color, int radius, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private int dp(int value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }
}