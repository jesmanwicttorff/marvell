package com.marvell.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.marvell.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MarvellDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MarvellDTO.class);
        MarvellDTO marvellDTO1 = new MarvellDTO();
        marvellDTO1.setId(1L);
        MarvellDTO marvellDTO2 = new MarvellDTO();
        assertThat(marvellDTO1).isNotEqualTo(marvellDTO2);
        marvellDTO2.setId(marvellDTO1.getId());
        assertThat(marvellDTO1).isEqualTo(marvellDTO2);
        marvellDTO2.setId(2L);
        assertThat(marvellDTO1).isNotEqualTo(marvellDTO2);
        marvellDTO1.setId(null);
        assertThat(marvellDTO1).isNotEqualTo(marvellDTO2);
    }
}
