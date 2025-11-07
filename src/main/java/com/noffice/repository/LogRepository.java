package com.noffice.repository;

import com.noffice.entity.Logs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface LogRepository extends JpaRepository<Logs, Integer> {

        @Query(
                value = """
                SELECT *
                FROM logs l
                WHERE l.is_deleted = false
                  AND (CAST(:userId AS uuid) IS NULL OR l.create_by = CAST(:userId AS uuid))
                  AND (l.create_at >= COALESCE(CAST(:fromDateTime AS timestamp), l.create_at))
                  AND (l.create_at <= COALESCE(CAST(:toDateTime AS timestamp), l.create_at))
                  AND (:functionKey IS NULL OR l.params ->> 'action' ILIKE CONCAT('%', CAST(:functionKey AS TEXT), '%'))
                  AND (:actionKey IS NULL OR l.action_key ILIKE CONCAT('%', :actionKey, '%'))
                  AND (:partnerId IS NULL OR l.partner_id = CAST(:partnerId AS uuid))
                ORDER BY l.create_at DESC
                """,
                countQuery = """
                SELECT COUNT(*)
                FROM logs l
                WHERE l.is_deleted = false
                  AND (CAST(:userId AS uuid) IS NULL OR l.create_by = CAST(:userId AS uuid))
                  AND (l.create_at >= COALESCE(CAST(:fromDateTime AS timestamp), l.create_at))
                  AND (l.create_at <= COALESCE(CAST(:toDateTime AS timestamp), l.create_at))
                  AND (:functionKey IS NULL OR l.params ->> 'action' ILIKE CONCAT('%', CAST(:functionKey AS TEXT), '%'))
                  AND (:actionKey IS NULL OR l.action_key ILIKE CONCAT('%', :actionKey, '%'))
                  AND (:partnerId IS NULL OR l.partner_id = CAST(:partnerId AS uuid))
                """,
                nativeQuery = true
        )
        Page<Logs> getLogs(
                @Param("userId") UUID userId,
                @Param("fromDateTime") LocalDateTime fromDateTime,
                @Param("toDateTime") LocalDateTime toDateTime,
                @Param("functionKey") String functionKey,
                @Param("actionKey") String actionKey,
                @Param("partnerId") UUID partnerId,
                Pageable pageable
        );



}
