package Simulation;

import java.util.ArrayList;

public class QueueDistributor extends Queue {
	
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
	
	@Override
	public boolean askProduct(Machine machine)
	{
		
		
		// This is only possible with a non-empty queue
		if(productQueue.size()>0)
		{
			// If the machine accepts the product
			if(machine.giveProduct(productQueue.get(0)))
			{
				productQueue.remove(0);// Remove it from the queue
				return true;
			}
			else
				return false; // Machine rejected; don't queue request
		}
		else
		{
			// Nick: So this means that the machine is free?
			requests.add(machine);
			return false; // queue request
		}
	}
	
}
