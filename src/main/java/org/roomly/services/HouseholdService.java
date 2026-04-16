package org.roomly.services;

import lombok.RequiredArgsConstructor;
import org.roomly.repositories.HouseholdRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HouseholdService {
    private final HouseholdRepository houseHoldRepository;
    
    public String getHousehold () {
        return "HouseholdService";
    }
    
    public String addHousehold () {
        return "HouseholdService";
    }
    
    public String updateHousehold () {
        return "HouseholdService";
    }
    
    public String deleteHousehold () {
        return "HouseholdService";
    }
    
}
