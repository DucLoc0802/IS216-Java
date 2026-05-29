package PetHotel.gui.controller;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

final class SearchableComboBoxUtil {

    private static final String STATE_KEY = SearchableComboBoxUtil.class.getName() + ".state";

    private SearchableComboBoxUtil() {
    }

    static <T> void setup(ComboBox<T> comboBox, List<T> items, Function<T, String> displayText) {
        @SuppressWarnings("unchecked")
        SearchState<T> state = (SearchState<T>) comboBox.getProperties().get(STATE_KEY);

        if (state == null) {
            state = new SearchState<>(comboBox, displayText);
            comboBox.getProperties().put(STATE_KEY, state);
            state.install();
        } else {
            state.setDisplayText(displayText);
        }

        state.setItems(items);
    }

    static <T> T getSelectedOrExactTextMatch(ComboBox<T> comboBox) {
        T selected = comboBox.getValue();
        if (selected != null) {
            return selected;
        }

        @SuppressWarnings("unchecked")
        SearchState<T> state = (SearchState<T>) comboBox.getProperties().get(STATE_KEY);
        if (state == null) {
            return null;
        }

        T exactMatch = state.findExact(comboBox.getEditor().getText());
        if (exactMatch != null) {
            return exactMatch;
        }

        String query = normalize(comboBox.getEditor().getText());
        if (!query.isEmpty() && comboBox.getItems().size() == 1) {
            return comboBox.getItems().get(0);
        }

        return null;
    }

    private static final class SearchState<T> {
        private final ComboBox<T> comboBox;
        private Function<T, String> displayText;
        private final ObservableList<T> allItems = FXCollections.observableArrayList();
        private boolean updating;

        private SearchState(ComboBox<T> comboBox, Function<T, String> displayText) {
            this.comboBox = comboBox;
            this.displayText = displayText;
        }

        private void install() {
            comboBox.setEditable(true);
            comboBox.setCellFactory(param -> createCell());
            comboBox.setButtonCell(createCell());
            comboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(T item) {
                    return itemText(item);
                }

                @Override
                public T fromString(String text) {
                    return findExact(text);
                }
            });

            comboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
                if (updating || !comboBox.getEditor().isFocused()) {
                    return;
                }

                T selected = comboBox.getValue();
                if (selected != null && itemText(selected).equals(newText)) {
                    return;
                }
                if (selected != null && !itemText(selected).equals(newText)) {
                    comboBox.setValue(null);
                }

                filter(newText, true);
            });

            comboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (updating || newValue == null) {
                    return;
                }

                String text = itemText(newValue);
                updating = true;
                comboBox.getEditor().setText(text);
                comboBox.getEditor().positionCaret(text.length());
                updating = false;
            });

            comboBox.showingProperty().addListener((obs, wasShowing, showing) -> {
                if (showing) {
                    filter(comboBox.getEditor().getText(), false);
                }
            });
        }

        private ListCell<T> createCell() {
            return new ListCell<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : itemText(item));
                }
            };
        }

        private void setDisplayText(Function<T, String> displayText) {
            this.displayText = displayText;
        }

        private void setItems(List<T> items) {
            allItems.setAll(items);

            T selected = comboBox.getValue();
            if (selected != null && !allItems.contains(selected)) {
                comboBox.setValue(null);
            }

            filter(comboBox.getEditor().getText(), false);
        }

        private void filter(String text, boolean showPopup) {
            String typedText = text == null ? "" : text;
            String query = normalize(typedText);
            int caretPosition = comboBox.getEditor().getCaretPosition();

            ObservableList<T> filteredItems = FXCollections.observableArrayList();
            for (T item : allItems) {
                if (query.isEmpty() || normalize(itemText(item)).contains(query)) {
                    filteredItems.add(item);
                }
            }

            updating = true;
            comboBox.setItems(filteredItems);
            comboBox.getEditor().setText(typedText);
            comboBox.getEditor().positionCaret(Math.min(caretPosition, typedText.length()));
            updating = false;

            if (showPopup && comboBox.getScene() != null && comboBox.getEditor().isFocused()) {
                Platform.runLater(() -> {
                    if (!comboBox.isShowing()) {
                        comboBox.show();
                    }
                });
            }
        }

        private T findExact(String text) {
            String normalizedText = normalize(text);
            for (T item : allItems) {
                if (normalize(itemText(item)).equals(normalizedText)) {
                    return item;
                }
            }
            return null;
        }

        private String itemText(T item) {
            if (item == null) {
                return "";
            }
            String value = displayText.apply(item);
            return value == null ? "" : value;
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return normalized.toLowerCase(Locale.ROOT).trim();
    }
}
