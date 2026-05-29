package PetHotel.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import PetHotel.model.MaterialUsageConfirmRow;
import PetHotel.model.ServiceProductStandard;
import PetHotel.util.DBConnection;

public class ServiceProductStandardDAO {

    public List<ServiceProductStandard> findByServiceId(String serviceId) throws SQLException {
        List<ServiceProductStandard> list = new ArrayList<>();

        String sql =
            "SELECT sps.service_product_standard_id, sps.service_id, sps.product_id, " +
            "       p.product_name, sps.species, sps.min_weight_kg, sps.max_weight_kg, " +
            "       sps.usage_amount, sps.usage_unit, sps.note, sps.created_at, sps.updated_at " +
            "FROM service_product_standard sps " +
            "JOIN product p ON sps.product_id = p.product_id " +
            "WHERE sps.service_id = ? " +
            "ORDER BY sps.species, sps.min_weight_kg, p.product_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, serviceId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    public void insert(ServiceProductStandard sps) throws SQLException {
        String sql =
            "INSERT INTO service_product_standard ( " +
            " service_product_standard_id, service_id, product_id, species, " +
            " min_weight_kg, max_weight_kg, usage_amount, usage_unit, note, " +
            " created_at, updated_at " +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)";

        try (Connection conn = DBConnection.getConnection()) {
            sps.setServiceProductStandardId(generateNextId(conn));

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, sps.getServiceProductStandardId());
                ps.setString(2, sps.getServiceId());
                ps.setString(3, sps.getProductId());
                ps.setString(4, sps.getSpecies());
                ps.setBigDecimal(5, sps.getMinWeightKg());
                ps.setBigDecimal(6, sps.getMaxWeightKg());
                ps.setBigDecimal(7, sps.getUsageAmount());
                ps.setString(8, sps.getUsageUnit());
                ps.setString(9, sps.getNote());
                ps.executeUpdate();
            }
        }
    }

    public void update(ServiceProductStandard sps) throws SQLException {
        String sql =
            "UPDATE service_product_standard " +
            "SET product_id = ?, species = ?, min_weight_kg = ?, max_weight_kg = ?, " +
            "    usage_amount = ?, usage_unit = ?, note = ?, updated_at = SYSTIMESTAMP " +
            "WHERE service_product_standard_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sps.getProductId());
            ps.setString(2, sps.getSpecies());
            ps.setBigDecimal(3, sps.getMinWeightKg());
            ps.setBigDecimal(4, sps.getMaxWeightKg());
            ps.setBigDecimal(5, sps.getUsageAmount());
            ps.setString(6, sps.getUsageUnit());
            ps.setString(7, sps.getNote());
            ps.setString(8, sps.getServiceProductStandardId());

            ps.executeUpdate();
        }
    }

    public void delete(String serviceProductStandardId) throws SQLException {
        String sql =
            "DELETE FROM service_product_standard " +
            "WHERE service_product_standard_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, serviceProductStandardId);
            ps.executeUpdate();
        }
    }

    public List<MaterialUsageConfirmRow> findStandardsForBookingService(
            String bookingServiceId,
            String branchId
    ) throws SQLException {

        List<MaterialUsageConfirmRow> list = new ArrayList<>();

        String sql =
            "SELECT sps.product_id, " +
            "       p.product_name, " +
            "       sps.usage_amount, " +
            "       sps.usage_unit, " +
            "       sps.note, " +
            "       NVL(bi.quantity_in_stock, 0) AS inventory_quantity " +
            "FROM booking_services bs " +
            "JOIN pet pet ON bs.pet_id = pet.pet_id " +
            "JOIN service_product_standard sps " +
            "  ON sps.service_id = bs.service_id " +
            " AND sps.species = CASE " +
            "       WHEN UPPER(pet.species) IN ('DOG', 'CHÓ', 'CHO') THEN 'DOG' " +
            "       WHEN UPPER(pet.species) IN ('CAT', 'MÈO', 'MEO') THEN 'CAT' " +
            "       ELSE UPPER(pet.species) " +
            "     END " +
            " AND pet.weight_kg >= sps.min_weight_kg " +
            " AND pet.weight_kg <= sps.max_weight_kg " +
            "JOIN product p ON sps.product_id = p.product_id " +
            "LEFT JOIN branch_inventory bi " +
            "  ON bi.product_id = sps.product_id " +
            " AND bi.branch_id = ? " +
            "WHERE bs.booking_service_id = ? " +
            "ORDER BY p.product_name";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, branchId);
            ps.setString(2, bookingServiceId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MaterialUsageConfirmRow row = new MaterialUsageConfirmRow();

                    row.setProductId(rs.getString("product_id"));
                    row.setProductName(rs.getString("product_name"));

                    BigDecimal standardAmount = rs.getBigDecimal("usage_amount");
                    row.setStandardAmount(standardAmount);
                    row.setActualAmount(standardAmount);

                    row.setStandardUnit(rs.getString("usage_unit"));
                    row.setUsageUnit(rs.getString("usage_unit"));

                    row.setInventoryQuantity(rs.getBigDecimal("inventory_quantity"));
                    row.setNote(rs.getString("note"));

                    list.add(row);
                }
            }
        }

        return list;
    }

    private String generateNextId(Connection conn) throws SQLException {
        String sql =
            "SELECT NVL(MAX(TO_NUMBER(SUBSTR(service_product_standard_id, 4))), 0) + 1 AS next_id " +
            "FROM service_product_standard " +
            "WHERE REGEXP_LIKE(service_product_standard_id, '^SPS[0-9]{3}$')";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return String.format("SPS%03d", rs.getInt("next_id"));
            }
        }

        return "SPS001";
    }

    private ServiceProductStandard mapRow(ResultSet rs) throws SQLException {
        ServiceProductStandard sps = new ServiceProductStandard();

        sps.setServiceProductStandardId(rs.getString("service_product_standard_id"));
        sps.setServiceId(rs.getString("service_id"));
        sps.setProductId(rs.getString("product_id"));
        sps.setProductName(rs.getString("product_name"));
        sps.setSpecies(rs.getString("species"));
        sps.setMinWeightKg(rs.getBigDecimal("min_weight_kg"));
        sps.setMaxWeightKg(rs.getBigDecimal("max_weight_kg"));
        sps.setUsageAmount(rs.getBigDecimal("usage_amount"));
        sps.setUsageUnit(rs.getString("usage_unit"));
        sps.setNote(rs.getString("note"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            sps.setCreatedAt(created.toInstant().atOffset(java.time.ZoneOffset.UTC));
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            sps.setUpdatedAt(updated.toInstant().atOffset(java.time.ZoneOffset.UTC));
        }

        return sps;
    }
}