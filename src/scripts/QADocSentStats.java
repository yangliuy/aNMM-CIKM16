/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts;

import com.FileUtil;
import conf.PathConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author lyang
 */
public class QADocSentStats {
    
    public static void main(String args[]) throws IOException{
        
    	if(args.length < 1){
			System.err.println("please input params: modelInputDataFolder ");
		}
		String modelInputDataFolder = args[0];
        String QASentWordIndexFile = modelInputDataFolder + "qaSentWordIndex";
        ArrayList<String> qaSentLines = new ArrayList<String>();
        //0341
        
        FileUtil.readLines(QASentWordIndexFile, qaSentLines);
        double[] counter = new double[5];
        double sentLenMin = 10;
        double sentLenMax = 0;
        double zeroLenCounter = 0;
        double sentLenSum = 0;
        Map<Integer, Integer> sentLenCountMap = new TreeMap<Integer, Integer>(); //key len/10  value counter
        for(String sentLine : qaSentLines){
            String label = sentLine.split(" ")[1];
            double qSentLen = Double.parseDouble(sentLine.split(" ")[2]);
            double aSentLen = Double.parseDouble(sentLine.split(" ")[3]);
            sentLenMin = Math.min(sentLenMin, aSentLen);
            sentLenMax = Math.max(sentLenMax, aSentLen);
            sentLenSum += aSentLen;
            addSentLenToCountMap(sentLenCountMap, aSentLen);
            if(aSentLen == 0) zeroLenCounter ++;
            if(label.equals("1")) counter[1]++;
            else if(label.equals("2")) counter[2]++;
            else if(label.equals("3")) counter[3]++;
            else if(label.equals("4")) counter[4]++;
            else counter[0]++;
        }
        double totalSentCount = (double)qaSentLines.size();
        for(int i = 0; i < 5; i++){
            System.out.println(i + "\t" + counter[i]);
            System.out.println(i + "\t" + counter[i] / totalSentCount);
        }
        System.out.println("total Sent num: " + totalSentCount);
        System.out.println("sentLen min: " + sentLenMin);
        System.out.println("sentLen max: " + sentLenMax);
        System.out.println("sentLen average: " + sentLenSum / totalSentCount);
        System.out.println("zero len sent count: " + zeroLenCounter);
        
        for(int len : sentLenCountMap.keySet()){
            int count = sentLenCountMap.get(len);
            System.out.println(len + "\t" + count);
        } 
    }

    private static void addSentLenToCountMap(Map<Integer, Integer> sentLenCountMap, double qSentLen) {
        int key = (int)qSentLen / 10;
        if(sentLenCountMap.containsKey(key)){
            sentLenCountMap.put(key, sentLenCountMap.get(key) + 1);
        } else {
            sentLenCountMap.put(key, 1);
        }
    }
}
