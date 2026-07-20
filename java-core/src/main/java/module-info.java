module  org.quark.ray {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.sun.jna;
    requires com.sun.jna.platform;

    exports org.quark.ray;

    opens org.quark.ray to javafx.fxml;
}