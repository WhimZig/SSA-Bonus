package Simulation;

import java.util.ArrayList;
/**
 *	A sink
 *	@author Joel Karel
 *	@version %I%, %G%
 */
public class Sink implements ProductAcceptor
{
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
	/** Counter to number products */
	private int number;
	/** Name of the sink */
	private String name;
	
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
			System.out.print(e.get(i));
			System.out.println(t.get(i));
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
				start = t.get(i);
			}
			else if (e.get(i).equals("Production started")){
				start = t.get(i);
			}
		}
		if(p.prod==ProductType.GPU) {
			GPUDelay.add((start-created));
		}else {
			regularDelay.add((start-created));
		}
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
}