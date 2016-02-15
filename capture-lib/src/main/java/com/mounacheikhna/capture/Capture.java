package com.mounacheikhna.capture;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.util.Log;
import android.view.View;

import org.hamcrest.Matcher;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import static android.graphics.Bitmap.CompressFormat.PNG;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static com.mounacheikhna.capture.Chmod.chmodPlusR;

/**
 * Created by m.cheikhna on 14/02/16.
 */
public class Capture {

    private static final Pattern TAG_PATTERN = Pattern.compile("[a-zA-Z0-9_-]+");
    private static final String SCREENSHOTS_DIR_NAME = "screenshots";
    static final String NAME_SEPARATOR = "_";

    private static final String TAG = "Capture";
    private static final String EXTENSION = ".png";

    public static void screenshot(final String screenshotName) {
    }

    private static void takeScreenshot(File file, final Activity activity) throws IOException {
        View view = activity.getWindow().getDecorView();
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), ARGB_8888);

        if (Looper.myLooper() == Looper.getMainLooper()) {
            // On main thread already, Just Do It™.
            drawDecorViewToBitmap(activity, bitmap);
        } else {
            // On a background thread, post to main.
            final CountDownLatch latch = new CountDownLatch(1);
            activity.runOnUiThread(new Runnable() {
                @Override public void run() {
                    try {
                        drawDecorViewToBitmap(activity, bitmap);
                    } finally {
                        latch.countDown();
                    }
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                String msg = "Unable to get screenshot " + file.getAbsolutePath();
                Log.e(TAG, msg, e);
                throw new RuntimeException(msg, e);
            }
        }

        OutputStream fos = null;
        try {
            fos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(PNG, 100 /* quality */, fos);
            chmodPlusR(file);
        } finally {
            bitmap.recycle();
            if (fos != null) {
                fos.close();
            }
        }
    }

    private static void drawDecorViewToBitmap(Activity activity, Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        activity.getWindow().getDecorView().draw(canvas);
    }

    private static File getFilesDirectory(Context context) throws IOException {
        File directory = null;

        if (Build.VERSION.SDK_INT >= 21) {
            File externalDir = new File(Environment.getExternalStorageDirectory(), getDirectoryName(context));
            directory = initializeDirectory(externalDir);
        }

        if (directory == null) {
            File internalDir = new File(context.getDir(SCREENSHOTS_DIR_NAME, Context.MODE_WORLD_READABLE), getDirectoryName(context));
            directory = initializeDirectory(internalDir);
        }

        if (directory == null) {
            throw new IOException("Unable to get a screenshot storage directory");
        }

        Log.d(TAG, "Using screenshot storage directory: " + directory.getAbsolutePath());
        return directory;
    }

    private static String localeToDirName(Locale locale) {
        return locale.getLanguage() + "-" + locale.getCountry() + "/images/screenshots";
    }

    private static File initializeDirectory(File dir) {
        try {
            createPathTo(dir);

            if (dir.isDirectory() && dir.canWrite()) {
                return dir;
            }
        } catch (IOException ignored) {}

        return null;
    }

    private static void createPathTo(File dir) throws IOException {
        File parent = dir.getParentFile();
        if (!parent.exists()) {
            createPathTo(parent);
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Unable to create output dir: " + dir.getAbsolutePath());
        }
        Chmod.chmodPlusRWX(dir);
    }

    private static String getDirectoryName(Context context) {
        return context.getPackageName() + "/" + SCREENSHOTS_DIR_NAME;
    }

}
