package com.noffice.ultils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;

import javax.xml.namespace.QName;

import com.noffice.entity.Attachs;
import com.noffice.entity.User;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.model.fields.merge.MailMerger;
import org.docx4j.model.fields.merge.MailMerger.OutputField;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import java.util.regex.Pattern;

import jakarta.xml.bind.JAXBElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

public class FileUtils {
	@Value("${save_path}")
	private String savePath;
	public static String convertDateToString(Date date, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		if (date == null) {
			return "";
		}
		return dateFormat.format(date);
	}
	private static final Pattern MERGEFIELD_PATTERN = Pattern.compile("MERGEFIELD\\s+([a-zA-Z0-9_]+)");

	public static boolean rowContainsMergeField(Tr row, String fieldName) {
		List<Object> texts = getAllElementFromObject(row, Text.class);
		for (Object obj : texts) {
			Text text = (Text) obj;
			if (text.getValue() != null && text.getValue().contains(fieldName)) {
				return true;
			}
		}
		return false;
	}

	public static void flattenFieldsInRow(Tr row) {
		List<Object> runs = getAllElementFromObject(row, R.class);
		for (Object r : runs) {
			R run = (R) r;
			run.getContent().removeIf(content -> content instanceof javax.xml.bind.JAXBElement
					&& (((javax.xml.bind.JAXBElement<?>) content).getName().getLocalPart().equals("fldChar")
							|| ((javax.xml.bind.JAXBElement<?>) content).getName().getLocalPart().equals("instrText")));
		}
	}

	public static void replaceMergeFieldsInRow(Tr row, Map<String, String> values) {
		 List<String> targetFields = Arrays.asList("dstc_cmnd", "dstc_noi_cap_cmnd", "dstc_cccd", "dstc_noi_cap_cccd");
		List<Object> texts = getAllElementFromObject(row, Text.class);
		for (Object obj : texts) {
			Text text = (Text) obj;
			for (Map.Entry<String, String> entry : values.entrySet()) {
				if (text.getValue() != null && text.getValue().contains(entry.getKey())) {
					text.setValue(entry.getValue());
				}
			}
		}
	}

	public static void replacePlaceholderInParagraph(P paragraph, String placeholder, String value) {
		List<Object> texts = getAllElementFromObject(paragraph, Text.class);
		for (Object obj : texts) {
			Text textElement = (Text) obj;
			if (textElement.getValue().contains(placeholder)) {
				textElement.setValue(textElement.getValue().replace(placeholder, value != null ? value : ""));
			}
		}
	}

	public static void replaceMergeFieldsInDocument(WordprocessingMLPackage wordMLPackage, Map<String, String> values) {
		List<Object> texts = getAllElementFromObject(wordMLPackage.getMainDocumentPart(), Text.class);

		for (Object obj : texts) {
			Text text = (Text) obj;
			for (Map.Entry<String, String> entry : values.entrySet()) {
				if (text.getValue() != null && text.getValue().contains(entry.getKey())) {
					text.setValue(entry.getValue());
				}
			}
		}
	}

	/**
	 * Escape các ký tự đặc biệt trong chuỗi để sử dụng với regex
	 */
	private static String escapeSpecialCharacters(String input) {
		// Thay thế các ký tự đặc biệt trong chuỗi để tránh lỗi regex
		return input.replaceAll("([\\\\{}()\\[\\].?*+^$|])", "\\\\$1");
	}

	public static List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
		List<Object> result = new ArrayList<>();
		if (obj == null || toSearch == null) {
			return result;
		}
		obj = unwrap(obj);
		if (obj == null) {
			return result;
		}

		if (toSearch.isAssignableFrom(obj.getClass())) {
			result.add(obj);
		} else if (obj instanceof ContentAccessor) {
			List<?> children = ((ContentAccessor) obj).getContent();
			for (Object child : children) {
				result.addAll(getAllElementFromObject(child, toSearch));
			}
		}
		return result;
	}

	private static Object unwrap(Object obj) {
		if (obj != null) {
			try {
				while (obj instanceof javax.xml.bind.JAXBElement) {
					obj = ((javax.xml.bind.JAXBElement<?>) obj).getValue();
				}
			} catch (Exception e) {
				System.out.println("Lỗi khi unwrap: " + e.getMessage());
				
			}
		}
		return obj;
	}

	public static boolean containsAnyMergeField(Tr row, List<String> fieldNames) {
		List<Object> texts = getAllElementFromObject(row, Text.class);
		for (Object obj : texts) {
			Text textElement = (Text) obj;
			String value = textElement.getValue();
			for (String fieldName : fieldNames) {
				if (value != null && value.contains(fieldName)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean containsAnyMergeField(Tc cell, List<String> fieldNames) {
		List<Object> texts = getAllElementFromObject(cell, Text.class);
		for (Object obj : texts) {
			Text textElement = (Text) obj;
			String value = textElement.getValue();
			for (String fieldName : fieldNames) {
				if (value != null && value.contains(fieldName)) {
					return true;
				}
			}
		}
		return false;
	}

//	public static void replaceMergeFieldWithParagraphs(String mergeFieldName, List<Object> newParagraphs,
//			WordprocessingMLPackage wordMLPackage) {
//		MainDocumentPart mainDoc = wordMLPackage.getMainDocumentPart();
//		List<Object> content = mainDoc.getContent();
//
//		for (int i = 0; i < content.size(); i++) {
//			Object obj = content.get(i);
//			if (obj instanceof P) {
//				P paragraph = (P) obj;
//				if (paragraphContainsMergeField(paragraph, mergeFieldName)) {
//					// Xóa mergefield
//					content.remove(i);
//					// Thêm đoạn mới tại đúng vị trí
//					content.addAll(i, newParagraphs);
//					return;
//				}
//			}
//		}
//	}

	private static boolean paragraphContainsMergeField(P paragraph, String fieldName) {
		List<Object> texts = getAllElementFromObject(paragraph, Text.class);
		for (Object obj : texts) {
			Text text = (Text) obj;
			if (text.getValue() != null && text.getValue().contains(fieldName)) {
				return true;
			}
		}
		return false;
	}

	// Helper method to replace placeholder with content
	public static void replacePlaceholderWithContent(WordprocessingMLPackage wordMLPackage, String placeholder,
			List<Object> content) throws Exception {
		MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
		List<Object> paragraphs = mainDocumentPart.getContent();

		for (int i = 0; i < paragraphs.size(); i++) {
			Object obj = paragraphs.get(i);
			if (obj instanceof P) {
				P paragraph = (P) obj;

				List<Object> texts = getAllElementFromObject(paragraph, Text.class);

				for (Object textObj : texts) {
					Text textElement = (Text) textObj;
					if (textElement.getValue().contains("«" + placeholder + "»")) {
						// Lưu lại style gốc của đoạn có chứa placeholder
						PPr originalPPr = paragraph.getPPr();

						// Áp dụng lại style gốc cho từng đoạn mới được chèn vào
						for (Object newObj : content) {
							if (newObj instanceof P) {
								P newPara = (P) newObj;
								if (originalPPr != null) {
									newPara.setPPr(XmlUtils.deepCopy(originalPPr));
								}
							}
						}

						// Thay thế đoạn
						paragraphs.remove(i);
						paragraphs.addAll(i, content);
						return;
					}
				}
			}
		}
	}

//	public static void replacePlaceholderWithAltChunk(WordprocessingMLPackage wordMLPackage, String placeholder,
//			List<ByteArrayOutputStream> byteArrayOutputs) throws Exception {
//
//		MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
//		List<Object> content = mainDocumentPart.getContent();
//
//		// Lấy tất cả các paragraph
//		List<Object> paragraphs = getAllElementFromObject(mainDocumentPart, P.class);
//
//		int index = 0;
//
//		for (Object paraObj : paragraphs) {
//			P paragraph = (P) paraObj;
//			String text = getTextFromParagraph(paragraph);
//
//			if (text != null && text.contains(placeholder)) {
//				int indexInContent = content.indexOf(XmlUtils.unwrap(paragraph));
//				if (indexInContent == -1)
//					continue;
//
//				// Xoá placeholder paragraph
//				content.remove(indexInContent);
//
//				for (ByteArrayOutputStream baos : byteArrayOutputs) {
//					// Tạo một part mới từ ByteArrayOutputStream
//					AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(
//							new PartName("/chunk" + index + ".docx"));
//					afiPart.setBinaryData(baos.toByteArray());
//					afiPart.setContentType(new ContentType(
//							"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"));
//
//					// Gắn phần chunk vào tài liệu chính
//					Relationship altChunkRel = mainDocumentPart.addTargetPart(afiPart);
//
//					// Tạo AltChunk element
//					CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk();
//					ac.setId(altChunkRel.getId());
//
//					javax.xml.bind.JAXBElement<CTAltChunk> altChunkElement = new javax.xml.bind.JAXBElement<>(
//							new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "altChunk"),
//							CTAltChunk.class, ac);
//
//					// Thêm AltChunk vào đúng vị trí
//					content.add(indexInContent++, altChunkElement);
//
//					index++;
//				}
//
//				break; // Chỉ thay thế placeholder đầu tiên tìm được
//			}
//		}
//	}
//
//	private static String getTextFromParagraph(P paragraph) {
//		StringBuilder sb = new StringBuilder();
//		List<Object> texts = getAllElementFromObject(paragraph, Text.class);
//		for (Object obj : texts) {
//			Text textElement = (Text) obj;
//			sb.append(textElement.getValue());
//		}
//		return sb.toString();
//	}

	public static void replaceTableRows(List<Object> rows, Tbl table, String keyField, List<String> fieldList,
			List<Map<String, String>> dataList) {
// Kiểm tra đầu vào
		if (rows == null || rows.isEmpty() || dataList == null || dataList.isEmpty()) {
			System.out.println("Rows hoặc dataList rỗng. Bỏ qua thay thế bảng cho keyField: " + keyField);
			return;
		}

// Kiểm tra table.getContent()
		if (table.getContent() == null || table.getContent().isEmpty()) {
			System.out.println("Nội dung bảng rỗng cho keyField: " + keyField);
			return;
		}

// Kiểm tra xem bảng có chứa keyField không
		boolean isTargetTable = rows.stream().map(rowObj -> (Tr) rowObj)
				.anyMatch(row -> FileUtils.rowContainsMergeField(row, keyField));

		if (!isTargetTable) {
			System.out.println("Bảng không chứa keyField: " + keyField);
			return;
		}

		int templateStartIndex = -1;
		int templateRowCount = 0;

// Tìm dòng mẫu
		for (int i = 0; i < rows.size(); i++) {
			Tr row = (Tr) rows.get(i);
			if (templateStartIndex == -1 && FileUtils.rowContainsMergeField(row, keyField)) {
				templateStartIndex = i;
			}
			if (templateStartIndex != -1) {
				if (FileUtils.containsAnyMergeField(row, fieldList)) {
					templateRowCount++;
				} else {
					break;
				}
			}
		}

// Kiểm tra templateStartIndex và templateRowCount
		if (templateStartIndex < 0 || templateRowCount <= 0) {
			System.out.println("Không tìm thấy dòng mẫu hợp lệ cho keyField: " + keyField);
			return;
		}

		List<Tr> newRows = new ArrayList<>();
		for (Map<String, String> person : dataList) {
			for (int i = 0; i < templateRowCount && (templateStartIndex + i) < rows.size(); i++) {
				Tr templateRow = (Tr) rows.get(templateStartIndex + i);

				if (FileUtils.rowContainsMergeField(templateRow, "dstc_cmnd")
						|| FileUtils.rowContainsMergeField(templateRow, "dstc_noi_cap_cmnd")) {

					String cccd = person.getOrDefault("dstc_cmnd", "").trim();
					String noiCap = person.getOrDefault("dstc_noi_cap_cmnd", "").trim();
					if (cccd.isEmpty() || noiCap.isEmpty())
						continue;
				}

				Tr newRow = XmlUtils.deepCopy(templateRow);
				FileUtils.replaceMergeFieldsInRow(newRow, person);
				FileUtils.flattenFieldsInRow(newRow);
				newRows.add(newRow);
			}
		}

// Ghi log trạng thái
		System.out.println("Rows size: " + rows.size());
		System.out.println("Table content size: " + table.getContent().size());
		System.out.println("templateStartIndex: " + templateStartIndex);
		System.out.println("templateRowCount: " + templateRowCount);
		System.out.println("newRows size: " + newRows.size());

// Xóa dòng mẫu
		for (int i = templateRowCount - 1; i >= 0; i--) {
			int indexToRemove = templateStartIndex + i;
			if (indexToRemove >= 0 && indexToRemove < table.getContent().size()) {
				table.getContent().remove(indexToRemove);
			} else {
				System.out.println("Chỉ số không hợp lệ để xóa: " + indexToRemove + " cho keyField: " + keyField);
			}
		}

// Nếu không có dòng mới nào -> Xóa toàn bộ bảng khỏi tài liệu
		if (newRows.isEmpty()) {
			System.out.println("Không có dòng mới được tạo. Tiến hành xóa toàn bộ bảng chứa keyField: " + keyField);
			Object parent = table.getParent();
			if (parent instanceof ContentAccessor) {
				((ContentAccessor) parent).getContent().remove(table);
			} else {
				System.out.println("Không thể xóa bảng vì parent không phải ContentAccessor: " + parent);
			}
			return;
		}

// Chèn các dòng mới
		if (templateStartIndex >= 0 && templateStartIndex <= table.getContent().size()) {
			table.getContent().addAll(templateStartIndex, newRows);
		} else {
			System.out.println("Không thể chèn dòng mới. templateStartIndex: " + templateStartIndex
					+ ", table content size: " + table.getContent().size() + ", newRows size: " + newRows.size());
		}
	}

	public static void replaceTableColumns(List<Object> rows, Tbl table, String keyField, List<String> fieldList,
			List<Map<String, String>> dataList) {
		if (rows == null || rows.isEmpty() || dataList == null || dataList.isEmpty()) {
			System.out.println("Dữ liệu rỗng, bỏ qua replace cột.");
			return;
		}

// Tìm dòng đầu tiên có chứa keyField
		int templateRowIndex = -1;
		Tr templateRow = null;

		for (int i = 0; i < rows.size(); i++) {
			Tr row = (Tr) rows.get(i);
			if (rowContainsMergeField(row, keyField)) {
				templateRowIndex = i;
				templateRow = row;
				break;
			}
		}

		if (templateRow == null) {
			System.out.println("Không tìm thấy dòng chứa keyField: " + keyField);
			return;
		}

// Tìm cell mẫu trong dòng đó
		Tc templateCell = null;
		for (Object cellObj : templateRow.getContent()) {
			Object realObj = XmlUtils.unwrap(cellObj);
			if (realObj instanceof Tc && containsAnyMergeField((Tc) realObj, fieldList)) {
				templateCell = (Tc) realObj;
				break;
			}
		}

		if (templateCell == null) {
			System.out.println("Không tìm thấy ô chứa keyField: " + keyField);
			return;
		}

// Tạo các cell mới và gắn vào dòng
		List<Tc> newCells = new ArrayList<>();
		for (Map<String, String> data : dataList) {
			Tc newCell = XmlUtils.deepCopy(templateCell);
			replaceMergeFieldsInCell(newCell, data);
			flattenFieldsInCell(newCell);
			newCells.add(newCell);
		}

// Xóa tất cả ô trong dòng mẫu và gắn lại các ô mới theo chiều ngang
		templateRow.getContent().clear();
		templateRow.getContent().addAll(newCells);

		System.out.println("Đã tạo " + newCells.size() + " cột từ dòng mẫu chứa keyField: " + keyField);
	}

	public static void replaceSingleFields(WordprocessingMLPackage wordMLPackage, Map<String, String> singleFields)
			throws Exception {
		Map<DataFieldName, String> fieldMap = new HashMap<>();
		for (Map.Entry<String, String> entry : singleFields.entrySet()) {
			fieldMap.put(new DataFieldName(entry.getKey()), entry.getValue());
		}
		MailMerger.setMERGEFIELDInOutput(OutputField.REMOVED);
		MailMerger.performMerge(wordMLPackage, fieldMap, true);
	}

	public static void replaceMergeFieldsInCell(Tc cell, Map<String, String> data) {
		List<Object> texts = getAllElements(cell, Text.class);
		for (Object obj : texts) {
			Text textElement = (Text) obj;
			String value = textElement.getValue();
			if (value != null && value.contains("«") && value.contains("»")) {
				for (Map.Entry<String, String> entry : data.entrySet()) {
					String field = "«" + entry.getKey() + "»";
					if (value.contains(field)) {
						value = value.replace(field, entry.getValue() != null ? entry.getValue() : "");
					}
				}
				textElement.setValue(value);
			}
		}
	}

	public static void flattenFieldsInCell(Tc cell) {
		// Với docx4j 3.2.1: chỉ cần giữ lại phần tử Text và xóa các phần tử không phải
		// Text
		List<Object> runs = getAllElements(cell, R.class);
		for (Object obj : runs) {
			R run = (R) obj;
			run.getContent().removeIf(content -> {
				Object unwrapped = XmlUtils.unwrap(content);
				// Xóa nếu không phải Text
				return !(unwrapped instanceof Text);
			});
		}
	}

	public static <T> List<Object> getAllElements(Object obj, Class<T> toSearch) {
		List<Object> result = new ArrayList<>();
		if (obj == null)
			return result;

		if (obj instanceof JAXBElement) {
			obj = ((JAXBElement<?>) obj).getValue();
		}

		if (toSearch.isAssignableFrom(obj.getClass())) {
			result.add(obj);
		}

		if (obj instanceof ContentAccessor) {
			List<?> children = ((ContentAccessor) obj).getContent();
			for (Object child : children) {
				result.addAll(getAllElements(child, toSearch));
			}
		}
		return result;
	}

	public static String removeVietnamese(String input) {
		if (input == null) return "";

		// B1: Loại bỏ dấu tiếng Việt
		String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

		// B2: Bỏ khoảng trắng
		normalized = normalized.replaceAll("\\s+", "");

		// B3: Loại bỏ ký tự đặc biệt, chỉ giữ chữ cái, số và dấu gạch dưới (_)
		return normalized.replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
	}
//	public static String generateNewPathQTD(String fullPath) {
//	    File file = new File(fullPath);
//	    String folderPath = file.getParent();
//	    String fileName = file.getName(); // VD: 1750913710553_Hợp đồng tín dụng_1.docx
//
//	    // Lấy phần tên sau dấu "_" đầu tiên (giữ lại đuôi)
//	    String baseFileName = fileName.substring(fileName.indexOf("_") + 1);
//
//	    // Đảm bảo có đuôi .doc hoặc .docx
//	    if (!baseFileName.toLowerCase().endsWith(".doc") && !baseFileName.toLowerCase().endsWith(".docx")) {
//	        baseFileName += ".docx";
//	    }
//
//	    String timestamp = convertDateToString(new Date(), "ddMMyyyy_HHmmssSS");
//	    String newFileName = timestamp + "_" + baseFileName;
//
//	    return folderPath + File.separator + newFileName;
//	}
	public static String getAllText(Object obj) {
		StringBuilder result = new StringBuilder();
		List<Object> texts = getAllElementFromObject(obj, Text.class);
		for (Object t : texts) {
			Text text = (Text) t;
			result.append(text.getValue());
		}
		return result.toString();
	}
	public static void replaceTextPlaceholdersInParagraphs(List<Object> paragraphs, Map<String, String> values) {
	    for (Object obj : paragraphs) {
	        if (obj instanceof P) {
	            P para = (P) obj;
	            List<Object> texts = getAllElementFromObject(para, Text.class);
	            for (Object t : texts) {
	                Text text = (Text) t;
	                String textValue = text.getValue();
	                if (textValue != null && textValue.contains("«")) {
	                    for (Map.Entry<String, String> entry : values.entrySet()) {
	                        String placeholder = "«" + entry.getKey() + "»";
	                        if (textValue.contains(placeholder)) {
	                            text.setValue(textValue.replace(placeholder, entry.getValue()));
	                        }
	                    }
	                }
	            }
	        }
	    }
	}
//	 public static void replaceMergeFieldsRecursive(List<Object> contents, Map<String, String> valuesMap) {
//	        for (Object obj : contents) {
//	            Object unwrapped = XmlUtils.unwrap(obj);
//
//	            if (unwrapped instanceof P) {
//	                P paragraph = (P) unwrapped;
//	                List<Object> runs = paragraph.getContent();
//
//	                for (int i = 0; i < runs.size(); i++) {
//	                    Object runObj = XmlUtils.unwrap(runs.get(i));
//	                    if (runObj instanceof R) {
//	                        R run = (R) runObj;
//	                        List<Object> texts = run.getContent();
//	                        for (Object t : texts) {
//	                            Object inner = XmlUtils.unwrap(t);
//	                            if (inner instanceof Text) {
//	                                Text text = (Text) inner;
//	                                String value = text.getValue();
//	                                if (value != null && value.contains("MERGEFIELD")) {
//	                                    Matcher matcher = MERGEFIELD_PATTERN.matcher(value);
//	                                    if (matcher.find()) {
//	                                        String fieldName = matcher.group(1);
//	                                        String replacement = valuesMap.getOrDefault(fieldName, "");
//
//	                                        // Tìm R tiếp theo chứa giá trị merge field để thay thế nội dung
//	                                        int nextIndex = i + 1;
//	                                        while (nextIndex < runs.size()) {
//	                                            Object nextObj = XmlUtils.unwrap(runs.get(nextIndex));
//	                                            if (nextObj instanceof R) {
//	                                                R nextRun = (R) nextObj;
//	                                                for (Object o : nextRun.getContent()) {
//	                                                    Object un = XmlUtils.unwrap(o);
//	                                                    if (un instanceof Text) {
//	                                                        ((Text) un).setValue(replacement);
//	                                                        break;
//	                                                    }
//	                                                }
//	                                                break;
//	                                            }
//	                                            nextIndex++;
//	                                        }
//	                                    }
//	                                }
//	                            }
//	                        }
//	                    }
//	                }
//	            } else if (unwrapped instanceof Tbl) {
//	                Tbl tbl = (Tbl) unwrapped;
//	                for (Object row : tbl.getContent()) {
//	                    Tr tr = (Tr) XmlUtils.unwrap(row);
//	                    for (Object cell : tr.getContent()) {
//	                        Tc tc = (Tc) XmlUtils.unwrap(cell);
//	                        replaceMergeFieldsRecursive(tc.getContent(), valuesMap);
//	                    }
//	                }
//	            }
//	        }
//	    }

	public static List<Attachs> saveFile(MultipartFile[] files, User token) throws IOException {
		if (files == null || files.length == 0) {
			throw new IOException("Files are empty");
		}
		String savePath = AppConfig.get("save_path");

		LocalDate today = LocalDate.now();
		String year = String.valueOf(today.getYear());
		String month = String.format("%02d", today.getMonthValue());
		String day  = String.format("%02d", today.getDayOfMonth());

		Path targetDir = Paths.get(savePath, "uploads", year, month, day);
		Files.createDirectories(targetDir);

		List<Attachs> lstAttach = new ArrayList<>();

		for (MultipartFile file : files) {

			String originalFilename = file.getOriginalFilename();
			String extension = "";

			if (originalFilename != null && originalFilename.contains(".")) {
				extension = originalFilename.substring(originalFilename.lastIndexOf("."));
			}

			String newFileName = UUID.randomUUID().toString() + extension;

			Path targetPath = targetDir.resolve(newFileName);

			Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
			String relativeFolder = String.join("/", "/uploads", year, month, day);
			String relativeFilePath = relativeFolder + "/" + newFileName;
			Attachs attach = new Attachs();
			attach.setAttachName(originalFilename);
			attach.setSavePath("save_path");
			attach.setAttachPath(relativeFilePath);
			attach.setDateCreate(LocalDateTime.now());
			attach.setIsActive(true);
			lstAttach.add(attach);
		}


		return lstAttach;
	}

	public static String removeAccent(String text) {
		if (text == null) return null;
		String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
		return normalized.replaceAll("\\p{M}", "");
	}

}