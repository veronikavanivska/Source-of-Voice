package org.example.auth.repositories;

import org.example.auth.entities.Role;
import org.example.auth.entities.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findRoleByName(RoleName name);

}
