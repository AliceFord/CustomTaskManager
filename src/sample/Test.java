package sample;

import com.profesorfalken.jpowershell.PowerShell;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;

public class Test {
    public static void main(String[] args) {
        PowerShell ps = PowerShell.openSession();
        long start = System.nanoTime();
        ps.executeCommand("(get-date) - (gcim Win32_OperatingSystem).LastBootUpTime").getCommandOutput();
        System.out.println((System.nanoTime() - start) / 1000000000.0);
        start = System.nanoTime();
        try {
            String command = "powershell.exe \"(get-date) - (gcim Win32_OperatingSystem).LastBootUpTime\"";
            Process powerShellProcess = Runtime.getRuntime().exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(powerShellProcess.getInputStream()));
            String line;
            for (int i = 0; i < 6; i++)
                stdInput.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println((System.nanoTime() - start) / 1000000000.0);
    }
}