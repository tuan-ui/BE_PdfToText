package com.noffice.repository;

import com.noffice.entity.Domain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DomainRepository extends JpaRepository<Domain, Long> {

	@Query("""
			FROM Domain d
			WHERE d.isDeleted = false
			AND (COALESCE(:searchString, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.domainCode AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.domainName AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.domainDescription AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
					)
			AND (COALESCE(:domainCode, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.domainCode AS string)))
			LIKE CONCAT('%', LOWER(:domainCode), '%')
					)
			AND (COALESCE(:domainName, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.domainName AS string)))
			LIKE CONCAT('%', LOWER(:domainName), '%')
					)
			AND (COALESCE(:domainDescription, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.domainDescription AS string)))
			LIKE CONCAT('%', LOWER(:domainDescription), '%')
					)
			AND d.partnerId = :partnerId
			ORDER BY d.createAt DESC
			""")
	Page<Domain> getDomainWithPagination(
			@Param("searchString") String searchString,
			@Param("domainCode") String domainCode,
			@Param("domainName") String domainName,
			@Param("domainDescription") String domainDescription,
			@Param("partnerId") UUID partnerId,
			Pageable pageable
	);

	@Query(value = "FROM Domain d " +
			"WHERE(:domainCode IS NULL OR LOWER(d.domainCode) = LOWER(:domainCode)) " +
			"AND d.isDeleted = false AND d.partnerId = :partnerId ")
	Domain findByCode(@Param("domainCode") String domainCode, @Param("partnerId") UUID partnerId);

	@Query(value = "FROM Domain d " +
			"WHERE d.domainCode = :domainCode " +
			"AND d.isDeleted = false ")
	Domain findByDomainCode(@Param("domainCode") String domainCode);

	@Query(value = "FROM Domain d " +
			"WHERE d.domainCode = :domainCode")
	Domain findByDomainCodeIncludeDeleted(@Param("domainCode") String domainCode);

	@Query(value = "FROM Domain d " +
			"WHERE d.id = :id")
	Domain findByDomainIdIncludeDeleted(@Param("id") UUID id);

	@Query("FROM Domain d WHERE d.partnerId = :partnerId  AND d.isDeleted = false AND d.isActive = true")
	List<Domain> getAllDomain(UUID partnerId);
}