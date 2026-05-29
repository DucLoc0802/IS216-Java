package PetHotel.bus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import PetHotel.model.AuditLog;

public class AuditLogLocalService {

    private static final String FILE_NAME = "audit_logs.json";

    // Ghi log đồng bộ để tránh race conditions trong desktop app
    public static synchronized void log(String employeeId, String employeeName, String action, String details) {
        try {
            String logId = "LOG" + String.format("%06d", (int)(Math.random() * 1000000));
            AuditLog log = new AuditLog(logId, employeeId, employeeName, action, details, OffsetDateTime.now());

            String jsonLine = String.format(
                "{\"logId\":\"%s\",\"employeeId\":\"%s\",\"employeeName\":\"%s\",\"action\":\"%s\",\"details\":\"%s\",\"createdAt\":\"%s\"}",
                escapeJson(log.getLogId()),
                escapeJson(log.getEmployeeId()),
                escapeJson(log.getEmployeeName()),
                escapeJson(log.getAction()),
                escapeJson(log.getDetails()),
                log.getCreatedAt().toString()
            );

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(FILE_NAME, true), StandardCharsets.UTF_8))) {
                writer.write(jsonLine);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Không thể ghi log cục bộ: " + e.getMessage());
        }
    }

    public static synchronized List<AuditLog> getAllLogs() {
        List<AuditLog> list = new ArrayList<>();
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                AuditLog log = parseJsonLine(line);
                if (log != null) {
                    list.add(log);
                }
            }
        } catch (IOException e) {
            System.err.println("Không thể đọc log cục bộ: " + e.getMessage());
        }

        // Đảo ngược danh sách để log mới nhất hiển thị lên đầu
        Collections.reverse(list);
        return list;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static AuditLog parseJsonLine(String line) {
        try {
            String logId = getJsonField(line, "logId");
            String employeeId = getJsonField(line, "employeeId");
            String employeeName = getJsonField(line, "employeeName");
            String action = getJsonField(line, "action");
            String details = getJsonField(line, "details");
            String createdAtStr = getJsonField(line, "createdAt");

            OffsetDateTime createdAt = OffsetDateTime.parse(createdAtStr);
            return new AuditLog(logId, employeeId, employeeName, action, details, createdAt);
        } catch (Exception e) {
            return null; // bỏ qua dòng lỗi
        }
    }

    private static String getJsonField(String line, String fieldName) {
        String keyPattern = "\"" + fieldName + "\":\"";
        int index = line.indexOf(keyPattern);
        if (index == -1) return "";
        int start = index + keyPattern.length();
        int end = line.indexOf("\"", start);
        if (end == -1) return "";
        String val = line.substring(start, end);
        return val.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r");
    }
}
