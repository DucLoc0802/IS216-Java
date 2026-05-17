package PetHotel.model;

import java.time.OffsetDateTime;

/**
 * ServiceCategory — Ánh xạ bảng CATEGORY_SERVICES.
 *
 * Schema:
 *   service_category_id   VARCHAR2(10)    PK
 *   category_name         NVARCHAR2(80)   NOT NULL, UNIQUE
 *   note                  CLOB
 *   created_at            TIMESTAMP(6) WITH TIME ZONE
 *   updated_at            TIMESTAMP(6) WITH TIME ZONE
 */
public class ServiceCategory {

    private String serviceCategoryId;
    private String categoryName;
    private String note;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────

    public ServiceCategory() {}

    public ServiceCategory(String serviceCategoryId, String categoryName) {
        this.serviceCategoryId = serviceCategoryId;
        this.categoryName = categoryName;
    }

    public ServiceCategory(String serviceCategoryId, String categoryName, String note,
                          OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.serviceCategoryId = serviceCategoryId;
        this.categoryName = categoryName;
        this.note = note;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public String getServiceCategoryId() {
        return serviceCategoryId;
    }

    public void setServiceCategoryId(String serviceCategoryId) {
        this.serviceCategoryId = serviceCategoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return categoryName;
    }

}
