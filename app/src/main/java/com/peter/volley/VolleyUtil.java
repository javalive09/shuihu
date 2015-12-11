package com.peter.volley;

import android.content.Context;

import com.peter.volley.RequestQueue;
import com.peter.volley.toolbox.Volley;

public class VolleyUtil {

	private volatile static RequestQueue requestQueue;

	/** 返回RequestQueue单例 **/
	public static RequestQueue getQueue(Context context) {
		if (requestQueue == null) {
			synchronized (VolleyUtil.class) {
				if (requestQueue == null) {
					requestQueue = Volley.newRequestQueue(context.getApplicationContext());
				}
			}
		}
		return requestQueue;
	}
	
}
