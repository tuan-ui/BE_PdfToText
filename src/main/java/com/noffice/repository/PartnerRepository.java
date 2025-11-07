package com.noffice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.noffice.entity.Partners;

import java.util.UUID;

public interface PartnerRepository extends JpaRepository<Partners, Long> {

	@Query(value = "FROM Partners p WHERE p.id= :partnerId AND p.isDeleted = false")
	Partners getPartnerById(@Param("partnerId") UUID partnerId);
	@Query(value = "FROM Partners p WHERE p.id= :partnerId ")
	Partners getPartnerByIdIncluideDeleted(@Param("partnerId") UUID partnerId);

	@Query(value = "FROM Partners p WHERE p.partnerCode= :partnerCode AND p.isDeleted = false")
	Partners getPartnerByCode(@Param("partnerCode") String partnerCode);

	@Query("""
			FROM Partners d
			WHERE d.isDeleted = false
			AND (COALESCE(:searchString, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.partnerName AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.email AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.phone AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
						OR LOWER(FUNCTION('convert_to_unsign', CAST(d.address AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
					)
			AND (COALESCE(:partnerName, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.partnerName AS string)))
			LIKE CONCAT('%', LOWER(:partnerName), '%')
					)
			AND (COALESCE(:email, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.email AS string)))
			LIKE CONCAT('%', LOWER(:email), '%')
					)
			AND (COALESCE(:phone, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.phone AS string)))
			LIKE CONCAT('%', LOWER(:phone), '%')
					)
			AND (COALESCE(:address, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(d.address AS string)))
			LIKE CONCAT('%', LOWER(:address), '%')
					)
			ORDER BY d.createAt DESC
			""")
	Page<Partners> searchPartners(@Param("searchString") String searchString,
								  @Param("partnerName") String partnerName,
								  @Param("email") String email,
								  @Param("phone") String phone,
								  @Param("address") String address,
								  Pageable pageable);
}
