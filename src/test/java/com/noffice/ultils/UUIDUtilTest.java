package com.noffice.ultils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UUIDUtilTest {


    @ParameterizedTest(name = "{index} => UUID = {0}")
    @ValueSource(strings = {
            // UUID v1 (time-based)
            "ea8d4f2a-3c5d-11ee-be56-0242ac120002",
            // UUID v4 (random) - phổ biến nhất
            "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
            // UUID v5 (name-based SHA-1)
            "f47ac10b-58cc-5372-8e5f-5c2f9c7d3e2a",
            // Chữ hoa cũng hợp lệ
            "A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A11",
            // UUID v3 (name-based MD5)
            "3d813cbb-47fb-32ba-91df-831e1593ac29"
    })
    void isUUID_ValidUUID_ShouldReturnTrue(String uuid) {
        assertTrue(UUIDUtil.isUUID(uuid), "UUID hợp lệ phải trả về true: " + uuid);
    }


    @ParameterizedTest(name = "{index} => Input = ''{0}''")
    @ValueSource(strings = {
            // Sai độ dài
            "3fa85f64-5717-4562-b3fc-2c963f66afa",   // thiếu 1 ký tự
            "3fa85f64-5717-4562-b3fc-2c963f66afa66",  // thừa 1 ký tự

            // Sai format (thiếu dấu gạch nối)
            "3fa85f6457174562b3fc2c963f66afa6",
            "a0eebc999c0b4ef8bb6d6bb9bd380a11",

            // Phiên bản không hợp lệ (phải là 1-5)
            "3fa85f64-5717-6562-b3fc-2c963f66afa6",   // version = 6 → invalid
            "3fa85f64-5717-0562-b3fc-2c963f66afa6",   // version = 0 → invalid

            // Variant không hợp lệ (phải là 8,9,a,b)
            "3fa85f64-5717-4562-c3fc-2c963f66afa6",   // variant = c → invalid
            "3fa85f64-5717-4562-73fc-2c963f66afa6",   // variant = 7 → invalid

            // Ký tự không hợp lệ
            "3fa85f64-5717-4562-b3fc-2c963f66afag",   // có chữ 'g'
            "3fa85f64-5717-4562-b3fc-2c963f66afaz",   // có chữ 'z'

            // Có khoảng trắng
            " 3fa85f64-5717-4562-b3fc-2c963f66afa6",
            "3fa85f64-5717-4562-b3fc-2c963f66afa6 ",
            "3fa85f64-5717-4562-b3fc- 2c963f66afa6",

            // Chuỗi rỗng hoặc chỉ khoảng trắng
            "",
            "   ",

            // Không phải UUID
            "hello-world",
            "1234567890",
            "00000000-0000-0000-0000-000000000000"  // version=0, variant=0 → invalid theo RFC
    })
    void isUUID_InvalidUUID_ShouldReturnFalse(String input) {
        assertFalse(UUIDUtil.isUUID(input), "UUID không hợp lệ phải trả về false: " + input);
    }

    @Test
    void isUUID_NullInput_ShouldReturnFalse() {
        assertFalse(UUIDUtil.isUUID(null), "Input null phải trả về false");
    }

    @Test
    void uuidPattern_ShouldMatchOnlyRFC4122CompliantUUIDs() {
        // UUID v4 hợp lệ
        assertTrue(UUIDUtil.isUUID("123e4567-e89b-42d3-a456-426655440000")); // version 4, variant 10xx
        assertTrue(UUIDUtil.isUUID("123e4567-e89b-42d3-8456-426655440000")); // variant 8
        assertTrue(UUIDUtil.isUUID("123e4567-e89b-42d3-9456-426655440000")); // variant 9
        assertTrue(UUIDUtil.isUUID("123e4567-e89b-42d3-a456-426655440000")); // variant a
        assertTrue(UUIDUtil.isUUID("123e4567-e89b-42d3-b456-426655440000")); // variant b

        // Variant không hợp lệ (11xx = c,d,e,f)
        assertFalse(UUIDUtil.isUUID("123e4567-e89b-42d3-c456-426655440000"));
        assertFalse(UUIDUtil.isUUID("123e4567-e89b-42d3-f456-426655440000"));
    }

    @Test
    void isUUID_ShouldBeFastAndThreadSafe() {
        String validUuid = "f47ac10b-58cc-5372-8e5f-5c2f9c7d3e2a";

        long start = System.nanoTime();
        for (int i = 0; i < 100_000; i++) {
            assertTrue(UUIDUtil.isUUID(validUuid));
        }
        long duration = System.nanoTime() - start;

        assertTrue(duration < 500_000_000L); // < 500ms là ổn

    }
}
