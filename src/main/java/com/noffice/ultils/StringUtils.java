package com.noffice.ultils;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.persistence.Column;

public final class StringUtils {
	/**
	 * escape special character in sql
	 *
	 * @param input input
	 * @return String
	 */
	public static String escapeSql(String input) {
		String result = input.trim().replace("/", "//").replace("_", "/_").replace("%", "/%");
		return result;
	}

	/**
	 * Get like string in sql
	 *
	 * @param content
	 * @return String content
	 */
	public static String toLikeAndLowerCaseString(String content) {
		return "%" + StringUtils.escapeSql(content.toLowerCase().trim()) + "%";
	}

	public static String toLikeAndLowerCase(String content) {
		return StringUtils.escapeSql(content.toLowerCase().trim());
	}

	public static String toLikeString(String content) {
		return "%" + StringUtils.escapeSql(content.trim()) + "%";
	}

	public static String unAccent(String s) {
		String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(temp).replaceAll("").replaceAll("Đ", "D").replaceAll("đ", "d");
	}

	/**
	 * @return Y2025/M04/D25: Year/Month/Day current
	 */
	public static String getCurrentDateFormatted() {
		LocalDate today = LocalDate.now();
		return String.format("Y%04d" + File.separator + "M%02d" + File.separator + "D%02d", today.getYear(),
				today.getMonthValue(), today.getDayOfMonth());
	}
	
	/**
	 * @param fileName
	 * @return fileName_UUID.[extension]
	 */
	public static String getUniqueFileName(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		String baseName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
		String extension = (dotIndex == -1) ? "" : fileName.substring(dotIndex);

		// Thêm UUID hoặc timestamp vào cuối tên file
		String uniqueName = baseName + "_" + UUID.randomUUID().toString() + extension;
		return uniqueName;
	}
	
	public static String convertNumberToVietnameseWords(long number) {
	    if (number == 0) return "Không";

	    String[] units = {"", "nghìn", "triệu", "tỷ"};
	    String[] numberWords = {
	        "không", "một", "hai", "ba", "bốn",
	        "năm", "sáu", "bảy", "tám", "chín"
	    };

	    StringBuilder result = new StringBuilder();
	    int unitIndex = 0;

	    while (number > 0) {
	        int group = (int)(number % 1000);
	        if (group != 0) {
	            String groupWords = convertThreeDigits(group, numberWords);
	            if (!groupWords.isEmpty()) {
	                result.insert(0, groupWords + " " + units[unitIndex] + " ");
	            }
	        } else if (unitIndex == 3 && result.length() > 0) {
	            result.insert(0, units[unitIndex] + " ");
	        }
	        number /= 1000;
	        unitIndex++;
	    }

	    String finalResult = result.toString().trim().replaceAll("\\s+", " ");
	    return Character.toUpperCase(finalResult.charAt(0)) + finalResult.substring(1);
	}
	
	public static String formatNumberWithDots(String number) {
	    if (number == null || number.isEmpty()) {
	        return "";
	    }
	    try {
	        double value = Double.parseDouble(number);
	        DecimalFormat formatter = new DecimalFormat("#,###,###.##");
	        String formatted = formatter.format(value).replace(",", ".");
	        if (formatted.endsWith(".00")) {
	            formatted = formatted.substring(0, formatted.length() - 3);
	        }
	        return formatted;
	    } catch (NumberFormatException e) {
	        return number;
	    }
	}

	private static String convertThreeDigits(int number, String[] words) {
	    int hundred = number / 100;
	    int tenUnit = number % 100;
	    int ten = tenUnit / 10;
	    int unit = tenUnit % 10;

	    StringBuilder result = new StringBuilder();

	    if (hundred > 0) {
	        result.append(words[hundred]).append(" trăm");
	    }

	    if (ten > 1) {
	        result.append(" ").append(words[ten]).append(" mươi");
	        if (unit == 1) result.append(" mốt");
	        else if (unit == 5) result.append(" lăm");
	        else if (unit > 0) result.append(" ").append(words[unit]);
	    } else if (ten == 1) {
	        result.append(" mười");
	        if (unit == 1) result.append(" một");
	        else if (unit == 5) result.append(" lăm");
	        else if (unit > 0) result.append(" ").append(words[unit]);
	    } else if (unit > 0) {
	        if (hundred > 0) result.append(" linh");
	        result.append(" ").append(words[unit]);
	    }

	    return result.toString().trim();
	}

	/**
     * Thêm timestamp vào tên file, trước phần mở rộng. Ví dụ:
     * "abc.pdf" → "abc_102205_07052025.pdf"
     * 
     * @param originalName tên file gốc (vd: abc.pdf)
     * @return tên file mới có kèm timestamp
     */
    public static String appendTimestampToFileName(String originalName) {
        if (originalName == null || originalName.isEmpty()) {
            return originalName;
        }

        int dotIndex = originalName.lastIndexOf('.');
        String baseName = (dotIndex != -1) ? originalName.substring(0, dotIndex) : originalName;
        String extension = (dotIndex != -1) ? originalName.substring(dotIndex) : "";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss_dd-MM-yyyy");
        String timestamp = LocalDateTime.now().format(formatter);

        return baseName + "_" + timestamp + extension;
    }
    
    public static Long safeParseLong(String value) {
        try {
            return (value != null) ? Long.parseLong(value) : 0L;
        } catch (NumberFormatException e) {
            System.err.println("Lỗi parse Long: " + value);
            return 0L;
        }
    }

	public static String normalizePartnerName(String name) {
		String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
		normalized = normalized.replaceAll("\\p{M}", ""); // loại bỏ dấu
		normalized = normalized.replaceAll("[^\\p{ASCII}]", ""); // loại bỏ ký tự unicode còn sót
		normalized = normalized.replaceAll("[^a-zA-Z0-9\\s]", ""); // bỏ ký tự đặc biệt
		normalized = normalized.replace("Đ", "D").replace("đ", "d"); // xử lý riêng cho Đ/đ
		normalized = normalized.trim().replaceAll("\\s+", ""); // loại bỏ khoảng trắng thừa
		normalized = normalized.toUpperCase(); // chuyển thành chữ hoa
		return normalized;
	}

	public static boolean getOperand(String expression, int number) {
		if(expression == null || expression.equals("")) return false;
		if(expression.contains(" or ")) {
			String[] parts = expression.split(" or ");
			if(parts.length == 2) {
				String expressionLeft = parts[0], expressionRight = parts[1];
				Boolean leftResult = executeExpression(expressionLeft, number);
				Boolean rightResult = executeExpression(expressionRight, number);
				return leftResult || rightResult;
			}
		}
		else if(expression.contains(" and ")) {
			String[] parts = expression.split(" and ");
			if(parts.length == 2) {
				String expressionLeft = parts[0], expressionRight = parts[1];
				Boolean leftResult = executeExpression(expressionLeft, number);
				Boolean rightResult = executeExpression(expressionRight, number);
				return leftResult && rightResult;
			}
		}
		return executeExpression(expression, number);
	}

	public static boolean getOperand(String expression, String value) {
		if(expression == null || expression.equals("")) return false;
		if(expression.contains(" or ")) {
			String[] parts = expression.split(" or ");
			if(parts.length == 2) {
				String expressionLeft = parts[0], expressionRight = parts[1];
				Boolean leftResult = executeExpression(expressionLeft, value);
				Boolean rightResult = executeExpression(expressionRight, value);
				return leftResult || rightResult;
			}
		}
		else if(expression.contains(" and ")) {
			String[] parts = expression.split(" and ");
			if(parts.length == 2) {
				String expressionLeft = parts[0], expressionRight = parts[1];
				Boolean leftResult = executeExpression(expressionLeft, value);
				Boolean rightResult = executeExpression(expressionRight, value);
				return leftResult && rightResult;
			}
		}
		return executeExpression(expression, value);
	}

	public static boolean executeExpression(String expression, int x) {
		expression = expression.replaceAll("\\s+", "");
		int xIndex = expression.indexOf("x");

		if (xIndex == -1) {
			throw new IllegalArgumentException("Expression must contain 'x'");
		}

		// Split into left and right of 'x'
		String leftExpr = expression.substring(0, xIndex);
		String rightExpr = expression.substring(xIndex + 1);

		Boolean left = evaluateComparison(leftExpr, x, true);
		Boolean right = evaluateComparison(rightExpr, x, false);

		if (left != null && right != null) return left && right;
		if (left != null) return left;
		return right != null ? right : false;
	}

	public static boolean executeExpression(String expression, String str) {
		expression = expression.replaceAll("\\s+", "");
		int xIndex = expression.indexOf("x");

		if (xIndex == -1) {
			throw new IllegalArgumentException("Expression must contain 'x'");
		}

		// Split into left and right of 'x'
		String leftExpr = expression.substring(0, xIndex);
		String rightExpr = expression.substring(xIndex + 1);

		Boolean left = evaluateComparison(leftExpr, str, true);
		Boolean right = evaluateComparison(rightExpr, str, false);

		if (left != null && right != null) return left && right;
		if (left != null) return left;
		return right != null ? right : false;
	}

	private static Boolean evaluateComparison(String expr, int x, boolean isLeft) {
		if (expr == null || expr.isEmpty()) return null;

		String operator = null;
		int value;

		// Check for operator and extract number
		if (expr.contains("<=")) operator = "<=";
		else if (expr.contains(">=")) operator = ">=";
		else if (expr.contains("<")) operator = "<";
		else if (expr.contains(">")) operator = ">";
		else if (expr.contains("==")) operator = "==";
		else if (expr.contains("!=")) operator = "!=";

		if (operator == null) return null;

		try {
			String numberStr = expr.replace(operator, "");
			value = Integer.parseInt(numberStr);
		} catch (NumberFormatException e) {
			return null;
		}

		return switch (operator) {
			case "<" -> isLeft ? value < x : x < value;
			case "<=" -> isLeft ? value <= x : x <= value;
			case ">" -> isLeft ? value > x : x > value;
			case ">=" -> isLeft ? value >= x : x >= value;
			case "==" -> isLeft ? value == x : x == value;
			case "!=" -> isLeft ? value != x : x != value;
			default -> null;
		};
	}

	private static Boolean evaluateComparison(String expr, String str, boolean isLeft) {
		if (expr == null || expr.isEmpty()) return null;

		String operator = null;
		String value;

		// Check for operator and extract number
		if (expr.contains("==")) operator = "==";

		if (operator == null) return null;

		try {
			String numberStr = expr.replace(operator, "");
			value = numberStr;
		} catch (NumberFormatException e) {
			return null;
		}

		return switch (operator) {
			case "==" -> isLeft ? value.equals(str) : str.equals(value);
			default -> null;
		};
	}
	public static String removeAccents(String value) {
		if (value == null || value.isEmpty()) {
			return "";
		}
		// Tách các ký tự có dấu thành ký tự gốc và dấu
		String temp = Normalizer.normalize(value, Normalizer.Form.NFD);
		// Pattern để tìm các ký tự dấu
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		// Thay thế ký tự dấu bằng chuỗi rỗng và chuyển sang chữ thường
		return pattern.matcher(temp)
				.replaceAll("")
				.toLowerCase()
				.replaceAll("đ", "d"); // Xử lý riêng chữ 'đ'
	}
	
	public static List<String> truncateFields(Object entity) {
	    List<String> all = new ArrayList<>();
	    all.addAll(truncateStringFields(entity));
	    all.addAll(truncateNumericFields(entity));
	    return all;
	}

	public static List<String> truncateStringFields(Object entity) {
	    List<String> truncatedFields = new ArrayList<>();
	    for (Field field : entity.getClass().getDeclaredFields()) {
	        field.setAccessible(true);
	        try {
	            if (field.getType().equals(String.class)) {
	                Column column = field.getAnnotation(Column.class);
	                if (column != null && column.length() > 0) {
	                    String value = (String) field.get(entity);
	                    if (value != null && value.length() > column.length()) {
	                        field.set(entity, value.substring(0, column.length()));
	                        truncatedFields.add(field.getName());
	                    }
	                }
	            }
	        } catch (IllegalAccessException e) {
				System.out.println("Error : " + e.getMessage());
	        }
	    }
	    return truncatedFields;
	}

	public static List<String> truncateNumericFields(Object entity) {
	    List<String> truncatedFields = new ArrayList<>();
	    for (Field field : entity.getClass().getDeclaredFields()) {
	        field.setAccessible(true);
	        try {
	            if (field.getType().equals(BigDecimal.class)) {
	                Column column = field.getAnnotation(Column.class);
	                if (column != null && column.precision() > 0 && column.scale() >= 0) {
	                    BigDecimal value = (BigDecimal) field.get(entity);
	                    if (value != null) {
	                        // Tổng số chữ số (precision) và số chữ số sau dấu thập phân (scale)
	                        int precision = column.precision();
	                        int scale = column.scale();

	                        // Làm tròn xuống nếu vượt quá scale
	                        BigDecimal rounded = value.setScale(scale, RoundingMode.DOWN);

	                        // Kiểm tra phần nguyên có vượt quá (precision - scale) không
	                        if (rounded.precision() - rounded.scale() > (precision - scale)) {
	                            // Cắt phần nguyên nếu vượt quá
	                            BigDecimal maxAllowed = BigDecimal.TEN.pow(precision - scale).subtract(BigDecimal.valueOf(0.01));
	                            field.set(entity, maxAllowed.setScale(scale, RoundingMode.DOWN));
	                        } else {
	                            field.set(entity, rounded);
	                        }

	                        // Ghi nhận nếu có điều chỉnh
	                        if (value.compareTo((BigDecimal) field.get(entity)) != 0) {
	                            truncatedFields.add(field.getName());
	                        }
	                    }
	                }
	            }
	        } catch (IllegalAccessException e) {
				System.out.println("Error : " + e.getMessage());
	        }
	    }
	    return truncatedFields;
	}

}