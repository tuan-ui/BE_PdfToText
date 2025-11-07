package com.noffice.repository;

import com.noffice.entity.DocType;
import com.noffice.entity.HolidayType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HolidayTypeRepository extends JpaRepository<HolidayType, Long> {
	@Query("""
			FROM HolidayType ht
			WHERE ht.isDeleted = false
			AND (COALESCE(:searchString, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(ht.holidayTypeCode AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
			OR LOWER(FUNCTION('convert_to_unsign', CAST(ht.holidayTypeName AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
			OR LOWER(FUNCTION('convert_to_unsign', CAST(ht.description AS string)))
			LIKE CONCAT('%', LOWER(:searchString), '%')
					)
			AND (COALESCE(:holidayTypeCode, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(ht.holidayTypeCode AS string)))
			LIKE CONCAT('%', LOWER(:holidayTypeCode), '%')
					)
			AND (COALESCE(:holidayTypeName, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(ht.holidayTypeName AS string)))
			LIKE CONCAT('%', LOWER(:holidayTypeName), '%')
					)
			AND (COALESCE(:description, '') = ''
			OR LOWER(FUNCTION('convert_to_unsign', CAST(ht.description AS string)))
			LIKE CONCAT('%', LOWER(:description), '%')
					)
			AND ht.partnerId = :partnerId
			ORDER BY ht.createAt DESC
			""")
	Page<HolidayType> searchHolidayTypes(@Param("searchString") String searchString,
										 @Param("holidayTypeCode") String holidayTypeCode,
										 @Param("holidayTypeName") String holidayTypeName,
										 @Param("description") String description,
										 @Param("partnerId") UUID partnerId,
										 Pageable pageable);

	@Query(value = "FROM HolidayType ht " +
			"WHERE(:holidayTypeCode IS NULL OR LOWER(ht.holidayTypeCode) = LOWER(:holidayTypeCode)) " +
			"AND ht.isDeleted = false AND ht.partnerId = :partnerId ")
	DocType findByCode(@Param("holidayTypeCode") String holidayTypeCode, @Param("partnerId") UUID partnerId);

	@Query(value = "FROM HolidayType ht WHERE ht.holidayTypeCode= :holidayTypeCode AND ht.isDeleted = false")
	HolidayType getHolidayTypeByCode(@Param("holidayTypeCode") String holidayTypeCode);

	@Query("FROM HolidayType ht WHERE ht.partnerId = :partnerId  AND ht.isDeleted = false AND ht.isActive = true")
	List<HolidayType> getAllHolidayType(UUID partnerId);

	@Query(value = "FROM HolidayType ht " +
			"WHERE ht.id = :id")
	HolidayType findByHolidayTypeIdIncludeDeleted(@Param("id") UUID id);
}
