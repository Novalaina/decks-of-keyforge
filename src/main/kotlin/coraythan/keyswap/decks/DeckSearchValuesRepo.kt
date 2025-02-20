package coraythan.keyswap.decks

import coraythan.keyswap.decks.models.DeckSasValuesSearchable
import coraythan.keyswap.decks.models.DeckSasValuesUpdatable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

@Transactional
interface DeckSasValuesSearchableRepo : CrudRepository<DeckSasValuesSearchable, Long> {
    @Query("SELECT dsvs.sasRating FROM DeckSasValuesSearchable dsvs WHERE dsvs.deckId = :deckId")
    fun findSasForDeckId(@Param(value = "deckId") deckId: Long): Int

    fun findByDeckId(deckId: Long): DeckSasValuesSearchable
}

@Transactional
interface DeckSasValuesUpdatableRepo : CrudRepository<DeckSasValuesUpdatable, Long> {

    fun existsByIdGreaterThan(idGreaterThan: Long): Boolean

    @Modifying
    @Query(
        value = """
            ALTER TABLE deck_sas_values_updatable RENAME TO deck_sas_values_temp_table_name;
            ALTER TABLE deck_sas_values_searchable RENAME TO deck_sas_values_updatable;
            ALTER TABLE deck_sas_values_temp_table_name RENAME TO deck_sas_values_searchable;
                
            ALTER SEQUENCE deck_search_values_1_sequence RENAME TO deck_search_values_sequence_temp;
            ALTER SEQUENCE deck_search_values_2_sequence RENAME TO deck_search_values_1_sequence;
            ALTER SEQUENCE deck_search_values_sequence_temp RENAME TO deck_search_values_2_sequence;
        """,
        nativeQuery = true
    )
    fun swapSearchAndUpdateTables()

    @Modifying
    @Query(
        value = """
            CREATE INDEX IF NOT EXISTS dsv1_sas_asc_id_idx ON deck_sas_values_updatable (sas_rating, id);
            CREATE INDEX IF NOT EXISTS dsv1_sas_desc_id_idx ON deck_sas_values_updatable (sas_rating DESC, id);
            CREATE INDEX IF NOT EXISTS dsv1_import_date_time_asc_id_idx ON deck_sas_values_updatable (import_date_time, id);
            CREATE INDEX IF NOT EXISTS dsv1_import_date_time_desc_id_idx ON deck_sas_values_updatable (import_date_time DESC, id);
            CREATE INDEX IF NOT EXISTS deck_search_values_name_desc_id_idx ON deck_sas_values_updatable (name DESC, id);
            CREATE INDEX IF NOT EXISTS deck_search_values_name_asc_id_idx ON deck_sas_values_updatable (name ASC, id);
            
            CREATE INDEX IF NOT EXISTS dsv1_deck_name_lower ON deck_sas_values_updatable USING gin (LOWER(name) gin_trgm_ops);
            CREATE INDEX IF NOT EXISTS dsv1_expansion_idx ON deck_sas_values_updatable (expansion);
            CREATE INDEX IF NOT EXISTS dsv1_maverick_count_idx ON deck_sas_values_updatable (maverick_count);
            CREATE INDEX IF NOT EXISTS dsv1_raw_amber_idx ON deck_sas_values_updatable (raw_amber);
            CREATE INDEX IF NOT EXISTS dsv1_bonus_draw_idx ON deck_sas_values_updatable (bonus_draw);
            CREATE INDEX IF NOT EXISTS dsv1_bonus_capture_idx ON deck_sas_values_updatable (bonus_capture);
            CREATE INDEX IF NOT EXISTS dsv1_creature_count_idx ON deck_sas_values_updatable (creature_count);
            CREATE INDEX IF NOT EXISTS dsv1_action_count_idx ON deck_sas_values_updatable (action_count);
            CREATE INDEX IF NOT EXISTS dsv1_artifact_count_idx ON deck_sas_values_updatable (artifact_count);
            CREATE INDEX IF NOT EXISTS dsv1_upgrade_count_idx ON deck_sas_values_updatable (upgrade_count);
            CREATE INDEX IF NOT EXISTS dsv1_token_number_idx ON deck_sas_values_updatable (token_number);
            CREATE INDEX IF NOT EXISTS dsv1_card_names_idx ON deck_sas_values_updatable USING gin (card_names gin_trgm_ops);
            CREATE INDEX IF NOT EXISTS dsv1_house_names_string_idx ON deck_sas_values_updatable USING gin (house_names_string gin_trgm_ops);
            
            CREATE INDEX IF NOT EXISTS dsv1_sas_rating_idx ON deck_sas_values_updatable (sas_rating);
            CREATE INDEX IF NOT EXISTS dsv1_synergy_rating_idx ON deck_sas_values_updatable (synergy_rating);
            CREATE INDEX IF NOT EXISTS dsv1_antisynergy_rating_idx ON deck_sas_values_updatable (antisynergy_rating);
            CREATE INDEX IF NOT EXISTS dsv1_amber_control_idx ON deck_sas_values_updatable (amber_control);
            CREATE INDEX IF NOT EXISTS dsv1_expected_amber_idx ON deck_sas_values_updatable (expected_amber);
            CREATE INDEX IF NOT EXISTS dsv1_artifact_control_idx ON deck_sas_values_updatable (artifact_control);
            CREATE INDEX IF NOT EXISTS dsv1_creature_control_idx ON deck_sas_values_updatable (creature_control);
            CREATE INDEX IF NOT EXISTS dsv1_efficiency_idx ON deck_sas_values_updatable (efficiency);
            CREATE INDEX IF NOT EXISTS dsv1_recursion_idx ON deck_sas_values_updatable (recursion);
            CREATE INDEX IF NOT EXISTS dsv1_disruption_idx ON deck_sas_values_updatable (disruption);
            CREATE INDEX IF NOT EXISTS dsv1_effective_power_idx ON deck_sas_values_updatable (effective_power);
            
            CREATE INDEX IF NOT EXISTS dsv1_deck_id_idx ON deck_sas_values_updatable (deck_id);
        """,
        nativeQuery = true
    )
    fun addIndexesToTable1()

    @Modifying
    @Query(
        value = """
            DROP INDEX IF EXISTS dsv1_sas_asc_id_idx;
            DROP INDEX IF EXISTS dsv1_sas_desc_id_idx;
            DROP INDEX IF EXISTS dsv1_import_date_time_asc_id_idx;
            DROP INDEX IF EXISTS dsv1_import_date_time_desc_id_idx;
            DROP INDEX IF EXISTS deck_search_values_name_desc_id_idx;
            DROP INDEX IF EXISTS deck_search_values_name_asc_id_idx;
            
            DROP INDEX IF EXISTS dsv1_deck_name_lower;
            DROP INDEX IF EXISTS dsv1_expansion_idx;
            DROP INDEX IF EXISTS dsv1_maverick_count_idx;
            DROP INDEX IF EXISTS dsv1_raw_amber_idx;
            DROP INDEX IF EXISTS dsv1_bonus_draw_idx;
            DROP INDEX IF EXISTS dsv1_bonus_capture_idx;
            DROP INDEX IF EXISTS dsv1_creature_count_idx;
            DROP INDEX IF EXISTS dsv1_action_count_idx;
            DROP INDEX IF EXISTS dsv1_artifact_count_idx;
            DROP INDEX IF EXISTS dsv1_upgrade_count_idx;
            DROP INDEX IF EXISTS dsv1_token_number_idx;
            DROP INDEX IF EXISTS dsv1_card_names_idx;
            DROP INDEX IF EXISTS dsv1_house_names_string_idx;
            
            DROP INDEX IF EXISTS dsv1_sas_rating_idx;
            DROP INDEX IF EXISTS dsv1_synergy_rating_idx;
            DROP INDEX IF EXISTS dsv1_antisynergy_rating_idx;
            DROP INDEX IF EXISTS dsv1_amber_control_idx;
            DROP INDEX IF EXISTS dsv1_expected_amber_idx;
            DROP INDEX IF EXISTS dsv1_artifact_control_idx;
            DROP INDEX IF EXISTS dsv1_creature_control_idx;
            DROP INDEX IF EXISTS dsv1_efficiency_idx;
            DROP INDEX IF EXISTS dsv1_recursion_idx;
            DROP INDEX IF EXISTS dsv1_disruption_idx;
            DROP INDEX IF EXISTS dsv1_effective_power_idx;
            
            DROP INDEX IF EXISTS dsv1_deck_id_idx;
        """,
        nativeQuery = true
    )
    fun dropIndexesFromTable1()

    @Modifying
    @Query(
        value = """
            CREATE INDEX IF NOT EXISTS dsv2_sas_asc_id_idx ON deck_sas_values_updatable (sas_rating, id);
            CREATE INDEX IF NOT EXISTS dsv2_sas_desc_id_idx ON deck_sas_values_updatable (sas_rating DESC, id);
            CREATE INDEX IF NOT EXISTS dsv2_import_date_time_asc_id_idx ON deck_sas_values_updatable (import_date_time, id);
            CREATE INDEX IF NOT EXISTS dsv2_import_date_time_desc_id_idx ON deck_sas_values_updatable (import_date_time DESC, id);
            
            CREATE INDEX IF NOT EXISTS dsv2_deck_name_lower ON deck_sas_values_updatable USING gin (LOWER(name) gin_trgm_ops);
            CREATE INDEX IF NOT EXISTS dsv2_expansion_idx ON deck_sas_values_updatable (expansion);
            CREATE INDEX IF NOT EXISTS dsv2_maverick_count_idx ON deck_sas_values_updatable (maverick_count);
            CREATE INDEX IF NOT EXISTS dsv2_raw_amber_idx ON deck_sas_values_updatable (raw_amber);
            CREATE INDEX IF NOT EXISTS dsv2_bonus_draw_idx ON deck_sas_values_updatable (bonus_draw);
            CREATE INDEX IF NOT EXISTS dsv2_bonus_capture_idx ON deck_sas_values_updatable (bonus_capture);
            CREATE INDEX IF NOT EXISTS dsv2_creature_count_idx ON deck_sas_values_updatable (creature_count);
            CREATE INDEX IF NOT EXISTS dsv2_action_count_idx ON deck_sas_values_updatable (action_count);
            CREATE INDEX IF NOT EXISTS dsv2_artifact_count_idx ON deck_sas_values_updatable (artifact_count);
            CREATE INDEX IF NOT EXISTS dsv2_upgrade_count_idx ON deck_sas_values_updatable (upgrade_count);
            CREATE INDEX IF NOT EXISTS dsv2_token_number_idx ON deck_sas_values_updatable (token_number);
            CREATE INDEX IF NOT EXISTS dsv2_card_names_idx ON deck_sas_values_updatable USING gin (card_names gin_trgm_ops);
            CREATE INDEX IF NOT EXISTS dsv2_house_names_string_idx ON deck_sas_values_updatable USING gin (house_names_string gin_trgm_ops);
            
            CREATE INDEX IF NOT EXISTS dsv2_sas_rating_idx ON deck_sas_values_updatable (sas_rating);
            CREATE INDEX IF NOT EXISTS dsv2_synergy_rating_idx ON deck_sas_values_updatable (synergy_rating);
            CREATE INDEX IF NOT EXISTS dsv2_antisynergy_rating_idx ON deck_sas_values_updatable (antisynergy_rating);
            CREATE INDEX IF NOT EXISTS dsv2_amber_control_idx ON deck_sas_values_updatable (amber_control);
            CREATE INDEX IF NOT EXISTS dsv2_expected_amber_idx ON deck_sas_values_updatable (expected_amber);
            CREATE INDEX IF NOT EXISTS dsv2_artifact_control_idx ON deck_sas_values_updatable (artifact_control);
            CREATE INDEX IF NOT EXISTS dsv2_creature_control_idx ON deck_sas_values_updatable (creature_control);
            CREATE INDEX IF NOT EXISTS dsv2_efficiency_idx ON deck_sas_values_updatable (efficiency);
            CREATE INDEX IF NOT EXISTS dsv2_recursion_idx ON deck_sas_values_updatable (recursion);
            CREATE INDEX IF NOT EXISTS dsv2_disruption_idx ON deck_sas_values_updatable (disruption);
            CREATE INDEX IF NOT EXISTS dsv2_effective_power_idx ON deck_sas_values_updatable (effective_power);
            
            CREATE INDEX IF NOT EXISTS dsv2_deck_id_idx ON deck_sas_values_updatable (deck_id);
        """,
        nativeQuery = true
    )
    fun addIndexesToTable2()

    @Modifying
    @Query(
        value = """
            DROP INDEX IF EXISTS dsv2_sas_asc_id_idx;
            DROP INDEX IF EXISTS dsv2_sas_desc_id_idx;
            DROP INDEX IF EXISTS dsv2_import_date_time_asc_id_idx;
            DROP INDEX IF EXISTS dsv2_import_date_time_desc_id_idx;
            
            DROP INDEX IF EXISTS dsv2_deck_name_lower;
            DROP INDEX IF EXISTS dsv2_expansion_idx;
            DROP INDEX IF EXISTS dsv2_maverick_count_idx;
            DROP INDEX IF EXISTS dsv2_raw_amber_idx;
            DROP INDEX IF EXISTS dsv2_bonus_draw_idx;
            DROP INDEX IF EXISTS dsv2_bonus_capture_idx;
            DROP INDEX IF EXISTS dsv2_creature_count_idx;
            DROP INDEX IF EXISTS dsv2_action_count_idx;
            DROP INDEX IF EXISTS dsv2_artifact_count_idx;
            DROP INDEX IF EXISTS dsv2_upgrade_count_idx;
            DROP INDEX IF EXISTS dsv2_token_number_idx;
            DROP INDEX IF EXISTS dsv2_card_names_idx;
            DROP INDEX IF EXISTS dsv2_house_names_string_idx;
            
            DROP INDEX IF EXISTS dsv2_sas_rating_idx;
            DROP INDEX IF EXISTS dsv2_synergy_rating_idx;
            DROP INDEX IF EXISTS dsv2_antisynergy_rating_idx;
            DROP INDEX IF EXISTS dsv2_amber_control_idx;
            DROP INDEX IF EXISTS dsv2_expected_amber_idx;
            DROP INDEX IF EXISTS dsv2_artifact_control_idx;
            DROP INDEX IF EXISTS dsv2_creature_control_idx;
            DROP INDEX IF EXISTS dsv2_efficiency_idx;
            DROP INDEX IF EXISTS dsv2_recursion_idx;
            DROP INDEX IF EXISTS dsv2_disruption_idx;
            DROP INDEX IF EXISTS dsv2_effective_power_idx;
            
            DROP INDEX IF EXISTS dsv2_deck_id_idx;
        """,
        nativeQuery = true
    )
    fun dropIndexesFromTable2()
}
