package Simulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 
 * Put this data collection code in a seperate file because it caused runtime errors in older java versions
 */
public final class FullDataCollect {

	@SuppressWarnings("unchecked")
	public static void data_collect(Sink si, boolean get_py_or_mat_data, boolean store_to_file, String file_name) {
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
