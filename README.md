# aNMM-CIKM16

#Implementation of Attention-Based Neural Matching Model
====================================================================

/**
Copyright (C) 2016 by Center for Intelligent Information Retrieval / University of Massachusetts Amherst

The package of aNMM is distributed for research purpose, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

More details can be found in the following paper:

Liu Yang, Qingyao Ai, Jiafeng Guo, W. Bruce Croft, aNMM: Ranking Short Answer Texts with Attention-Based Neural Matching Model, In Proceedings of the 25th ACM International Conference on Information and Knowledge Management (CIKM 2016), Indianapolis, IN, USA. October 24-28, 2016. Full Paper. (https://ciir-publications.cs.umass.edu/pub/web/getpdf.php?id=1240 or http://dl.acm.org/citation.cfm?id=2983818)

Feel free to contact the following people if you find any problems in the package.
lyang@cs.umass.edu * */

#Introduction

1. An alternative to question answering methods based on feature engineering, deep learning approaches such as convolutional neural networks (CNNs) and Long Short-Term Memory Models (LSTMs) have recently been proposed for semantic matching of questions and answers. To achieve good results, however, these models have been combined with additional features such as word overlap or BM25 scores. Without this combination, these models perform significantly worse than methods based on linguistic feature engineering. In this paper, we propose an attention based neural matching model for ranking short answer text. We adopt value-shared weighting scheme instead of position-shared weighting scheme for combining different matching signals and incorporate question term importance learning using question attention network. Using the popular benchmark TREC QA data, we show that the relatively simple aNMM model can significantly outperform other neural network models that have been used for the question answering task, and is competitive with models that are combined with additional features. When aNMM is combined with additional features, it outperforms all baselines.

2. This package implements the aNMM-1 and aNMM-2 model proposed in this paper:Liu Yang, Qingyao Ai, Jiafeng Guo, W. Bruce Croft, aNMM: Ranking Short Answer Texts with Attention-Based Neural Matching Model, In Proceedings of the 25th ACM International Conference on Information and Knowledge Management (CIKM 2016). (https://ciir-publications.cs.umass.edu/pub/web/getpdf.php?id=1240 or http://dl.acm.org/citation.cfm?id=2983818). If you use this code, please cite this paper.

# How to run

### Run with Jar files
  ```
  java -jar aNMM1.jar queryFile trainFile validFile testFile preTrainedWordVecFile ModelResDataFolder dataName modelName binNum vectorDimen runModelType
  ```
  Where
- queryFile: the path of the question files. e.g. data/TRECQA/ModelInputData/trecqa.queries
- trainFile: the path of training file. e.g. data/TRECQA/ModelInputData/train-less-than-40.manual-edit.qsent
- validFile: the path of validation file. e.g. data/TRECQA/ModelInputData/dev-less-than-40.manual-edit.qsent
- testFile: the path of testing file. e.g. data/TRECQA/ModelInputData/test-less-than-40.manual-edit.qsent
- preTrainedWordVecFile: the path of the pre-trained word embedding file. e.g. data/TRECQA/WordVec/wiki_skipgram_win5_dim200.vec
- ModelResDataFolder: the folder to store the model training results. e.g. data/TRECQA/ModelRes/output_v41_softmax_binw/
- dataName: the name of experimental data. The current option is TRECQA.
- modelName: the version of models. current options: V4-1(aNMM-1 setting) for aNMM1.jar and V5-1(aNMM-2 setting) for aNMM2.jar. There are more options which can be found in the code comments of aNMM1.java and aNMM2.java.
- binNum: number of bins in the value-shared weighting scheme.
- vectorDimen: number of word vector dimensions in the pre-trained word embedding.
- runModelType:

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

###　Run by importing this project into Eclipse

The code is organized by the Eclipse IDE. You should be able to import this project into Eclipse after you clone it. This make it more convenient to read and modify the code. If you want to run it from Eclipse, you need to pass the right parameters specified in Section " Run with Jar files" and run aNMM1.java or aNMM2.java to start the program.

# Sample output

### Sample output of aNMM-1
```
create path : data/TRECQA/ModelRes/output_v41_anmm1/
runModelType: Validation
termToWordVectorMap size: 218317
current read doc: data/TRECQA/ModelInputData/train-less-than-40.manual-edit.qsent
current read doc: data/TRECQA/ModelInputData/dev-less-than-40.manual-edit.qsent
output word index done!
total number of QA sent pairs in training data: 4715
total number of QA sent pairs in testing data: 1147
total number of QA triples in training data: 47845
total number of QA triples in testing data: 4392
Iteration/Epoch 1
Test1 Time: 2016-12-09 15:07:33
Current loss on training data: 46035.6151296588
Test2 Time: 2016-12-09 15:07:34
Finish batch 1, update model parameters
Finish batch 2, update model parameters
Finish batch 3, update model parameters
Finish batch 4, update model parameters
Finish batch 5, update model parameters
Finish batch 6, update model parameters
Finish batch 7, update model parameters
Finish batch 8, update model parameters
Finish batch 9, update model parameters
Finish batch 10, update model parameters
Finish batch 11, update model parameters
Finish batch 12, update model parameters
Finish batch 13, update model parameters
Finish batch 14, update model parameters
Finish batch 15, update model parameters
Finish batch 16, update model parameters
Finish batch 17, update model parameters
Finish batch 18, update model parameters
Finish batch 19, update model parameters
Finish batch 20, update model parameters
Finish batch 21, update model parameters
Finish batch 22, update model parameters
Finish batch 23, update model parameters
Finish batch 24, update model parameters
Finish batch 25, update model parameters
Finish batch 26, update model parameters
Finish batch 27, update model parameters
Finish batch 28, update model parameters
Finish batch 29, update model parameters
Finish batch 30, update model parameters
Finish batch 31, update model parameters
Finish batch 32, update model parameters
Finish batch 33, update model parameters
Finish batch 34, update model parameters
Finish batch 35, update model parameters
Finish batch 36, update model parameters
Finish batch 37, update model parameters
Finish batch 38, update model parameters
Finish batch 39, update model parameters
Finish batch 40, update model parameters
Finish batch 41, update model parameters
Finish batch 42, update model parameters
Finish batch 43, update model parameters
Finish batch 44, update model parameters
Finish batch 45, update model parameters
Finish batch 46, update model parameters
Finish batch 47, update model parameters
Finish the last batch, update model parameters
Finish one Epoch!
After scan all the triples, wkChangeSumSquare = 299.6152736347496
After scan all the triples, vpChangeSumSquare = 156.93927404087347
Iteration/Epoch 2
Test1 Time: 2016-12-09 15:10:20
Current loss on training data: 17977.519661668553
Current loss on validation/dev data: 3637.728969222303
Test2 Time: 2016-12-09 15:10:22
Finish batch 1, update model parameters
Finish batch 2, update model parameters
Finish batch 3, update model parameters
Finish batch 4, update model parameters
Finish batch 5, update model parameters
Finish batch 6, update model parameters
Finish batch 7, update model parameters
Finish batch 8, update model parameters
Finish batch 9, update model parameters
Finish batch 10, update model parameters
Finish batch 11, update model parameters
Finish batch 12, update model parameters
Finish batch 13, update model parameters
Finish batch 14, update model parameters
Finish batch 15, update model parameters
Finish batch 16, update model parameters
Finish batch 17, update model parameters
Finish batch 18, update model parameters
Finish batch 19, update model parameters
Finish batch 20, update model parameters
Finish batch 21, update model parameters
Finish batch 22, update model parameters
Finish batch 23, update model parameters
Finish batch 24, update model parameters
Finish batch 25, update model parameters
Finish batch 26, update model parameters
Finish batch 27, update model parameters
Finish batch 28, update model parameters
Finish batch 29, update model parameters
Finish batch 30, update model parameters
Finish batch 31, update model parameters
Finish batch 32, update model parameters
Finish batch 33, update model parameters
Finish batch 34, update model parameters
Finish batch 35, update model parameters
Finish batch 36, update model parameters
Finish batch 37, update model parameters
Finish batch 38, update model parameters
Finish batch 39, update model parameters
Finish batch 40, update model parameters
Finish batch 41, update model parameters
Finish batch 42, update model parameters
Finish batch 43, update model parameters
Finish batch 44, update model parameters
Finish batch 45, update model parameters
Finish batch 46, update model parameters
Finish batch 47, update model parameters
Finish the last batch, update model parameters
Finish one Epoch!
After scan all the triples, wkChangeSumSquare = 35.23044274864642
After scan all the triples, vpChangeSumSquare = 44.33977586672522
Iteration/Epoch 3
Test1 Time: 2016-12-09 15:13:00
Current loss on training data: 14421.003022897045
Current loss on validation/dev data: 3624.0323821569414
Test2 Time: 2016-12-09 15:13:01
Finish batch 1, update model parameters
Finish batch 2, update model parameters
Finish batch 3, update model parameters
Finish batch 4, update model parameters
Finish batch 5, update model parameters
Finish batch 6, update model parameters
...
```

###　Sample output of aNMM-2
```
runModelType: Validation
termToWordVectorMap size: 218317
current read doc: data/TRECQA/ModelInputData/train-less-than-40.manual-edit.qsent
current read doc: data/TRECQA/ModelInputData/dev-less-than-40.manual-edit.qsent
output word index done!
total number of QA sent pairs in training data: 4715
total number of QA sent pairs in testing data: 1147
queryNum in the qaDocSet: 93
queryNum in the qaDocSet: 81
total number of QA triples in training data: 47845
total number of QA triples in testing data: 4392
modelName: V5-1
maxThreadNum which is the available cores: 4
Iteration/Epoch 1
Test1 Time: 2016-12-09 15:25:22
Current loss on training data: 47765.96030856929
Test2 Time: 2016-12-09 15:25:29
Finish batch 1, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:25:38
Finish batch 2, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:25:47
Finish batch 3, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:25:56
Finish batch 4, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:26:03
Finish batch 5, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:26:12
Finish batch 6, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:26:20
Finish batch 7, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:26:27
Finish batch 8, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:26:33
Finish batch 9, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:26:40
Finish batch 10, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:26:47
Finish batch 11, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:26:57
Finish batch 12, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:27:04
Finish batch 13, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:27:10
Finish batch 14, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:27:18
Finish batch 15, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:27:25
Finish batch 16, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:27:32
Finish batch 17, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:27:39
Finish batch 18, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:27:47
Finish batch 19, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:27:56
Finish batch 20, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:28:05
Finish batch 21, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:28:13
Finish batch 22, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:28:20
Finish batch 23, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:28:28
Finish batch 24, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:28:37
Finish batch 25, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:28:44
Finish batch 26, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:28:51
Finish batch 27, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:28:59
Finish batch 28, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:29:07
Finish batch 29, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:29:13
Finish batch 30, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:29:18
Finish batch 31, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:29:26
Finish batch 32, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:29:32
Finish batch 33, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:29:39
Finish batch 34, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:29:49
Finish batch 35, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:29:55
Finish batch 36, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:30:03
Finish batch 37, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:30:10
Finish batch 38, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:30:18
Finish batch 39, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:30:26
Finish batch 40, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:30:34
Finish batch 41, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:30:42
Finish batch 42, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:30:49
Finish batch 43, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:30:57
Finish batch 44, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:31:02
Finish batch 45, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:31:08
Finish batch 46, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:31:15
Finish batch 47, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:31:23
Finish last batch, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:31:29
After scan all the triples, wtkChangeSumSquare = 40.352836438789964
After scan all the triples, rtChangeSumSquare = 35.846697560864406
After scan all the triples, vpChangeSumSquare = 18.72115474157675
Iteration/Epoch 2
Test1 Time: 2016-12-09 15:31:29
Current loss on training data: 47770.88122559699
Current loss on validation/dev data: 4389.849432405724
Test2 Time: 2016-12-09 15:31:36
Finish batch 1, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:31:44
Finish batch 2, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:31:54
Finish batch 3, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:32:02
Finish batch 4, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:32:10
Finish batch 5, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:32:18
Finish batch 6, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:32:26
Finish batch 7, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:32:34
Finish batch 8, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:32:40
Finish batch 9, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:32:46
Finish batch 10, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:32:56
Finish batch 11, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:33:05
Finish batch 12, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:33:13
Finish batch 13, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:33:19
Finish batch 14, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:33:27
Finish batch 15, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:33:34
Finish batch 16, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:33:41
Finish batch 17, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:33:48
Finish batch 18, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:33:54
Finish batch 19, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:34:03
Finish batch 20, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:34:12
Finish batch 21, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:34:20
Finish batch 22, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:34:28
Finish batch 23, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:34:35
Finish batch 24, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:34:44
Finish batch 25, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:34:51
Finish batch 26, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:34:58
Finish batch 27, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:35:06
Finish batch 28, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:35:13
Finish batch 29, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:35:19
Finish batch 30, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:35:25
Finish batch 31, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:35:33
Finish batch 32, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:35:38
Finish batch 33, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:35:46
Finish batch 34, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:35:55
Finish batch 35, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:36:01
Finish batch 36, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:36:09
Finish batch 37, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:36:16
Finish batch 38, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:36:23
Finish batch 39, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:36:32
Finish batch 40, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:36:40
Finish batch 41, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:36:48
Finish batch 42, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:36:55
Finish batch 43, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:37:02
Finish batch 44, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:37:08
Finish batch 45, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:37:13
Finish batch 46, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:37:21
Finish batch 47, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:37:29
Finish last batch, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:37:34
After scan all the triples, wtkChangeSumSquare = 105.8038931047181
After scan all the triples, rtChangeSumSquare = 158.39176885396574
After scan all the triples, vpChangeSumSquare = 74.08961405417756
Iteration/Epoch 3
Test1 Time: 2016-12-09 15:37:34
Current loss on training data: 17726.786863048106
Current loss on validation/dev data: 3576.849487041899
Test2 Time: 2016-12-09 15:37:41
Finish batch 1, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:37:50
Finish batch 2, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:37:59
Finish batch 3, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:38:08
Finish batch 4, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:38:16
Finish batch 5, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:38:24
Finish batch 6, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:38:32
Finish batch 7, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:38:39
Finish batch 8, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:38:46
Finish batch 9, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:38:52
Finish batch 10, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:38:59
Finish batch 11, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:39:08
Finish batch 12, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:39:16
Finish batch 13, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:39:22
...
```



