package Simulation;

import java.util.Random;

/**
 *	Machine in a factory
 *	@author Joel Karel
 *	@version %I%, %G%
 */
public class Machine implements CProcess,ProductAcceptor
{
	/** Product that is being handled  */
	protected Product product;
	/** Eventlist that will manage events */
	protected final CEventList eventlist;
	/** Queue from which the machine has to take products */
	protected Queue queue;
	/** Sink to dump products */
	ProductAcceptor sink;
	/** Status of the machine (b=busy, i=idle) */
	protected char status;
	/** Machine name */
	protected final String name;
	/** Mean processing time */
	protected double meanProcTime;
	/** Processing times (in case pre-specified) */
	protected double[] processingTimes;
	/** Processing time iterator */
	protected int procCnt;
	
	/** 
	 * Standard deviation
	 */
	protected double std;
	
	// I'm making it static so that I can easily modify it later on
	// That way each machine can easily have a different seed
	static long seed = 0;
	Random rand_generator = new Random(seed);

	/**
	 * Constructor
	 * 	Idea behind it's existence is just to allow the GPU machine to have a default way of using it
	 * 	Shouldn't really be used by itself, as it's worthless
	 * @param e
	 * @param name
	 */
	
	public Machine(CEventList e, String name) {
		this.eventlist = e;
		this.name = name;
	}
	
	
	/**
	*	Constructor
	*        Service times are exponentially distributed with mean 30
	*	@param q	Queue from which the machine has to take products
	*	@param s	Where to send the completed products
	*	@param e	Eventlist that will manage events
	*	@param n	The name of the machine
	*/
	public Machine(Queue q, ProductAcceptor s, CEventList e, String n)
	{
		status='i';
		queue=q;
		sink=s;
		eventlist=e;
		name=n;
		meanProcTime=30;
		// Just assuming default var, just because
		std = 1;
		queue.askProduct(this); // this adds the machine to the reference list of the queue
	}

	/**
	*	Constructor
	*        Service times are exponentially distributed with specified mean
	*	@param q	Queue from which the machine has to take products
	*	@param s	Where to send the completed products
	*	@param e	Eventlist that will manage events
	*	@param n	The name of the machine
	*        @param m	Mean processing time
	*/
	public Machine(Queue q, ProductAcceptor s, CEventList e, String n, double m)
	{
		status='i';
		queue=q;
		sink=s;
		eventlist=e;
		name=n;
		meanProcTime=m;
		// I'll just assume standard variance
		this.std = 1;
		queue.askProduct(this);
	}
	
	public Machine(Queue q, ProductAcceptor s, CEventList e, String n, double m, double var)
	{
		status='i';
		queue=q;
		sink=s;
		eventlist=e;
		name=n;
		meanProcTime=m;
		this.std = var;
		queue.askProduct(this);
	}
	
	/**
	*	Constructor
	*        Service times are pre-specified
	*	@param q	Queue from which the machine has to take products
	*	@param s	Where to send the completed products
	*	@param e	Eventlist that will manage events
	*	@param n	The name of the machine
	*        @param st	service times
	*/
	public Machine(Queue q, ProductAcceptor s, CEventList e, String n, double[] st)
	{
		status='i';
		queue=q;
		sink=s;
		eventlist=e;
		name=n;
		meanProcTime=-1;
		processingTimes=st;
		procCnt=0;
		queue.askProduct(this);
	}

	/**
	*	Method to have this object execute an event
	*	@param type	The type of the event that has to be executed
	*	@param tme	The current time
	*/
	public void execute(ProductType type, double tme)
	{
		// show arrival
		System.out.println("Product finished at time = " + tme);
		// Remove product from system
		product.stamp(tme,"Production complete",name);
		sink.giveProduct(product);
		product=null;
		// set machine status to idle
		status='i';
		// Ask the queue for products
		queue.askProduct(this);
	}
	
	/**
	*	Let the machine accept a product and let it start handling it
	*	@param p	The product that is offered
	*	@return	true if the product is accepted and started, false in all other cases
	*/
        @Override
	public boolean giveProduct(Product p)
	{
		// Only accept something if the machine is idle
		if(status=='i')
		{
			// accept the product
			product=p;
			// mark starting time
			product.stamp(eventlist.getTime(),"Production started",name);
			// start production
			startProduction(p.prod);
			// Flag that the product has arrived
			return true;
		}
		// Flag that the product has been rejected
		else return false;
	}
	
	/**
	*	Starting routine for the production
	*	Start the handling of the current product with an exponentionally distributed processingtime with average 30
	*	This time is placed in the eventlist
	*/
	private void startProduction(ProductType type)
	{
		// generate duration
		if(meanProcTime>0)
		{
			double duration = drawRandomNormal(meanProcTime, std);
			// Create a new event in the eventlist
			double tme = eventlist.getTime();
			eventlist.add(this,type,tme+duration); //target,type,time
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

	public static double drawRandomExponential(double mean)
	{
		// draw a [0,1] uniform distributed number
		double u = Math.random();
		// Convert it into a exponentially distributed random variate with mean 33
		double res = -mean*Math.log(u);
		return res;
	}
	
	// I modified it to guarantee no values less than 1
	// As that's required by the general system
	// Not the best implementation, but it works
	protected double drawRandomNormal(double mean, double std) {
		double val = mean + rand_generator.nextGaussian()*std;
		if (val < 1) {
			val = 1.0;
		}
		
		return val;
	}

}