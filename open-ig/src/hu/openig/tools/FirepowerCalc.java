/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;
/** Calculate firepower composition from weights. */
public final class FirepowerCalc {
	/** Utility class. */
	private FirepowerCalc() {
		
	}
	/**
	 * @param args no arguments
	 */
	public static void main(String[] args) {
		findSum(360, 10, 30, 35);
	}
	/**
	 * Print sums which add up to the total.
	 * @param total the target total
	 * @param weights the weights
	 */
	static void findSum(int total, int... weights) {
		int maxPerItem = 20;
		long[] variables = new long[weights.length];
		long combinations = (long)Math.pow(maxPerItem, weights.length);
		outer:
		for (long n = 0; n < combinations; n++) {
			long idx = n;
			long sum = 0;
			for (int k = 0; k < weights.length; k++) {
				variables[k] = (idx % maxPerItem);
				if (variables[k] == 0) {
					continue outer;
				}
				idx /= maxPerItem;
				sum += variables[k] * weights[k];
			}
			if (sum == total) {
				for (int k = 0; k < weights.length; k++) {
					if (k > 0) {
						System.out.printf(" + ");
					}
					System.out.printf("(%d) * %d", weights[k], variables[k]);
				}
				System.out.printf("%n");
			}
		}
	}
}
