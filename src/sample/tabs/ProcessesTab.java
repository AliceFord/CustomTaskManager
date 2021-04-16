package sample.tabs;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class ProcessesTab extends Tab {
    private final ObservableList<ProcessStats> data =
            FXCollections.observableArrayList(
                    new ProcessStats("Test", "1%", "10 MB", "1 MB/s")
            );
    
    public ProcessesTab() {
        TableView table = new TableView();
        TableColumn nameCol = new TableColumn("Name");
        nameCol.setMinWidth(100);
        nameCol.setCellValueFactory(
                new PropertyValueFactory<ProcessStats, String>("name"));
    
        TableColumn cpuCol = new TableColumn("CPU");
        cpuCol.setMinWidth(100);
        cpuCol.setCellValueFactory(
                new PropertyValueFactory<ProcessStats, String>("cpu"));
    
        TableColumn memoryCol = new TableColumn("Memory");
        memoryCol.setMinWidth(100);
        memoryCol.setCellValueFactory(
                new PropertyValueFactory<ProcessStats, String>("memory"));
    
        TableColumn diskCol = new TableColumn("Disk");
        diskCol.setMinWidth(100);
        diskCol.setCellValueFactory(
                new PropertyValueFactory<ProcessStats, String>("disk"));
    
    
        table.getColumns().addAll(nameCol, cpuCol, memoryCol);
        table.setItems(data);
        
        VBox test = new VBox();
        test.getChildren().add(table);
        
        setText("Processes");
        setContent(test);
    }
    
    public static class ProcessStats {
        
        private final SimpleStringProperty name;
        private final SimpleStringProperty cpu;
        private final SimpleStringProperty memory;
        private final SimpleStringProperty disk;
        
        private ProcessStats(String name, String cpu, String memory, String disk) {
            this.name = new SimpleStringProperty(name);
            this.cpu = new SimpleStringProperty(cpu);
            this.memory = new SimpleStringProperty(memory);
            this.disk = new SimpleStringProperty(disk);
        }
        
        public String getName() {
            return name.get();
        }
        
        public void setName(String name) {
            this.name.set(name);
        }
        
        public String getCpu() {
            return cpu.get();
        }
        
        public void setCpu(String cpu) { this.cpu.set(cpu); }
    
        public String getMemory() {
            return memory.get();
        }
    
        public void setMemory(String memory) { this.memory.set(memory); }
    
        public String getDisk() {
            return disk.get();
        }
    
        public void setDisk(String disk) { this.disk.set(disk); }
    }
}
