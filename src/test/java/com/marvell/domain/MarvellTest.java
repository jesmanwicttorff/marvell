package com.marvell.domain;

import static com.marvell.domain.DistribuidorTestSamples.*;
import static com.marvell.domain.MarvellTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.marvell.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MarvellTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Marvell.class);
        Marvell marvell1 = getMarvellSample1();
        Marvell marvell2 = new Marvell();
        assertThat(marvell1).isNotEqualTo(marvell2);

        marvell2.setId(marvell1.getId());
        assertThat(marvell1).isEqualTo(marvell2);

        marvell2 = getMarvellSample2();
        assertThat(marvell1).isNotEqualTo(marvell2);
    }

    @Test
    void distribuidorTest() throws Exception {
        Marvell marvell = getMarvellRandomSampleGenerator();
        Distribuidor distribuidorBack = getDistribuidorRandomSampleGenerator();

        marvell.addDistribuidor(distribuidorBack);
        assertThat(marvell.getDistribuidors()).containsOnly(distribuidorBack);
        assertThat(distribuidorBack.getDistribuidoid()).isEqualTo(marvell);

        marvell.removeDistribuidor(distribuidorBack);
        assertThat(marvell.getDistribuidors()).doesNotContain(distribuidorBack);
        assertThat(distribuidorBack.getDistribuidoid()).isNull();

        marvell.distribuidors(new HashSet<>(Set.of(distribuidorBack)));
        assertThat(marvell.getDistribuidors()).containsOnly(distribuidorBack);
        assertThat(distribuidorBack.getDistribuidoid()).isEqualTo(marvell);

        marvell.setDistribuidors(new HashSet<>());
        assertThat(marvell.getDistribuidors()).doesNotContain(distribuidorBack);
        assertThat(distribuidorBack.getDistribuidoid()).isNull();
    }
}
