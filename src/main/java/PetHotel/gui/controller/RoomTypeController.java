package PetHotel.gui.controller;

import PetHotel.bus.TypeRoomBUS;
import PetHotel.model.TypeRoom;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class RoomTypeController {

    @FXML private TableView<TypeRoom> typeTable;
    @FXML private TableColumn<TypeRoom, String> colTypeId;
    @FXML private TableColumn<TypeRoom, String> colTypeName;
    @FXML private TableColumn<TypeRoom, Integer> colMaxPets;
    @FXML private TableColumn<TypeRoom, Double> colMaxWeight;
    @FXML private TableColumn<TypeRoom, Double> colPrice;
    @FXML private TableColumn<TypeRoom, String> colActive;

    private final TypeRoomBUS typeRoomBUS = new TypeRoomBUS();

    @FXML
    public void initialize() {
        setupColumns();
        loadTypeRooms();
    }

    private void setupColumns() {
        colTypeId.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getTypeRoomId()));
        colTypeName.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getTypeName()));
        colMaxPets.setCellValueFactory(d ->
            new SimpleObjectProperty<>(d.getValue().getMaxPets()));
        colMaxWeight.setCellValueFactory(d ->
            new SimpleObjectProperty<>(d.getValue().getMaxWeightKg()));
        colPrice.setCellValueFactory(d ->
            new SimpleObjectProperty<>(d.getValue().getBasePricePerDay()));
        colActive.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().isActive() ? "Hoạt động" : "Ngưng"));
    }

    private void loadTypeRooms() {
        try {
            List<TypeRoom> types = typeRoomBUS.getAllTypeRooms();
            typeTable.setItems(FXCollections.observableArrayList(types));
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Lỗi tải loại phòng: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void onClose(ActionEvent event) {
        Stage stage = (Stage) typeTable.getScene().getWindow();
        stage.close();
    }
}
