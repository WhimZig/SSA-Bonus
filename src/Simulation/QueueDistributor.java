package Simulation;

import java.util.ArrayList;

public class QueueDistributor implements ProductAcceptor {
	
	ArrayList<Queue> queues;
	
	public QueueDistributor(ArrayList<Queue> queues) {
		this.queues = queues;
	}

	@Override
	public boolean giveProduct(Product p) {
		
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
		
		return true;
	}
	
}
