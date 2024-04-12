module App {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.remixicon;


    opens edu.metrostate to javafx.fxml;
    exports edu.metrostate;
}