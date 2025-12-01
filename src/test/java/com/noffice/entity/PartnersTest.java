package com.noffice.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PartnersTest {

    @Test
    void testDefaultValues_andGettersSetters() {
        Partners p = new Partners();

        // thiết lập giá trị
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        p.setId(id);
        p.setPartnerCode("P001");
        p.setPartnerName("Partner One");
        p.setAddress("123 Street");
        p.setFax("0123456789");
        p.setEmail("email@example.com");
        p.setPhone("0123456789");
        p.setWebsite("https://partner.com");
        p.setImgLogo("logo.png");
        p.setTaxCode("TAX001");
        p.setIsActive(true);
        p.setIsDeleted(false);
        p.setCreateAt(now);
        p.setUpdateAt(now);

        // kiểm tra getter
        assertThat(p.getId()).isEqualTo(id);
        assertThat(p.getPartnerCode()).isEqualTo("P001");
        assertThat(p.getPartnerName()).isEqualTo("Partner One");
        assertThat(p.getAddress()).isEqualTo("123 Street");
        assertThat(p.getFax()).isEqualTo("0123456789");
        assertThat(p.getEmail()).isEqualTo("email@example.com");
        assertThat(p.getPhone()).isEqualTo("0123456789");
        assertThat(p.getWebsite()).isEqualTo("https://partner.com");
        assertThat(p.getImgLogo()).isEqualTo("logo.png");
        assertThat(p.getTaxCode()).isEqualTo("TAX001");
        assertThat(p.getIsActive()).isTrue();
        assertThat(p.getIsDeleted()).isFalse();
        assertThat(p.getCreateAt()).isEqualTo(now);
        assertThat(p.getUpdateAt()).isEqualTo(now);
    }

    @Test
    void testAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Partners p = new Partners();
        p.setId(id);
        p.setPartnerCode("P001");
        p.setPartnerName("Partner One");
        p.setAddress("123 Street");
        p.setFax("0123456789");
        p.setEmail("email@example.com");
        p.setPhone("0123456789");
        p.setWebsite("https://partner.com");
        p.setImgLogo("logo.png");
        p.setTaxCode("TAX001");
        p.setIsActive(true);
        p.setIsDeleted(false);
        p.setCreateAt(now);
        p.setUpdateAt(now);

        assertThat(p.getPartnerCode()).isEqualTo("P001");
        assertThat(p.getPartnerName()).isEqualTo("Partner One");
    }

    @Test
    void testToString_containsFields() {
        Partners p = new Partners();
        p.setPartnerCode("P001");
        p.setPartnerName("Partner One");

        String str = p.toString();
        assertThat(str).contains("P001");
        assertThat(str).contains("Partner One");
    }

    @Test
    void testClone_createsDeepCopyWithPartnerIdNull() {
        Partners original = new Partners();
        original.setPartnerCode("P001");
        original.setPartnerName("Partner One");
        original.setPartnerId(UUID.randomUUID());

        Partners cloned = original.clone();

        assertThat(cloned).isNotSameAs(original);

        assertThat(cloned.getPartnerCode()).isEqualTo(original.getPartnerCode());
        assertThat(cloned.getPartnerName()).isEqualTo(original.getPartnerName());

        assertThat(cloned.getPartnerId()).isNull();
    }
}
