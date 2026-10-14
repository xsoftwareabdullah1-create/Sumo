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
        // Windows 10-inspired desktop wallpaper: sunset gradient with a glowing window motif.
        desktop.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(245, 128, 145), Color.rgb(170, 91, 179), Color.rgb(49, 84, 168), Color.rgb(15, 28, 75)}));

        LinearLayout wallpaper = new LinearLayout(this);
        wallpaper.setOrientation(LinearLayout.VERTICAL);
        wallpaper.setPadding(dp(12), dp(8), dp(12), dp(42));
        desktop.addView(wallpaper, new FrameLayout.LayoutParams(-1, -1));

        // Desktop shortcut rail.
        LinearLayout shortcuts = new LinearLayout(this);
        shortcuts.setOrientation(LinearLayout.VERTICAL);
        shortcuts.setGravity(android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL);
        wallpaper.addView(shortcuts, new LinearLayout.LayoutParams(dp(74), -1));
        addDesktopShortcut(shortcuts, "▣", "This PC", v -> showLibraryWindow());
        addDesktopShortcut(shortcuts, "◎", "Browser", v -> showBrowserWindow());
        addDesktopShortcut(shortcuts, "▤", "SUMO EXE", v -> pickExe());
        addDesktopShortcut(shortcuts, "⚙", "Settings", v -> showStatus("SUMO settings • Android desktop mode"));

        // Main window styled like a desktop browser, similar to the reference recording.
        LinearLayout window = new LinearLayout(this);
        window.setOrientation(LinearLayout.VERTICAL);
        window.setBackground(round(Color.rgb(248, 249, 252), dp(8), Color.rgb(215, 219, 229)));
        FrameLayout.LayoutParams wp = new FrameLayout.LayoutParams(
                (int)(getResources().getDisplayMetrics().widthPixels * 0.72f),
                (int)(getResources().getDisplayMetrics().heightPixels * 0.76f),
                android.view.Gravity.CENTER);
        desktop.addView(window, wp);

        LinearLayout titlebar = new LinearLayout(this);
        titlebar.setOrientation(LinearLayout.HORIZONTAL);
        titlebar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        titlebar.setPadding(dp(12), 0, dp(8), 0);
        titlebar.setBackgroundColor(Color.rgb(238, 240, 246));
        window.addView(titlebar, new LinearLayout.LayoutParams(-1, dp(36)));
        titlebar.addView(label("🌐  Browser  —  SUMO Desktop", 12, Color.rgb(45, 48, 58), false),
                new LinearLayout.LayoutParams(0, -2, 1));
        TextView minimize = label("—    □    ×", 15, Color.rgb(60, 60, 70), false);
        titlebar.addView(minimize);

        LinearLayout addressbar = new LinearLayout(this);
        addressbar.setOrientation(LinearLayout.HORIZONTAL);
        addressbar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        addressbar.setPadding(dp(10), dp(5), dp(10), dp(5));
        window.addView(addressbar, new LinearLayout.LayoutParams(-1, dp(43)));
        addressbar.addView(label("←   →   ⟳", 15, Color.DKGRAY, false));
        TextView address = label("  🔒  https://www.google.com", 12, Color.rgb(75, 78, 88), false);
        address.setPadding(dp(12), dp(8), dp(12), dp(8));
        address.setBackground(round(Color.WHITE, dp(18), Color.rgb(220, 223, 230)));
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(0, -1, 1);
        ap.leftMargin = dp(10);
        addressbar.addView(address, ap);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(18), dp(10), dp(18), dp(8));
        page.setBackgroundColor(Color.WHITE);
        window.addView(page, new LinearLayout.LayoutParams(-1, 0, 1));
        TextView google = label("Google", 30, Color.rgb(66, 133, 244), true);
        google.setGravity(android.view.Gravity.CENTER);
        page.addView(google);
        TextView search = label("⌕   Search the web or enter a URL", 13, Color.rgb(90, 95, 105), false);
        search.setGravity(android.view.Gravity.CENTER_VERTICAL);
        search.setPadding(dp(18), 0, dp(18), 0);
        search.setBackground(round(Color.WHITE, dp(24), Color.rgb(220, 223, 230)));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels * 0.43f), dp(42));
        sp.topMargin = dp(12);
        page.addView(search, sp);
        search.setOnClickListener(v -> showBrowserWindow());
        TextView helper = label("SUMO desktop preview • Tap desktop icons to open tools", 11, Color.rgb(100, 105, 115), false);
        helper.setPadding(0, dp(10), 0, dp(4));
        page.addView(helper);

        LinearLayout quick = new LinearLayout(this);
        quick.setOrientation(LinearLayout.HORIZONTAL);
        quick.setGravity(android.view.Gravity.CENTER);
        page.addView(quick, new LinearLayout.LayoutParams(-1, dp(46)));
        Button importButton = new Button(this);
        importButton.setText("＋ Import EXE");
        importButton.setAllCaps(false);
        importButton.setTextSize(11);
        importButton.setOnClickListener(v -> pickExe());
        quick.addView(importButton);
        Button libraryButton = new Button(this);
        libraryButton.setText("▣ EXE Library");
        libraryButton.setAllCaps(false);
        libraryButton.setTextSize(11);
        libraryButton.setOnClickListener(v -> showLibraryWindow());
        quick.addView(libraryButton);
        Button engineButton = new Button(this);
        engineButton.setText("⚙ Engine");
        engineButton.setAllCaps(false);
        engineButton.setTextSize(11);
        engineButton.setOnClickListener(v -> showStatus("Runtime status: EXE files can be imported and stored. A Windows compatibility engine is not integrated yet."));
        quick.addView(engineButton);

        // Bottom taskbar.
        LinearLayout taskbar = new LinearLayout(this);
        taskbar.setOrientation(LinearLayout.HORIZONTAL);
        taskbar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        taskbar.setPadding(dp(10), 0, dp(12), 0);
        taskbar.setBackgroundColor(Color.rgb(25, 31, 49));
        FrameLayout.LayoutParams tp = new FrameLayout.LayoutParams(-1, dp(38), android.view.Gravity.BOTTOM);
        desktop.addView(taskbar, tp);
        TextView start = label("⊞", 24, Color.WHITE, true);
        start.setGravity(android.view.Gravity.CENTER);
        taskbar.addView(start, new LinearLayout.LayoutParams(dp(42), -1));
        start.setOnClickListener(v -> showStartMenu());
        TextView task = label("  ⌕  Search    |    🌐 Browser    |    ▣ This PC", 12, Color.WHITE, false);
        taskbar.addView(task, new LinearLayout.LayoutParams(0, -1, 1));
        TextView clock = label("SUMO  •  Android", 10, Color.WHITE, false);
        taskbar.addView(clock);

        pointer = new View(this);
        pointer.setBackground(round(Color.WHITE, dp(1), Color.BLACK));
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(dp(8), dp(13));
        pp.leftMargin = dp(30);
        pp.topMargin = dp(40);
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

    private void addDesktopShortcut(LinearLayout parent, String glyph, String title, View.OnClickListener click) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(android.view.Gravity.CENTER);
        item.setPadding(dp(2), dp(5), dp(2), dp(5));
        TextView icon = label(glyph, 26, Color.WHITE, true);
        icon.setGravity(android.view.Gravity.CENTER);
        item.addView(icon, new LinearLayout.LayoutParams(-1, dp(32)));
        TextView text = label(title, 10, Color.WHITE, true);
        text.setGravity(android.view.Gravity.CENTER);
        item.addView(text);
        item.setOnClickListener(click);
        parent.addView(item, new LinearLayout.LayoutParams(-1, dp(63)));
    }

    private void showLibraryWindow() {
        if (libraryDir == null) libraryDir = new File(getFilesDir(), "incoming-exe");
        File[] files = libraryDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".exe"));
        StringBuilder message = new StringBuilder("Files saved privately inside SUMO:\n\n");
        if (files == null || files.length == 0) message.append("This folder is empty. Import a trusted .exe file first.");
        else {
            for (File file : files) message.append("▣  ").append(file.getName()).append("  •  ").append(formatSize(file.length())).append('\n');
            message.append("\nNote: files are stored, not executed. Windows runtime is not integrated yet.");
        }
        new android.app.AlertDialog.Builder(this)
                .setTitle("This PC  ›  SUMO EXE Library")
                .setMessage(message.toString())
                .setPositiveButton("Import EXE", (d, w) -> pickExe())
                .setNeutralButton("Refresh", (d, w) -> refreshLibrary())
                .setNegativeButton("Close", null).show();
    }

    private void showBrowserWindow() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Browser")
                .setMessage("Browser window preview\n\nIn this desktop build, the browser area is a UI simulation. Real web browsing and running Windows applications are not yet connected.")
                .setPositiveButton("OK", null).show();
    }

    private void showStartMenu() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Start")
                .setItems(new String[]{"This PC / EXE Library", "Import EXE", "Browser", "Engine status"}, (d, which) -> {
                    if (which == 0) showLibraryWindow();
                    else if (which == 1) pickExe();
                    else if (which == 2) showBrowserWindow();
                    else showStatus("Runtime status: no Windows execution engine is integrated yet.");
                }).show();
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