module  org.quark.ray {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.sun.jna;
    requires com.sun.jna.platform;

    exports org.quark.ray;
    exports org.quark.ray.core;

    opens org.quark.ray.controller to javafx.fxml;
}