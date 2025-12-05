package com.noffice.entity;

import com.noffice.ultils.Constants;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NodeDeptUserTest {

    @Test
    void testNoArgsConstructor() {
        NodeDeptUser node = new NodeDeptUser();
        assertNull(node.getDocId());
        assertNull(node.getStep());
        assertNull(node.getId());
    }

    @Test
    void testAllArgsConstructor() {
        UUID docId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID deptId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        NodeDeptUser node = new NodeDeptUser(
                docId, "APPROVE", userId, deptId, "Phòng Kế toán",
                roleId, "SERIAL", "Ghi chú", 2
        );

        assertEquals(docId, node.getDocId());
        assertEquals("APPROVE", node.getStep());
        assertEquals(userId, node.getUserId());
        assertEquals(deptId, node.getDeptId());
        assertEquals("Phòng Kế toán", node.getDeptName());
        assertEquals(roleId, node.getRoleId());
        assertEquals("SERIAL", node.getApproveType());
        assertEquals("Ghi chú", node.getNote());
        assertEquals(2, node.getIndex());
    }

    @Test
    void testSettersAndGetters() {
        NodeDeptUser node = new NodeDeptUser();

        UUID docId = UUID.randomUUID();
        node.setDocId(docId);
        node.setStep("REVIEW");
        node.setUserId(UUID.randomUUID());
        node.setDeptId(UUID.randomUUID());
        node.setDeptName("IT Department");
        node.setRoleId(UUID.randomUUID());
        node.setApproveType("PARALLEL");
        node.setNote("Urgent approval");
        node.setIndex(5);

        assertEquals(docId, node.getDocId());
        assertEquals("REVIEW", node.getStep());
        assertEquals("IT Department", node.getDeptName());
        assertEquals("PARALLEL", node.getApproveType());
        assertEquals("Urgent approval", node.getNote());
        assertEquals(5, node.getIndex());
    }

    @Test
    void testEqualsAndHashCode_SameObject() {
        UUID id = UUID.randomUUID();
        NodeDeptUser node1 = new NodeDeptUser();
        node1.setId(id);

        NodeDeptUser node2 = new NodeDeptUser();
        node2.setId(id);

        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());
    }

    @Test
    void testEqualsAndHashCode_DifferentId() {
        UUID id = UUID.randomUUID();
        NodeDeptUser node1 = new NodeDeptUser();
        node1.setDocId(id);
        node1.setStep("REVIEW");

        NodeDeptUser node2 = new NodeDeptUser();
        node2.setDocId(id);
        node2.setStep("APPROVE");

        assertNotEquals(node1, node2);
        assertNotEquals(node1.hashCode(), node2.hashCode());
    }

    @Test
    void testEquals_NullAndDifferentClass() {
        NodeDeptUser node = new NodeDeptUser();
        node.setId(UUID.randomUUID());

        assertNotEquals(null, node);
        assertNotEquals("Not a NodeDeptUser", node);
    }

    @Test
    void testToString() {
        UUID docId = UUID.randomUUID();
        NodeDeptUser node = new NodeDeptUser();
        node.setId(UUID.randomUUID());
        node.setDocId(docId);
        node.setStep("APPROVE");
        node.setIndex(1);

        String str = node.toString();
        assertTrue(str.contains(docId.toString()));
        assertTrue(str.contains("APPROVE"));
        assertTrue(str.contains("index=1"));
    }

    @Test
    void testInheritance_BaseEntityFields() {
        UUID id = UUID.randomUUID();
        NodeDeptUser node = new NodeDeptUser();
        node.setCreateAt(LocalDateTime.now());
        node.setCreateBy(id);
        node.setIsDeleted(Constants.isDeleted.DELETED);

        assertNotNull(node.getCreateAt());
        assertEquals(id, node.getCreateBy());
        assertEquals(Constants.isDeleted.DELETED, node.getIsDeleted());
    }
}