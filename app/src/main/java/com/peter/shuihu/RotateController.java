package com.peter.shuihu;

import com.peter.volley.toolbox.ImageLoader;
import com.peter.volley.toolbox.NetworkImageView;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

public class RotateController implements OnClickListener{

	NetworkImageView front;
	NetworkImageView back;
	View card;
	
	public RotateController(ViewGroup root) {
		Context context = root.getContext();
		LayoutInflater factory = LayoutInflater.from(context);
		card = factory.inflate(R.layout.cardview, root, false);		
		front = (NetworkImageView) card.findViewById(R.id.front);
		back = (NetworkImageView) card.findViewById(R.id.back);
		front.setOnClickListener(this);
		back.setOnClickListener(this);
	}
	
	public View getCard() {
		return card;
	}
	
	public void setBackImage(ImageLoader mImageLoader, String url) {
		back.setDefaultImageResId(R.drawable.def);
		back.setImageUrl(url, mImageLoader, true);
	}
	
	public void setFrontImage(ImageLoader mImageLoader, String url) {
		front.setDefaultImageResId(R.drawable.def);
		front.setImageUrl(url, mImageLoader, true);
	}

	public void onClick(final View v) {
		
		switch(v.getId()) {
		case R.id.front:
			ObjectAnimator anim = ObjectAnimator.ofFloat(card, "rotationY", 0, 180);
			anim.setDuration(600);
			anim.start();
			int pivotX = card.getWidth() / 2;
			int pivotY = card.getHeight() / 2;
			card.setPivotX(pivotX);
			card.setPivotY(pivotY);
			anim.addUpdateListener(new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					Float rotate = (Float) animation.getAnimatedValue();
					Log.i("peter", "rotate = " + rotate);
					if(rotate >=90) {
						if(front.getVisibility() == View.VISIBLE) {
							front.setVisibility(View.GONE);
							back.setRotationY(180);
						}
					}
				}
			});
			
			break;
		case R.id.back:
			anim = ObjectAnimator.ofFloat(card, "rotationY", 180, 0);
			anim.setDuration(600);
			anim.start();
			pivotX = card.getWidth() / 2;
			pivotY = card.getHeight() / 2;
			card.setPivotX(pivotX);
			card.setPivotY(pivotY);
			anim.addUpdateListener(new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					Float rotate = (Float) animation.getAnimatedValue();
					Log.i("peter", "rotate = " + rotate);
					
					if(rotate <=90) {
						if(front.getVisibility() == View.GONE) {
							front.setVisibility(View.VISIBLE);
						}
					}
				}
			});
			
			break;
		}
	}

}
