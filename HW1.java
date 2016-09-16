import java.net.URL;
import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;

/*
* A simple Jave command line program calling the VMware ESXi API to 
* print out host name, datastore, network, and power on or power off the VMs, etc.. 
*/

public class HW1
{
    public static void main(String[] args) throws Exception
    {
        String ip = null, login = null, password = null;
        int hostCounter = 0; 
        int vMCounter = 0;
        int datastoreCounter = 0; 
        int networkCounter = 0;
        
        if (args.length >= 3) {
            ip = args[0];
            login = args[1];
            password = args[2];
        }
        else{   
          System.out.println("Usage: java HW1 IP login password");
          return;
        }
      
        ServiceInstance si = new ServiceInstance(new URL("https://" + ip + "/sdk"), login, password, true);
        
        Folder rootFolder = si.getRootFolder();
        ManagedEntity[] mev = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
        ManagedEntity[] meh = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
        if (mev == null || mev.length == 0) {
			return;
        }
        
        if (meh == null || meh.length == 0) {
            return;
        }
         
    for (ManagedEntity me: meh) {
        System.out.println("Host[" + hostCounter + "]:");
        HostSystem host = (HostSystem) me;
        System.out.println("Name = " + host.getName());
        HostHardwareInfo hw = host.getHardware();
        System.out.println("Product Full Name = " + host.getConfig().getProduct().getFullName());
        
        Datastore[] datastore = host.getDatastores();
        for (Datastore datastore1 : datastore) {
            DatastoreSummary dss = datastore1.getSummary();
            System.out.println("Datastore[" + datastoreCounter + "]: Name = " + dss.getName() + ", Capacity = " + dss.getCapacity() +
                        ", Freespace = " + dss.getFreeSpace());
            datastoreCounter++;
            }
        
        Network[] nw = host.getNetworks();
        for (Network nw1 : nw){
             System.out.println( "Network[" + networkCounter + "]: name = " + nw1.getName()); 
             networkCounter++; 
            }
        System.out.printf("\n");
        hostCounter++; 
    }
         
    for (ManagedEntity me: mev) {
        System.out.println("VM[" + vMCounter + "]:");
        VirtualMachine vm = (VirtualMachine) me;
        VirtualMachineConfigInfo vminfo = vm.getConfig();
        System.out.println("Name = " + vm.getName());
        System.out.println("GuestOS = " + vminfo.getGuestFullName()); 
        System.out.println("Guest State = " + vm.getGuest().getGuestState());
        System.out.println("Power State = " + vm.getRuntime().getPowerState());
        
        VirtualMachinePowerState vmPowerState = vm.getRuntime().getPowerState(); 
        if (vmPowerState == VirtualMachinePowerState.poweredOff)
        {
            Task task = vm.powerOnVM_Task(null);  
            task.waitForTask(); 
            System.out.println("Power on VM: status = " + task.getTaskInfo().getState()); 
        }
        else if (vmPowerState == VirtualMachinePowerState.poweredOn)
        {
            Task task = vm.powerOffVM_Task();
            task.waitForTask(); 
            System.out.println("Power off VM: status = " + task.getTaskInfo().getState()); 
        }
        
        Task[] task = vm.getRecentTasks(); 
        for (Task task1 : task)
        {
            System.out.println("task: target = " + task1.getTaskInfo().entityName + ", op = " + task1.getTaskInfo().name + 
                    ", startTime = " + task1.getTaskInfo().getStartTime().getTime()); 
        }
        System.out.printf("\n");
        vMCounter++; 
    }
    si.getServerConnection().logout();
    }
}