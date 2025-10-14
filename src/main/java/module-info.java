module se233.kellion {
    requires javafx.controls;
    requires javafx.fxml;


    opens se233.kellion to javafx.fxml;
    exports se233.kellion;
}