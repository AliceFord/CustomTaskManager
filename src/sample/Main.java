package sample;

import com.profesorfalken.jpowershell.PowerShell;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import sample.tabs.PerformanceTab;
import sample.tabs.ProcessesTab;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Main extends Application {
    
    
    @Override
    public void start(Stage stage) {
        stage.setTitle("Custom Task Manager");
    
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(new ProcessesTab(), new PerformanceTab());
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        Scene scene = new Scene(tabPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
