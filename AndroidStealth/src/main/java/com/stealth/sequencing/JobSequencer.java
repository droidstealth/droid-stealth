package com.stealth.sequencing;

import java.util.LinkedList;

import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;

/**
 * Let's one perform jobs in sequence.
 * Created by OlivierHokke on 06-Apr-14.
 */
public class JobSequencer {

	private LinkedList<IJob> mSteps;
	private Runnable mFail;
	private Runnable mSuccess;

	public JobSequencer(Runnable fail, Runnable success) {
		mFail = fail;
		mSuccess = success;
		mSteps = new LinkedList<IJob>();
	}

	/**
	 * Starts the sequencer (on current thread)
	 */
	public void start() {
		Utils.d("Starting job queue");
		nextJob();
	}

	/**
	 * Starts the sequencer on a separate thread
	 */
	public void startThreaded() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				start();
			}
		}).start();
	}

	/**
	 * Resets the steps to be taken for booting
	 */
	public void resetJobs() {
		mSteps = new LinkedList<IJob>();
	}

	/**
	 * Performs the next step as they are queued
	 */
	public void nextJob() {
		if (mSteps.isEmpty()) {
			Utils.d("Job list done. Great success!");
			mSuccess.run();
		} else {
			Utils.d("Doing next job.");
			mSteps.pop().doJob(new IOnResult<Boolean>() {
				@Override
				public void onResult(Boolean result) {
					if (result) {
						Utils.d("Job succeeded.");
						nextJob();
					}
					else {
						Utils.d("Job failed.");
						mFail.run();
					}
				}
			});
		}
	}

	/**
	 * Adds a step to the sequencer
	 */
	public void addJob(IJob step) {
		mSteps.add(step);
	}
}
