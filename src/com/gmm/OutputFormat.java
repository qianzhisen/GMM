package com.gmm;

import java.text.DecimalFormat;
/**
 *  �����趨��ʽ���С��      2014/9/25
 * @author Wang Wenfu
 *
 */
class OutputFormat {
	private static DecimalFormat fmt = new DecimalFormat("0.00000000 ");

	public static String formatOut(double x) {
		return fmt.format(x);
	}
}

