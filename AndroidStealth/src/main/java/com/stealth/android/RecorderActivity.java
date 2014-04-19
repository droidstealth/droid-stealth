/*
 * The application needs to have the permission to write to external storage
 * if the output file is written to the external storage, and also the
 * permission to record audio. These permissions must be set in the
 * application's AndroidManifest.xml file, with something like:
 *
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.RECORD_AUDIO" />
 *
 */
package com.stealth.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RecorderActivity extends ActionBarActivity {
	private static final String SOUND_DIR = "Sounds";
	private static final int MAX_AMPLITUDE = 32767;
	private static int SAMPLING_INTERVAL = 100;
	private static int ANIMATION_STEP_SIZE = 20;

	private Uri mOutputUri = null;

	private MediaRecorder mRecorder = null;

	private MediaPlayer mPlayer = null;

	private View mRecordLayout;
	private View mSubmitLayout;

	private ImageView mPlayButton;

	private ProgressBar mVolumeLevel;

	private Handler mHandler;
	private VolumeAnimationTask mAnimationTask;

	Runnable mVolumeChecker = new Runnable() {
		@Override
		public void run() {
			displayVolumeLevel();
			mHandler.postDelayed(mVolumeChecker, SAMPLING_INTERVAL);
		}
	};

	/**
	 * Sets up the activity view and loads the uri to write the recording to
	 *
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recorder_activity);

		mRecordLayout = findViewById(R.id.action_toggle_record);
		mSubmitLayout = findViewById(R.id.action_toggle_submit);
		mPlayButton = ((ImageView) findViewById(R.id.record_action_play));

		mVolumeLevel = (ProgressBar) findViewById(R.id.volume_level);

		mRecordLayout.setVisibility(View.VISIBLE);
		mSubmitLayout.setVisibility(View.GONE);

		mOutputUri = getOutputUri();

		mHandler = new Handler();

		setupListeners();
	}

	/**
	 * Releases media player and recorder
	 */
	@Override
	public void onPause() {
		super.onPause();
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}

		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}

		removeRecording();
	}

	/**
	 * Sets up button listeners
	 */
	private void setupListeners() {
		findViewById(R.id.record_action_record).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mRecorder == null) {
					ImageView image = (ImageView) view;
					image.setImageResource(R.drawable.ic_cab_done_holo_dark);

					startRecording();
				} else {
					ImageView image = (ImageView) view;
					image.setImageResource(R.drawable.record);

					stopRecording();
					showSubmitView();
				}
			}
		});

		findViewById(R.id.record_action_submit).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				stopPlaying();
				returnResult();
			}
		});

		findViewById(R.id.record_action_back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				stopPlaying();
				removeRecording();
				showRecordView();
			}
		});

		findViewById(R.id.record_action_play).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mPlayer != null) {
					stopPlaying();
				} else {
					startPlaying();
				}
			}
		});
	}

	/**
	 * Sets the new volume level in the progress bar
	 */
	private void displayVolumeLevel() {
		if (mRecorder != null) {
			/*
			 * getMaxAmplitude returns a 16-bit unsigned int, giving a max of MAX_AMPLITUDE. Progress
			 * bar goes up to 100. This way we get a decent approximation of the possible range.
			 */
			int level = (int) (mRecorder.getMaxAmplitude() / (double) MAX_AMPLITUDE * 100);
			mVolumeLevel.setProgress(level);

			if(mAnimationTask != null){
				mAnimationTask.cancel(true);
			}
			mAnimationTask = new VolumeAnimationTask(level);
			mAnimationTask.execute();

		}
	}

	/**
	 * Sets view to record mode
	 */
	private void showRecordView() {
		mRecordLayout.setVisibility(View.VISIBLE);
		mSubmitLayout.setVisibility(View.GONE);

		((ImageView) findViewById(R.id.record_action_record)).setImageResource(R.drawable.record);
	}

	/**
	 * Sets view to submit mode
	 */
	private void showSubmitView() {
		mRecordLayout.setVisibility(View.GONE);
		mSubmitLayout.setVisibility(View.VISIBLE);

		mPlayButton.setImageResource(R.drawable.play);
	}

	/**
	 * Clears the recording if any was made
	 */
	private void removeRecording() {
		if (mOutputUri != null) {
			File recording = FileUtils.getFile(this, mOutputUri);
			if (recording != null && recording.exists()) {
				recording.delete();
			}
		}
	}

	/**
	 * Returns the preferred output uri for the recording. Checks the intent first for an output
	 * file that has been set. If none has been set, it checks availability of the Sounds folder.
	 * If neither are available, the cache of this application is used.
	 *
	 * @return
	 */
	private Uri getOutputUri() {
		Uri outputUri = getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT);

		if (outputUri == null) {
			File outDir = new File(Environment.getExternalStorageDirectory(), SOUND_DIR);
			if (!outDir.exists())
				outDir.mkdir();
			else if (!outDir.isDirectory()) {
				outDir = getExternalCacheDir();
			}
			SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd_hhmmss");
			String format = s.format(new Date());

			outputUri = Uri.fromFile(new File(outDir, "SND_" + format + ".3gp"));
		}

		return outputUri;
	}

	/**
	 * Starts playing the recorded audio fragment
	 */
	private void startPlaying() {
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(this, mOutputUri);
			mPlayer.prepare();
			mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mediaPlayer) {
					stopPlaying();
				}
			});
			mPlayer.start();
			mPlayButton.setImageResource(R.drawable.pause);
		} catch (IOException e) {
		}
	}

	/**
	 * Stops playback of the recorded audio fragment
	 */
	private void stopPlaying() {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
			mPlayButton.setImageResource(R.drawable.play);
		}
	}

	/**
	 * Starts the media recorder to record a new fragment
	 */
	private void startRecording() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(FileUtils.getPath(this, mOutputUri));
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try {
			mRecorder.prepare();
		} catch (IOException e) {
		}

		mRecorder.start();
		mVolumeChecker.run();
	}

	/**
	 * Stops the recorder
	 */
	private void stopRecording() {
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;

			mHandler.removeCallbacks(mVolumeChecker);
			if (mAnimationTask != null) {
				mAnimationTask.cancel(true);
			}
			mVolumeLevel.setProgress(0);
		}
	}


	/**
	 * Returns the recorded data uri
	 */
	private void returnResult() {
		Intent data = new Intent();

		data.setData(mOutputUri);

		if (getParent() == null) {
			setResult(Activity.RESULT_OK, data);
		} else {
			getParent().setResult(Activity.RESULT_OK, data);
		}
		finish();
	}

	/**
	 * Helper class for smoother progress interpolation
	 */
	private class VolumeAnimationTask extends AsyncTask<Void, Void, Void> {
		private int mStepNumber;
		private long mSleepTime;
		private float mStepDir;

		public VolumeAnimationTask(int newVolume){
			int startVolume = mVolumeLevel.getProgress();
			mStepDir = Math.signum(newVolume - startVolume);
			mStepNumber = (int)((newVolume - startVolume) / (float) ANIMATION_STEP_SIZE);
			mSleepTime = mStepNumber / SAMPLING_INTERVAL;
		}

		@Override
		protected Void doInBackground(Void... voids) {
			for(int i = 0; i < mStepNumber; i++){
				if(isCancelled()) {
					break;
				}

				publishProgress();

				try {
					Thread.sleep(mSleepTime);
				} catch (InterruptedException e) {
					break;
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			int currentProgress = mVolumeLevel.getProgress();
			mVolumeLevel.setProgress(currentProgress + (int)(mStepDir * ANIMATION_STEP_SIZE));
		}
	}
}