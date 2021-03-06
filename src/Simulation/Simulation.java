/**
 * A class for running the simulation from the main method
 *
 * This class foes not contain any instance variables and only contains static methods
 *
 **/

package Simulation;

import java.util.ArrayList;
import java.util.function.Function;
import java.io.FileWriter;
import java.io.IOException;

public class Simulation {

	public static final int SINK_DEBUG 		= 1 << 0; // 1
	public static final int SOURCE_DEBUG 	= 1 << 1; // 2
	public static final int MACHINE_DEBUG 	= 1 << 2; // 4
	public static final int GET_RESULTS 	= 1 << 3; // 8
	public static final int STORE_TO_FILE 	= 1 << 4; // 16

	public static int DEBUG = GET_RESULTS | STORE_TO_FILE;

	public static void main(String[] args) {
		int size = 1000;
		double time = 10080*4;

		run_all(size, time, "output2_routine_0.txt", "output2_routine_1.txt", "output2_routine_2.txt");

    }

	/** Run all three setups of simulation to be compared and save their data to seperate files */
	public static void run_all(int size, double time, String filename0, String filename1, String filename2) {
		run_and_save(size, time, filename0, Simulation::simulate_default);
		run_and_save(size, time, filename1, Simulation::simulate_seperate);
		run_and_save(size, time, filename2, Simulation::simulate_smart);
	}

	/** Run the simulation of a certain type and store its data to a file */
	public static void run_and_save(int size, double time, String filename, Function<Double, double[]> simulator) {
		double[] mean_delay = new double[size];
		double[] mean_gpu_delay = new double[size];
		double[] mean_regular_delay = new double[size];
		double[] percentile_90_delay = new double[size];
		double[] percentile_90_gpu_delay = new double[size];
		double[] percentile_90_regular_delay = new double[size];


		for(int j=0; j<size; j++) {
			double[] results = simulator.apply(time);

			mean_delay[j] = results[0];
	    	mean_gpu_delay[j] = results[1];
	    	mean_regular_delay[j] = results[2];
	    	percentile_90_delay[j] = results[3];
	    	percentile_90_gpu_delay[j] = results[4];
	    	percentile_90_regular_delay[j] = results[5];
		}

		store_data(mean_delay, mean_gpu_delay, mean_regular_delay, percentile_90_delay,
				percentile_90_gpu_delay, percentile_90_regular_delay, filename);
	}

	public static double[] simulate_default(double max_time) {
		return simulate_default(max_time, DEBUG);
	}
	public static double[] simulate_default(double max_time, int DEBUG) {
		return simulate_default(6, 2, max_time, DEBUG);
	}
	public static double[] simulate_default(int num_CPU_cores, int num_GPU_cores, double max_time, int DEBUG) {
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

    	double[] results = mean_and_percentile_collect(si);

		return results;
    }

	public static double[] simulate_seperate(double max_time) {
		return simulate_seperate(max_time, DEBUG);
	}
	public static double[] simulate_seperate(double max_time, int DEBUG) {
		return simulate_seperate(6, 2, max_time, DEBUG);
	}
	public static double[] simulate_seperate(int num_CPU_cores, int num_GPU_cores, double max_time, int DEBUG) {
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

    	double[] results = mean_and_percentile_collect(si);

		return results;
    }

	public static double[] simulate_smart(double max_time) {
		return simulate_smart(max_time, DEBUG);
	}
	public static double[] simulate_smart(double max_time, int DEBUG) {
		return simulate_smart(6, 2, max_time, DEBUG);
	}
	public static double[] simulate_smart(int num_CPU_cores, int num_GPU_cores, double max_time, int DEBUG) {
		CEventList l = new CEventList();
    	// A queue for the machine

    	Queue RQ = new Queue();
    	GPUQueue GQ = new GPUQueue();

    	// The Sink
    	Sink si = new Sink("Sink 1");
    	// machines don't need to be assigned to variables because they are linked to l and the q's
    	ArrayList<Machine> regs = new ArrayList<>();
    	for (int i=0; i < num_CPU_cores; i++) regs.add(new Machine(RQ, si, l, "Machine "+(i+1), 145, 42));
    	ArrayList<GPUMachine> gpus = new ArrayList<>();
    	for (int i=0; i < num_GPU_cores; i++) gpus.add(new GPUMachine(GQ, si, l, "GPUMachine "+(i+1), 145, 42, 240, 50));

    	QueueDistributor qd = new QueueDistributor(RQ, GQ, regs, gpus);
    	attach_source(l, qd);

    	start(l, si, max_time, DEBUG, "smart");

    	double[] results = mean_and_percentile_collect(si);

		return results;
    }

	private static void attach_source(CEventList l, ProductAcceptor qd) {
		// sources don't need to be assigned to variables because they are linked to l and qd
    	// rates are given in "per hour", convert to "per minute" by taking '/60'
    	// period is given in hours, so we multiply that by 60 to get it in minutes
    	new Source(qd,l,"Source normal", 2./60, 0.8/60, 24.*60);
    	new Source(qd,l,"Source GPU", 300);
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

			// uncomment to store data for visualizations
    	//FullDataCollect.data_collect(si, get_results, save_data, file_name);
	}

	/** storing to file of the 6 main performance measures (means and 90th-percentiles for gpu, regular and the total) */
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
			e.printStackTrace();
		}


    }

    /** extracting of the 6 main performance measures from a Sink object after simulation*/
    private static double[] mean_and_percentile_collect(Sink si){
    	double[] lists = {si.getMeanAll(), si.getMeanGPU(),
    			si.getMeanRegular(), si.getPercentileAll(0.9), si.getPercentileGPU(0.9), si.getPercentileRegular(0.9)};
    	return lists;
    }

}
