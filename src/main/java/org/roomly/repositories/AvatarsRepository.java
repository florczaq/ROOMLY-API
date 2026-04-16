package org.roomly.repositories;

import org.roomly.entities.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvatarsRepository extends JpaRepository<Avatar, Integer> {
}
