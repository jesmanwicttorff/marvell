package com.marvell.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.marvell.IntegrationTest;
import com.marvell.domain.Distribuidor;
import com.marvell.repository.DistribuidorRepository;
import com.marvell.service.dto.DistribuidorDTO;
import com.marvell.service.mapper.DistribuidorMapper;
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
 * Integration tests for the {@link DistribuidorResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class DistribuidorResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_SITIO_WEB = "AAAAAAAAAA";
    private static final String UPDATED_SITIO_WEB = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/distribuidors";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private DistribuidorRepository distribuidorRepository;

    @Autowired
    private DistribuidorMapper distribuidorMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDistribuidorMockMvc;

    private Distribuidor distribuidor;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Distribuidor createEntity(EntityManager em) {
        Distribuidor distribuidor = new Distribuidor().nombre(DEFAULT_NOMBRE).sitioWeb(DEFAULT_SITIO_WEB);
        return distribuidor;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Distribuidor createUpdatedEntity(EntityManager em) {
        Distribuidor distribuidor = new Distribuidor().nombre(UPDATED_NOMBRE).sitioWeb(UPDATED_SITIO_WEB);
        return distribuidor;
    }

    @BeforeEach
    public void initTest() {
        distribuidor = createEntity(em);
    }

    @Test
    @Transactional
    void createDistribuidor() throws Exception {
        int databaseSizeBeforeCreate = distribuidorRepository.findAll().size();
        // Create the Distribuidor
        DistribuidorDTO distribuidorDTO = distribuidorMapper.toDto(distribuidor);
        restDistribuidorMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(distribuidorDTO))
            )
            .andExpect(status().isCreated());

        // Validate the Distribuidor in the database
        List<Distribuidor> distribuidorList = distribuidorRepository.findAll();
        assertThat(distribuidorList).hasSize(databaseSizeBeforeCreate + 1);
        Distribuidor testDistribuidor = distribuidorList.get(distribuidorList.size() - 1);
        assertThat(testDistribuidor.getNombre()).isEqualTo(DEFAULT_NOMBRE);
        assertThat(testDistribuidor.getSitioWeb()).isEqualTo(DEFAULT_SITIO_WEB);
    }

    @Test
    @Transactional
    void createDistribuidorWithExistingId() throws Exception {
        // Create the Distribuidor with an existing ID
        distribuidor.setId(1L);
        DistribuidorDTO distribuidorDTO = distribuidorMapper.toDto(distribuidor);

        int databaseSizeBeforeCreate = distribuidorRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDistribuidorMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(distribuidorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Distribuidor in the database
        List<Distribuidor> distribuidorList = distribuidorRepository.findAll();
        assertThat(distribuidorList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllDistribuidors() throws Exception {
        // Initialize the database
        distribuidorRepository.saveAndFlush(distribuidor);

        // Get all the distribuidorList
        restDistribuidorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(distribuidor.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].sitioWeb").value(hasItem(DEFAULT_SITIO_WEB)));
    }

    @Test
    @Transactional
    void getDistribuidor() throws Exception {
        // Initialize the database
        distribuidorRepository.saveAndFlush(distribuidor);

        // Get the distribuidor
        restDistribuidorMockMvc
            .perform(get(ENTITY_API_URL_ID, distribuidor.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(distribuidor.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.sitioWeb").value(DEFAULT_SITIO_WEB));
    }

    @Test
    @Transactional
    void getNonExistingDistribuidor() throws Exception {
        // Get the distribuidor
        restDistribuidorMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDistribuidor() throws Exception {
        // Initialize the database
        distribuidorRepository.saveAndFlush(distribuidor);

        int databaseSizeBeforeUpdate = distribuidorRepository.findAll().size();

        // Update the distribuidor
        Distribuidor updatedDistribuidor = distribuidorRepository.findById(distribuidor.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDistribuidor are not directly saved in db
        em.detach(updatedDistribuidor);
        updatedDistribuidor.nombre(UPDATED_NOMBRE).sitioWeb(UPDATED_SITIO_WEB);
        DistribuidorDTO distribuidorDTO = distribuidorMapper.toDto(updatedDistribuidor);

        restDistribuidorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, distribuidorDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(distribuidorDTO))
            )
            .andExpect(status().isOk());

        // Validate the Distribuidor in the database
        List<Distribuidor> distribuidorList = distribuidorRepository.findAll();
        assertThat(distribuidorList).hasSize(databaseSizeBeforeUpdate);
        Distribuidor testDistribuidor = distribuidorList.get(distribuidorList.size() - 1);
        assertThat(testDistribuidor.getNombre()).isEqualTo(UPDATED_NOMBRE);
        assertThat(testDistribuidor.getSitioWeb()).isEqualTo(UPDATED_SITIO_WEB);
    }

    @Test
    @Transactional
    void putNonExistingDistribuidor() throws Exception {
        int databaseSizeBeforeUpdate = distribuidorRepository.findAll().size();
        distribuidor.setId(longCount.incrementAndGet());

        // Create the Distribuidor
        DistribuidorDTO distribuidorDTO = distribuidorMapper.toDto(distribuidor);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDistribuidorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, distribuidorDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(distribuidorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Distribuidor in the database
        List<Distribuidor> distribuidorList = distribuidorRepository.findAll();
        assertThat(distribuidorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDistribuidor() throws Exception {
        int databaseSizeBeforeUpdate = distribuidorRepository.findAll().size();
        distribuidor.setId(longCount.incrementAndGet());

        // Create the Distribuidor
        DistribuidorDTO distribuidorDTO = distribuidorMapper.toDto(distribuidor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDistribuidorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(distribuidorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Distribuidor in the database
        List<Distribuidor> distribuidorList = distribuidorRepository.findAll();
        assertThat(distribuidorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDistribuidor() throws Exception {
        int databaseSizeBeforeUpdate = distribuidorRepository.findAll().size();
        distribuidor.setId(longCount.incrementAndGet());

        // Create the Distribuidor
        DistribuidorDTO distribuidorDTO = distribuidorMapper.toDto(distribuidor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDistribuidorMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(distribuidorDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Distribuidor in the database
        List<Distribuidor> distribuidorList = distribuidorRepository.findAll();
        assertThat(distribuidorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDistribuidorWithPatch() throws Exception {
        // Initialize the database
        distribuidorRepository.saveAndFlush(distribuidor);

        int databaseSizeBeforeUpdate = distribuidorRepository.findAll().size();

        // Update the distribuidor using partial update
        Distribuidor partialUpdatedDistribuidor = new Distribuidor();
        partialUpdatedDistribuidor.setId(distribuidor.getId());

        partialUpdatedDistribuidor.sitioWeb(UPDATED_SITIO_WEB);

        restDistribuidorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDistribuidor.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedDistribuidor))
            )
            .andExpect(status().isOk());

        // Validate the Distribuidor in the database
        List<Distribuidor> distribuidorList = distribuidorRepository.findAll();
        assertThat(distribuidorList).hasSize(databaseSizeBeforeUpdate);
        Distribuidor testDistribuidor = distribuidorList.get(distribuidorList.size() - 1);
        assertThat(testDistribuidor.getNombre()).isEqualTo(DEFAULT_NOMBRE);
        assertThat(testDistribuidor.getSitioWeb()).isEqualTo(UPDATED_SITIO_WEB);
    }

    @Test
    @Transactional
    void fullUpdateDistribuidorWithPatch() throws Exception {
        // Initialize the database
        distribuidorRepository.saveAndFlush(distribuidor);

        int databaseSizeBeforeUpdate = distribuidorRepository.findAll().size();

        // Update the distribuidor using partial update
        Distribuidor partialUpdatedDistribuidor = new Distribuidor();
        partialUpdatedDistribuidor.setId(distribuidor.getId());

        partialUpdatedDistribuidor.nombre(UPDATED_NOMBRE).sitioWeb(UPDATED_SITIO_WEB);

        restDistribuidorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDistribuidor.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedDistribuidor))
            )
            .andExpect(status().isOk());

        // Validate the Distribuidor in the database
        List<Distribuidor> distribuidorList = distribuidorRepository.findAll();
        assertThat(distribuidorList).hasSize(databaseSizeBeforeUpdate);
        Distribuidor testDistribuidor = distribuidorList.get(distribuidorList.size() - 1);
        assertThat(testDistribuidor.getNombre()).isEqualTo(UPDATED_NOMBRE);
        assertThat(testDistribuidor.getSitioWeb()).isEqualTo(UPDATED_SITIO_WEB);
    }

    @Test
    @Transactional
    void patchNonExistingDistribuidor() throws Exception {
        int databaseSizeBeforeUpdate = distribuidorRepository.findAll().size();
        distribuidor.setId(longCount.incrementAndGet());

        // Create the Distribuidor
        DistribuidorDTO distribuidorDTO = distribuidorMapper.toDto(distribuidor);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDistribuidorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, distribuidorDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(distribuidorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Distribuidor in the database
        List<Distribuidor> distribuidorList = distribuidorRepository.findAll();
        assertThat(distribuidorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDistribuidor() throws Exception {
        int databaseSizeBeforeUpdate = distribuidorRepository.findAll().size();
        distribuidor.setId(longCount.incrementAndGet());

        // Create the Distribuidor
        DistribuidorDTO distribuidorDTO = distribuidorMapper.toDto(distribuidor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDistribuidorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(distribuidorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Distribuidor in the database
        List<Distribuidor> distribuidorList = distribuidorRepository.findAll();
        assertThat(distribuidorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDistribuidor() throws Exception {
        int databaseSizeBeforeUpdate = distribuidorRepository.findAll().size();
        distribuidor.setId(longCount.incrementAndGet());

        // Create the Distribuidor
        DistribuidorDTO distribuidorDTO = distribuidorMapper.toDto(distribuidor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDistribuidorMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(distribuidorDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Distribuidor in the database
        List<Distribuidor> distribuidorList = distribuidorRepository.findAll();
        assertThat(distribuidorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDistribuidor() throws Exception {
        // Initialize the database
        distribuidorRepository.saveAndFlush(distribuidor);

        int databaseSizeBeforeDelete = distribuidorRepository.findAll().size();

        // Delete the distribuidor
        restDistribuidorMockMvc
            .perform(delete(ENTITY_API_URL_ID, distribuidor.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Distribuidor> distribuidorList = distribuidorRepository.findAll();
        assertThat(distribuidorList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
