package com.stealth.sequencing;

import com.stealth.utils.IOnResult;

/**
 * Interface for the JobSequencer so you can perform tasks in sequence
 * Created by OlivierHokke on 06-Apr-14.
 */
public interface IJob {
	void doJob(IOnResult<Boolean> onReady);
}
