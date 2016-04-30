package com.peter.shuihu;

import com.peter.shuihu.ShuiHu.Item;
import com.peter.volley.VolleyUtil;
import com.peter.volley.toolbox.ImageLoader;
import com.peter.volley.toolbox.ImageLoader.ImageCache;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity {

	ViewPager mPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mPager = (ViewPager)findViewById(R.id.vp);
		ShuiHu sh = (ShuiHu) getApplication();
		ImageAdapter mAdapter = new ImageAdapter(MainActivity.this, sh.getItems());
		mPager.setAdapter(mAdapter);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
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
		}
		return super.onOptionsItemSelected(item);
	}

	public void sendMailByIntent() {
		Intent data=new Intent(Intent.ACTION_SENDTO);
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
			mImageLoader = new ImageLoader(VolleyUtil.getQueue(context), new LruImageCache(2));
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
