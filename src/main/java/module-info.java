module com.llmons.notepadlite {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.llmons.notepadlite to javafx.fxml;
    exports com.llmons.notepadlite;
}