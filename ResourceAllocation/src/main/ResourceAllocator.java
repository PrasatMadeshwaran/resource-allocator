package main;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Stack;
import java.util.stream.Collectors;


public class ResourceAllocator {

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		int hours, cpus;
		double price;
		System.out.println("Enter number of cpu's needed");
		cpus = in.nextInt();
		System.out.println("Enter the number of hours resource needed for");
		hours = in.nextInt();
		System.out.println("Enter the maximum amount you could pay for resources");
		price = in.nextDouble();
		Map<String, Map<String, Double>> instances = initializeMap();
		instances.forEach((region, serverMap) -> {
			if (cpus == 0) {
				double perHourCost = price / hours;
				Stack<Integer> cpuStack= new Stack<Integer>();
				Stack<Double> valueStack= new Stack<Double>();
				prepareSortedMap(serverMap,cpuStack,valueStack);
				Map<Integer,Integer> countMap = new HashMap<>();
				double remainingCost = perHourCost;
				while(remainingCost>0 &&(!valueStack.isEmpty()&&!cpuStack.isEmpty())) {
					if(valueStack.peek()*cpuStack.peek()<remainingCost) {
						int oneValue = (int)Math.abs(remainingCost/(valueStack.peek()*cpuStack.peek()));
						remainingCost = remainingCost-(oneValue*(valueStack.peek()*cpuStack.peek()));
						countMap.put(cpuStack.peek(), oneValue);
					}
					valueStack.pop();
					cpuStack.pop();
				}
				double tCost = price-(remainingCost*hours);
				Map<String,Integer> resultMap = new HashMap<>();
				countMap.forEach((type,count)->{
					resultMap.put(getValue(type), count);
				});
				System.out.println("Region: "+region+" Cost: $"+tCost+" Total CPU's: "+getTotalCpuCount(resultMap));
				System.out.println("Cpu Specifications\n"+resultMap);
				return;
			}else if((price==0||(cpus!=0)&&(price!=0&&hours!=0))) {
				Stack<Integer> cpuStack= new Stack<Integer>();
				Stack<Double> valueStack= new Stack<Double>();
				prepareSortedMap(serverMap,cpuStack,valueStack);
				int remCpuCount=cpus;
				int totalCpuCount =0;
				double totalCost=0;
				Map<Integer,Integer> countMap = new HashMap<>();
				while(remCpuCount>0) {
					if(remCpuCount%cpuStack.peek()!=remCpuCount) {
						int typeCpuCount =(int)Math.abs(remCpuCount/(cpuStack.peek()));
						totalCpuCount +=typeCpuCount*cpuStack.peek();
						double cost =valueStack.peek()*hours*typeCpuCount*cpuStack.peek();
						totalCost+=cost;
						remCpuCount=cpus-totalCpuCount;
						countMap.put(cpuStack.peek(), typeCpuCount);
					}
					valueStack.pop();
					cpuStack.pop();
				}
				Map<String,Integer> resultMap = new HashMap<>();
				countMap.forEach((type,count)->{
					resultMap.put(getValue(type), count);
				});
				if((cpus!=0)&&(price!=0)&&(hours!=0)) {
					if(totalCost<=price) {
						System.out.println("Region: "+region+" Cost: $"+totalCost+" Total CPU's: "+getTotalCpuCount(resultMap));
						System.out.println("Cpu Specifivations\n"+resultMap);
					}else {
						System.out.println("Could not allocate for this requirement. Minimum price for "+getTotalCpuCount(resultMap)+"cpu's is $"+totalCost);
						
					}
				}else {
					System.out.println("Region: "+region+" Cost: $"+totalCost+" Total CPU's: "+getTotalCpuCount(resultMap));
					System.out.println("Cpu Specifications\n"+resultMap);
				}
				return;
		    }
		});
	}
	
	static void prepareSortedMap(Map<String, Double> serverMap, Stack<Integer> cpuStack, Stack<Double> valueStack) {
		Map<String, Double> costPerHourMap = new HashMap<>();
		serverMap.forEach((type, value) -> {
			int cpuCount = getCpuSize(type);
			costPerHourMap.put(type, value / cpuCount);
		});
		Map<String, Double> costCpuPerHrSortedMap = costPerHourMap.entrySet().stream()
				.sorted(Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		costCpuPerHrSortedMap.forEach((type,cost)->{
			cpuStack.push(getCpuSize(type));
			valueStack.push(cost);
		});
	}
	static int getCpuSize(String value) {
		if(value.equals("large")) {
			return 1;
		}else if(value.equals("xlarge")) {
			return 2;
		}else if(value.equals("2xlarge")) {
			return 4;
		}else if(value.equals("4xlarge")) {
			return 8;
		}else if(value.equals("8xlarge")) {
			return 16;
		}else if(value.equals("10xlarge")) {
			return 32;
		}else {
			return 0;
		}
	}
	static String getValue(int value) {
		if(value==1) {
			return "large";
		}else if(value==2) {
			return "xlarge";
		}else if(value==4) {
			return "2xlarge";
		}else if(value==8) {
			return "4xlarge";
		}else if(value==16) {
			return "8xlarge";
		}else if(value==32) {
			return "10xlarge";
		}else {
			return "";
		}
	}
	static int getTotalCpuCount(Map<String,Integer> resultMap) {
		int totalCount=0;
		if(resultMap.get("large")!=null)
			totalCount+=(resultMap.get("large")*1);
		if(resultMap.get("xlarge")!=null)
			totalCount+=(resultMap.get("xlarge")*2);
		if(resultMap.get("2xlarge")!=null)
			totalCount+=(resultMap.get("2xlarge")*4);
		if(resultMap.get("4xlarge")!=null)
			totalCount+=(resultMap.get("4xlarge")*8);
		if(resultMap.get("8xlarge")!=null)
			totalCount+=(resultMap.get("8xlarge")*16);
		if(resultMap.get("10xlarge")!=null)
			totalCount+=(resultMap.get("10xlarge")*32);
		return totalCount;
	}
	static Map<String,Integer> printList(List<Integer> list){
		Map<String,Integer> resultMap = new HashMap<>();
		if(list.contains(1)) 
			resultMap.put("large", Collections.frequency(list, 1));
		if(list.contains(2)) 
			resultMap.put("xlarge", Collections.frequency(list, 2));
		if(list.contains(4)) 
			resultMap.put("2xlarge", Collections.frequency(list, 4));
		if(list.contains(8)) 
			resultMap.put("4xlarge", Collections.frequency(list, 8));
		if(list.contains(16)) 
			resultMap.put("8xlarge", Collections.frequency(list, 16));
		if(list.contains(32)) 
			resultMap.put("10xlarge", Collections.frequency(list, 32));
		return resultMap;
	}
	static Map<String, Map<String,Double>> initializeMap(){
		Map<String, Map<String,Double>> instances = new HashMap<>();
		Map<String, Double> usEastServerMap = new HashMap<>();
		usEastServerMap.put("large", 0.12);
		usEastServerMap.put("xlarge", 0.23);
		usEastServerMap.put("2xlarge", 0.45);
		usEastServerMap.put("4xlarge", 0.774);
		usEastServerMap.put("8xlarge", 1.4);
		usEastServerMap.put("10xlarge", 2.82);
		instances.put("us-east", usEastServerMap);
		Map<String, Double> usWestServerMap = new HashMap<>();
		usWestServerMap.put("large", 0.14);
		usWestServerMap.put("2xlarge", 0.413);
		usWestServerMap.put("4xlarge", 0.89);
		usWestServerMap.put("8xlarge", 1.3);
		usWestServerMap.put("10xlarge", 2.97);
		instances.put("us-west", usWestServerMap);
		return instances;
	}
}
