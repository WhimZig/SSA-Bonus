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
	public static boolean DEBUG = true;
	
	/** Eventlist that will be requested to construct events */
	private CEventList list;
	/** Queue that buffers products for the machine */
	private ProductAcceptor queue;
	/** Name of the source */
	private String name;
	/** Interarrival times (in case pre-specified) */
	private double[] interarrivalTimes;
	/** Interarrival time iterator */
	private int interArrCnt;
	/** true if using data instead of distributions*/
	private boolean useData;
	
	private double meanS;
	private double meanNS;
	private double amplitude;
	private double period;
	
	// Random number generator and its seed
	// Can be modified to allow for different seeds
	protected static long seed = System.currentTimeMillis();
	static {
		System.out.println("Seed used for Source : "+seed);
	}
	protected static Random generator = new Random(seed);
	protected static Randomizer randomizer = new Randomizer(seed);
	
	ProductType prod;
	
	/**
	 * @param q any product acceptor (for example, a QueueDistributor)
	 * @param l an event-list
	 * @param n the name of this source
	 * @param e the product-type that this source exclusively generates
	 * @param meanS the mean inter-arrival-time for GPU products
	 * @param meanNS the mean arrival-rate for CPU products
	 * @param amplitude the amplitude of the periodic CPU product arrival distribution
	 * @param period the period of the periodic CPU product arrival distribution
	 */
	public Source(ProductAcceptor q, CEventList l, String n, ProductType e, 
			double meanS, double meanNS, double amplitude, double period) {
		list = l;
		queue = q;
		name = n;
		prod = e;
		useData = false;
		this.meanS = meanS;
		this.meanNS = meanNS;
		this.amplitude = amplitude;
		this.period = period;
		switch (e) {
			case GPU: list.add(this,prod,drawRandomExponential(meanS)); break;
			default: list.add(this,prod,drawNonStationaryExponential
					(l.getTime(), period, amplitude, meanNS));
		}
	}
	
	/*
	 * GPU task generator with the given mean inter-arrival-time
	 */
	public Source(ProductAcceptor q,CEventList l,String n, double m_iat)
	{
		this(q, l, n, ProductType.GPU, m_iat, 2./60, 0.8/60, 24.*60);
	}
	/**
	 * CPU task generator with the given periodic qualities
	 */
	public Source(ProductAcceptor q, CEventList l, String n, double m_rate, double amplitude, double period) {
		this(q, l, n, ProductType.Normal, 0.2/60, m_rate, amplitude, period);
	}

	/**
	*	Source from data
	*//*
	* Not used
	public Source(ProductAcceptor q,CEventList l,String n,double[] ia)
	{
		list = l;
		queue = q;
		name = n;
		useData = true;
		interarrivalTimes=ia;
		interArrCnt=0;
		list.add(this,prod,interarrivalTimes[0]); //target,type,time
	}*/
	
    @Override
	public void execute(ProductType type, double time)
	{
		// show arrival if needed
		if (DEBUG) System.out.println("Arrival at time = " + time + " of product " + type);
		// give arrived product to queue
		Product p = new Product(type);
		p.stamp(time,"Creation",name);
		queue.giveProduct(p);
		// generate duration
		if(!useData)
		{
			
			double duration = -1;
			
			switch (prod) {
				case GPU: duration = drawRandomExponential(meanS); break;
				default: duration = 
						drawNonStationaryExponential(list.getTime(), period, amplitude, meanNS);
			}
			// Create a new event in the eventlist
			list.add(this,prod,time+duration); //target,type,time
		}
		else {
			interArrCnt++;
			if(interarrivalTimes.length>interArrCnt)
			{
				list.add(this,prod,time+interarrivalTimes[interArrCnt]); //target,type,time
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
		// using the inverse transformation method
		double res = -mean*Math.log(u);
		return res;
	}
	
	public static double drawNonStationaryExponential(double time, double period, double amplitude, double mean) {
		return randomizer.nextNonStationaryPoisson(time, period, amplitude, mean);
	}
	
	// This assumes that it'll always be with a sinusoid version
	// Method given follows thinning algorithm suggested here: http://www.columbia.edu/~ks20/4404-Sigman/4404-Notes-NSP.pdf
	// this method was deprecated as we finished implementation of the inverse transformation method (in Randomizer.java) earlier
	// (this method, shown below, is not functioning correctly)
	@Deprecated
	public double drawNonStationaryExponential(double mean, double cur_time, double amplitude) {
		double time = turnToDayTime(cur_time);
		
		// The upper bound is very simple to get
		// Just saving it in case it's needed in another weird way
		double upperBound = mean + Math.abs(amplitude)*Math.abs(mean);
		boolean invalid_number = true;
		
		double u = generator.nextDouble();
		
		double final_value = -(1.0/upperBound)*Math.log(u);
		
		while(invalid_number) {
			
			double validity_check = generator.nextDouble();
			
			double evaluation_at_t = evaluateSinusoidTime(mean, time, amplitude)/upperBound;
			
			if (validity_check <= evaluation_at_t) {
				invalid_number = false;
				break;
			}
			
			u = generator.nextDouble();
			
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
		return time%24;
	}
	
	/*
	 * I really need to understand how the time is working related to period
	 * Because this value is just kinda lazy and assumes the time is in perfect format
	 * Which it really isn't
	 */
	public static double evaluateSinusoidTime(double mean, double time, double amplitude) {
		return mean + amplitude*Math.sin(time);
	}
	
	/*
	 * Extensive testing of thinning vs inverse (Newton):
	 *  - inverse matches 1/rate(t) very closely
	 *  - thinning seems to not change much over time and just matches the overal mean
	 *  
	 *  - I don't know what's wrong with thinning.. I tried fixing it but I can't find a way
	 * 
	public static void main(String[] args) {
		
		//
		CEventList l = new CEventList();
    	ArrayList<Queue> qlist = new ArrayList<>();
    	qlist.add(new Queue());
    	qlist.add(new Queue());
    	QueueDistributor qd = new QueueDistributor(qlist);
    	//
		
		Source s = new Source(qd, l, "testing");
		Randomizer r = new Randomizer();
		double mean = 2;
		double amplitude = 0.8;
		double period = 24;
		// rate = mean + amplitude * sin( T * 2pi / period)
		double max_T = 72;
		int iters = 10000;
		Randomizer.ITERATIONS = 12;
		ArrayList<ArrayList<Double>> vals_N = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> vals_P = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> times = new ArrayList<Double>();
		double total_time_N = 0, total_time_P = 0;
		
		for (double T=0; T <= max_T; T += 0.5) {
			times.add(T);
			ArrayList<Double> list_N = new ArrayList<Double>();
			ArrayList<Double> list_P = new ArrayList<Double>();
			
			long tim = System.currentTimeMillis();
			for (int i=0; i < iters; i++)
				list_N.add(s.drawNonStationaryExponential(mean, T, amplitude));
			total_time_N += (System.currentTimeMillis() - tim) / 1000d;
			
			tim = System.currentTimeMillis();
			for (int i=0; i < iters; i++)
				list_P.add(r.nextNonStationaryPoisson(T, period, amplitude, mean));
			total_time_P += (System.currentTimeMillis() - tim) / 1000d;
			
			vals_N.add(list_N);
			vals_P.add(list_P);
		}
		
		System.out.format("time N : %e seconds\ntime P : %e seconds\n", 
				total_time_N / (iters * times.size()), 
				total_time_P / (iters * times.size()));
		
		double total_error_N = 0;
		double total_error_P = 0;
		
		ArrayList<Double> Ts = new ArrayList<>();
		ArrayList<Double> Ns = new ArrayList<>();
		ArrayList<Double> Ps = new ArrayList<>();
		
		for (int i=0; i < times.size(); i++) {
			ArrayList<Double> N = vals_N.get(i);
			ArrayList<Double> P = vals_P.get(i);
			double sum_N = 0, sum_P = 0;
			for (int j=0; j < iters; j++) {
				sum_N += N.get(j);
				sum_P += P.get(j); }
			double mean_N = sum_N / iters;
			double mean_P = sum_P / iters;
			double mean_T = 1 / (mean + amplitude * Math.sin(times.get(i) * 2 * Math.PI / period));
			double error_N = Math.abs(mean_T - mean_N) / mean_T;
			double error_P = Math.abs(mean_T - mean_P) / mean_T;
			total_error_N += error_N;
			total_error_P += error_P;
			Ts.add(mean_T);
			Ns.add(mean_N);
			Ps.add(mean_P);
			
			//System.out.format("%.3f; %.3f; %.3f -> %.3f; %,3f\n", mean_N, mean_P, mean_T, error_N, error_P);
		}
		System.out.format("error N : %e\nerror P : %e\n", 
				total_error_N / (times.size()), 
				total_error_P / (times.size()));
		
		System.out.print("Ts = ["+Ts.get(0));
		for (int i=1; i < Ts.size(); i++)
			System.out.print(", "+Ts.get(i));
		System.out.println("]");
		
		System.out.print("Ps = ["+Ps.get(0));
		for (int i=1; i < Ps.size(); i++)
			System.out.print(", "+Ps.get(i));
		System.out.println("]");
		
		System.out.print("Ns = ["+Ns.get(0));
		for (int i=1; i < Ns.size(); i++)
			System.out.print(", "+Ns.get(i));
		System.out.println("]");
		
		System.out.print("times = ["+times.get(0));
		for (int i=1; i < times.size(); i++)
			System.out.print(", "+times.get(i));
		System.out.println("]");
	}
	*/
	
}