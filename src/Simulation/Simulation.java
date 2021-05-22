/**
 *	Example program for using eventlists
 *	@author Joel Karel
 *	@version %I%, %G%
 */

package Simulation;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

public class Simulation {
	
	public static final int SINK_DEBUG 		= 1 << 0; // 1
	public static final int SOURCE_DEBUG 	= 1 << 1; // 2
	public static final int MACHINE_DEBUG 	= 1 << 2; // 4
	public static final int GET_RESULTS 	= 1 << 3; // 8
	public static final int STORE_TO_FILE 	= 1 << 4; // 16
	
	public static void main(String[] args) {
    	simulate_default(2000000, GET_RESULTS | STORE_TO_FILE);
    	simulate_seperate(2000000, GET_RESULTS | STORE_TO_FILE);
    }
	
	private static void attach_source(CEventList l, ProductAcceptor qd) {
		// sources don't need to be assigned to variables because they are linked to l and qd
    	// rates are given in "per hour", convert to "per minute" by taking '/60'
    	// period is given in hours, so we multiply that by 60 to get it in minutes
    	new Source(qd,l,"Source normal", 2./60, 0.8/60, 24.*60);
    	new Source(qd,l,"Source GPU", 300);
	}
	
	public static void simulate_default(double max_time, int DEBUG) {
		simulate_default(6, 2, max_time, DEBUG);
	}
	public static void simulate_default(int num_CPU_cores, int num_GPU_cores, double max_time, int DEBUG) {
		int num_cores = num_CPU_cores + num_GPU_cores;
    	CEventList l = new CEventList();
    	// A queue for the machine
    	
    	ArrayList<Queue> qlist = new ArrayList<>();
    	for (int i=0; i < num_CPU_cores; i++) qlist.add(new Queue());
    	for (int i=0; i < num_GPU_cores; i++) qlist.add(new GPUQueue());
    	
    	QueueDistributor qd = new QueueDistributor(qlist);
    	attach_source(l, qd);
    	
    	// The Sink
    	Sink si = new Sink("Sink 1");
    	// machines don't need to be assigned to variables because they are linked to l and the q's
    	for (int i=0; i < num_CPU_cores; i++) new Machine(qlist.get(i), si, l, "Machine "+(i+1), 145, 42);
    	for (int i=num_CPU_cores; i < num_cores; i++) 
    		new GPUMachine(qlist.get(i), si, l, "GPUMachine "+(i-num_CPU_cores+1), 145, 42, 240, 50);
    	
    	start(l, si, max_time, DEBUG, "default");
    }
	
	public static void simulate_seperate(double max_time, int DEBUG) {
		simulate_seperate(6, 2, max_time, DEBUG);
	}
	public static void simulate_seperate(int num_CPU_cores, int num_GPU_cores, double max_time, int DEBUG) {
		CEventList l = new CEventList();
    	// A queue for the machine
    	
    	Queue RQ = new Queue();
    	GPUQueue GQ = new GPUQueue();
    	
    	QueueDistributor qd = new QueueDistributor(RQ, GQ);
    	attach_source(l, qd);
    	
    	// The Sink
    	Sink si = new Sink("Sink 1");
    	// machines don't need to be assigned to variables because they are linked to l and the q's
    	for (int i=0; i < num_CPU_cores; i++) new Machine(RQ, si, l, "Machine "+(i+1), 145, 42);
    	for (int i=0; i < num_GPU_cores; i++) new GPUMachine(GQ, si, l, "GPUMachine "+(i+1), 145, 42, 240, 50);
    	
    	start(l, si, max_time, DEBUG, "seperate");
    }
	
	private static void start(CEventList l, Sink si, double max_time, int DEBUG, String file_name) {
		// debug config
    	Sink.DEBUG = (DEBUG & SINK_DEBUG) > 0;
    	Source.DEBUG = (DEBUG & SOURCE_DEBUG) > 0;
    	Machine.DEBUG = (DEBUG & MACHINE_DEBUG) > 0;
    	boolean get_results = (DEBUG & GET_RESULTS) > 0;
    	boolean save_data = (DEBUG & STORE_TO_FILE) > 0;
    	
    	// start the eventlist
    	l.start(max_time);
    	
    	data_collect(si, get_results, save_data, file_name);
	}
    
    @SuppressWarnings("unchecked")
	private static void data_collect(Sink si, boolean get_py_or_mat_data, boolean store_to_file, String file_name) {
    	// collecting data for matlab:
    	ArrayList<Double>[] lists = new ArrayList[] {si.getRegularDelays(), si.getGPUDelays(),
    			si.getAllDelays(), si.getRegularTimes(), si.getGPUTimes(), si.getAllTimes()};
    	String[] names = new String[]
    			{"reg_delays", "gpu_delays", "all_delays", "reg_times", "gpu_times", "all_times"};
    	
    	System.out.format("\nfinished simulation after %d iterations\n", lists[5].size());
    	
    	PrintStream out = setup_writer(store_to_file, file_name);
    	if (get_py_or_mat_data)
	    	for (int i=0; i < lists.length; i++) { // CANADA uses "." as decimal seperator
	    		out.format(Locale.CANADA, "%s = [%e", names[i], lists[i].get(0));
	    		for (int k=1; k < lists[i].size(); k++)
	    			out.format(Locale.CANADA, ", %e", lists[i].get(k));
	    		out.println("];");
	    	}
    	
    	System.out.format(Locale.CANADA, "Regular mean delay time - %.3f\nGPU mean delay time - %.3f\n"+
    	"Overall mean delay time - %.3f\nRegular 90th percentile delay time - %.3f\n"+
    	"GPU 90th percentile delay time - %.3f\nAll 90th percentile delay time - %.3f\n",
    			si.getMeanRegular(), si.getMeanGPU(), si.getMeanAll(),
    			si.getPercentileRegular(0.9), si.getPercentileGPU(0.9), si.getPercentileAll(0.9));
    }
    
    private static PrintStream setup_writer(boolean save_data, String run_name) {
    	PrintStream out = System.out;
    	if (save_data) {
	    	File folder = new File("data_output");
	    	folder.mkdirs();
	    	List<String> file_names = List.of(folder.listFiles()).stream().map(f -> f.getName()).collect(Collectors.toList());
	    	int index = 1;
	    	while (file_names.contains(String.format("%s%03d.txt", run_name, index))) index++;
			try {
				out = new PrintStream(new File(String.format("data_output/%s%03d.txt", run_name, index)));
			} catch (FileNotFoundException e) { e.printStackTrace(); System.exit(-1); }
    	}
    	return out;
    }
    
}
