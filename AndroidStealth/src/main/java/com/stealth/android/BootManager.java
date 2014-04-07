package com.stealth.android;

import android.content.Context;
import com.stealth.files.FileIndex;
import com.stealth.sequencing.IJob;
import com.stealth.sequencing.JobSequencer;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;
import pin.PinManager;

/**
 * This class ensures everything is created as it should and all resources are ready and available to use
 * Created by OlivierHokke on 06-Apr-14.
 */
public class BootManager {

	private static boolean sBooted = false;
	private static IOnResult<Boolean> sCallback = null;
	private static String sPin = null;
	private static Context sContext = null;

	/**
	 * Method used to fail the boot: remember that the app has failed, and run
	 * the callback on the main thread so the called of the boot can sadly continue with
	 * failing.
	 */
	private static final Runnable sFail = new Runnable() {
		@Override
		public void run() {
			sBooted = false;
			Utils.d("App failed to boot.");
			Utils.runOnMain(new Runnable() {
				@Override
				public void run() {
					sCallback.onResult(false);
				}
			});
		}
	};

	/**
	 * Method used to finalize the boot: remember that app has booted, and run
	 * the callback on the main thread so the called of the boot can happily continue with
	 * its tasks.
	 */
	private static final Runnable sSuccess = new Runnable() {
		@Override
		public void run() {
			sBooted = true;
			Utils.d("App booted!");
			Utils.runOnMain(new Runnable() {
				@Override
				public void run() {
					sCallback.onResult(true);
				}
			});
		}
	};

	/**
	 * Boots up everything that is needed to let the application function properly.
	 * @param context the application context that will be used for booting
	 * @param pin the pin that should be checked
	 * @param callback the method that will be called when booting is ready.
	 */
	public static void boot(Context context, String pin, IOnResult<Boolean> callback){
		sCallback = callback;
		sPin = pin;
		sContext = context;

		// step 0: TODO load keys
		// step 1: decrypt & read pins, then check pin
		// step 2: decrypt & read file index (if not booted yet)
		// step 3: ...
		// step 4: profit
		Utils.d("BOOTING");
		JobSequencer jobs = new JobSequencer(sFail, sSuccess);
		jobs.addJob(sInitializeSystem);
		jobs.addJob(sCheckPin);
		if (!sBooted) {
			jobs.addJob(sCreateFileIndex);
		}

		jobs.startAsync();
	}

	/**
	 * Boots up everything that is needed to let the application function properly.
	 * @param context the application context that will be used for booting
	 * @param callback the method that will be called when booting is ready.
	 */
	public static void boot(Context context, IOnResult<Boolean> callback){
		boot(context, null, callback);
	}

	/**
	 * The job that will initialize the system with the correct values
	 */
	private static final IJob sInitializeSystem = new IJob() {
		@Override
		public void doJob(IOnResult<Boolean> onReady) {
			Utils.d("Initializing system");
			Utils.setContext(sContext.getApplicationContext());
			onReady.onResult(true);
		}
	};

	/**
	 * Checks if the pin is valid
	 * @param pin the pin to check
	 * @param callback the method to notify the result
	 */
	private static void checkPin(String pin, IOnResult<Boolean> callback) {
		if (BuildConfig.DEBUG || PinManager.get().isPin(pin) || pin == null) {
			if (PinManager.get().isPin(pin)) Utils.d("Pin was correct"); // it was indeed the pin
			// TODO let real or fake pin have an influence
			callback.onResult(true);
		} else {
			Utils.d("Incorrect pin");
			callback.onResult(false);
		}
	}

	/**
	 * The job that will check the pin
	 */
	private static final IJob sCheckPin = new IJob() {
		@Override
		public void doJob(IOnResult<Boolean> onReady) {
			checkPin(sPin, onReady);
		}
	};

	/**
	 * Creates the file index
	 * @param callback the method to notify the result
	 */
	private static void createFileIndex(final IOnResult<Boolean> callback) {
		FileIndex.create(false, new IOnResult<FileIndex>() { // STEP 2
			@Override
			public void onResult(FileIndex result) {
				if (result != null) { // STEP 3
					Utils.d("Created file index");
					callback.onResult(true);
				} else { // STEP 2
					Utils.d("Failed to create file index");
					callback.onResult(false);
				}
			}
		});
	}

	/**
	 * The job that will create the file index
	 */
	private static final IJob sCreateFileIndex = new IJob() {
		@Override
		public void doJob(IOnResult<Boolean> onReady) {
			createFileIndex(onReady);
		}
	};

}
