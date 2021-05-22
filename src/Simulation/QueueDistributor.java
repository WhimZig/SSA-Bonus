package Simulation;

import java.util.ArrayList;

public class QueueDistributor implements ProductAcceptor {
	
	public static final int DEFAULT_ROUTINE = 0;
	public static final int TWO_QUEUES = 1;
	public static final int SMART_QUEUE = 2;
	
	// For default state
	ArrayList<Queue> queues;
	
	// For 'two-queues'/'seperate' state
	Queue rQueue;
	GPUQueue gQueue;
	
	// For smart-queue state
	ArrayList<Machine> regs;
	ArrayList<GPUMachine> gpus;
	
	private final int routine;
	
	public QueueDistributor(ArrayList<Queue> queues) {
		this.queues = queues;
		routine = DEFAULT_ROUTINE;
	}
	public QueueDistributor(Queue regular_queue, GPUQueue gpu_queue) {
		rQueue = regular_queue;
		gQueue = gpu_queue;
		routine = TWO_QUEUES;
	}
	public QueueDistributor(Queue regular_queue, GPUQueue gpu_queue, ArrayList<Machine> reg_machines, ArrayList<GPUMachine> gpu_machines) {
		rQueue = regular_queue;
		gQueue = gpu_queue;
		regs = new ArrayList<>(reg_machines);
		gpus = new ArrayList<>(gpu_machines);
		routine = SMART_QUEUE;
	}

	@Override
	public boolean giveProduct(Product p) {
		
		switch(routine) {
		case (TWO_QUEUES)	: two_queue_routine(p); break;
		case (SMART_QUEUE)	: smart_queue_routine(p); break;
		default				: default_routine(p);
		}
		
		return true;
	}
	
	/**
	 * Give the product to the smallest queue it can go in;
	 * GPU queue size is the sum of both sub-queues
	 */
	public void default_routine(Product p) {
		if(p.prod == ProductType.GPU) {
			int small = Integer.MAX_VALUE;
			Queue correct = null;
			
			for(Queue q : queues) {
				if(q instanceof GPUQueue) {
					if(q.getSize() < small) {
						small = q.getSize();
						correct = q;
					}
				}
			}
			// If this somehow screws up, we cry
			correct.giveProduct(p);
			
		} else {
		
			int small = Integer.MAX_VALUE;
			Queue correct = null;
			
			for(Queue q : queues) {
				if(q.getSize() < small) {
					small = q.getSize();
					correct = q;
				}
			}
			correct.giveProduct(p);
		}
	}
	
	public void two_queue_routine(Product p) {
		switch (p.prod) {
		case GPU	: gQueue.giveProduct(p); break;
		default 	: rQueue.giveProduct(p);
		}
	}
	
	public void smart_queue_routine(Product p) {
		switch (p.prod) {
		case GPU	: gQueue.giveProduct(p); break;
		default 	: 
			for (Machine m : regs) if (m.isIdle()) { rQueue.giveProduct(p); return; }
			for (GPUMachine m : gpus) if (m.isIdle()) { gQueue.giveProduct(p); return; }
			rQueue.giveProduct(p);
		}
	}
	
}
