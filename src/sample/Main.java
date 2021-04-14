package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

class CPUQuery implements Runnable {
    private OutputStreamWriter osw;
    private BufferedReader in;
    private XYChart.Series graph;
    private int counter = 1;
    
    CPUQuery(OutputStreamWriter osw, BufferedReader in, XYChart.Series graph) {
        this.osw = osw;
        this.in = in;
        this.graph = graph;
    }
    
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                osw.write("mpstat 1 1\n");
                osw.flush();
                String line;
                for (int i = 0; i < 3; i++) {
                    in.readLine();
                }
                line = in.readLine();
                in.readLine();
                List<String> data = new LinkedList<String>(Arrays.asList(line.split(" +")));
                Platform.runLater(() -> {
                    graph.getData().add(new XYChart.Data(counter, 100 - Float.parseFloat(data.get(11))));
                    if (graph.getData().size()>=60) graph.getData().remove(0);
                });
                counter++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

public class Main extends Application {
    XYChart.Series cpuGraph;
    Thread queryThread;
    
    @Override
    public void start(Stage stage) {
        stage.setTitle("Area Chart Sample");
        final NumberAxis xAxis = new NumberAxis();
        xAxis.setForceZeroInRange(false);
        final NumberAxis yAxis = new NumberAxis(0, 100, 1);
        final AreaChart<Number, Number> ac =
                new AreaChart<Number, Number>(xAxis, yAxis);
        ac.setAnimated(false);
        ac.setTitle("CPU Usage");
    
        cpuGraph = new XYChart.Series();
        cpuGraph.setName("CPU");
    
        ac.getData().addAll(cpuGraph);
        
        Button cpuButton = new Button("CPU");
        Button memoryButton = new Button("Memory");
    
        GridPane gridPane = new GridPane();
        
        gridPane.add(ac, 1, 0, 1, 2);
        gridPane.add(cpuButton, 0, 0);
        gridPane.add(memoryButton, 0, 1);
    
        Scene scene = new Scene(gridPane, 800, 600);
        stage.setScene(scene);
        stage.show();

//        try {
//            String line;
//            Process p = Runtime.getRuntime().exec("tasklist");
//            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            while ((line = input.readLine()) != null) {
//                System.out.println(line);
//            }
//            input.close();
//        } catch (Exception err) {
//            err.printStackTrace();
//        }

//        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
//        for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
//            method.setAccessible(true);
//            if (method.getName().startsWith("get")
//                    && Modifier.isPublic(method.getModifiers())) {
//                Object value;
//                try {
//                    value = method.invoke(operatingSystemMXBean);
//                } catch (Exception e) {
//                    value = e;
//                }
//                System.out.println(method.getName() + " = " + value);
//            }
//        }
        try {
            ProcessBuilder pb = new ProcessBuilder("wsl");
            Process p = pb.start();
            OutputStreamWriter osw = new OutputStreamWriter(p.getOutputStream());
            osw.write("mpstat\n");
            osw.flush();
            String line;
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            for (int i = 0; i < 3; i++) {
                line = input.readLine();
            }
            line = input.readLine();
            System.out.println(line);
            List<String> data = new LinkedList<String>(Arrays.asList(line.split(" +")));
            cpuGraph.getData().add(new XYChart.Data(1, 100 - Float.parseFloat(data.get(11))));
            //input.close();
            //osw.close();
            CPUQuery cpuQuery = new CPUQuery(osw, input, cpuGraph);
            queryThread  = new Thread(cpuQuery);
            queryThread.start();
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void stop() throws Exception {
        queryThread.interrupt();
        super.stop();
    }
}
