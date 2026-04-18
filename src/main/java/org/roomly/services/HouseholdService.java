package org.roomly.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roomly.dto.HouseholdDTO;
import org.roomly.entities.Household;
import org.roomly.enums.CodeCharacters;
import org.roomly.generators.GeneratedCodeFactory;
import org.roomly.repositories.HouseholdRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class HouseholdService {
    private final HouseholdRepository houseHoldRepository;
    
    public String getHousehold () {
        return "HouseholdService";
    }
    
    @Transactional
    public HouseholdDTO createHousehold (String name, int membersLimit) {
        var context = SecurityContextHolder.getContext();
        var code = generateNewJoinCode();
        var id = generateNewHouseholdId();
        
        Household household = houseHoldRepository.save(new Household()
          .setId(id)
          .setName(name)
          .setMembersLimit(membersLimit)
          .setJoinCode(code)
          .setOwnerId(Objects.requireNonNull(context.getAuthentication()).getName())
        );
        log.info(household.toString());
        return new HouseholdDTO(household.getName(), household.getJoinCode(), household.getMembersLimit());
    }
    
    public HouseholdDTO joinHousehold (String joinCode) {
        var household = houseHoldRepository
          .findByJoinCode((joinCode))
          .orElseThrow(() -> new IllegalArgumentException("Household with join code " + joinCode + " not found"));
        
        
        
        return new HouseholdDTO(household.getName(), household.getJoinCode(), household.getMembersLimit());
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
    
}
