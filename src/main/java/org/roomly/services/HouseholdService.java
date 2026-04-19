package org.roomly.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roomly.dto.HouseholdDTO;
import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.enums.CodeCharacters;
import org.roomly.generators.GeneratedCodeFactory;
import org.roomly.repositories.HouseholdRepository;
import org.roomly.repositories.ProfileRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class HouseholdService {
    private final HouseholdRepository houseHoldRepository;
    private final ProfileRepository profileRepository;
    
    public HouseholdDTO getHousehold (String householdId) {
        return houseHoldRepository
          .findById(householdId)
          .orElseThrow(() -> new IllegalArgumentException("Household with id " + householdId + " not found"))
          .toDTO();
    }
    
    @Transactional
    public HouseholdDTO createHousehold (String name, int membersLimit) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String ownerId = authentication != null ? authentication.getName() : null;
        
        Profile ownerProfile = ownerId != null
                               ? profileRepository.findById(ownerId).orElse(null)
                               : null;
        
        Household household = houseHoldRepository.save(
          new Household()
            .setId(generateNewHouseholdId())
            .setName(name)
            .setMembersLimit(membersLimit)
            .setJoinCode(generateNewJoinCode())
            .setOwner(ownerProfile)
        );
        
        log.info(household.toString());
        return household.toDTO();
    }
    
    public Household getHouseHoldByJoinCode (String joinCode) {
        return houseHoldRepository
          .findByJoinCode((joinCode))
          .orElseThrow(() -> new IllegalArgumentException("Household with join code " + joinCode + " not found"));
    }
    
    public String updateHousehold () {
        return "HouseholdService";
    }
    
    public String deleteHousehold () {
        return "HouseholdService";
    }
    
    public String generateNewJoinCode () {
        String code;
        do code = GeneratedCodeFactory.generate(6, CodeCharacters.LOWERCASE_LETTERS_AND_DIGITS);
        while (houseHoldRepository.existsByJoinCode((code)));
        return code;
    }
    
    public String generateNewHouseholdId () {
        String id;
        do id = GeneratedCodeFactory.generate(7, CodeCharacters.LOWERCASE_LETTERS_AND_DIGITS);
        while (houseHoldRepository.existsById(id));
        
        return id;
    }
    
    //TODO temporary for testing, delete in production
    public String getHouseholdInfoTest (String householdId) {
        var household = houseHoldRepository.findById(householdId)
          .orElseThrow(() -> new IllegalArgumentException("Household with id " + householdId + " not found"));
        
        var users = profileRepository.findAllByHouseholdId(householdId);
        
        StringBuilder sb = new StringBuilder();
        sb.append(household).append("\nMembers:\n");
        users.forEach(user -> sb.append(user.toString()).append("\n"));
        
        return sb.toString();
    }
    
}
