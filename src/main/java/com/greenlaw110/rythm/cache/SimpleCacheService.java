/* 
 * Copyright (C) 2013 The Rythm Engine project
 * Gelin Luo <greenlaw110(at)gmail.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.greenlaw110.rythm.cache;

import com.greenlaw110.rythm.internal.RythmThreadFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: luog
 * Date: 13/04/12
 * Time: 10:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleCacheService implements ICacheService {

    public static final SimpleCacheService INSTANCE = new SimpleCacheService();

    private static class TimerThreadFactory extends RythmThreadFactory {
        private TimerThreadFactory() {
            super("rythm-timer");
        }
    }

    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1, new TimerThreadFactory());

    private SimpleCacheService() {
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                List<String> toBeRemoved = new ArrayList<String>();
                for (Map.Entry<String, Item> entry : cache_.entrySet()) {
                    int ttl = entry.getValue().ttl;
                    if (ttl > 0) {
                        entry.getValue().ttl--;
                    } else if (ttl == 0) {
                        toBeRemoved.add(entry.getKey());
                    } else {
                        // do nothing
                    }
                }
                for (String key : toBeRemoved) {
                    cache_.remove(key);
                }
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);

    }

    private static class Item {
        String key;
        Serializable value;
        int ttl;

        Item(String key, Serializable value, int ttl) {
            this.key = key;
            this.value = value;
            this.ttl = ttl;
        }
    }

    private ConcurrentHashMap<String, Item> cache_ = new ConcurrentHashMap<String, Item>();

    @Override
    public void put(String key, Serializable value, int ttl) {
        if (null == key) throw new NullPointerException();
        if (0 == ttl) {
            ttl = defaultTTL;
        }
        Item item = cache_.get(key);
        if (null == item) {
            Item newItem = new Item(key, value, ttl);
            item = cache_.putIfAbsent(key, newItem);
            if (null != item) {
                item.value = value;
                item.ttl = ttl;
            }
        } else {
            item.value = value;
            item.ttl = ttl;
        }
    }

    @Override
    public void put(String key, Serializable value) {
        put(key, value, defaultTTL);
    }

    @Override
    public Serializable remove(String key) {
        Item item = cache_.remove(key);
        return null == item ? null : item.value;
    }

    @Override
    public void clean() {
        cache_.clear();
    }

    @Override
    public Serializable get(String key) {
        Item item = cache_.get(key);
        return null == item ? null : item.value;
    }

    @Override
    public boolean contains(String key) {
        return cache_.contains(key);
    }

    private int defaultTTL = 60;

    @Override
    public void setDefaultTTL(int ttl) {
        if (ttl == 0) throw new IllegalArgumentException("time to live value couldn't be zero");
        this.defaultTTL = ttl;
    }

    @Override
    public void shutdown() {
        scheduler.shutdown();
    }

    @Override
    protected void finalize() throws Throwable {
        shutdown();
    }
}
