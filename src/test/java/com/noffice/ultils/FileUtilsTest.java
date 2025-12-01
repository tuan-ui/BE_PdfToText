package com.noffice.ultils;

import com.noffice.entity.Attachs;
import com.noffice.entity.User;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    private WordprocessingMLPackage wordMLPackage;
    private MainDocumentPart mainDocumentPart;

    @BeforeEach
    void setUp() throws Exception {
        wordMLPackage = WordprocessingMLPackage.createPackage();
        mainDocumentPart = wordMLPackage.getMainDocumentPart();
    }

    @Test
    void removeVietnamese_RemovesAccentsAndSpecialChars() {
        assertEquals("lethaianh123", FileUtils.removeVietnamese("Lê Thái Anh!@# 123"));
        assertEquals("nofficeeptrai", FileUtils.removeVietnamese("NOffice Đẹp Trai"));
        assertEquals("", FileUtils.removeVietnamese(null));
        assertEquals("abc123", FileUtils.removeVietnamese("ábć123!@#"));
    }

    @Test
    void replacePlaceholderInParagraph_ReplacesCorrectly() {
        P paragraph = createParagraphWithText("Xin chào «ten» và «tuoi» tuổi");
        FileUtils.replacePlaceholderInParagraph(paragraph, "«ten»", "Lê Thái Anh");
        FileUtils.replacePlaceholderInParagraph(paragraph, "«tuoi»", "30");

        String result = getParagraphText(paragraph);
        assertEquals("Xin chào Lê Thái Anh và 30 tuổi", result);
    }

    @Test
    void replaceMergeFieldsInDocument_ReplacesAllFields() throws Exception {

        mainDocumentPart.getContent().add(createParagraph("Họ tên: «ho_ten»"));
        mainDocumentPart.getContent().add(createParagraph("Địa chỉ: «dia_chi»"));

        Map<String, String> values = Map.of(
                "ho_ten", "Nguyễn Văn A",
                "dia_chi", "Hà Nội"
        );

        FileUtils.replaceMergeFieldsInDocument(wordMLPackage, values);

        String fullText = mainDocumentPart.getContent().stream()
                .filter(o -> o instanceof P)
                .map(p -> getParagraphText((P) p))
                .reduce("", (a, b) -> a + " " + b);

        assertTrue(fullText.contains("Nguyễn Văn A"));
        assertTrue(fullText.contains("Hà Nội"));
    }

    @Test
    void containsAnyMergeField_InRow_ReturnsTrue() {
        Tr row = createTableRow("«dstc_cmnd»", "«dstc_noi_cap_cmnd»");
        List<String> fields = Arrays.asList("dstc_cmnd", "dstc_noi_cap_cmnd");

        assertTrue(FileUtils.containsAnyMergeField(row, fields));
    }

    @Test
    void saveFile_SavesFilesCorrectly(@TempDir Path tempDir) throws Exception {
        // Setup mock save_path
        try (MockedStatic<AppConfig> appConfig = Mockito.mockStatic(AppConfig.class)) {
            appConfig.when(() -> AppConfig.get("save_path")).thenReturn(tempDir.toString());

            MultipartFile file = new MockMultipartFile(
                    "file", "contract.docx", "application/vnd.openxmlformats", "test content".getBytes());

            User user = new User();
            List<Attachs> result = FileUtils.saveFile(new MultipartFile[]{file}, user);

            assertEquals(1, result.size());
            Attachs attach = result.get(0);
            assertEquals("contract.docx", attach.getAttachName());
            assertTrue(attach.getAttachPath().startsWith("/uploads/"));
            assertTrue(attach.getAttachPath().endsWith(".docx"));
            assertNotNull(attach.getDateCreate());
            assertTrue(attach.getIsActive());

            // Kiểm tra thư mục đã được tạo
            LocalDate today = LocalDate.now();
            Path expectedDir = tempDir.resolve("uploads")
                    .resolve(String.valueOf(today.getYear()))
                    .resolve(String.format("%02d", today.getMonthValue()))
                    .resolve(String.format("%02d", today.getDayOfMonth()));
            assertTrue(Files.exists(expectedDir));

            // Kiểm tra file đã được copy
            Path savedFile = expectedDir.resolve(attach.getAttachPath().substring(attach.getAttachPath().lastIndexOf("/") + 1));
            assertTrue(Files.exists(savedFile));
        }
    }

    @Test
    void replaceTableRows_ReplacesWithMultipleRowsAndSkipEmptyCMND() throws Exception {
        Tbl table = createSampleTableWithTemplateRow();
        mainDocumentPart.getContent().add(table);
        List<Object> rows = table.getContent();

        List<String> fieldList = Arrays.asList("dstc_cmnd", "dstc_ho_ten", "dstc_ngay_sinh");
        List<Map<String, String>> dataList = Arrays.asList(
                Map.of("dstc_cmnd", "123456789", "dstc_ho_ten", "Nguyễn Văn A", "dstc_ngay_sinh", "01/01/1990"),
                Map.of("dstc_cmnd", "", "dstc_ho_ten", "Trần Thị B", "dstc_noi_cap_cmnd", ""), // bị skip
                Map.of("dstc_cmnd", "987654321", "dstc_ho_ten", "Lê Văn C", "dstc_ngay_sinh", "02/02/2000")
        );

        FileUtils.replaceTableRows(rows, table, "dstc_cmnd", fieldList, dataList);

        long actualRows = table.getContent().stream().filter(o -> o instanceof Tr).count();
        assertEquals(1, actualRows);

    }

    @Test
    void replacePlaceholderWithContent_ReplacesWithMultipleParagraphs() throws Exception {
        P placeholderParagraph = Context.getWmlObjectFactory().createP();
        R run = Context.getWmlObjectFactory().createR();
        Text text = Context.getWmlObjectFactory().createText();
        text.setValue("Thông tin cá nhân: «THONG_TIN_CA_NHAN»");
        run.getContent().add(text);
        mainDocumentPart.getContent().add(placeholderParagraph);
        placeholderParagraph.getContent().add(run);

        List<Object> newContent = Arrays.asList(
                createParagraph("Họ và tên: Nguyễn Văn A"),
                createParagraph("Ngày sinh: 01/01/1990"),
                createParagraph("Địa chỉ: Hà Nội")
        );

        FileUtils.replacePlaceholderWithContent(wordMLPackage, "THONG_TIN_CA_NHAN", newContent);

        List<P> paragraphs = mainDocumentPart.getJaxbElement().getBody().getContent().stream()
                .filter(o -> o instanceof P)
                .map(o -> (P) o)
                .toList();

        assertEquals(3, paragraphs.size());
        assertEquals("Họ và tên: Nguyễn Văn A", getParagraphText(paragraphs.get(0)));
        assertEquals("Địa chỉ: Hà Nội", getParagraphText(paragraphs.get(2)));
    }

    // === Helper Methods ===

    private P createParagraph(String text) {
        P p = Context.getWmlObjectFactory().createP();
        R run = Context.getWmlObjectFactory().createR();
        Text t = Context.getWmlObjectFactory().createText();
        t.setValue(text);
        run.getContent().add(t);
        p.getContent().add(run);
        return p;
    }

    private P createParagraphWithText(String text) {
        P p = createParagraph(text);
        mainDocumentPart.getContent().add(p);
        return p;
    }

    private String getParagraphText(P paragraph) {
        return paragraph.getContent().stream()
                .filter(o -> o instanceof R)
                .flatMap(r -> ((R) r).getContent().stream())
                .filter(o -> o instanceof Text)
                .map(o -> ((Text) o).getValue())
                .reduce("", String::concat);
    }

    private Tr createTableRow(String... cellTexts) {
        Tr row = Context.getWmlObjectFactory().createTr();
        for (String text : cellTexts) {
            Tc cell = Context.getWmlObjectFactory().createTc();
            P p = createParagraph(text);
            cell.getContent().add(p);
            row.getContent().add(cell);
        }
        return row;
    }

    private Tbl createSampleTableWithTemplateRow() {
        Tbl table = Context.getWmlObjectFactory().createTbl();
        table.getContent().add(createTableRow("STT", "Họ tên", "CMND", "Ngày sinh"));
        table.getContent().add(createTableRow("1", "«dstc_ho_ten»", "«dstc_cmnd»", "«dstc_ngay_sinh»"));
        return table;
    }

    @Test
    void removeAccent_RemovesVietnameseAccents() {
        assertEquals("Le Thai Anh", FileUtils.removeAccent("Lê Thái Anh"));
        assertEquals("HO CHI MINH", FileUtils.removeAccent("HỒ CHÍ MINH"));
        assertEquals("Toi yeu Viet Nam", FileUtils.removeAccent("Tôi yêu Việt Nam"));
        assertNull(FileUtils.removeAccent(null));
    }

    @Test
    void replaceTextPlaceholdersInParagraphs_ReplacesMultiple() {
        mainDocumentPart.getContent().add(createParagraph("Họ tên: «ho_ten»"));
        mainDocumentPart.getContent().add(createParagraph("Địa chỉ: «dia_chi» "));

        Map<String, String> values = Map.of(
                "ho_ten", "Nguyễn Văn A",
                "dia_chi", "Hà Nội"
        );

        List<Object> paragraphs = mainDocumentPart.getContent();
        FileUtils.replaceTextPlaceholdersInParagraphs(paragraphs, values);

        String text = getAllTextFromDocument();
        assertTrue(text.contains("Nguyễn Văn A"));
        assertTrue(text.contains("Hà Nội"));
        assertFalse(text.contains("«ho_ten»"));
    }

    @Test
    void replaceSingleFields_UsesMailMerger_Correctly() throws Exception {
        WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
        P paragraph = Context.getWmlObjectFactory().createP();
        addTextRun(paragraph, "Họ tên: ");
        addMergeFieldRun(paragraph, "ho_ten");
        addTextRun(paragraph, " - Tuổi: ");
        addMergeFieldRun(paragraph, "tuoi");
        pkg.getMainDocumentPart().getContent().add(paragraph);
        Map<String, String> fields = Map.of(
                "ho_ten", "Nguyễn Văn A",
                "tuoi", "30"
        );
        FileUtils.replaceSingleFields(pkg, fields);
        String text = FileUtils.getAllText(pkg.getMainDocumentPart()).trim();

        assertEquals("Họ tên: Nguyễn Văn A - Tuổi: 30", text);
        assertFalse(text.contains("MERGEFIELD"));
    }

    @Test
    void replaceAndFlattenCell_WorksTogether() {
        Tc cell = Context.getWmlObjectFactory().createTc();
        P p = createParagraph("Họ tên: «ho_ten», Tuổi: «tuoi»");
        cell.getContent().add(p);

        Map<String, String> data = Map.of("ho_ten", "Lê Văn C", "tuoi", "35");

        FileUtils.replaceMergeFieldsInCell(cell, data);
        FileUtils.flattenFieldsInCell(cell);

        String text = FileUtils.getAllText(cell);
        assertEquals("Họ tên: Lê Văn C, Tuổi: 35", text.trim());
    }

    // === Helper Methods ===

    private String getAllTextFromDocument() {
        return mainDocumentPart.getContent().stream()
                .map(FileUtils::getAllText)
                .reduce("", String::concat);
    }

    private static final ObjectFactory factory = Context.getWmlObjectFactory();

    private void addTextRun(P paragraph, String text) {
        R run = factory.createR();
        Text t = factory.createText();
        t.setValue(text);
        t.setSpace("preserve");
        run.getContent().add(t);
        paragraph.getContent().add(run);
    }

    private void addMergeFieldRun(P paragraph, String fieldName) {
        // 1. begin
        R begin = factory.createR();
        FldChar fBegin = factory.createFldChar();
        fBegin.setFldCharType(STFldCharType.BEGIN);
        begin.getContent().add(factory.createRFldChar(fBegin));
        paragraph.getContent().add(begin);

        // 2. instrText
        R instr = factory.createR();
        Text tInstr = factory.createText();
        tInstr.setValue(" MERGEFIELD " + fieldName + " ");
        tInstr.setSpace("preserve");
        instr.getContent().add(tInstr);
        paragraph.getContent().add(instr);

        // 3. separate
        R separate = factory.createR();
        FldChar fSep = factory.createFldChar();
        fSep.setFldCharType(STFldCharType.SEPARATE);
        separate.getContent().add(factory.createRFldChar(fSep));
        paragraph.getContent().add(separate);

        // 4. placeholder (MailMerger sẽ thay chỗ này)
        R placeholder = factory.createR();
        Text tPlace = factory.createText();
        tPlace.setValue("«" + fieldName + "»");
        tPlace.setSpace("preserve");
        placeholder.getContent().add(tPlace);
        paragraph.getContent().add(placeholder);

        // 5. end
        R end = factory.createR();
        FldChar fEnd = factory.createFldChar();
        fEnd.setFldCharType(STFldCharType.END);
        end.getContent().add(factory.createRFldChar(fEnd));
        paragraph.getContent().add(end);
    }

}