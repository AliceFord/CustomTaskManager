package sample.tabs;

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
import javafx.scene.control.Tab;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

class CPUQuery implements Runnable {
    private final OutputStreamWriter osw;
    private final BufferedReader in;
    private final XYChart.Series graph;
    private final NumberAxis xAxis;
    private final List<Label> labels; // Utilisation, speed, processes, threads, handles, uptime
    private final PowerShell[] powerShells = new PowerShell[6]; // Same order as above
    private int counter = 0;
    
    CPUQuery(OutputStreamWriter osw, BufferedReader in, XYChart.Series graph, NumberAxis xAxis, List<Label> labels) {
        this.osw = osw;
        this.in = in;
        this.graph = graph;
        this.xAxis = xAxis;
        this.labels = labels;
        
        Thread[] threads = new Thread[6];
        
        for (int i = 0; i < 6; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                PowerShell current = PowerShell.openSession();
                synchronized (powerShells) {
                    powerShells[finalI] = current;
                }
            });
            threads[i].start();
        }
        
        for (int i = 0; i < 6; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        for (int i = 0; i < 6; i++) {
            threads[i].interrupt();
        }
    }
    
    private String getUptime(String uptime) {
        String[] uptimeList = uptime.split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(("00" + uptimeList[i+2].substring(uptimeList[i+2].indexOf(':') + 2, uptimeList[i+2].length() - 1)).substring((uptimeList[i+2].substring(uptimeList[i+2].indexOf(':') + 2, uptimeList[i+2].length() - 1).length()))).append(":");
        }
        return sb.toString().substring(0, sb.toString().length()-1);
    }
    
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            AtomicReference<String> uptime = new AtomicReference<>();
            Thread t1 = new Thread(() -> {
                uptime.set(powerShells[5].executeCommand("(get-date) - (gcim Win32_OperatingSystem).LastBootUpTime").getCommandOutput());
            });
            
            AtomicReference<String> processes = new AtomicReference<>();
            Thread t2 = new Thread(() -> {
                processes.set(powerShells[2].executeCommand("((Get-Counter '\\Process(*)\\% Processor Time' -ErrorAction SilentlyContinue).CounterSamples).Count").getCommandOutput());
            });
            
            AtomicReference<String> cpuUtilisation = new AtomicReference<>();
            Thread t3 = new Thread(() -> {
                String temp = powerShells[0].executeCommand("Get-Counter '\\Processor(_Total)\\% Processor Time'").getCommandOutput();
                cpuUtilisation.set(temp.substring(temp.indexOf(" :")+2).replaceAll(" ", ""));
            });
            
            AtomicReference<String> threads = new AtomicReference<>();
            Thread t4 = new Thread(() -> {
                threads.set(powerShells[3].executeCommand("(Get-Process|Select-Object -ExpandProperty Threads -ErrorAction SilentlyContinue).Count").getCommandOutput());
            });
            
            t1.start();
            t2.start();
            t3.start();
            t4.start();
            
            try {
                t1.join();
                t2.join();
                t3.join();
                t4.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            String finalCpuUtilisation = cpuUtilisation.get();
            Platform.runLater(() -> {
                graph.getData().add(new XYChart.Data(counter,  Float.parseFloat(finalCpuUtilisation)));
                xAxis.setUpperBound(Math.max(counter, 60));
                xAxis.setLowerBound(Math.max(counter - 59, 0));
                if (graph.getData().size()>60) graph.getData().remove(0);
                counter++;
                labels.get(0).setText(String.format("%.0f", Float.parseFloat(finalCpuUtilisation)) + "%");
                labels.get(2).setText(processes.get());
                labels.get(3).setText(threads.get());
                labels.get(5).setText(getUptime(uptime.get()));
            });
        }
        for (PowerShell shell : powerShells) shell.close();
    }
}

public class PerformanceTab extends Tab {
    XYChart.Series cpuGraph;
    Thread queryThread;
    NumberAxis xAxis;
    
    public PerformanceTab() {
        setText("Performance");
        xAxis = new NumberAxis(0, 60, 1);
        xAxis.setForceZeroInRange(false);
        final NumberAxis yAxis = new NumberAxis(0, 100, 1);
        final AreaChart<Number, Number> ac =
                new AreaChart<>(xAxis, yAxis);
        ac.setAnimated(false);
        ac.setLegendVisible(false);
        ac.setCreateSymbols(false);
        ac.setTitle("% Utilisation");
        
        cpuGraph = new XYChart.Series();
        
        ac.getData().addAll(cpuGraph);
        
        Label cpuLabel = new Label("CPU");
        cpuLabel.setFont(new Font(30));
        Label cpuModel = new Label();
        cpuModel.setFont(new Font(15));
        
        HBox topCPUBox = new HBox();
        topCPUBox.setSpacing(10);
        
        BorderPane bp = new BorderPane();
        bp.setBottom(topCPUBox);
        
        AnchorPane apLeft = new AnchorPane();
        apLeft.getChildren().add(cpuLabel);
        AnchorPane apRight = new AnchorPane();
        apRight.getChildren().add(cpuModel);
        
        topCPUBox.getChildren().addAll(apLeft, apRight);
        
        HBox.setHgrow(apLeft, Priority.ALWAYS);
        
        GridPane cpuStatsPane = new GridPane();
        
        Label utilisationLabel = new Label("Utilisation");
        Label utilisationDataLabel = new Label("0%!");
        Label speedLabel = new Label("Speed");
        Label speedDataLabel = new Label("3.30 GHz!");
        Label processLabel = new Label("Processes");
        Label processDataLabel = new Label("100!");
        Label threadsLabel = new Label("Threads");
        Label threadsDataLabel = new Label("8000!");
        Label handlesLabel = new Label("Handles");
        Label handlesDataLabel = new Label("170000!");
        Label uptimeLabel = new Label("Uptime");
        Label uptimeDataLabel = new Label("7:10:34:01!");
        
        cpuStatsPane.add(utilisationLabel, 0, 0);
        cpuStatsPane.add(utilisationDataLabel, 0, 1);
        cpuStatsPane.add(speedLabel, 1, 0);
        cpuStatsPane.add(speedDataLabel, 1, 1, 2, 1);
        cpuStatsPane.add(processLabel, 0, 2);
        cpuStatsPane.add(processDataLabel, 0, 3);
        cpuStatsPane.add(threadsLabel, 1, 2);
        cpuStatsPane.add(threadsDataLabel, 1, 3);
        cpuStatsPane.add(handlesLabel, 2, 2);
        cpuStatsPane.add(handlesDataLabel, 2, 3);
        cpuStatsPane.add(uptimeLabel, 0, 4);
        cpuStatsPane.add(uptimeDataLabel, 0, 5, 2, 1);
        
        ColumnConstraints cpuStatsPaneC1 = new ColumnConstraints();
        cpuStatsPaneC1.setPercentWidth(100/3.0);
        ColumnConstraints cpuStatsPaneC2 = new ColumnConstraints();
        cpuStatsPaneC2.setPercentWidth(100/3.0);
        ColumnConstraints cpuStatsPaneC3 = new ColumnConstraints();
        cpuStatsPaneC3.setPercentWidth(100/3.0);
        
        cpuStatsPane.getColumnConstraints().addAll(cpuStatsPaneC1, cpuStatsPaneC2, cpuStatsPaneC3);
        
        RowConstraints cpuStatsPaneR1 = new RowConstraints();
        cpuStatsPaneR1.setPercentHeight(100/12.0);
        RowConstraints cpuStatsPaneR2 = new RowConstraints();
        cpuStatsPaneR2.setPercentHeight(300/12.0);
        RowConstraints cpuStatsPaneR3 = new RowConstraints();
        cpuStatsPaneR3.setPercentHeight(100/12.0);
        RowConstraints cpuStatsPaneR4 = new RowConstraints();
        cpuStatsPaneR4.setPercentHeight(300/12.0);
        RowConstraints cpuStatsPaneR5 = new RowConstraints();
        cpuStatsPaneR5.setPercentHeight(100/12.0);
        RowConstraints cpuStatsPaneR6 = new RowConstraints();
        cpuStatsPaneR6.setPercentHeight(300/12.0);
        
        GridPane detailedCPUStatsPane = new GridPane();
        
        Label baseSpeedLabel = new Label("Base Speed: ");
        Label baseSpeedDataLabel = new Label("2.84 GHz!");
        Label socketsLabel = new Label("Sockets: ");
        Label socketsDataLabel = new Label("1!");
        Label coresLabel = new Label("Cores: ");
        Label coresDataLabel = new Label("2!");
        Label logicalProcessorsLabel = new Label("Logical Processors: ");
        Label logicalProcessorsDataLabel = new Label("2!");
        Label virtualisationLabel = new Label("Virtualisation: ");
        Label virtualisationDataLabel = new Label("Disabled!");
        Label l1CacheLabel = new Label("L1 Cache: ");
        Label l1CacheDataLabel = new Label("128 KB!");
        Label l2CacheLabel = new Label("L2 Cache: ");
        Label l2CacheDataLabel = new Label("2.0 MB!");
        Label l3CacheLabel = new Label("L3 Cache: ");
        Label l3CacheDataLabel = new Label("8.0 MB!");
        
        
        detailedCPUStatsPane.add(baseSpeedLabel, 0, 0);
        detailedCPUStatsPane.add(baseSpeedDataLabel, 1, 0);
        detailedCPUStatsPane.add(socketsLabel, 0, 1);
        detailedCPUStatsPane.add(socketsDataLabel, 1, 1);
        detailedCPUStatsPane.add(coresLabel, 0, 2);
        detailedCPUStatsPane.add(coresDataLabel, 1, 2);
        detailedCPUStatsPane.add(logicalProcessorsLabel, 0, 3);
        detailedCPUStatsPane.add(logicalProcessorsDataLabel, 1, 3);
        detailedCPUStatsPane.add(virtualisationLabel, 0, 4);
        detailedCPUStatsPane.add(virtualisationDataLabel, 1, 4);
        detailedCPUStatsPane.add(l1CacheLabel, 0, 5);
        detailedCPUStatsPane.add(l1CacheDataLabel, 1, 5);
        detailedCPUStatsPane.add(l2CacheLabel, 0, 6);
        detailedCPUStatsPane.add(l2CacheDataLabel, 1, 6);
        detailedCPUStatsPane.add(l3CacheLabel, 0, 7);
        detailedCPUStatsPane.add(l3CacheDataLabel, 1, 7);
        
        cpuStatsPane.getRowConstraints().addAll(cpuStatsPaneR1, cpuStatsPaneR2, cpuStatsPaneR3, cpuStatsPaneR4, cpuStatsPaneR5, cpuStatsPaneR6);
        
        HBox bottomCPUBox = new HBox();
        bottomCPUBox.getChildren().addAll(cpuStatsPane, detailedCPUStatsPane);
        
        VBox cpuBox = new VBox();
        cpuBox.getChildren().addAll(topCPUBox, ac, bottomCPUBox);
        
        Button cpuButton = new Button("CPU");
        Button memoryButton = new Button("Memory");
        
        GridPane gridPane = new GridPane();
        
        gridPane.add(cpuBox, 1, 0, 1, Integer.MAX_VALUE);
        gridPane.add(cpuButton, 0, 0);
        gridPane.add(memoryButton, 0, 1);
        
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(20);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(80);
        
        gridPane.getColumnConstraints().addAll(c1, c2);
        
        RowConstraints[] rows = new RowConstraints[gridPane.getRowCount()];
        for (int i = 0; i < gridPane.getRowCount(); i++) {
            rows[i] = new RowConstraints();
            rows[i].setPercentHeight((1 / (float)gridPane.getRowCount()) * 100);
        }
        
        gridPane.getRowConstraints().addAll(rows);
        gridPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("white"), new CornerRadii(0), new Insets(0))));
        
        setContent(gridPane);

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
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            CPUQuery cpuQuery = new CPUQuery(osw, input, cpuGraph, xAxis, new ArrayList<>(Arrays.asList(utilisationDataLabel, speedDataLabel, processDataLabel, threadsDataLabel, handlesDataLabel, uptimeDataLabel)));
            
            
            osw.write("cat /proc/cpuinfo\n");
            osw.flush();
            String[] cpuInfoData = parseCPUInfoFile(input, 8, "model name", "cpu MHz", "cpu cores", "siblings", "cache size");
            cpuModel.setText(cpuInfoData[0]);
            baseSpeedDataLabel.setText(String.format("%.2f", Float.parseFloat(cpuInfoData[1]) / 1000) + " GHz");
            coresDataLabel.setText(cpuInfoData[2]);
            logicalProcessorsDataLabel.setText(cpuInfoData[3]);
            l1CacheDataLabel.setText(cpuInfoData[4]);
            
            queryThread  = new Thread(cpuQuery);
            queryThread.start();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String[] parseCPUInfoFile(BufferedReader in, int processors, String... values) throws IOException {
        String[] out = new String[8];
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            for (int i = 0; i < values.length; i++) {
                if (line.startsWith(values[i])) {
                    out[i] = line.substring(line.indexOf(':')+2);
                    break;
                }
            }
        }
        for (int i=0; i<processors-1; i++) {
            while (!(line = in.readLine()).isEmpty()) {
                ;
            }
        }
        return out;
    }
}
