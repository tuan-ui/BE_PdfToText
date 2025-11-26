package com.noffice.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContractTypeTest {
    @Test
    void dataAnnotation_Works() {
        ContractType contractType = new ContractType();
        contractType.setContractTypeCode("DOM001");
        contractType.setContractTypeName("Phòng Hành chính");
        contractType.setContractTypeDescription("Mô tả phòng ban");

        assertEquals("DOM001", contractType.getContractTypeCode());
        assertEquals("Phòng Hành chính", contractType.getContractTypeName());
    }

    @Test
    void constructors_Works() {
        ContractType empty = new ContractType();
        assertNotNull(empty);

        ContractType full = new ContractType("CODE123", "Tên ContractType", "Mô tả dài");
        assertEquals("CODE123", full.getContractTypeCode());
        assertEquals("Tên ContractType", full.getContractTypeName());
    }

    @Test
    void equalsHashCode_IgnoresBaseEntityFields() {
        ContractType d1 = new ContractType("ABC", "Tên", "Desc");
        ContractType d2 = new ContractType("ABC", "Tên", "Desc");

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void equals_DifferentCode_NotEqual() {
        ContractType d1 = new ContractType("CODE1", "Name", null);
        ContractType d2 = new ContractType("CODE2", "Name", null);

        assertNotEquals(d1, d2);
    }

    @Test
    void toString_NoStackOverflow() {
        ContractType contractType = new ContractType("TEST", "Test ContractType", "OK");
        String str = contractType.toString();

        assertTrue(str.contains("ContractType"));
        assertTrue(str.contains("contractTypeCode=TEST"));
        assertFalse(str.contains("BaseEntity"));
    }
}
