package main.anmm1;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import main.core.GenQAMatchMatrixKMaxPooling;
import main.core.QADocDataPreprocess;
import main.core.QADocuments;


/**Main class of the whole data pipeline (V4-1 which is aNMM1)
 * 
 * Step1: Read serialized data and init QA match matrix based on pre-trained word vectors 
 * with word2vec tool. Then do K-max pooling for each query
 * word and output the matrix after K-max pooling.
 * 
 * Step2: Pair-wise training to learning optimal weights
 * 
 * Version update logs:
  	//V0 No gate
  	//V1 HasGate Sigmoid
	//V2 HasGateMQbar Sigmoid
	//V3-2 BinWeight & Has Gate Sigmoid
	//V3-3 BinWeight & HasGateMQbar Sigmoid
	//V4-1 softmax gate function & BinWeight (aNMM-1 model)
	//V4-2 softmax gate function & BinWeight & MQbar
	//v4-3 softmax gate function & BinWeight & Query Term Vector Normalization
	//From V4-4 to V4-6, we integrate IR scores/IDF scores into the model or combine the learning score with WO/BM25 features with logistic regression
	//The final goal is to beat the best performance from SIG15 CDNN paper
	//V4-4 idf as query term weighting & BinWeight
	//V4-5 softmax gate function & idf weighting QAmatchmatrix &BinWeight
	//V4-6 a logistic regression classifier trained with three features: word co-occurrence count, IDF weighted word co-occurrence count, QA match probability learned by nntextmatch
	//V5-1 Add a hidden layer & Multiple bin sizes in the added hidden layer (used fixed bin size firstly then update)
	//Need to implement this setting for softmax gate function which can further improve the performance
	//Consider mini-batch gradient descent to get more robust performance
	 The latest version update see the comments within the main function
 * 
 *
 * @author Liu Yang
 * @email  lyang@cs.umass.edu
 */

public class aNMM1 {

	public static void main(String args[]) throws IOException, ClassNotFoundException{
		
		if(args.length < 11) {
			System.err.println("please input params: queryFile trainFile validationFile testFile preTrainedWordVecFile ModelResDataPath dataName modelName binNum vectorDimen.   all paths should be with /");
			System.exit(1);
		}
		String queryFile = args[0]; //data/TRECQA/ModelInputData/trecqa.queries
		String trainFile = args[1]; //data/TRECQA/ModelInputData/train-less-than-40.manual-edit.qsent
		String validFile = args[2]; //data/TRECQA/ModelInputData/dev-less-than-40.manual-edit.qsent
		String testFile = args[3]; //data/TRECQA/ModelInputData/test-less-than-40.manual-edit.qsent
		String preTrainedWordVecFile = args[4]; //data/TRECQA/WordVec/wiki_skipgram_win5_dim200.vec
		String ModelResDataFolder = args[5]; //data/TRECQA/ModelRes/output_v41_softmax_binw/
		String dataName = args[6]; //options: WebAP TRECQA YahooCQA WikiQA
		String modelName = args[7]; //options: V0(No gate) V1(Has gate) V2(HashGateMinusBar) V3-2 V3-3 V4-1 V4-2 V4-3
		String binNum = args[8]; //number of bins
		String vectorDimen = args[9]; //number of word vector dimensions
		String runModelType = args[10];//options: Validation-- In this run type, the test data is validFile. We want to tune hyper-parameter with validFile
									   //		  Testing-- In this run type, the test data is testFile. We want to do prediction on testFile with the optimal parameters learned on validFile
		
		File modelResPath = new File(ModelResDataFolder);
		if(!modelResPath.exists()) {
			System.out.println("create path : " + ModelResDataFolder);
			modelResPath.mkdir();
		} else {
			System.out.println(ModelResDataFolder + " exist! ");
		}
		System.out.println("runModelType: " + runModelType);
        Map<String, Double[]> termToWordVectorMap = new HashMap<String, Double[]>(); //term -> wordVecString  / the separator is white space
        
        //1. Data preprocess
        //If there are too much negative samples, you need to do negative sampling. We don't need so much negative samples
        //The parameter for controlling the negative sampling ratio: negativeSampleRatio
        //Read both train and test data and compute loss/IR metrics on test data
        QADocuments trainQADocSet= new QADocuments();
        QADocuments testQADocSet = new QADocuments();
        QADocDataPreprocess QADP = new QADocDataPreprocess();
        GenQAMatchMatrixKMaxPooling genQAMatchMaxkP = new GenQAMatchMatrixKMaxPooling();
        genQAMatchMaxkP.initTermToWordVectorMap(termToWordVectorMap, preTrainedWordVecFile);
        trainQADocSet.readQADocs(trainFile, queryFile, true, dataName); //Do subsampling only when isSubsampling is true and dataName = "WebAP"; Otherwise, we don't do data subsampling
        if(runModelType.equals("Validation")){
        	testQADocSet.readQADocs(validFile, queryFile, false, dataName);
        } else if(runModelType.equals("Testing")){
        	testQADocSet.readQADocs(testFile, queryFile, false, dataName);
        } else {
        	System.err.println("Wrong run mode type: " + runModelType);
        }
        
        QADP.printWordDic(trainQADocSet, ModelResDataFolder);
        QADP.printQASentWordIndex(trainQADocSet, ModelResDataFolder);
        //QADP.printWore2VecTrainFile(trainQADocSet, ModelResDataFolder);
        QADP.printWordCountFile(trainQADocSet, ModelResDataFolder);
        QADP.printTermIDFFile(trainQADocSet, ModelResDataFolder);
        //FileUtil.saveClass(qaDocSet, serializedData);
        //qaDocSet = FileUtil.loadClass(qaDocSet, serializedData);
        System.out.println("total number of QA sent pairs in training data: " + trainQADocSet.docs.size());
        System.out.println("total number of QA sent pairs in testing data: " + testQADocSet.docs.size());
        
        //2. GenerateQA Match matrix
        genQAMatchMaxkP.geneateQAMatrix(trainQADocSet, termToWordVectorMap, Integer.valueOf(vectorDimen));
        genQAMatchMaxkP.geneateQAMatrix(testQADocSet, termToWordVectorMap, Integer.valueOf(vectorDimen));

        //String serializedDataWithQAMatrix = PathConfig.ModelInputDataFolder + "WebAPWithQAMatchMatrix.data";
        //FileUtil.saveClass(qaDocSet, serializedDataWithQAMatrix); 
        //In the development phrase, we can output the matrix after K-max pooling for testing purpose
        
        //3. K-max pooling
        //Pool the max K values and maintain the order
        //Update: After V3, we don't do K-max pooling and assign bin specific weight to matching signals
        if(modelName.equals("V0") || modelName.equals("V1") || modelName.equals("V2")){
        	genQAMatchMaxkP.doKMaxPooling(trainQADocSet);
        	genQAMatchMaxkP.doKMaxPooling(testQADocSet);
        }
        
        //4. Pair-wise training to learn optimal weight
        //   Output the final learned weights as the learned model
        //   Predict the matching score on test data and compute evaluation metrics
        PairwiseTraining pwtrain = new PairwiseTraining();
        pwtrain.initWeights(modelName, binNum, vectorDimen);
        //construct QA pos/neg triples and output to a file
        Set<String> trainQATriples = pwtrain.constructQAPNTriples(trainQADocSet);
        Set<String> testQATriples = pwtrain.constructQAPNTriples(testQADocSet);
        //FileUtil.saveClass(trainQADocSet, trainSerializedData);
        //FileUtil.saveClass(testQADocSet, testSerializedData);
        System.out.println("total number of QA triples in training data: " + trainQATriples.size());
        System.out.println("total number of QA triples in testing data: " + testQATriples.size());
        
        //pwtrain.saveQATripleFile(qaTriples, qaTripleFile);
        //Need to pass termToWordVectorMap to get the word embedding of the query words
        pwtrain.generateQrelFile(testQADocSet, ModelResDataFolder);
        
        if(modelName.equals("V0") || modelName.equals("V3-1")){ 
        	System.err.println("Obsolete modelName type : " + modelName);
        	System.exit(0);
        } else if(modelName.equals("V1") || modelName.equals("V2") 
        		|| modelName.equals("V3-2") || modelName.equals("V3-3") 
        		|| modelName.equals("V4-1") || modelName.equals("V4-2") || modelName.equals("V4-3")
        		|| modelName.equals("V4-4")){
        	//V1 HasGate Sigmoid
        	//V2 HasGateMQbar Sigmoid
        	//V3-2 BinWeight & Has Gate Sigmoid
        	//V3-3 BinWeight & HasGateMQbar Sigmoid
        	//V4-1 softmax gate function & BinWeight
        	//V4-2 softmax gate function & BinWeight & MQbar
        	//v4-3 softmax gate function & BinWeight & Query Term Vector Normalization
        	//From V4-4 to V4-7, we integrate IR scores/IDF scores into the model or combine the learning score with WO/BM25 features with logistic regression
        	//The goal is to beat the best performance from SIG15 CDNN paper
        	//V4-4 idf as query term weighting & BinWeight
        	//V4-5 query word embeding norm as the query term weight*BinWeight
        	//V4-6 softmax gate function & idf weighting QAmatchmatrix &BinWeight
        	//V4-7 a logistic regression classifier trained with three features: word co-occurrence count, IDF weighted word co-occurrence count, QA match probability learned by nntextmatch
        	//V4-8 a logistic regression classifier trained with two features: BM25 and QAMatch
        	//V4-9 a logistic regression classifier trained with two features: QL and QAMatch
        	//V5-1 Add a hidden layer & Multiple bin sizes in the added hidden layer  MultiGranBGNN Model (used fixed bin size firstly then update)
        	//Need to implement this setting for softmax gate function which can further improve the performance
        	//Consider mini-batch gradient descent to get more robust performance
        	System.out.println("Current model: aNMM1");
        	pwtrain.pairwiseSGDTraining(trainQADocSet, trainQATriples, testQADocSet, testQATriples, termToWordVectorMap, ModelResDataFolder, modelName, binNum, vectorDimen, runModelType); 
        	
        } else {
        	System.err.println("In main, unsupported modelName type : " + modelName);
        }
    }
}
