package com.noffice.repository;

import com.noffice.entity.ContractType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContractTypeRepository extends JpaRepository<ContractType, UUID> {

	@Query("""
			FROM ContractType d
			WHERE d.isDeleted = false
			AND (COALESCE(:searchString, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.contractTypeCode AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.contractTypeName AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.contractTypeDescription AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
					)
			AND (COALESCE(:ContractTypeCode, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.contractTypeCode AS string)))
			LIKE CONCAT('%', LOWER(:ContractTypeCode), '%')
					)
			AND (COALESCE(:ContractTypeName, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.contractTypeName AS string)))
			LIKE CONCAT('%', LOWER(:ContractTypeName), '%')
					)
			AND (COALESCE(:ContractTypeDescription, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.contractTypeDescription AS string)))
			LIKE CONCAT('%', LOWER(:ContractTypeDescription), '%')
					)
			AND d.partnerId = :partnerId
			ORDER BY d.createAt DESC
			""")
	Page<ContractType> getContractTypeWithPagination(
			@Param("searchString") String searchString,
			@Param("ContractTypeCode") String ContractTypeCode,
			@Param("ContractTypeName") String ContractTypeName,
			@Param("ContractTypeDescription") String ContractTypeDescription,
			@Param("partnerId") UUID partnerId,
			Pageable pageable
	);

	@Query(value = "FROM ContractType d " +
			"WHERE(:ContractTypeCode IS NULL OR LOWER(d.contractTypeCode) = LOWER(:ContractTypeCode)) " +
			"AND d.isDeleted = false AND d.partnerId = :partnerId ")
	ContractType findByCode(@Param("ContractTypeCode") String ContractTypeCode, @Param("partnerId") UUID partnerId);

	@Query(value = "FROM ContractType d " +
			"WHERE d.contractTypeCode = :ContractTypeCode " +
			"AND d.isDeleted = false ")
	ContractType findByContractTypeCode(@Param("ContractTypeCode") String ContractTypeCode);

	@Query("FROM ContractType d WHERE d.partnerId = :partnerId  AND d.isDeleted = false AND d.isActive = true")
	List<ContractType> getAllContractType(UUID partnerId);

	Optional<ContractType> findById(UUID id);

	@Query(value = "FROM ContractType d " +
			"WHERE d.id = :id")
	ContractType findByContractTypeIdIncludeDeleted(@Param("id") UUID id);
}