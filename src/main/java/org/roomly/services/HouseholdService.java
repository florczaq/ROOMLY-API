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

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class HouseholdService {
    private final HouseholdRepository householdRepository;
    private final ProfileRepository profileRepository;
    
    public Household getHousehold (String householdId) {
        return householdRepository
          .findById(householdId)
          .orElseThrow(
            () -> new IllegalArgumentException("Household with id %s not found".formatted(householdId)));
    }
    
    public Household getHouseHoldByJoinCode (String joinCode) {
        return householdRepository
          .findByJoinCode((joinCode))
          .orElseThrow(() -> new IllegalArgumentException(
            "Household with join code %s not found".formatted(joinCode)));
    }
    
    public String generateNewJoinCode () {
        String code;
        do code = GeneratedCodeFactory.generate(6, CodeCharacters.LOWERCASE_LETTERS_AND_DIGITS);
        while (householdRepository.existsByJoinCode((code)));
        return code;
    }
    
    public String generateNewHouseholdId () {
        String id;
        do id = GeneratedCodeFactory.generate(7, CodeCharacters.LOWERCASE_LETTERS_AND_DIGITS);
        while (householdRepository.existsById(id));
        
        return id;
    }
    
    public List<Household> getAllHouseholds () {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }
        return householdRepository.findAllByAccount(authentication.getName());
    }
    
    @Transactional
    public void removeMemberFromHousehold (Profile profile) {
        Household household = profile.getHousehold();
        household.getMembers().remove(profile);
        householdRepository.save(household);
        
        profileRepository.delete(profile);
    }
}