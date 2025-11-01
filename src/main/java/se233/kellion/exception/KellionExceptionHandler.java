package se233.kellion.exception;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.io.PrintWriter;
import java.io.StringWriter;

public class KellionExceptionHandler {

    public static void handle(Throwable e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Game Error");

            String header = getHeader(e);
            alert.setHeaderText(header);
            alert.setContentText(e.getMessage() != null ? e.getMessage() : "An unexpected error occurred.");

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionText = sw.toString();

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxHeight(200);

            alert.getDialogPane().setExpandableContent(textArea);
            alert.showAndWait();
        });
    }

    private static String getHeader(Throwable e) {
        if (e instanceof KellionInputException)
            return "Input Error";
        if (e instanceof KellionGameLoopException)
            return "Game Loop Error";
        if (e instanceof KellionResourceException)
            return "Resource Loading Error";
        if (e instanceof KellionException)
            return "Game Error";
        return "Unexpected Error";
    }
}
