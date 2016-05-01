package com.peter.shuihu;

import com.peter.shuihu.ShuiHu.Item;
import com.peter.volley.VolleyUtil;
import com.peter.volley.toolbox.ImageLoader;
import com.peter.volley.toolbox.ImageLoader.ImageCache;
import com.umeng.analytics.MobclickAgent;

import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    ViewPager mPager;
    int MAX_PROGRESS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mPager = (ViewPager) findViewById(R.id.vp);
        ShuiHu sh = (ShuiHu) getApplication();
        ImageAdapter mAdapter = new ImageAdapter(MainActivity.this, sh.getItems());
        mPager.setAdapter(mAdapter);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_choice:
                showList();
                break;
            case R.id.action_help:
                showAlertDialog(getString(R.string.action_help),
                        getString(R.string.action_help_msg));
                break;
            case R.id.action_about:
                showAlertDialog(getString(R.string.action_about),
                        getString(R.string.action_about_msg));
                break;

            case R.id.action_feedback:
                sendMailByIntent();
                break;
            case R.id.action_splash:
                Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                intent.setAction("show");
                startActivity(intent);
                break;
            case R.id.action_update:
                checkVersion("http://7xoxmg.com1.z0.glb.clouddn.com/shuihu_update_info");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkVersion(final String url) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Toast.makeText(MainActivity.this, R.string.update_toast_start, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected String doInBackground(Void... params) {
                return doGetVersionInfo(url);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                String[] results = result.split(";");
                int version = Integer.valueOf(results[0].trim());
                int currentVersion = getVersionCode();
                if (currentVersion < version) {
                    String url = results[1].trim();
                    if (!TextUtils.isEmpty(url)) {
                        showUpdataDialog(url);
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.update_toast_nonew, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void showUpdataDialog(final String url) {
        new AlertDialog.Builder(this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.update_dialog_title_one)
                .setPositiveButton(R.string.update_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        doDownloadApk(url);
                    }
                })
                .setNegativeButton(R.string.update_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .create().show();
    }

    private int getVersionCode() {//获取版本号(内部识别号)
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String doGetVersionInfo(String urlStr) {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5 * 1000);
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                int len = -1;
                byte[] buf = new byte[128];

                while ((len = is.read(buf)) != -1) {
                    baos.write(buf, 0, len);
                }
                baos.flush();
                return baos.toString();
            } else {
                throw new RuntimeException(" responseCode is not 200 ... ");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            }
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e) {
            }
            conn.disconnect();
        }
        return null;
    }

    private void doDownloadApk(final String apkUrl) {

        final ProgressDialog mProgressDialog = getProgressDialog();
        mProgressDialog.show();

        new AsyncTask<Void, Integer, String>() {

            int count;
            boolean finished;
            int current;
            int progress;

            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(apkUrl);
                    URLConnection conn = url.openConnection();
                    count = conn.getContentLength();
                    InputStream is = conn.getInputStream();
                    OutputStream os = new FileOutputStream(new File(getExternalFilesDir(null), "shuihu.apk"));
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    while (!finished) {
                        while ((len = is.read(buffer)) > 0) {
                            current += len;
                            os.write(buffer, 0, len);
                            progress = current * 100 / count;
                            publishProgress(progress);
                        }
                        finished = true;
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return progress + "";
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                mProgressDialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (progress >= MAX_PROGRESS) {
                    mProgressDialog.dismiss();
                    installApk(new File(getExternalFilesDir(null), "shuihu.apk").getAbsolutePath());
                }

            }
        }.execute();

    }

    private void installApk(String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + path),"application/vnd.android.package-archive");
        startActivity(intent);
    }

    private ProgressDialog getProgressDialog() {
        ProgressDialog mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setIconAttribute(android.R.attr.alertDialogIcon);
        mProgressDialog.setTitle(R.string.update_dialog_title);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(MAX_PROGRESS);
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getText(R.string.update_dialog_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked No so do some stuff */
                    }
                });
        return mProgressDialog;
    }

    public void sendMailByIntent() {
        Intent data = new Intent(Intent.ACTION_SENDTO);
        data.setData(Uri.parse(getString(R.string.setting_feedback_address)));
        data.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.setting_feedback));
        data.putExtra(Intent.EXTRA_TEXT, getString(R.string.setting_feedback_body));
        startActivity(data);
    }

    public AlertDialog showAlertDialog(String title, String content) {
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle(title);
        dialog.setMessage(content);
        dialog.show();
        return dialog;
    }

    private void showList() {
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.action_dianjiang)
                .setItems(R.array.select_jiang, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                /* User clicked so do some stuff */
                        mPager.setCurrentItem(which);
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

    }

    private class ImageAdapter extends PagerAdapter {

        SparseArray<Item> mItems;
        ImageLoader mImageLoader;

        public ImageAdapter(Context context, SparseArray<Item> items) {
            mItems = items;
            mImageLoader = new ImageLoader(VolleyUtil.getQueue(context), new LruImageCache(8));
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public boolean isViewFromObject(final View arg0, final Object arg1) {
            return arg0 == ((RotateController) arg1).getCardRoot();
        }

        @Override
        public Object instantiateItem(final ViewGroup container,
                                      final int position) {

            Item item = mItems.get(position);
            RotateController controller = new RotateController(container);

            controller.setBackImage(mImageLoader, item.mBack);
            controller.setFrontImage(mImageLoader, item.mFront);

            container.addView(controller.getCardRoot());
            return controller;
        }

        @Override
        public void destroyItem(final ViewGroup container, final int position,
                                final Object object) {
            container.removeView(((RotateController) object).getCardRoot());
        }

        @Override
        public int getItemPosition(final Object object) {
            return POSITION_NONE;
        }
    }

    class LruImageCache implements ImageCache {

        private LruCache<String, Bitmap> lruCache;

        public LruImageCache(int m) {
            Log.i("peter", "runtime memery = " + Runtime.getRuntime().maxMemory());
            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);//kilobyte
            int maxSize = maxMemory / m;
            Log.i("peter", "runtime maxSize maxMemory = " + maxMemory);
            Log.i("peter", "runtime maxSize = " + maxSize);
            lruCache = new LruCache<String, Bitmap>(maxSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };
        }

        @Override
        public Bitmap getBitmap(String url) {
            return lruCache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            lruCache.put(url, bitmap);
        }

    }


}
