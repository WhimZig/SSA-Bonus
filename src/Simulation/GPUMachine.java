package Simulation;

public class GPUMachine extends Machine {
	
	double meanGPUTime;
	double stdGPU;
	
	public GPUMachine(Queue q, ProductAcceptor s, CEventList e, String n, double m, double var, double gpum, double gpuvar)
	{
		super(e, n);
		status='i';
		queue=q;
		sink=s;
		meanProcTime=m;
		this.std = var;
		this.meanGPUTime = gpum;
		this.stdGPU = gpuvar;
		queue.askProduct(this);
	}
	
	@Override
	protected void startProduction(ProductType type)
	{
		// generate service time
		if(meanProcTime>0)
		{
			double duration;
			
			if(type == ProductType.Normal) {
				//System.out.println("servicing regular task");
				duration = drawRandomNormal(meanProcTime, std);
			} else {
				//System.out.println("servicing GPU task");
				duration = drawRandomNormal(meanGPUTime, stdGPU);
			}
			
			
			// Create a new event in the eventlist
			double time = eventlist.getTime();
			eventlist.add(this,type,time+duration);
			// set status to busy
			status='b';
		}
		else
		{
			if(processingTimes.length>procCnt)
			{
				eventlist.add(this,type,eventlist.getTime()+processingTimes[procCnt]); //target,type,time
				// set status to busy
				status='b';
				procCnt++;
			}
			else
			{
				eventlist.stop();
			}
		}
	}

}
