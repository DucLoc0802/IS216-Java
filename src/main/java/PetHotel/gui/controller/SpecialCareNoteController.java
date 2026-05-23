package PetHotel.gui.controller;

import PetHotel.dao.BookingDAO;
import PetHotel.model.Booking;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SpecialCareNoteController {

    @FXML private Label lblBookingId;
    @FXML private Label lblStatus;
    @FXML private Label lblCustomer;
    @FXML private Label lblPet;
    @FXML private TextArea txtNote;
    @FXML private Label lblError;
    @FXML private Button btnCancel;

    private final BookingDAO bookingDAO = new BookingDAO();
    private Booking currentBooking;
    private Runnable onSaveCallback;

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public void setBooking(Booking booking) {
        this.currentBooking = booking;
        lblBookingId.setText(booking.getBookingId());
        lblStatus.setText(booking.getStatus());
        lblCustomer.setText(booking.getCustomerName() != null ? booking.getCustomerName() : "—");
        lblPet.setText(booking.getPetName() != null ? booking.getPetName() : "—");
        txtNote.setText(booking.getSpecialNote() != null ? booking.getSpecialNote() : "");
    }

    @FXML
    public void onSave(ActionEvent event) {
        lblError.setVisible(false);
        if (txtNote.getText().trim().isEmpty()) {
            lblError.setText("Vui lòng nhập ghi chú chăm sóc.");
            lblError.setVisible(true);
            return;
        }
        try {
            bookingDAO.updateSpecialNote(currentBooking.getBookingId(),
                txtNote.getText().trim());
            currentBooking.setSpecialNote(txtNote.getText().trim());
            if (onSaveCallback != null) onSaveCallback.run();
            closeDialog();
        } catch (Exception e) {
            lblError.setText(e.getMessage());
            lblError.setVisible(true);
        }
    }

    @FXML
    public void onCancel(ActionEvent event) { closeDialog(); }

    private void closeDialog() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }
}