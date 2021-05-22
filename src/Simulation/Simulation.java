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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class Simulation {
	
	public static final int SINK_DEBUG 		= 1 << 0; // 1
	public static final int SOURCE_DEBUG 	= 1 << 1; // 2
	public static final int MACHINE_DEBUG 	= 1 << 2; // 4
	public static final int GET_RESULTS 	= 1 << 3; // 8
	public static final int STORE_TO_FILE 	= 1 << 4; // 16
	
	public static void main(String[] args) {
		int size = 1000;
		double time = 10080*4;
		
		double[] mean_delay = new double[size];
		double[] mean_gpu_delay = new double[size];
		double[] mean_regular_delay = new double[size];
		double[] percentile_90_delay = new double[size];
		double[] percentile_90_gpu_delay = new double[size];
		double[] percentile_90_regular_delay = new double[size];
		
		
		for(int j=0; j<size; j++) {
			double[] results = simulate_routine_1(time);
		
			mean_delay[j] = results[0];
	    	mean_gpu_delay[j] = results[1];
	    	mean_regular_delay[j] = results[2];
	    	percentile_90_delay[j] = results[3];
	    	percentile_90_gpu_delay[j] = results[4];
	    	percentile_90_regular_delay[j] = results[5];
		}
		
		store_data(mean_delay, mean_gpu_delay, mean_regular_delay, percentile_90_delay,
				percentile_90_gpu_delay, percentile_90_regular_delay, "output_routine_1.txt");
		
		
		mean_delay = new double[size];
		mean_gpu_delay = new double[size];
		mean_regular_delay = new double[size];
		percentile_90_delay = new double[size];
		percentile_90_gpu_delay = new double[size];
		percentile_90_regular_delay = new double[size];
		
		
		for(int j=0; j<size; j++) {
			double[] results = simulate_routine_0(time);
		
			mean_delay[j] = results[0];
	    	mean_gpu_delay[j] = results[1];
	    	mean_regular_delay[j] = results[2];
	    	percentile_90_delay[j] = results[3];
	    	percentile_90_gpu_delay[j] = results[4];
	    	percentile_90_regular_delay[j] = results[5];
		}
		
		store_data(mean_delay, mean_gpu_delay, mean_regular_delay, percentile_90_delay,
				percentile_90_gpu_delay, percentile_90_regular_delay, "output_routine_0.txt");
		
    }
    
	public static double[] simulate_routine_0(double time) {
		
		
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
    	
    	// debug off
    	Sink.DEBUG = false;
    	Source.DEBUG = false;
    	Machine.DEBUG = false;
    	
    	// set to false if you don't want all that output
    	/*boolean print_for_python_or_matlab = true;
    	// set to true to store results rather than printing them
    	boolean save_data = true;
    	*/
    	// start the eventlist
    	double max_time = time;
    	l.start(max_time);
    	
    	//data_collect(si, print_for_python_or_matlab, save_data, "data");
    	double[] results = mean_and_percentile_collect(si);
    	
		return results;
		
    }
	
	public static double[] simulate_routine_1(double time) {
		
		CEventList l = new CEventList();
		// A queue for the machine
	
		Queue RQ = new Queue();
		GPUQueue GQ = new GPUQueue();
	
		QueueDistributor qd = new QueueDistributor(RQ, GQ);
	
		// sources don't need to be assigned to variables because they are linked to l and qd
		new Source(qd,l,"Source normal", 30, ProductType.Normal);
		new Source(qd,l,"Source GPU", 360, ProductType.GPU);
	
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
    	
    	// set to false if you don't want all that output
    	//boolean print_for_python_or_matlab = true;
    	// set to true to store results rather than printing them
    	//boolean save_data = true;
    	
    	// start the eventlist
    	// We make the simulation last for one week
    	double max_time = time;
    	l.start(max_time);
    	
    	double[] results = mean_and_percentile_collect(si);
    	
		return results;
		
    	//data_collect(si, print_for_python_or_matlab, save_data, "data");
    }
    
    @SuppressWarnings("unchecked")
	/*private static void data_collect(Sink si, boolean get_py_or_mat_data, boolean store_to_file, String file_name) {
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
    }*/
    
    private static void store_data(double[] mean, double[] gpu_mean, double[] regular_mean,
    		double[] percentile, double[] percentile_gpu, double[] percentile_regular, String file_name) {
    	try {
			FileWriter wr = new FileWriter(file_name);
			
			String[] names = new String[]
	    			{"all_delays", "gpu_delays", "regular_delays", "all_percentile", "gpu_percentile", "regular_percentile"};
			
			double[][] list_of_lists = {mean, gpu_mean, regular_mean,
		    		percentile, percentile_gpu, percentile_regular};
			
			for(int i=0; i<names.length; i++) {
				String cur_name = names[i];
				double[] cur_data = list_of_lists[i];
				
				StringBuilder builder = new StringBuilder();
				
				builder.append(cur_name + "= [");
				builder.append(cur_data[0]);
				//builder.append(", ");
				
				for(int j=1; j<cur_data.length; j++) {
					builder.append(", ");
					builder.append(cur_data[j]);
					
		    	}
				
				builder.append("];");
				builder.append("\n");
				
				wr.write(builder.toString());
				
			}
			
			wr.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
    
    private static double[] mean_and_percentile_collect(Sink si){
    	double[] lists = {si.getMeanAll(), si.getMeanGPU(),
    			si.getMeanRegular(), si.getPercentileAll(0.9), si.getPercentileGPU(0.9), si.getPercentileRegular(0.9)};
    	return lists;
    }
    
    /*private static PrintStream setup_writer(boolean save_data, String run_name) {
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
    }*/
    
}
