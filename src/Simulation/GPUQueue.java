package Simulation;

import java.util.ArrayList;

public class GPUQueue extends Queue {
	
	private ArrayList<Product> priority;
	
	public GPUQueue() {
		super();
		priority = new ArrayList<>();
	}
	
	/**
	*	Asks a queue to give a product to a machine
	*	True is returned if a product could be delivered; false if the request is queued
	*/
	public boolean askProduct(Machine machine)
	{
		
		if(priority.size() > 0) {
			// If the machine accepts the product
			if(machine.giveProduct(priority.get(0)))
			{
				priority.remove(0);// Remove it from the queue
				return true;
			}
			else {
				// Here we assume that if there's no space for a priority object
				// Then there's no way it'll accept a normal product
				return false; // Machine rejected; don't queue request
			}
			
		} else {
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
				requests.add(machine);
				return false; // queue request
			}
			
		}

	}
	
	/**
	*	Offer a product to the queue
	*	It is investigated whether a machine wants the product, otherwise it is stored
	*/
	public boolean giveProduct(Product p)
	{
		// Check if the machine accepts it
		if(requests.size()<1) {
			if(p.prod == ProductType.Normal) {
				productQueue.add(p); // Otherwise store it
			} else {
				priority.add(p);
			}
		} else {
			boolean delivered = false;
			
			while(!delivered & (requests.size()>0))
			{
				delivered=requests.get(0).giveProduct(p);
				// remove the request regardless of whether or not the product has been accepted
				requests.remove(0);
			}
			
			// Determining in which queue to store the task
			if(!delivered) {
				if(p.prod == ProductType.Normal) {
					productQueue.add(p); // Otherwise store it
				} else {
					priority.add(p);
				}
			}
			
		}
		return true;
	}
	
	public int getSize() {
		return productQueue.size() + priority.size();
	}

}
