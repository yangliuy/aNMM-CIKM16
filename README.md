# aNMM-CIKM16

# Implementation of Attention-Based Neural Matching Model Proposed in CIKM16 for Answer Sentence Selection

/**
Copyright (C) 2016 by Center for Intelligent Information Retrieval / University of Massachusetts Amherst

The package of aNMM is distributed for research purpose, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

More details can be found in the following paper:

Liu Yang, Qingyao Ai, Jiafeng Guo, W. Bruce Croft, aNMM: Ranking Short Answer Texts with Attention-Based Neural Matching Model, In Proceedings of the 25th ACM International Conference on Information and Knowledge Management (CIKM 2016), Indianapolis, IN, USA. October 24-28, 2016. Full Paper. (https://ciir-publications.cs.umass.edu/pub/web/getpdf.php?id=1240 or http://dl.acm.org/citation.cfm?id=2983818)

Feel free to contact the following people if you find any problems in the package.
lyang@cs.umass.edu * */

# Introduction

1. As an alternative to question answering methods based on feature engineering, deep learning approaches such as convolutional neural networks (CNNs) and Long Short-Term Memory Models (LSTMs) have recently been proposed for semantic matching of questions and answers. To achieve good results, however, these models have been combined with additional features such as word overlap or BM25 scores. Without this combination, these models perform significantly worse than methods based on linguistic feature engineering. In this paper, we propose an attention based neural matching model for ranking short answer text. We adopt value-shared weighting scheme instead of position-shared weighting scheme for combining different matching signals and incorporate question term importance learning using question attention network. Using the popular benchmark TREC QA data, we show that the relatively simple aNMM model can significantly outperform other neural network models that have been used for the question answering task, and is competitive with models that are combined with additional features. When aNMM is combined with additional features, it outperforms all baselines.

2. This package implements the aNMM-1 and aNMM-2 model proposed in this paper:

	Liu Yang, Qingyao Ai, Jiafeng Guo, W. Bruce Croft, aNMM: Ranking Short Answer Texts with Attention-Based Neural Matching Model, In Proceedings of the 25th ACM International Conference on Information and Knowledge Management (CIKM 2016). (https://ciir-publications.cs.umass.edu/pub/web/getpdf.php?id=1240 or http://dl.acm.org/citation.cfm?id=2983818). 

	If you use this code, please cite this paper.

# How to run

### Run with Jar files
  ```
  java -jar aNMM1.jar queryFile trainFile validFile testFile preTrainedWordVecFile ModelResDataFolder dataName modelName binNum vectorDimen runModelType
  ```
  Where
- queryFile: the path of the question files. e.g. data/TRECQA/ModelInputData/trecqa.queries
- trainFile: the path of the training file. e.g. data/TRECQA/ModelInputData/train-less-than-40.manual-edit.qsent
- validFile: the path of the validation file. e.g. data/TRECQA/ModelInputData/dev-less-than-40.manual-edit.qsent
- testFile: the path of the testing file. e.g. data/TRECQA/ModelInputData/test-less-than-40.manual-edit.qsent
- preTrainedWordVecFile: the path of the pre-trained word embedding file. e.g. data/TRECQA/WordVec/wiki_skipgram_win5_dim200.vec
- ModelResDataFolder: the folder to store the model training results. e.g. data/TRECQA/ModelRes/output_v41_softmax_binw/
- dataName: the name of experimental data. The current option is TRECQA.
- modelName: the version of models. Current options: V4-1(aNMM-1 setting) for aNMM1.jar and V5-1(aNMM-2 setting) for aNMM2.jar. There are more options which can be found in the code comments of aNMM1.java and aNMM2.java.
- binNum: number of bins in the value-shared weighting scheme.
- vectorDimen: number of word vector dimensions in the pre-trained word embedding.
- runModelType:

Validation-- In this run type, the test data is validFile. We want to tune hyper-parameters with the validFile.
	
Testing-- In this run type, the test data is testFile. We want to do prediction on testFile with the optimal hyper-parameters learned on validFile.
 
Sample parameters:
  ```
For aNMM1:
java -jar aNMM1.jar data/TRECQA/ModelInputData/trecqa.queries data/TRECQA/ModelInputData/train-less-than-40.manual-edit.qsent data/TRECQA/ModelInputData/dev-less-than-40.manual-edit.qsent data/TRECQA/ModelInputData/test-less-than-40.manual-edit.qsent data/TRECQA/WordVec/wiki_skipgram_win5_dim200.vec data/TRECQA/ModelRes/output_v41_anmm1/ TRECQA V4-1 600 200 Validation
	
For aNMM2:
java -jar aNMM2.jar data/TRECQA/ModelInputData/trecqa.queries data/TRECQA/ModelInputData/train-less-than-40.manual-edit.qsent data/TRECQA/ModelInputData/dev-less-than-40.manual-edit.qsent data/TRECQA/ModelInputData/test-less-than-40.manual-edit.qsent data/TRECQA/WordVec/wiki_skipgram_win5_dim200.vec data/TRECQA/ModelRes/output_v51_anmm2/ TRECQA V5-1 200 200 Validation
  ```

Note that you can find most of the input files under data/TRECQA/ModelInputData/. The pre-trained word embedding files are too large to be uploaded to Github (max file limit = 100 MB). You can generate the word embedding files by yourself using the tool and data (Wiki Dump) in https://code.google.com/archive/p/word2vec/ or use the [Glove embedding](http://nlp.stanford.edu/projects/glove/). You can also contact me to get the word embedding files I used.

### Run by importing this project into Eclipse

The code is organized by the Eclipse IDE. You should be able to import this project into Eclipse after you clone it. This makes it more convenient to read and modify the code. If you want to run it from Eclipse, you need to pass the right parameters specified in Section "Run with Jar files" and run aNMM1.java or aNMM2.java to start the program.

# Sample output

```
### Sample output of aNMM-1
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
......
Finish batch 47, update model parameters
Finish the last batch, update model parameters
Finish one Epoch!
After scan all the triples, wkChangeSumSquare = 299.6152736347496
After scan all the triples, vpChangeSumSquare = 156.93927404087347
......

### Sample output of aNMM-2
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
......
Test3 Time when finished one batch: 2016-12-09 15:31:23
Finish last batch, update model parameters
Test3 Time when finished one batch: 2016-12-09 15:31:29
After scan all the triples, wtkChangeSumSquare = 40.352836438789964
After scan all the triples, rtChangeSumSquare = 35.846697560864406
After scan all the triples, vpChangeSumSquare = 18.72115474157675
......
```

# Compute MRR/MAP Metrics
You can compute MRR/MAP metrics with the qrel file and model score files under data/ModelRes folder with [trec_eval](http://trec.nist.gov/trec_eval/) tool. The program can generate the qrel file based on the annotations in TREC QA data for you.

# Download the TREC QA Data
You can download the TREC QA data from [ACL Wiki on Question Answering](https://aclweb.org/aclwiki/Question_Answering_(State_of_the_art)).

This ACL Wiki page also contains the state of the art methods for question answering with TREC QA data, which could be baselines in your future research.

# FAQ
### Q1:Is it possible for you to share the embeddings you have used in your experiments?
A1:  You can download the 200 dimensional word embedding I used in my experiments from here:

https://drive.google.com/file/d/0B8N4GAk1fdLEc0Npd2wzdkVYaEk/view?usp=sharing

For the other embedding files in 300, 500, 700, 900 dimensions, they are too big to be uploaded to my Google Drive. (total > 5 G) . So I suggest you to use the 200 dimensional word embedding file to start your experiments and then you can try other embedding files from https://code.google.com/archive/p/word2vec/ or https://nlp.stanford.edu/projects/glove/ . You can use different kinds of pre-trained word embeddings for aNMM as long as you explain your setting clearly in your paper or report.

# Change Logs
- Will add the implementation with Python/Tensorflow/Keras later. 
- The implementation of aNMM using Python/Tensorflow/Keras will be based on MatchZoo toolkit (https://github.com/faneshion/MatchZoo). You can take a look at this deep text matching toolkit to see the implementations of popular deep text matching models such as ARC-I/ARC-II, DSSM, CDSSM, MatchPyramid, MS-LSTM, DRMM, etc. We will add more deep text matching models into it. Stay tuned.


