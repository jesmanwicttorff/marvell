package com.marvell.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DistribuidorTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Distribuidor getDistribuidorSample1() {
        return new Distribuidor().id(1L).nombre("nombre1").sitioWeb("sitioWeb1");
    }

    public static Distribuidor getDistribuidorSample2() {
        return new Distribuidor().id(2L).nombre("nombre2").sitioWeb("sitioWeb2");
    }

    public static Distribuidor getDistribuidorRandomSampleGenerator() {
        return new Distribuidor()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .sitioWeb(UUID.randomUUID().toString());
    }
}
