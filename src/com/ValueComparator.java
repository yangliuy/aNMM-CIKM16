package com;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator implements Comparator<String>{
	Map<String, Integer> baseMap;
	
	public ValueComparator(Map<String, Integer> base){
		this.baseMap = base;
	}

	@Override
	public int compare(String o1, String o2) {
		// TODO Auto-generated method stub
		if(baseMap.get(o1) >= baseMap.get(o2)){
			return -1;
		} else {
			return 1;
		}
	}
	
}
