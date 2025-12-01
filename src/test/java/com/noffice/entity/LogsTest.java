package com.noffice.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LogsTest {

    @Test
    void testPrePersist_setsIdAndCreateAt() {
        Logs log = new Logs();
        assertThat(log.getId()).isNull();
        assertThat(log.getCreateAt()).isNull();

        log.onCreate();

        assertThat(log.getId()).isNotNull();
        assertThat(log.getCreateAt()).isNotNull();
    }

    @Test
    void testGettersAndSetters() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        UUID createBy = UUID.randomUUID();
        UUID objectId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();

        Map<String, Object> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", 123);

        Logs log = new Logs();
        log.setId(id);
        log.setActionKey("CREATE_USER");
        log.setCreateAt(now);
        log.setCreateBy(createBy);
        log.setObjectId(objectId);
        log.setPartnerId(partnerId);
        log.setIsActive(true);
        log.setIsDeleted(false);
        log.setParams(params);

        assertThat(log.getId()).isEqualTo(id);
        assertThat(log.getActionKey()).isEqualTo("CREATE_USER");
        assertThat(log.getCreateAt()).isEqualTo(now);
        assertThat(log.getCreateBy()).isEqualTo(createBy);
        assertThat(log.getObjectId()).isEqualTo(objectId);
        assertThat(log.getPartnerId()).isEqualTo(partnerId);
        assertThat(log.getIsActive()).isTrue();
        assertThat(log.getIsDeleted()).isFalse();
        assertThat(log.getParams()).isEqualTo(params);
    }

    @Test
    void testOnCreateDoesNotOverrideExistingValues() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Logs log = new Logs();
        log.setId(id);
        log.setCreateAt(now);

        log.onCreate();

        // phải giữ nguyên giá trị đã set
        assertThat(log.getId()).isEqualTo(id);
        assertThat(log.getCreateAt()).isEqualTo(now);
    }


    @Test
    void testDefaultValues_forIsActiveAndIsDeleted() {
        Logs log = new Logs();

        // mặc định phải true/false
        assertThat(log.getIsActive()).isTrue();
        assertThat(log.getIsDeleted()).isFalse();
    }

    @Test
    void testAllArgsConstructor_andGettersSetters() {
        UUID id = UUID.randomUUID();
        String actionKey = "CREATE_USER";
        LocalDateTime createAt = LocalDateTime.now();
        UUID createBy = UUID.randomUUID();
        UUID objectId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();
        Map<String, Object> params = new HashMap<>();
        params.put("key", "value");
        Boolean isActive = false;
        Boolean isDeleted = true;

        // Sử dụng AllArgsConstructor
        Logs log = new Logs(id, actionKey, createAt, createBy, params, objectId, partnerId, isActive, isDeleted);

        // Kiểm tra getter
        assertThat(log.getId()).isEqualTo(id);
        assertThat(log.getActionKey()).isEqualTo(actionKey);
        assertThat(log.getCreateAt()).isEqualTo(createAt);
        assertThat(log.getCreateBy()).isEqualTo(createBy);
        assertThat(log.getObjectId()).isEqualTo(objectId);
        assertThat(log.getPartnerId()).isEqualTo(partnerId);
        assertThat(log.getParams()).isEqualTo(params);
        assertThat(log.getIsActive()).isEqualTo(isActive);
        assertThat(log.getIsDeleted()).isEqualTo(isDeleted);

        // Kiểm tra setter
        log.setIsActive(true);
        log.setIsDeleted(false);
        assertThat(log.getIsActive()).isTrue();
        assertThat(log.getIsDeleted()).isFalse();
    }

    @Test
    void testOnCreateSetsIdAndCreateAtWhenNull() {
        Logs log = new Logs();
        log.setId(null);
        log.setCreateAt(null);

        log.onCreate();

        assertThat(log.getId()).isNotNull();
        assertThat(log.getCreateAt()).isNotNull();
    }

    @Test
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        Logs log1 = new Logs();
        log1.setId(id);

        Logs log2 = new Logs();
        log2.setId(id);

        assertThat(log1).isEqualTo(log2);
        assertThat(log1.hashCode()).isEqualTo(log2.hashCode());
    }

    @Test
    void testToString_includesActionKeyAndParams() {
        Logs log = new Logs();
        log.setActionKey("ACTION");
        Map<String, Object> params = new HashMap<>();
        params.put("k", "v");
        log.setParams(params);

        String str = log.toString();
        assertThat(str).contains("ACTION");
        assertThat(str).contains("params");
    }

}

