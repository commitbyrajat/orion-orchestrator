package com.example.msme.repository;

import com.example.msme.entity.PanHolder;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface PanHolderRepository extends JpaRepository<PanHolder, Long> {

  @EntityGraph(attributePaths = {
      "udyamRegistration",
      "udyamRegistration.gstRegistrations",
      "udyamRegistration.businessLocations"
  })
  @Query("select p from PanHolder p where p.panNumber = :panNumber")
  Optional<PanHolder> findWithKycByPanNumber(@Param("panNumber") String panNumber);

  @EntityGraph(attributePaths = {
      "udyamRegistration",
      "udyamRegistration.gstRegistrations",
      "udyamRegistration.businessLocations"
  })
  @Query("select p from PanHolder p where p.id = :id")
  Optional<PanHolder> findWithKycById(@Param("id") Long id);

  @EntityGraph(attributePaths = {"bankAccounts", "bankAccounts.transactions"})
  @Query("select p from PanHolder p where p.panNumber = :panNumber")
  Optional<PanHolder> findWithAccountsByPanNumber(@Param("panNumber") String panNumber);

  @EntityGraph(attributePaths = {"bankAccounts", "bankAccounts.transactions"})
  @Query("select p from PanHolder p where p.id = :id")
  Optional<PanHolder> findWithAccountsById(@Param("id") Long id);

  Optional<PanHolder> findByPanNumber(String panNumber);
}
