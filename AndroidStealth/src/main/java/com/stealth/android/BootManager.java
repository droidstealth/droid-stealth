package com.stealth.android;

import java.util.ArrayList;

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
	private static boolean sBootResult = false;
	private static boolean sBooting = false;
	private static String sPin = null;
	private static Context sContext = null;
	private static ArrayList<IOnResult<Boolean>> mBootCallbacks = new ArrayList<IOnResult<Boolean>>();

	/**
	 * Method used to fail the boot: remember that the app has failed, and run
	 * the callback on the main thread so the called of the boot can sadly continue with
	 * failing.
	 */
	private static final Runnable sFail = new Runnable() {
		@Override
		public void run() {
			booted(false);
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
			booted(true);
		}
	};

	/**
	 * The job that will initialize the system with the correct values
	 */
	private static final IJob sInitializeSystem = new IJob() {
		@Override
		public void doJob(IOnResult<Boolean> onReady) {
			Utils.setContext(sContext.getApplicationContext());
			onReady.onResult(true);
		}
	};

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
	 * The job that will create the file index
	 */
	private static final IJob sCreateFileIndex = new IJob() {
		@Override
		public void doJob(IOnResult<Boolean> onReady) {
			createFileIndex(onReady);
		}
	};

	/**
	 * Notify the bootmanager that we booted.
	 * @param result the state or result of the boot. Did it succeed?
	 */
	private static void booted(final boolean result) {
		sBooted = true;
		sBootResult = result;
		sBooting = false;

		if (result) {
			Utils.d("Booted!");
		} else {
			Utils.d("Boot failed...");
		}

		Utils.runOnMain(new Runnable() {
			@Override
			public void run() {
				for (IOnResult<Boolean> callback : mBootCallbacks) {
					if (callback != null) {
						callback.onResult(result);
					}
				}
				mBootCallbacks.clear();
			}
		});
	}

	/**
	 * Adds a callback to the list of callbacks. It will be called when booting is ready.
	 * @param callback the callback to add
	 */
	public static void addBootCallback(IOnResult<Boolean> callback){
		if (sBooted) {
			Utils.d("We already booted!");
			callback.onResult(sBootResult);
		} else if (callback != null) {
			Utils.d("Added boot callback to callback list.");
			mBootCallbacks.add(callback);
		}
	}

	/**
	 * Boots up everything that is needed to let the application function properly.
	 * @param context the application context that will be used for booting
	 * @param pin the pin that should be checked
	 * @param callback the method that will be called when booting is ready.
	 */
	public static void boot(Context context, String pin, IOnResult<Boolean> callback){

		addBootCallback(callback);

		if (sBooted || sBooting) {
			return;
		}

		Utils.d("Booting...");

		sBooting = true;
		sPin = pin;
		sContext = context;

		// step 0: TODO load keys
		// step 1: decrypt & read pins, then check pin
		// step 2: decrypt & read file index (if not booted yet)
		// step 3: ...
		// step 4: profit

		JobSequencer jobs = new JobSequencer(sFail, sSuccess);
		jobs.addJob(sInitializeSystem);
		jobs.addJob(sCheckPin);
		if (!sBooted) {
			jobs.addJob(sCreateFileIndex);
		}

		jobs.startThreaded();
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
	 * Checks if the pin is valid
	 * @param pin the pin to check
	 * @param callback the method to notify the result
	 */
	private static void checkPin(String pin, IOnResult<Boolean> callback) {
		if (BuildConfig.DEBUG || pin == null) {
			callback.onResult(true);
		} else if (PinManager.get().isPin(pin)) {
			Utils.d("Pin was correct");
			callback.onResult(true);
		} else {
			Utils.d("Incorrect pin");
			callback.onResult(false);
		}
	}

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

}
