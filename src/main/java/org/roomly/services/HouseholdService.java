package org.roomly.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.enums.CodeCharacters;
import org.roomly.generators.GeneratedCodeFactory;
import org.roomly.repositories.HouseholdRepository;
import org.roomly.repositories.ProfileRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for core household data operations.
 * <p>
 * Handles lookups by ID or join code, generation of unique household IDs and join codes,
 * retrieval of all households for the authenticated user, and member removal.
 * Complex multi-service flows (household creation, joining) are coordinated by
 * {@link HouseholdOrchestrationService}.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class HouseholdService {
    private final HouseholdRepository householdRepository;
    private final ProfileRepository profileRepository;

    /**
     * Returns the household with the given ID.
     *
     * @param householdId ID of the household
     * @return the matching {@link Household}
     * @throws IllegalArgumentException if no household exists with the given ID
     */
    public Household getHousehold (String householdId) {
        return householdRepository
          .findById(householdId)
          .orElseThrow(
            () -> new IllegalArgumentException("Household with id %s not found".formatted(householdId)));
    }

    /**
     * Returns the household associated with the given join code.
     *
     * @param joinCode the household join code
     * @return the matching {@link Household}
     * @throws IllegalArgumentException if no household exists with the given join code
     */
    public Household getHouseHoldByJoinCode (String joinCode) {
        return householdRepository
          .findByJoinCode((joinCode.toUpperCase()))
          .orElseThrow(() -> new IllegalArgumentException(
            "Household with join code %s not found".formatted(joinCode)));
    }

    /**
     * Generates a unique 6-character join code (lowercase letters and digits).
     * Retries until a code not already in use is found.
     *
     * @return a unique join code
     */
    public String generateNewJoinCode () {
        String code;
        do code = GeneratedCodeFactory.generate(6, CodeCharacters.UPPERCASE_LETTERS_AND_DIGITS);
        while (householdRepository.existsByJoinCode((code)));
        return code;
    }

    /**
     * Generates a unique 7-character household ID (lowercase letters and digits).
     * Retries until an ID not already in use is found.
     *
     * @return a unique household ID
     */
    public String generateNewHouseholdId () {
        String id;
        do id = GeneratedCodeFactory.generate(7, CodeCharacters.LOWERCASE_LETTERS_AND_DIGITS);
        while (householdRepository.existsById(id));

        return id;
    }

    /**
     * Returns all households the currently authenticated account belongs to.
     *
     * @return list of households for the authenticated user
     * @throws IllegalStateException if the user is not authenticated
     */
    public List<Household> getAllHouseholds () {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }
        return householdRepository.findAllByAccount(authentication.getName());
    }

    /**
     * Removes a profile from its household and deletes the profile entity.
     *
     * @param profile the profile to remove
     */
    @Transactional
    public void removeMemberFromHousehold (Profile profile) {
        Household household = profile.getHousehold();
        household.getMembers().remove(profile);
        householdRepository.save(household);

        profileRepository.delete(profile);
    }
}