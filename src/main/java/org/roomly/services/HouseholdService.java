package org.roomly.services;

import lombok.RequiredArgsConstructor;
import org.roomly.repositories.HouseHoldRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HouseholdService {
    private final HouseHoldRepository houseHoldRepository;
    
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
