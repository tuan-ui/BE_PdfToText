package com.noffice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.noffice.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	@Query("""
    SELECT DISTINCT u
    FROM User u
    WHERE u.isDeleted = false
      AND u.partnerId = :partnerId
      AND (
        :searchString IS NULL OR :searchString = ''
        OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchString, '%'))
        OR CAST(FUNCTION('unaccent', u.fullName) AS string) ILIKE CONCAT('%', :searchString, '%')
        OR CAST(FUNCTION('unaccent', u.email) AS string) ILIKE CONCAT('%', :searchString, '%')
        OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :searchString, '%'))
        OR LOWER(CAST(u.userCode AS string)) LIKE LOWER(CONCAT('%', :searchString, '%'))
      )
      AND (
        :userName IS NULL OR :userName = ''
        OR LOWER(u.username) LIKE LOWER(CONCAT('%', :userName, '%'))
      )
      AND (
        :fullName IS NULL OR :fullName = ''
        OR CAST(FUNCTION('unaccent', u.fullName) AS string) ILIKE CONCAT('%', :fullName, '%')
      )
      AND (
        :phone IS NULL OR :phone = ''
        OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :phone, '%'))
      )
      AND (
        :birthStr IS NULL OR :birthStr = ''
        OR CAST(u.birthday AS date) = FUNCTION('to_date', :birthStr, 'DD/MM/YYYY')
      )
      AND (
        :userCode IS NULL OR :userCode = ''
        OR LOWER(CAST(u.userCode AS string)) LIKE LOWER(CONCAT('%', :userCode, '%'))
      )
    ORDER BY u.createAt DESC
    """)
	Page<User> listUsersNative(
			@Param("searchString") String searchString,
			@Param("userName") String userName,
			@Param("fullName") String fullName,
			@Param("phone") String phone,
			@Param("birthStr") String birthStr,
			@Param("userCode") String userCode,
			@Param("partnerId") UUID partnerId,
			Pageable pageable
	);

//	    @Query("SELECT COUNT(DISTINCT u) FROM User u " +
//	           "LEFT JOIN RoleUserDepartment rud ON u.userId = rud.userId " +
//	           "WHERE u.isActive = 1 " +
//	           "AND (u.partnerId = :partnerId) " +
//	           "AND (:userName IS NULL OR :userName = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :userName, '%'))) " + "AND (:fullName IS NULL OR :fullName = '' OR u.fullNameUnaccented LIKE CONCAT('%', :fullName, '%')) " +
//	           "AND (:phone IS NULL OR :phone = '' OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :phone, '%'))) " +
//	           "AND (:birthday IS NULL OR :birthday = '' OR u.birthday = CAST(:birthday AS date)) " +
//	           "AND (:userCode IS NULL OR :userCode = '' OR LOWER(u.userCode) LIKE LOWER(CONCAT('%', :userCode, '%'))) " +
//	           "AND (:roleId IS NULL OR :roleId = -1 OR rud.roleId = :roleId) " +
//	           "AND (:departmentId IS NULL OR :departmentId = -1 OR rud.departmentId = :departmentId)")
//	    Integer countUsers(
//	            @Param("userName") String userName,
//	            @Param("fullName") String fullNameUnaccented,
//	            @Param("phone") String phone,
//	            @Param("birthday") String birthday,
//	            @Param("userCode") String userCode,
//	            @Param("roleId") Long roleId,
//	            @Param("departmentId") Long departmentId,
//	            @Param("partnerId") Long partnerId);
//
//
//	@Query("UPDATE User u SET u.isActive = 0 WHERE u.userId = :userId")
//	@Modifying
//	void deleteById(@Param("userId") Long userId);

	@Query("UPDATE User u SET u.password = :password WHERE u.id = :userId")
	@Modifying
	void resetPassword(@Param("userId") UUID userId, @Param("password") String password);

	@Query(
			"SELECT COUNT(u) FROM User u " +
			"WHERE u.username = :username " +
			"AND u.isDeleted = false " )
	Integer existsByUsername(@Param("username") String username);

	@Query(
			"SELECT COUNT(u) FROM User u " +
					"WHERE u.partnerId = :partnerId " +
					"AND u.isDeleted = false " )
	Integer existsUserByPartnerId(@Param("partnerId") UUID partnerId);
	
	@Query(
			"SELECT COUNT(u) FROM User u " +
			"WHERE u.userCode = :userCode " +
			"AND u.partnerId = :partnerId " +
			"AND u.isDeleted = false " )
	Integer existsByUserCode(@Param("userCode") String userCode,@Param("partnerId")UUID partnerId);

	@Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username) AND u.isDeleted = false")
	Optional<User> findByUsername(String username);

	@Query("FROM User u WHERE u.id = :id AND u.isDeleted = false")
	User getUserByUserId(UUID id);

	@Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username) ")
	User findByUsernameIncluideDeleted(String username);

	@Query("FROM User u WHERE u.id = :id ")
	User getUserByUserIdIncluideDeleted(UUID id);

	@Query("SELECT COUNT(r) > 0 FROM User r WHERE LOWER(r.userCode) = LOWER(:userCode) AND r.isActive = true AND r.isDeleted = false AND r.id <> :userId AND r.partnerId = :partnerId")
	boolean existsByUserCodeIgnoreCaseNotId(@Param("userCode") String userCode, @Param("userId") UUID userId, @Param("partnerId") UUID partnerId);

	@Query("FROM User u WHERE (u.partnerId = :partnerId) AND u.isDeleted = false")
	List<User> findUser(@Param("partnerId") UUID partnerId);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u WHERE u.userCode = :userCode AND u.partnerId = :partnerId AND u.isDeleted = false")
     boolean existsByUserCodeAndPartnerId(@Param("userCode") String userCode,
                                         @Param("partnerId") UUID partnerId);

	@Query("FROM User u WHERE u.id IN :userIds AND u.isDeleted = false")
	List<User> findAllById(List<UUID> userIds);

	@Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username) And LOWER(u.phone) = LOWER(:phone)  AND u.isDeleted = false")
	User findByUsernameandPhone(String username, String phone);

	@Modifying
	@Query("DELETE FROM User d WHERE d.id = :id")
	void deleteUserByUserId(@Param("id") UUID id);
}
