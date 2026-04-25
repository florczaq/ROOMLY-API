package org.roomly.services;

import org.roomly.entities.Household;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseholdRepository extends JpaRepository<Household, String> {
}