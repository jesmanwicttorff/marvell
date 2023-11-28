package com.marvell.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class MarvellTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Marvell getMarvellSample1() {
        return new Marvell().id(1L).nombre("nombre1").descripcion("descripcion1").imagenurl("imagenurl1");
    }

    public static Marvell getMarvellSample2() {
        return new Marvell().id(2L).nombre("nombre2").descripcion("descripcion2").imagenurl("imagenurl2");
    }

    public static Marvell getMarvellRandomSampleGenerator() {
        return new Marvell()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .descripcion(UUID.randomUUID().toString())
            .imagenurl(UUID.randomUUID().toString());
    }
}
