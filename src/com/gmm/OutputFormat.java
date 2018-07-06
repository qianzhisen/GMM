package com.gmm;

import java.text.DecimalFormat;
/**
 *  按照设定格式输出小数      2014/9/25
 * @author Wang Wenfu
 *
 */
class OutputFormat {
	private static DecimalFormat fmt = new DecimalFormat("0.00000000 ");

	public static String formatOut(double x) {
		return fmt.format(x);
	}
}

