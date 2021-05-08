/**
 *	Example program for using eventlists
 *	@author Joel Karel
 *	@version %I%, %G%
 */

package Simulation;

public class Simulation {

    public CEventList list;
    public Queue queue;
    public Source source;
    public Sink sink;
    public Machine mach;
	

        /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
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
    	l.start(2000); // 2000 is maximum time
    }
    
}
