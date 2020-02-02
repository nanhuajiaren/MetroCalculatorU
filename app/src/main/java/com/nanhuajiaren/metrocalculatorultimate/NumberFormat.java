package com.nanhuajiaren.metrocalculatorultimate;

public final class NumberFormat
{
	public static String showNumberInNoLessThan(int number,int length){
		if(number < 0){
			return number + "";
		}else{
			String s = number + "";
			while(s.length() < length){
				s = "0" + s;
			}
			return s;
		}
	}
}
