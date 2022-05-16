package io.mosip.registration.clientmanager.jobservice;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import androidx.work.Configuration;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.mosip.registration.clientmanager.spi.PacketService;

public class PacketStatusSyncJob extends JobService {

    private static final String TAG = PacketStatusSyncJob.class.getSimpleName();
    private Thread jobThread;

    @Inject
    PacketService packetService;

    public PacketStatusSyncJob() {
        Configuration.Builder builder = new Configuration.Builder();
        builder.setJobSchedulerJobIdRange(0, 1000);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidInjection.inject(this);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");

        jobThread = new Thread(() -> {
            if (triggerJob())
                Log.d(TAG, "Job succeeded");
            else
                Log.d(TAG, "Job failed");
            jobFinished(params, false);
        });

        jobThread.start();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (jobThread != null && jobThread.isAlive()) {
            jobThread.interrupt();
        }
        return true;
    }

    private boolean triggerJob() {
        Log.d(TAG, TAG + " Started");
        try {
            packetService.syncAllPacketStatus();
            return true;
        } catch (Exception e) {
            Log.e(TAG, TAG + " failed", e);
        }
        Log.d(TAG, TAG + " Completed");
        return false;
    }

}