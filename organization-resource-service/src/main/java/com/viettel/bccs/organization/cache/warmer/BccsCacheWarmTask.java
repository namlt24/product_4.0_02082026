package com.viettel.bccs.organization.cache.warmer;

import java.time.Duration;

public interface BccsCacheWarmTask {

    String cacheName();

    Duration interval();

    void warm();
}
