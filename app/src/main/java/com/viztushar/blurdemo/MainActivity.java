package com.viztushar.blurdemo;

import java.io.IOException;
import java.io.InputStream;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.viztushar.blur.StackBlurManager;


public class MainActivity extends RoboActivity {
    
	@InjectView(R.id.imageView)        ImageView    imageView;
	@InjectView(R.id.blur_amount)      SeekBar      seekBar;
	@InjectView(R.id.toggleButton)     ToggleButton toggleButton;
	@InjectView(R.id.typeSelectSpinner) Spinner     typeSelectSpinner;
	
	
	private StackBlurManager stackBlurManager;
	
	private String IMAGE_TO_ANALYZE = "android_platform_256.png";
	
	private int blurMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		stackBlurManager = new StackBlurManager(getBitmapFromAsset(this, "android_platform_256.png"));

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
				onBlur();
			}
		});
		
		toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					IMAGE_TO_ANALYZE = "image_transparency.png";
					stackBlurManager = new StackBlurManager(getBitmapFromAsset(getApplicationContext(), IMAGE_TO_ANALYZE));
					onBlur();
				} else {
					IMAGE_TO_ANALYZE = "android_platform_256.png";
					stackBlurManager = new StackBlurManager(getBitmapFromAsset(getApplicationContext(), IMAGE_TO_ANALYZE));
					onBlur();
				}
			}
		});

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.blur_modes, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		typeSelectSpinner.setAdapter(adapter);
		typeSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				setBlurMode(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch(item.getItemId()) {
			case R.id.menu_benchmark:
				Intent intent = new Intent(this, BenchmarkActivity.class);
				startActivity(intent);
				return true;
			default:
				return false;
		}
	}

	private Bitmap getBitmapFromAsset(Context context, String strName) {
        AssetManager assetManager = context.getAssets();
        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(strName);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            return null;
        }
        return bitmap;
    }

	public void setBlurMode(int mode) {
		this.blurMode = mode;
		onBlur();
	}

	private void onBlur() {
		int radius = seekBar.getProgress() * 5;
		switch(blurMode) {
			case 0:
				imageView.setImageBitmap(stackBlurManager.process(radius) );
				break;
			case 1:
				imageView.setImageBitmap(stackBlurManager.processNatively(radius) );
				break;

		}
	}
}
