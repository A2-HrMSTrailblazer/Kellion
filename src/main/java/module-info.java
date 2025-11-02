module se233.kellion {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j;

    opens se233.kellion to javafx.fxml;
    exports se233.kellion;
    exports se233.kellion.controller;
    exports se233.kellion.model;
    exports se233.kellion.view;
}
