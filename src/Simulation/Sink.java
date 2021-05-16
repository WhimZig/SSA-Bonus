package Simulation;

import java.util.ArrayList;
import java.util.Collections;
/**
 *	A sink
 *	@author Joel Karel, Nicolas, Simi, Pascal
 *	@version %I%, %G%
 */
public class Sink implements ProductAcceptor
{
	public static boolean DEBUG = true;
	
	/** All products are kept */
	private ArrayList<Product> products;
	/** All properties of products are kept */
	private ArrayList<Integer> numbers;
	private ArrayList<Double> times;
	private ArrayList<String> events;
	private ArrayList<String> stations;
	private ArrayList<ProductType> producttypes;
	private ArrayList<Double> regularDelay;
	private ArrayList<Double> GPUDelay;
	private ArrayList<Double> allDelays;
	// starting time of jobs; in order to plot and test periodicity of jobs
	private ArrayList<Double> regularTimes;
	private ArrayList<Double> GPUTimes;
	private ArrayList<Double> allTimes;
	/** Counter to number products */
	private int number;
	/** Name of the sink */
	private String name;
	
	private double totalDelay = 0;
	private double totalGPUDelay = 0;
	
	/**
	*	Constructor, creates objects
	*/
	public Sink(String n)
	{
		name = n;
		products = new ArrayList<>();
		numbers = new ArrayList<>();
		times = new ArrayList<>();
		events = new ArrayList<>();
		stations = new ArrayList<>();
		producttypes = new ArrayList<>();
		regularDelay = new ArrayList<>();
		GPUDelay = new ArrayList<>();
		allDelays = new ArrayList<>();
		regularTimes = new ArrayList<>();
		GPUTimes = new ArrayList<>();
		allTimes = new ArrayList<>();
		number = 0;
	}
	
        @Override
	public boolean giveProduct(Product p)
	{
		number++;
		products.add(p);
		producttypes.add(p.prod);
		// store stamps
		ArrayList<Double> t = p.getTimes();
		ArrayList<String> e = p.getEvents();
		ArrayList<String> s = p.getStations();
		for(int i=0;i<t.size();i++)
		{
			numbers.add(number);
			times.add(t.get(i));
			events.add(e.get(i));
			stations.add(s.get(i));
		}
		
		// calculate delay
		double created = 0;
		double start = 0;
		for(int i=0;i<t.size();i++)
		{
			if (e.get(i).equals("Creation")){
				created = t.get(i);
			}
			else if (e.get(i).equals("Production started")){
				start = t.get(i);
			}
		}
		double delay = start - created;
		// add it to the list so that it is ordered
		if(p.prod==ProductType.GPU) {
			GPUDelay.add(delay);
			GPUTimes.add(start);
			totalGPUDelay += delay;
		}else {
			regularDelay.add(delay);
			regularTimes.add(start);
			totalDelay += delay;
		}
		allDelays.add(delay);
		allTimes.add(start);
		
		print_delay();
		return true;
	}
	
	public int[] getNumbers()
	{
		numbers.trimToSize();
		int[] tmp = new int[numbers.size()];
		for (int i=0; i < numbers.size(); i++)
		{
			tmp[i] = (numbers.get(i)).intValue();
		}
		return tmp;
	}

	public double[] getTimes()
	{
		times.trimToSize();
		double[] tmp = new double[times.size()];
		for (int i=0; i < times.size(); i++)
		{
			tmp[i] = (times.get(i)).doubleValue();
		}
		return tmp;
	}

	public String[] getEvents()
	{
		String[] tmp = new String[events.size()];
		tmp = events.toArray(tmp);
		return tmp;
	}

	public String[] getStations()
	{
		String[] tmp = new String[stations.size()];
		tmp = stations.toArray(tmp);
		return tmp;
	}
	
	@SuppressWarnings("unchecked")
	private double getPercentile(ArrayList<Double> list, double percent) {
		if (percent < 0 || percent > 1) throw new AssertionError("percent must be within 0 and 1");
		ArrayList<Double> sorted = (ArrayList<Double>) list.clone();
		Collections.sort(sorted);
		return sorted.get((int) Math.ceil(0.9*sorted.size())-1);
	}
	
	// getters galore
	public double getPercentileRegular(double percent) 	{ return getPercentile(regularDelay, percent); 	}
	public double getPercentileGPU(double percent) 		{ return getPercentile(GPUDelay, percent); 		}
	public double getPercentileAll(double percent) 		{ return getPercentile(allDelays, percent); 	}
	public double getMeanRegular() 	{ return totalDelay / regularDelay.size(); 				}
	public double getMeanGPU() 		{ return totalGPUDelay / GPUDelay.size(); 				}
	public double getMeanAll() 		{ return (totalDelay+totalGPUDelay) / allDelays.size(); }
	public ArrayList<Double> getRegularDelays() { return regularDelay; 	}
	public ArrayList<Double> getGPUDelays() 	{ return GPUDelay; 		}
	public ArrayList<Double> getAllDelays() 	{ return allDelays; 	}
	public ArrayList<Double> getRegularTimes() 	{ return regularTimes; 	}
	public ArrayList<Double> getGPUTimes() 		{ return GPUTimes; 		}
	public ArrayList<Double> getAllTimes() 		{ return allTimes; 		}
	
	public void print_delay() {
		if (!DEBUG) return;
		System.out.println("Regular mean delay time - " + getMeanRegular());
		System.out.println("GPU mean delay time - " + getMeanGPU());
		System.out.println("Overall mean delay time - " + getMeanAll());
		if(regularDelay.size()>0)
			System.out.println("Regular 90th percentile delay time - " + getPercentileRegular(0.9));
		if(GPUDelay.size()>0)
			System.out.println("GPU 90th percentile delay time - " + getPercentileGPU(0.9));
		if(allDelays.size()>0)
			System.out.println("Overall 90th percentile delay time - " + getPercentileAll(0.9));
	}
}