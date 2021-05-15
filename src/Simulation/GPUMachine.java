package Simulation;

public class GPUMachine extends Machine {
	
	// There are a couple of ways of adding the constructor
	// But this felt good enough, just make it the default time no matter what
	// Makes my life easier I guess
	double meanGPUTime;
	double stdGPU;
	
	public GPUMachine(Queue q, ProductAcceptor s, CEventList e, String n, double m, double var)
	{
		super(e,n);
		status='i';
		queue=q;
		sink=s;
		meanProcTime=m;
		this.std = var;
		this.meanGPUTime = 240;
		this.stdGPU = 50;
		queue.askProduct(this);
	}
	
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
		// generate duration
		if(meanProcTime>0)
		{
			double duration;
			
			if(type == ProductType.Normal) {
				duration = drawRandomNormal(meanProcTime, std);
			} else {
				
				duration = drawRandomNormal(meanGPUTime, stdGPU);
			}
			
			
			// Create a new event in the eventlist
			double tme = eventlist.getTime();
			eventlist.add(this,type,tme+duration); //target,type,time
			// set status to busy
			status='b';
		}
		// I'm leaving everything here as is, just because we're not gonna use it
		// And modifying it felt too hard
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
