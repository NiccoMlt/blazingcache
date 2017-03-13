/*
 Licensed to Diennea S.r.l. under one
 or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership. Diennea S.r.l. licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */
package blazingcache.client.impl;

import blazingcache.utils.RawString;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages running fetches
 *
 * @author enrico.olivelli
 */
public class PendingFetchesManager {

    private final Map<RawString, Set<Long>> pendingFetchesByKey = new HashMap<>();
    private final AtomicLong idgenerator = new AtomicLong();
    private final ReentrantLock lock = new ReentrantLock();

    public long registerFetchForKey(RawString key) {
        long id = idgenerator.incrementAndGet();
        lock.lock();
        try {
            Set<Long> actual = pendingFetchesByKey.get(key);
            if (actual == null) {
                actual = new HashSet<>();
                pendingFetchesByKey.put(key, actual);
            }
            actual.add(id);
        } finally {
            lock.unlock();
        }
        return id;
    }

    public boolean consumeAndValidateFetchForKey(RawString key, long fetchId) {
        lock.lock();
        try {
            Set<Long> actual = pendingFetchesByKey.get(key);
            return actual != null && actual.remove(fetchId);
        } finally {
            lock.unlock();
        }
    }

    public void cancelFetchesForKey(RawString key) {
        lock.lock();
        try {
            pendingFetchesByKey.remove(key);
        } finally {
            lock.unlock();
        }

    }

    public void clear() {
        lock.lock();
        try {
            pendingFetchesByKey.clear();
        } finally {
            lock.unlock();
        }
    }

}
