package com.peter.shuihu;

import com.peter.shuihu.MyMenu.ItemViewCreater;
import com.peter.shuihu.MyMenu.ItemViewOnClickListener;
import com.peter.shuihu.ShuiHu.Item;
import com.peter.volley.VolleyUtil;
import com.peter.volley.toolbox.ImageLoader;
import com.peter.volley.toolbox.ImageLoader.ImageCache;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

public class MainActivity extends Activity {

	ViewPager mPager;
	private MyMenu mMenu;
    private int[] menuTitleRes = { 
    		R.string.action_dianjiang,
    		R.string.action_help, 
    		R.string.action_about,
    		R.string.action_splash
            };
	private TextView mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initMenu();
		mPager = (ViewPager)findViewById(R.id.vp);
		mTitle = (TextView) findViewById(R.id.card_name);
		final String[] items = getResources().getStringArray(R.array.select_jiang);
		mTitle.setText(items[0]);
		ShuiHu sh = (ShuiHu) getApplication();
		ImageAdapter mAdapter = new ImageAdapter(MainActivity.this, sh.getItems());
		mPager.addOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				mTitle.setText(items[position]);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		mPager.setAdapter(mAdapter);
	}
	
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.menu:
            mMenu.show();
            break;
        }
    }
	
	private void initMenu() {
		mMenu = new MyMenu(MainActivity.this);
        View anchor = findViewById(R.id.menu);
        mMenu.setAnchor(anchor);
        for (int i = 0; i < menuTitleRes.length; i++) {
            mMenu.addMenuItem(i, menuTitleRes[i], menuTitleRes[i]);
        }
        mMenu.setMenuItemCreater(new ItemViewCreater() {

            @Override
            public View createView(int position, ViewGroup parent) {
                LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                View menu = factory.inflate(R.layout.menu_item, parent, false);
                TextView tv = (TextView) menu.findViewById(R.id.text);
                tv.setText(menuTitleRes[position]);
                return menu;
            }
        });
        mMenu.setMenuItemOnClickListener(new ItemViewOnClickListener() {

            @Override
            public void OnItemClick(int order) {

                switch (order) {
                case R.string.action_dianjiang:
                	showList();
                    break;
                case R.string.action_help:
                	showToast(R.string.action_help_msg);
                    break;
                case R.string.action_about:
					String channel = getApplicationMetaValue("UMENG_CHANNEL");
					String about = getString(R.string.action_about_msg) +  "channel:" + channel;
                	showToast(about);
                    break;
                case R.string.action_splash:
                	Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                	intent.setAction("show");
                	startActivity(intent);
                	break;
                }
                mMenu.dismiss();
            }
        });
	}

	private String getApplicationMetaValue(String name) {
		String value= "";
		try {
			ApplicationInfo appInfo =getPackageManager()
					.getApplicationInfo(getPackageName(),
							PackageManager.GET_META_DATA);
			value = appInfo.metaData.getString(name);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return value;
	}
	
    private void showToast(int StringId) {
        Toast toast = Toast.makeText(getApplicationContext(), StringId, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void showToast(String str) {
        Toast toast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

	private void showList() {
		new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
        .setTitle(R.string.action_dianjiang)
        .setItems(R.array.select_jiang, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                /* User clicked so do some stuff */
            	mPager.setCurrentItem(which);
            }
        })
        .create().show();
	}
	
	private class ImageAdapter extends PagerAdapter {

		SparseArray<Item> mItems;
		ImageLoader mImageLoader;
		
		public ImageAdapter(Context context, SparseArray<Item> items) {
			mItems = items;
			mImageLoader = new ImageLoader(VolleyUtil.getQueue(context), new LruImageCache(2));
		}
		
		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public boolean isViewFromObject(final View arg0, final Object arg1) {
			return arg0 == ((RotateController) arg1).getCard();
		}

		@Override
		public Object instantiateItem(final ViewGroup container,
				final int position) {
			
			Item item = mItems.get(position);
			RotateController controller = new RotateController(container);
			
			controller.setBackImage(mImageLoader, item.mBack);
			controller.setFrontImage(mImageLoader, item.mFront);
			
			container.addView(controller.getCard());
			return controller;
		}

		@Override
		public void destroyItem(final ViewGroup container, final int position,
				final Object object) {
			container.removeView(((RotateController) object).getCard());
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
			int maxSize = maxMemory / m;// 默认1/8大小
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
