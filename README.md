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

2. This package implements the aNMM-1 and aNMM-2 model proposed in the following paper: Liu Yang, Qingyao Ai, Jiafeng Guo, W. Bruce Croft, aNMM: Ranking Short Answer Texts with Attention-Based Neural Matching Model, In Proceedings of the 25th ACM International Conference on Information and Knowledge Management (CIKM 2016), Indianapolis, IN, USA. October 24-28, 2016. Full Paper. (https://ciir-publications.cs.umass.edu/pub/web/getpdf.php?id=1240 or http://dl.acm.org/citation.cfm?id=2983818) . If you use this code, please cite this paper.

3. How to run

  3.1 Run with Jar files

  3.2 Run by importing this project into Eclipse

4. Sample output

Will updated more later.



