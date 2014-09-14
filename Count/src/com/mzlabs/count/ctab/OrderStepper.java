package com.mzlabs.count.ctab;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.mzlabs.count.util.IntVec;

public final class OrderStepper {
	public final int dim;
	public final int bound;
	private final BigInteger[] factorial;
	private final Map<Integer,Iterable<int[]>> seqCache = new HashMap<Integer,Iterable<int[]>>();
	
	/**
	 * 
	 * @param dim>0
	 * @param bound>=0
	 */
	private OrderStepper(final int dim, final int bound) {
		this.dim = dim;
		this.bound = bound;
		if((dim<=0)||(bound<0)) {
			throw new IllegalArgumentException("(" + dim + "," + bound + ")");
		}
		factorial = new BigInteger[Math.max(2,dim+1)];
		factorial[0] = BigInteger.ONE;
		factorial[1] = BigInteger.ONE;
		for(int i=2;i<=dim;++i) {
			factorial[i] = factorial[i-1].multiply(BigInteger.valueOf(i));
		}
	}
	
	private static final Map<IntVec,OrderStepper> stepperCache = new HashMap<IntVec,OrderStepper>();
	
	/**
	 * 
	 * @param dim>0
	 * @param bound>=0
	 */
	public static OrderStepper mkStepper(final int dim, final int bound) {
		final IntVec key = new IntVec(new int[] {dim,bound});
		OrderStepper cached = null;
		synchronized (stepperCache) {
			cached = stepperCache.get(key);
			if(null==cached) {
				cached = new OrderStepper(dim,bound);
				stepperCache.put(key,cached);
			}
		}
		return cached;
	}
	
	/**
	 * 
	 * @param targetSum if >0 check if there is valid start
	 * @return
	 */
	public int[] first(final int targetSum) {
		final int[] x = new int[dim];
		if(targetSum>0) {
			if(advanceLEIs(x,targetSum)) {
				return x;
			} else {
				return null;
			}
		} else {
			return x;
		}
	}
	
	/**
	 * step through all x s.t. 0<=x<=b and x[i+1]>=x[i]
	 * @param bounds
	 * @param x start at all zeros
	 * @return true if valid vector
	 */
	public boolean advanceLEI(final int[] x) {
		// find right-most advanceble position
		int i = dim-1;
		do {
			if(x[i]<bound) {
				final int nv = x[i]+1;
				for(int j=i;j<dim;++j) {
					x[j] = nv;
				}
				return true;
			}
			--i;
		} while(i>=0);
		return false;
	}
	
	public boolean advanceLEIs(final int[] x, final int targetSum) {
		while(true) {
			if(!advanceLEI(x)) {
				return false;
			}
			int sum = 0;
			for(final int xi: x) {
				sum += xi;
			}
			if(targetSum==sum) {
				return true;
			}
		}
	}
	
	/**
	 * TODO: buld one of these that is space and time efficient
	 * @param targetSum>=0
	 * @return
	 */
	public Iterable<int[]> stepSequencesA(final int targetSum) {
		final Integer key = targetSum;
		Iterable<int[]> cached = null;
		synchronized (seqCache) {
			cached = seqCache.get(key);
			if(null==cached) {
				final ArrayList<int[]> steps = new ArrayList<int[]>(2000);
				if(null==cached) {
					int[] x = first(targetSum);
					do {
						steps.add(Arrays.copyOf(x,x.length));
					} while(advanceLEIs(x,targetSum));
				}
				cached = steps;
				seqCache.put(key, cached);
			}
		}
		return cached;
	}
	
	/**
	 * @param x a sorted vector of integers of length dim
	 * @return number of distinct permutations of x
	 */
	public BigInteger nPerm(final int[] x) {
		BigInteger r = factorial[dim];
		int runLength = 1;
		for(int i=0;i<dim;++i) {
			if((i+1>=dim)||(x[i+1]!=x[i])) {
				if(runLength>1) {
					r = r.divide(factorial[runLength]);
				}
				runLength = 1;
			} else {
				runLength += 1;
			}
		}
		return r;
	}
	
	/**
	 * should have sum_{advance(x)} nPerm(x) = (bound+1)^dim
	 * 
	 */
	public boolean checks() {
		final int[] x = first(-1);
		BigInteger sum = BigInteger.ZERO;
		do {
			final BigInteger nperm = nPerm(x);
			//System.out.println(Arrays.toString(x) + "\t" + nperm);
			sum = sum.add(nperm);
		} while(advanceLEI(x));
		final BigInteger check = BigInteger.valueOf(bound+1).pow(dim);
		return check.compareTo(sum)==0;
	}
}