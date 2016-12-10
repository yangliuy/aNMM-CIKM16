package conf;

public class ModelParams {

    //Parameters for model
	
	public static int maxPoolingSizeK = 10;
	
	//public static int binNum = 21; move to cmd to tune this parameter more quickly
	
	public static int preprocessASentMinLen = 2;
	
	public static int preprocessASentMaxLen = 500;
	
	//public static int wordVecDimension = 200; move to cmd to tune this parameter more quickly
	
	public static double[] wk;
	
	public static double[] vp;
	
	public static double[] rt;
	
	public static double[][] wtk; //a T by K matrix after adding a hidden layer. This is the parameter from input layer to hidden layer 1
	
	public static double eta1 = 0.025; //Word2Vec by Mikolov set initial learning rate as 0.025
	
	public static double eta2 = 0.025; //Word2Vec by Mikolov set initial learning rate as 0.025
	
	public static double eta3 = 0.025;
	
	public static double wvChangeThreshold = 0.00001;
	
	public static double lossChangeThreshold = 0.00001;
	
	public static double negativeSampleRatio = 0.01; // suggested value is 0.05-0.1 originalPos : originalNeg = 1:140 only sample for WebAP
	
	public static int iterations = 300; // max number of iterations / epochs
	
	public static int saveStep = 25;
	
	public static int beginSaveIters = 5;
	
	public static int batchSize = 1000;// batchSize in mini-batch gradient descent
	
	public static double defaultIDFValue = 8.0;// IDFMap may not contain some rare terms in testing data, we use default idf value for it
	
	public static int addedHiddenLayerRTDim = 10; // the dimension/node number of the added hidden layer which is also the dimension of the parameter rt. will tune this parameter later
	
	public static double mu = 10; //parameter in QL score
	
	//public static int maxThreadNum = 10;// the max number of threads. should be similar to the number of cores on the PC
	
	
}
