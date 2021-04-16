package sample;

import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.ptql.ProcessFinder;

public class Test {
    
    public static void main(String[] args) throws SigarException {
        Sigar  sigar = new Sigar();
        ProcessFinder find = new ProcessFinder(sigar);
        long pid = find.findSingleProcess("Exe.Name.ct=explorer");
        ProcMem mem = new ProcMem();
        mem.gather(sigar, pid);
        System.out.println(mem.getSize());
    }
    
} 