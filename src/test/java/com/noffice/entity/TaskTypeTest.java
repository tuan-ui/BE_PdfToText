package com.noffice.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskTypeTest {

    @Test
    void dataAnnotation_Works() {
        TaskType taskType = new TaskType();
        taskType.setTaskTypeCode("DOM001");
        taskType.setTaskTypeName("Phòng Hành chính");
        taskType.setTaskTypeDescription("Mô tả phòng ban");
        taskType.setTaskTypePriority(1);

        assertEquals("DOM001", taskType.getTaskTypeCode());
        assertEquals("Phòng Hành chính", taskType.getTaskTypeName());
        assertEquals(1, taskType.getTaskTypePriority());
    }

    @Test
    void constructors_Works() {
        TaskType empty = new TaskType();
        assertNotNull(empty);

        TaskType full = new TaskType("CODE123", "Tên TaskType", "Mô tả dài", 1);
        assertEquals("CODE123", full.getTaskTypeCode());
        assertEquals("Tên TaskType", full.getTaskTypeName());
        assertEquals(1, full.getTaskTypePriority());
    }

    @Test
    void equalsHashCode_IgnoresBaseEntityFields() {
        TaskType d1 = new TaskType("ABC", "Tên", "Desc", 1);
        TaskType d2 = new TaskType("ABC", "Tên", "Desc", 1);

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void equals_DifferentCode_NotEqual() {
        TaskType d1 = new TaskType("CODE1", "Name", null, 1);
        TaskType d2 = new TaskType("CODE2", "Name", null, 2);

        assertNotEquals(d1, d2);
    }

    @Test
    void toString_NoStackOverflow() {
        TaskType taskType = new TaskType("TEST", "Test TaskType", "OK", 1);
        String str = taskType.toString();

        assertTrue(str.contains("TaskType"));
        assertTrue(str.contains("taskTypeCode=TEST"));
        assertFalse(str.contains("BaseEntity"));
    }
}
