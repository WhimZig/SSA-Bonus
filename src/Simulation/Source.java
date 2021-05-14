package Simulation;

import java.util.Random;

/**
 *	A source of products
 *	This class implements CProcess so that it can execute events.
 *	By continuously creating new events, the source keeps busy.
 *	@author Joel Karel
 *	@version %I%, %G%
 */
public class Source implements CProcess
{
	/** Eventlist that will be requested to construct events */
	private CEventList list;
	/** Queue that buffers products for the machine */
	private ProductAcceptor queue;
	/** Name of the source */
	private String name;
	/** Mean interarrival time */
	private double meanArrTime;
	/** Interarrival times (in case pre-specified) */
	private double[] interarrivalTimes;
	/** Interarrival time iterator */
	private int interArrCnt;
	
	// Random number generator and its seed
	// Should be modified to allow for different seeds, but this works
	protected long seed = 4;
	protected Random generator = new Random(seed);
	
	
	ProductType prod;

	/**
	*	Constructor, creates objects
	*        Interarrival times are exponentially distributed with mean 33
	*	@param q	The receiver of the products
	*	@param l	The eventlist that is requested to construct events
	*	@param n	Name of object
	*/
	public Source(ProductAcceptor q,CEventList l,String n)
	{
		list = l;
		queue = q;
		name = n;
		meanArrTime=33;
		this.prod = ProductType.Normal;
		// put first event in list for initialization
		list.add(this,prod,drawRandomExponential(meanArrTime)); //target,type,time
	}
	
	public Source(ProductAcceptor q,CEventList l,String n, ProductType e)
	{
		list = l;
		queue = q;
		name = n;
		meanArrTime=33;
		prod = e;
		// put first event in list for initialization
		list.add(this,prod,drawRandomExponential(meanArrTime)); //target,type,time
	}

	/**
	*	Constructor, creates objects
	*        Interarrival times are exponentially distributed with specified mean
	*	@param q	The receiver of the products
	*	@param l	The eventlist that is requested to construct events
	*	@param n	Name of object
	*	@param m	Mean arrival time
	*/
	public Source(ProductAcceptor q,CEventList l,String n,double m)
	{
		list = l;
		queue = q;
		name = n;
		meanArrTime=m;
		// put first event in list for initialization
		list.add(this,prod,drawRandomExponential(meanArrTime)); //target,type,time
	}

	/**
	*	Constructor, creates objects
	*        Interarrival times are prespecified
	*	@param q	The receiver of the products
	*	@param l	The eventlist that is requested to construct events
	*	@param n	Name of object
	*	@param ia	interarrival times
	*/
	public Source(ProductAcceptor q,CEventList l,String n,double[] ia)
	{
		list = l;
		queue = q;
		name = n;
		meanArrTime=-1;
		interarrivalTimes=ia;
		interArrCnt=0;
		// put first event in list for initialization
		list.add(this,prod,interarrivalTimes[0]); //target,type,time
	}
	
        @Override
	public void execute(ProductType type, double tme)
	{
		// show arrival
		System.out.println("Arrival at time = " + tme);
		// give arrived product to queue
		Product p = new Product(type);
		p.stamp(tme,"Creation",name);
		queue.giveProduct(p);
		// generate duration
		if(meanArrTime>0)
		{
			double duration = drawRandomExponential(meanArrTime);
			// Create a new event in the eventlist
			list.add(this,prod,tme+duration); //target,type,time
		}
		else
		{
			interArrCnt++;
			if(interarrivalTimes.length>interArrCnt)
			{
				list.add(this,prod,tme+interarrivalTimes[interArrCnt]); //target,type,time
			}
			else
			{
				list.stop();
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
	
	// This assumes that it'll always be with a sinusoid version
	// Method givne follows thinning algorithm suggested here: http://www.columbia.edu/~ks20/4404-Sigman/4404-Notes-NSP.pdf
	public double drawNonStationaryExponential(double mean, double cur_time, double amplitude) {
		double time = turnToDayTime(cur_time);
		
		// The upper bound is very simple to get
		// Just saving it in case it's needed in another weird way
		double upperBound = mean + Math.abs(amplitude)*Math.abs(mean);
		boolean invalid_number = true;
		
		double u = this.generator.nextDouble();
		
		double final_value = -(1.0/upperBound)*Math.log(u);
		
		// I guess I can transform this into a if true value later?
		// I'll worry about it later I guess
		while(invalid_number) {
			
			double validity_check = this.generator.nextDouble();
			
			double evaluation_at_t = evaluateSinusoidTime(mean, time, amplitude)/upperBound;
			
			if (validity_check <= evaluation_at_t) {
				invalid_number = false;
				break;
			}
			
			u = this.generator.nextDouble();
			
			final_value = -(1.0/upperBound)*Math.log(u);
			
		}
		
		return final_value;
	}
	
	/*
	 * Temporary helper method
	 * The objective of this is to transform a given time into what the time is related to a given day
	 * So if the time says it's 30 hours, it'll transform it into 6 hours
	 * Currently just divides by 24, as I'm not confident with how time is being processed
	 * But this should be modified later on I guess
	 * Having the method separately does make things easier
	 */
	public static double turnToDayTime(double time) {
		return time/24;
	}
	
	/*
	 * I really need to understand how the time is working related to period
	 * Because this value is just kinda lazy and assumes the time is in perfect format
	 * Which it really isn't
	 */
	public static double evaluateSinusoidTime(double mean, double time, double amplitude) {
		return mean + amplitude*Math.sin(time);
	}
	
}