package com.marvell.service.mapper;

import org.junit.jupiter.api.BeforeEach;

class MarvellMapperTest {

    private MarvellMapper marvellMapper;

    @BeforeEach
    public void setUp() {
        marvellMapper = new MarvellMapperImpl();
    }
}
