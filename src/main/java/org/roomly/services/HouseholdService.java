package org.roomly.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.roomly.dto.HouseholdDTO;
import org.roomly.entities.Household;
import org.roomly.enums.CodeCharacters;
import org.roomly.generators.GeneratedCodeFactory;
import org.roomly.repositories.HouseholdRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HouseholdService {
    private final HouseholdRepository houseHoldRepository;
    
    public String getHousehold () {
        return "HouseholdService";
    }
    
    @Transactional
    public HouseholdDTO addHousehold (HouseholdDTO newHousehold) {
        var code = generateNewJoinCode();
        var id = generateNewHouseholdId();
        
        Household household = houseHoldRepository.save(new Household()
          .setId(id)
          .setName(newHousehold.name())
          .setMembersLimit(newHousehold.membersLimit())
          .setJoinCode(code)
        );
        
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
