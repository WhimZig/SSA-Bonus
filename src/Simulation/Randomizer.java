package Simulation;

import java.util.Random;

public class Randomizer {

	public final long seed;
	private final Random source;
	
	public static int ITERATIONS = 20;
	
	public static void main(String[] args) {
		Randomizer r = new Randomizer();
		double T = 0;
		for (int i=0; i < 20; i++) {
			System.out.format("t = %.3f\n", T);
			T += r.nextNonStationaryPoisson(T, 24, 0.8, 2);
		}
		System.out.format("t = %.3f\n", T);
		System.out.println("end");
	}
	
	public Randomizer() {
		this(System.currentTimeMillis());
	}
	public Randomizer(long seed) {
		this.seed = seed;
		this.source = new Random(seed);
	}
	
	private double get_mean_arrival_rate_times_t(double start_time, double end_time, double period, double amplitude, double mean) {
		double T1 = start_time, T2 = end_time, P = period, A = amplitude, M = mean;
		double w = 0.5 * P / Math.PI;
		double Z = w * Math.cos(T1 / w);
		double L = M * (T2-T1) - A * w * Math.cos(T2/w) + A * Z;
		return L; // / (T2 - T1);
	}
	/*
	private double get_mean_arrival_rate(double start_time, double end_time, double period, double amplitude, double mean) {
		return get_mean_arrival_rate_times_t(start_time, end_time, period, amplitude, mean) / (end_time - start_time);
	}*/
	
	public double non_stationary_poisson_CDF(double current_time, double interarrival_time, double period, double amplitude, double mean) {
		double lt = get_mean_arrival_rate_times_t(current_time, current_time + interarrival_time, period, amplitude, mean);
		return 1 - Math.exp(-lt);
	}
	
	public double non_stationary_poisson_PDF(double current_time, double interarrival_time, double period, double amplitude, double mean) {
		double lt = get_mean_arrival_rate_times_t(current_time, current_time + interarrival_time, period, amplitude, mean);
		return (lt / interarrival_time) * Math.exp(- lt);
	}
	
	public double non_stationary_poisson_iCDF(double current_time, double probability, double period, double amplitude, double mean) {
		double x_i = 1/mean;
		// Newton's method using the CDF and the PDF of our non-stationary distribution:
		for (int i=0; i < ITERATIONS; i++) {
			double f = non_stationary_poisson_CDF(current_time, x_i, period, amplitude, mean) - probability;
			double fp = non_stationary_poisson_PDF(current_time, x_i, period, amplitude, mean);
			double x_ip1 = x_i - f / fp;
			x_i = x_ip1;
		}
		double P = non_stationary_poisson_CDF(current_time, x_i, period, amplitude, mean);
		System.out.format("error = %e\n", Math.abs(P - probability));
		return x_i;
	}
	
	public double nextNonStationaryPoisson(double current_time, double period, double amplitude, double mean) {
		double p = source.nextDouble();
		return non_stationary_poisson_iCDF(current_time, p, period, amplitude, mean);
	}
	
}
