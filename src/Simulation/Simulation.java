/**
 *	Example program for using eventlists
 *	@author Joel Karel
 *	@version %I%, %G%
 */

package Simulation;

import java.util.ArrayList;
import java.util.*;

public class Simulation {

    public CEventList list;
    public Queue queue;
    public Source source;
    public Sink sink;
    public Machine mach;
	

        /**
     * @param args the command line arguments
     */
    @SuppressWarnings("unchecked")
	public static void main(String[] args) {
    	
    	/*
    	// Create an eventlist
    	CEventList l = new CEventList();
    	
    	// A queue for the machine
    	// TODO: create a new version of Queue that allows for there to be items with priority
    	// TODO: Allow for there to be an all encompassing queue, so that the source can be common
    	
    	// Here I'll write the 6 queues for the normal events
    	Queue q1 = new Queue();
    	Queue q2 = new Queue();
    	Queue q3 = new Queue();
    	Queue q4 = new Queue();
    	Queue q5 = new Queue();
    	Queue q6 = new Queue();
    	
    	Queue qgpu1 = new Queue();
    	Queue qgpu2 = new Queue();
	
	
    	// The two sources of jobs
    	// TODO: Modify them so that they work with the correct queue and eventlist
    	Source regular = new Source(q1,l,"Regular Source");
    	Source gpu_source = new Source(q2,l,"GPU Source");
    	
    	// A sink
    	// Sink just seems to be the way of getting rid of the items
    	Sink si = new Sink("Sink 1");
    	
    	
    	
    	// A machine
    	// So I assume that these are the machines in charge of processing the stuff
    	// Guess that means there's 8 of them
    	// TODO: Modify so that there is a machine that orders items according to some priority
    	Machine m = new Machine(q1,si,l,"Machine 1");
    	
    	
    	// start the eventlist
    	l.start(2000); // 2000 is maximum time*/
    	
    	CEventList l = new CEventList();
    	// A queue for the machine
    	Queue q1 = new Queue();
    	Queue q2 = new Queue();
    	Queue q3 = new Queue();
    	Queue q4 = new Queue();
    	Queue q5 = new Queue();
    	Queue q6 = new Queue();
    	Queue gpuq1 = new GPUQueue();
    	Queue gpuq2 = new GPUQueue();
    	
    	
    	ArrayList<Queue> qlist = new ArrayList<>();
    	qlist.add(q1);
    	qlist.add(q2);
    	qlist.add(q3);
    	qlist.add(q4);
    	qlist.add(q5);
    	qlist.add(q6);
    	qlist.add(gpuq1);
    	qlist.add(gpuq2);
    	
    	
    	QueueDistributor qd = new QueueDistributor(qlist);
    	
    	
    	// sources don't need to be assigned to variables because they are linked to l and qd
    	new Source(qd,l,"Source normal", 30, ProductType.Normal);
    	new Source(qd,l,"Source GPU", 360, ProductType.GPU);
    	
    	
    	// The Sink
    	Sink si = new Sink("Sink 1");
    	// machines don't need to be assigned to variables because they are linked to l and the q's
    	new Machine(q1,si,l,"Machine 1", 145, 42);
    	new Machine(q2,si,l,"Machine 2", 145, 42);
    	new Machine(q3,si,l,"Machine 3", 145, 42);
    	new Machine(q4,si,l,"Machine 4", 145, 42);
    	new Machine(q5,si,l,"Machine 5", 145, 42);
    	new Machine(q6,si,l,"Machine 6", 145, 42);
    	
    	new GPUMachine(gpuq1,si,l, "GPUMachine 1", 145, 42);
    	new GPUMachine(gpuq2,si,l, "GPUMachine 2", 145, 42);
    	
    	// debug off
    	Sink.DEBUG = false;
    	Source.DEBUG = false;
    	Machine.DEBUG = false;
    	
    	// start the eventlist
    	l.start(20000); // 2000 is maximum time
    	
    	// collecting data for matlab:
    	ArrayList<Double>[] lists = new ArrayList[] {si.getRegularDelays(), si.getGPUDelays(),
    			si.getAllDelays(), si.getRegularTimes(), si.getGPUTimes(), si.getAllTimes()};
    	String[] names = new String[]
    			{"reg_delays", "gpu_delays", "all_delays", "reg_times", "gpu_times", "all_times"};
    	
    	// set to false if you don't want all that output
    	boolean print_for_python_or_matlab = true;
    	
    	if (print_for_python_or_matlab)
	    	for (int i=0; i < lists.length; i++) { // CANADA uses "." as decimal seperator
	    		System.out.format(Locale.CANADA, "%s = [%e", names[i], lists[i].get(0));
	    		for (int k=1; k < lists[i].size(); k++)
	    			System.out.format(Locale.CANADA, ", %e", lists[i].get(k));
	    		System.out.println("];");
	    	}
    	
    	System.out.format("Regular mean delay time - %.3f\nGPU mean delay time - %.3f\n"+
    	"Overall mean delay time - %.3f\nRegular 90th percentile delay time - %.3f\n"+
    	"GPU 90th percentile delay time - %.3f\nAll 90th percentile delay time - %.3f\n",
    			si.getMeanRegular(), si.getMeanGPU(), si.getMeanAll(),
    			si.getPercentileRegular(0.9), si.getPercentileGPU(0.9), si.getPercentileAll(0.9));
    	
    }
    
}
