package com.tapleader;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mehdi akbarian on 2017-04-19.
 * profile: http://ir.linkedin.com/in/mehdiakbarian
 */

class TLock {
        private AtomicBoolean isLocked = new AtomicBoolean(false);
        public synchronized void lock(){
            isLocked.set(true);
        }

        public synchronized void unlock(){
            isLocked.set(false);
        }

    public synchronized boolean isLocked(){
        return this.isLocked.get();
    }
}
