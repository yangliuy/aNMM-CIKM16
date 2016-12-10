# aNMM-CIKM16

Implementation of Attention-Based Neural Matching Model
====================================================================

/**
Copyright (C) 2016 by Center for Intelligent Information Retrieval / University of Massachusetts Amherst

The package of aNMM is distributed for research purpose, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

If you use this code, please cite the following paper:

Liu Yang, Qingyao Ai, Jiafeng Guo, W. Bruce Croft, aNMM: Ranking Short Answer Texts with Attention-Based Neural Matching Model, In Proceedings of the 25th ACM International Conference on Information and Knowledge Management (CIKM 2016), Indianapolis, IN, USA. October 24-28, 2016. Full Paper. (https://ciir-publications.cs.umass.edu/pub/web/getpdf.php?id=1240 or http://dl.acm.org/citation.cfm?id=2983818)

Feel free to contact the following people if you find any problems in the package.
lyang@cs.umass.edu * */

Brief Introduction
===================

1. An alternative to question answering methods based on feature engineering, deep learning approaches such as convolutional neural networks (CNNs) and Long Short-Term Memory Models (LSTMs) have recently been proposed for semantic matching of questions and answers. To achieve good results, however, these models have been combined with additional features such as word overlap or BM25 scores. Without this combination, these models perform significantly worse than methods based on linguistic feature engineering. In this paper, we propose an attention based neural matching model for ranking short answer text. We adopt value-shared weighting scheme instead of position-shared weighting scheme for combining different matching signals and incorporate question term importance learning using question attention network. Using the popular benchmark TREC QA data, we show that the relatively simple aNMM model can significantly outperform other neural network models that have been used for the question answering task, and is competitive with models that are combined with additional features. When aNMM is combined with additional features, it outperforms all baselines.

2. This package implements the aNMM-1 and aNMM-2 model proposed in this paper: Liu Yang, Qingyao Ai, Jiafeng Guo, W. Bruce Croft, aNMM: Ranking Short Answer Texts with Attention-Based Neural Matching Model, In CIKM 2016. (https://ciir-publications.cs.umass.edu/pub/web/getpdf.php?id=1240 or http://dl.acm.org/citation.cfm?id=2983818). If you use this code, please cite this paper.

3. How to run

  3.1 Run with Jar files
  ```
  java -jar aNMM1.jar queryFile trainFile validFile testFile preTrainedWordVecFile ModelResDataFolder dataName modelName binNum vectorDimen runModelType
  ```
  Where
  
  queryFile: the path of the question files. e.g. data/TRECQA/ModelInputData/trecqa.queries
  trainFile: the path of training file. e.g. data/TRECQA/ModelInputData/train-less-than-40.manual-edit.qsent
  validFile: the path of validation file. e.g. data/TRECQA/ModelInputData/dev-less-than-40.manual-edit.qsent
  testFile: the path of testing file. e.g. data/TRECQA/ModelInputData/test-less-than-40.manual-edit.qsent
  preTrainedWordVecFile: the path of the pre-trained word embedding file. e.g. data/TRECQA/WordVec/wiki_skipgram_win5_dim200.vec
  ModelResDataFolder: the folder to store the model training results. e.g. data/TRECQA/ModelRes/output_v41_softmax_binw/
  dataName: the name of experimental data. The current option is TRECQA.
  modelName: the version of models. current options: V4-1(aNMM-1 setting) for aNMM1.jar and V5-1(aNMM-2 setting) for aNMM2.jar. There are more options which can be found in the code comments of aNMM1.java and aNMM2.java.
  binNum: number of bins in the value-shared weighting scheme.
  vectorDimen: number of word vector dimensions in the pre-trained word embedding.
  runModelType: 
  	current options: 
  	Validation-- In this run type, the test data is validFile. We want to tune hyper-parameters with the validFile.
  	Testing-- In this run type, the test data is testFile. We want to do prediction on testFile with the optimal parameters learned on validFile.
  	
  	Sample parameters:
  	```
  	For aNMM1:

	java -jar aNMM1.jar data/TRECQA/ModelInputData/trecqa.queries data/TRECQA/ModelInputData/train-less-than-40.manual-edit.qsent data/TRECQA/ModelInputData/dev-less-than-40.manual-edit.qsent data/TRECQA/ModelInputData/test-less-than-40.manual-edit.qsent data/TRECQA/WordVec/wiki_skipgram_win5_dim200.vec data/TRECQA/ModelRes/output_v41_anmm1/ TRECQA V4-1 600 200 Validation

	For aNMM2:
	java -jar aNMM2.jar data/TRECQA/ModelInputData/trecqa.queries data/TRECQA/ModelInputData/train-less-than-40.manual-edit.qsent data/TRECQA/ModelInputData/dev-less-than-40.manual-edit.qsent data/TRECQA/ModelInputData/test-less-than-40.manual-edit.qsent data/TRECQA/WordVec/wiki_skipgram_win5_dim200.vec data/TRECQA/ModelRes/output_v51_anmm2/ TRECQA V5-1 200 200 Validation
  	
  	```
  	Note that you can find most of the input files under data/TRECQA/ModelInputData/. The pre-trained word embedding files are too large to be uploaded to Github (max file limit = 100 MB). You can generate the word embedding files by yourself using the tool and data (Wiki Dump) in https://code.google.com/archive/p/word2vec/ . You can also contact me to get the word embedding files I used.

  3.2 Run by importing this project into Eclipse
  The code is organized by the Eclipse IDE. You should be able to import this project into Eclipse after you clone it. This make it more convenient to read and modify the code. If you want to run it from Eclipse, you need to pass the right parameters specified in Section 3.1 and run aNMM1.java or aNMM2.java to start the program.

4. Sample output

Sample output of aNMM-1
```
a
```

Sample output of aNMM-2
```
b
```



