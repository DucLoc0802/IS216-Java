package PetHotel.model;

public class CategoryProduct {
    private String productCategoryId;
    private String categoryName;

    public CategoryProduct() {}

    public CategoryProduct(String productCategoryId, String categoryName) {
        this.productCategoryId = productCategoryId;
        this.categoryName = categoryName;
    }

    public String getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(String productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return categoryName == null ? productCategoryId : categoryName;
    }
}
