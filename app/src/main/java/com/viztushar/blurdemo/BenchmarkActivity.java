package com.viztushar.blurdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import com.viztushar.blur.StackBlurManager;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class BenchmarkActivity extends RoboActivity {

	@InjectView(R.id.progress_java)       ProgressBar javaProgressbar;
	@InjectView(R.id.progress_native)     ProgressBar nativeProgressbar;
	@InjectView(R.id.detail_java)         TextView    javaDetail;
	@InjectView(R.id.detail_native)       TextView    nativeDetail;
	@InjectView(R.id.blur_amount)         SeekBar     blurAmt;
	@InjectView(R.id.blur_amount_detail)  TextView    blurAmtText;
	@InjectView(R.id.result_img)          ImageView   resultImage;

	private BenchmarkTask benchmarkTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_benchmark);

		blurAmt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				blurAmtText.setText(progress + " px");
				if(benchmarkTask != null) {
					benchmarkTask.cancel(true);
				}
				benchmarkTask = new BenchmarkTask();
				benchmarkTask.execute(progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
	}

	private class BenchmarkTask extends AsyncTask<Integer, BlurBenchmarkResult, Bitmap> {
		private int max = Integer.MIN_VALUE;
		private Bitmap outBitmap;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			javaProgressbar.setProgress(-1);
			nativeProgressbar.setProgress(-1);


			javaProgressbar.setIndeterminate(true);
			nativeProgressbar.setIndeterminate(true);


			javaDetail.setText("");
			nativeDetail.setText("");

		}

		@Override
		protected Bitmap doInBackground(Integer... params) {
			if(params.length != 1 || params[0] == null)
				throw new IllegalArgumentException("Pass only 1 Integer to BenchmarkTask");
			int blurAmount = params[0];
			Bitmap inBitmap, blurredBitmap;
			Paint paint = new Paint();

			inBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image_transparency);

			outBitmap = Bitmap.createBitmap(inBitmap.getWidth(), inBitmap.getHeight(), inBitmap.getConfig());
			Canvas canvas = new Canvas(outBitmap);

			long time;
			StackBlurManager blurManager = new StackBlurManager(inBitmap);

			BlurBenchmarkResult result;

			// Java
			time = SystemClock.elapsedRealtime();
			blurredBitmap = blurManager.process(blurAmount);
			result = new BlurBenchmarkResult("Java", (int) (SystemClock.elapsedRealtime() - time));
			canvas.save();
			canvas.clipRect(0, 0, outBitmap.getWidth() / 3, outBitmap.getHeight());
			canvas.drawBitmap(blurredBitmap, 0, 0, paint);
			canvas.restore();
			publishProgress(result);
			blurredBitmap.recycle();

			if(isCancelled())
				return outBitmap;

			// Native

			time = SystemClock.elapsedRealtime();
			blurredBitmap = blurManager.processNatively(blurAmount);
			result = new BlurBenchmarkResult("Native", (int) (SystemClock.elapsedRealtime() - time));
			canvas.save(Canvas.CLIP_SAVE_FLAG);
			canvas.clipRect(outBitmap.getWidth() / 3, 0, 2 * outBitmap.getWidth() / 3, inBitmap.getHeight());
			canvas.drawBitmap(blurredBitmap, 0, 0, paint);
			canvas.restore();
			publishProgress(result);
			blurredBitmap.recycle();

			return outBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			resultImage.setImageBitmap(result);
		}

		@Override protected void onProgressUpdate(BlurBenchmarkResult... values) {
			super.onProgressUpdate(values);
			resultImage.setImageBitmap(outBitmap);
			for (BlurBenchmarkResult benchmark : values) {
				if(benchmark == null)
					continue;
				if(benchmark.processingMillis > max) {
					max = benchmark.processingMillis;
					if(!javaProgressbar.isIndeterminate())
						javaProgressbar.setMax(max);
					if(!nativeProgressbar.isIndeterminate())
						nativeProgressbar.setMax(max);

				}
				if("Java".equals(benchmark.processingType)) {
					javaProgressbar.setIndeterminate(false);
					javaProgressbar.setMax(max);
					javaProgressbar.setProgress(benchmark.processingMillis);
					javaDetail.setText(benchmark.processingMillis + " ms");
				}
				else if("Native".equals(benchmark.processingType)) {
					nativeProgressbar.setIndeterminate(false);
					nativeProgressbar.setMax(max);
					nativeProgressbar.setProgress(benchmark.processingMillis);
					nativeDetail.setText(benchmark.processingMillis + " ms");
				}

			}
		}

	}

	private static class BlurBenchmarkResult {
		public final int processingMillis;
		public final String processingType;

		private BlurBenchmarkResult(String processingType, int processingMillis) {
			this.processingType = processingType;
			this.processingMillis = processingMillis;
		}
	}
}
