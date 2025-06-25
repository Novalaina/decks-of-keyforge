DROP TABLE card_identifier;

DELETE
FROM card_edit_history
WHERE
    extra_card_info_id IN (SELECT extra_card_info_id FROM extra_card_info WHERE card_name_url = 'annihilation-ritual-');

DELETE
FROM syn_trait_value
WHERE trait_info_id IN (SELECT trait_info_id FROM extra_card_info WHERE card_name_url = 'annihilation-ritual-');

DELETE
FROM syn_trait_value
WHERE synergy_info_id IN (SELECT synergy_info_id FROM extra_card_info WHERE card_name_url = 'annihilation-ritual-');

DELETE
FROM extra_card_info
WHERE card_name_url = 'annihilation-ritual-';

DELETE
FROM dok_card
WHERE card_title_url = 'annihilation-ritual-';
