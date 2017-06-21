package com.tapleader;

import android.os.Handler;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mehdi akbarian on 2017-04-19.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class TLock {

    private AtomicBoolean isLocked = new AtomicBoolean(false);
    private long waitBound=15000;
    public synchronized void lock() {
        isLocked.set(true);
        WaitBound(waitBound);
    }

    public synchronized void unlock() {
        isLocked.set(false);
    }

    public synchronized boolean isLocked() {
        return this.isLocked.get();
    }

   public void setWaitBound(long waitBound){
       this.waitBound=waitBound;
   }

    private synchronized void WaitBound(long bound) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isLocked != null && isLocked.get()) {
                    isLocked.set(false);
                }
            }
        }, bound);
    }
}
