package com.marvell.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.marvell.IntegrationTest;
import com.marvell.domain.Marvell;
import com.marvell.repository.MarvellRepository;
import com.marvell.service.dto.MarvellDTO;
import com.marvell.service.mapper.MarvellMapper;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link MarvellResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MarvellResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final String DEFAULT_IMAGENURL = "AAAAAAAAAA";
    private static final String UPDATED_IMAGENURL = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/marvells";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private MarvellRepository marvellRepository;

    @Autowired
    private MarvellMapper marvellMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMarvellMockMvc;

    private Marvell marvell;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Marvell createEntity(EntityManager em) {
        Marvell marvell = new Marvell().nombre(DEFAULT_NOMBRE).descripcion(DEFAULT_DESCRIPCION).imagenurl(DEFAULT_IMAGENURL);
        return marvell;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Marvell createUpdatedEntity(EntityManager em) {
        Marvell marvell = new Marvell().nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION).imagenurl(UPDATED_IMAGENURL);
        return marvell;
    }

    @BeforeEach
    public void initTest() {
        marvell = createEntity(em);
    }

    @Test
    @Transactional
    void createMarvell() throws Exception {
        int databaseSizeBeforeCreate = marvellRepository.findAll().size();
        // Create the Marvell
        MarvellDTO marvellDTO = marvellMapper.toDto(marvell);
        restMarvellMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(marvellDTO)))
            .andExpect(status().isCreated());

        // Validate the Marvell in the database
        List<Marvell> marvellList = marvellRepository.findAll();
        assertThat(marvellList).hasSize(databaseSizeBeforeCreate + 1);
        Marvell testMarvell = marvellList.get(marvellList.size() - 1);
        assertThat(testMarvell.getNombre()).isEqualTo(DEFAULT_NOMBRE);
        assertThat(testMarvell.getDescripcion()).isEqualTo(DEFAULT_DESCRIPCION);
        assertThat(testMarvell.getImagenurl()).isEqualTo(DEFAULT_IMAGENURL);
    }

    @Test
    @Transactional
    void createMarvellWithExistingId() throws Exception {
        // Create the Marvell with an existing ID
        marvell.setId(1L);
        MarvellDTO marvellDTO = marvellMapper.toDto(marvell);

        int databaseSizeBeforeCreate = marvellRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMarvellMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(marvellDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Marvell in the database
        List<Marvell> marvellList = marvellRepository.findAll();
        assertThat(marvellList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllMarvells() throws Exception {
        // Initialize the database
        marvellRepository.saveAndFlush(marvell);

        // Get all the marvellList
        restMarvellMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(marvell.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].imagenurl").value(hasItem(DEFAULT_IMAGENURL)));
    }

    @Test
    @Transactional
    void getMarvell() throws Exception {
        // Initialize the database
        marvellRepository.saveAndFlush(marvell);

        // Get the marvell
        restMarvellMockMvc
            .perform(get(ENTITY_API_URL_ID, marvell.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(marvell.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION))
            .andExpect(jsonPath("$.imagenurl").value(DEFAULT_IMAGENURL));
    }

    @Test
    @Transactional
    void getNonExistingMarvell() throws Exception {
        // Get the marvell
        restMarvellMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMarvell() throws Exception {
        // Initialize the database
        marvellRepository.saveAndFlush(marvell);

        int databaseSizeBeforeUpdate = marvellRepository.findAll().size();

        // Update the marvell
        Marvell updatedMarvell = marvellRepository.findById(marvell.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMarvell are not directly saved in db
        em.detach(updatedMarvell);
        updatedMarvell.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION).imagenurl(UPDATED_IMAGENURL);
        MarvellDTO marvellDTO = marvellMapper.toDto(updatedMarvell);

        restMarvellMockMvc
            .perform(
                put(ENTITY_API_URL_ID, marvellDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(marvellDTO))
            )
            .andExpect(status().isOk());

        // Validate the Marvell in the database
        List<Marvell> marvellList = marvellRepository.findAll();
        assertThat(marvellList).hasSize(databaseSizeBeforeUpdate);
        Marvell testMarvell = marvellList.get(marvellList.size() - 1);
        assertThat(testMarvell.getNombre()).isEqualTo(UPDATED_NOMBRE);
        assertThat(testMarvell.getDescripcion()).isEqualTo(UPDATED_DESCRIPCION);
        assertThat(testMarvell.getImagenurl()).isEqualTo(UPDATED_IMAGENURL);
    }

    @Test
    @Transactional
    void putNonExistingMarvell() throws Exception {
        int databaseSizeBeforeUpdate = marvellRepository.findAll().size();
        marvell.setId(longCount.incrementAndGet());

        // Create the Marvell
        MarvellDTO marvellDTO = marvellMapper.toDto(marvell);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMarvellMockMvc
            .perform(
                put(ENTITY_API_URL_ID, marvellDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(marvellDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Marvell in the database
        List<Marvell> marvellList = marvellRepository.findAll();
        assertThat(marvellList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMarvell() throws Exception {
        int databaseSizeBeforeUpdate = marvellRepository.findAll().size();
        marvell.setId(longCount.incrementAndGet());

        // Create the Marvell
        MarvellDTO marvellDTO = marvellMapper.toDto(marvell);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMarvellMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(marvellDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Marvell in the database
        List<Marvell> marvellList = marvellRepository.findAll();
        assertThat(marvellList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMarvell() throws Exception {
        int databaseSizeBeforeUpdate = marvellRepository.findAll().size();
        marvell.setId(longCount.incrementAndGet());

        // Create the Marvell
        MarvellDTO marvellDTO = marvellMapper.toDto(marvell);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMarvellMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(marvellDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Marvell in the database
        List<Marvell> marvellList = marvellRepository.findAll();
        assertThat(marvellList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMarvellWithPatch() throws Exception {
        // Initialize the database
        marvellRepository.saveAndFlush(marvell);

        int databaseSizeBeforeUpdate = marvellRepository.findAll().size();

        // Update the marvell using partial update
        Marvell partialUpdatedMarvell = new Marvell();
        partialUpdatedMarvell.setId(marvell.getId());

        restMarvellMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMarvell.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedMarvell))
            )
            .andExpect(status().isOk());

        // Validate the Marvell in the database
        List<Marvell> marvellList = marvellRepository.findAll();
        assertThat(marvellList).hasSize(databaseSizeBeforeUpdate);
        Marvell testMarvell = marvellList.get(marvellList.size() - 1);
        assertThat(testMarvell.getNombre()).isEqualTo(DEFAULT_NOMBRE);
        assertThat(testMarvell.getDescripcion()).isEqualTo(DEFAULT_DESCRIPCION);
        assertThat(testMarvell.getImagenurl()).isEqualTo(DEFAULT_IMAGENURL);
    }

    @Test
    @Transactional
    void fullUpdateMarvellWithPatch() throws Exception {
        // Initialize the database
        marvellRepository.saveAndFlush(marvell);

        int databaseSizeBeforeUpdate = marvellRepository.findAll().size();

        // Update the marvell using partial update
        Marvell partialUpdatedMarvell = new Marvell();
        partialUpdatedMarvell.setId(marvell.getId());

        partialUpdatedMarvell.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION).imagenurl(UPDATED_IMAGENURL);

        restMarvellMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMarvell.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedMarvell))
            )
            .andExpect(status().isOk());

        // Validate the Marvell in the database
        List<Marvell> marvellList = marvellRepository.findAll();
        assertThat(marvellList).hasSize(databaseSizeBeforeUpdate);
        Marvell testMarvell = marvellList.get(marvellList.size() - 1);
        assertThat(testMarvell.getNombre()).isEqualTo(UPDATED_NOMBRE);
        assertThat(testMarvell.getDescripcion()).isEqualTo(UPDATED_DESCRIPCION);
        assertThat(testMarvell.getImagenurl()).isEqualTo(UPDATED_IMAGENURL);
    }

    @Test
    @Transactional
    void patchNonExistingMarvell() throws Exception {
        int databaseSizeBeforeUpdate = marvellRepository.findAll().size();
        marvell.setId(longCount.incrementAndGet());

        // Create the Marvell
        MarvellDTO marvellDTO = marvellMapper.toDto(marvell);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMarvellMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, marvellDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(marvellDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Marvell in the database
        List<Marvell> marvellList = marvellRepository.findAll();
        assertThat(marvellList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMarvell() throws Exception {
        int databaseSizeBeforeUpdate = marvellRepository.findAll().size();
        marvell.setId(longCount.incrementAndGet());

        // Create the Marvell
        MarvellDTO marvellDTO = marvellMapper.toDto(marvell);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMarvellMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(marvellDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Marvell in the database
        List<Marvell> marvellList = marvellRepository.findAll();
        assertThat(marvellList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMarvell() throws Exception {
        int databaseSizeBeforeUpdate = marvellRepository.findAll().size();
        marvell.setId(longCount.incrementAndGet());

        // Create the Marvell
        MarvellDTO marvellDTO = marvellMapper.toDto(marvell);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMarvellMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(marvellDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Marvell in the database
        List<Marvell> marvellList = marvellRepository.findAll();
        assertThat(marvellList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMarvell() throws Exception {
        // Initialize the database
        marvellRepository.saveAndFlush(marvell);

        int databaseSizeBeforeDelete = marvellRepository.findAll().size();

        // Delete the marvell
        restMarvellMockMvc
            .perform(delete(ENTITY_API_URL_ID, marvell.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Marvell> marvellList = marvellRepository.findAll();
        assertThat(marvellList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
