package com.marvell.domain;

import static com.marvell.domain.DistribuidorTestSamples.*;
import static com.marvell.domain.MarvellTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.marvell.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DistribuidorTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Distribuidor.class);
        Distribuidor distribuidor1 = getDistribuidorSample1();
        Distribuidor distribuidor2 = new Distribuidor();
        assertThat(distribuidor1).isNotEqualTo(distribuidor2);

        distribuidor2.setId(distribuidor1.getId());
        assertThat(distribuidor1).isEqualTo(distribuidor2);

        distribuidor2 = getDistribuidorSample2();
        assertThat(distribuidor1).isNotEqualTo(distribuidor2);
    }

    @Test
    void distribuidoidTest() throws Exception {
        Distribuidor distribuidor = getDistribuidorRandomSampleGenerator();
        Marvell marvellBack = getMarvellRandomSampleGenerator();

        distribuidor.setDistribuidoid(marvellBack);
        assertThat(distribuidor.getDistribuidoid()).isEqualTo(marvellBack);

        distribuidor.distribuidoid(null);
        assertThat(distribuidor.getDistribuidoid()).isNull();
    }
}
