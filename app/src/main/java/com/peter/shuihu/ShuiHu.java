package com.peter.shuihu;

import android.app.Application;
import android.util.SparseArray;

public class ShuiHu extends Application{

	private static final String URL_HEAD = "http://7xoxmg.com1.z0.glb.clouddn.com/";
	private static final String URL_END = ".png";
	SparseArray<Item> mItems = new SparseArray<Item>(108);
	
	private boolean mHashInit = false;
	public void init() {
		if(mHashInit) {
			return;
		}
		
		for (int i = 0; i < 108; i++) {
			String frontName = "";
			String backName = "";
			int index = i + 1;
			if (index < 10) {
				frontName = "s00" + index + "_1";
				backName = "s00" + index + "_2";
			} else if (index < 100) {
				frontName = "s0" + index + "_1";
				backName = "s0" + index + "_2";
			} else if (index < 110) {
				frontName = "s" + index + "_1";
				backName = "s" + index + "_2";
			}
			
			Item item = new Item();
			item.mFront = URL_HEAD + frontName + URL_END;
			item.mBack = URL_HEAD + backName + URL_END;
			item.isFront = true;
			mItems.put(i, item);
		}
		mHashInit = true;
	}
	
	public boolean isInit() {
		return mHashInit;
	}
	
	public SparseArray<Item> getItems() {
		if(!mHashInit) {
			init();
		}
		return mItems;
	}
	
	public static class Item {
		String mFront;
		String mBack;
		boolean isFront;
	}
}
