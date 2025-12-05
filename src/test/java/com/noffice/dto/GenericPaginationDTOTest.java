package com.noffice.dto;

import org.apache.poi.ss.formula.functions.T;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenericPaginationDTOTest {

    @Test
    void testGetterSetterEqualsHashCodeToString() {
        long totals = 1;
        List<T> datas = List.of();

        // Test NoArgsConstructor + Setter
        GenericPaginationDTO<T> dto = new GenericPaginationDTO<T>();
        dto.setTotals(totals);
        dto.setDatas(datas);

        // Test Getter
        assertEquals(totals, dto.getTotals());
        assertEquals(datas, dto.getDatas());

        // Test AllArgsConstructor
        GenericPaginationDTO<T> dto2 = new GenericPaginationDTO<T>(totals, datas);
        assertEquals(totals, dto2.getTotals());
        assertEquals(datas, dto2.getDatas());

    }
}
