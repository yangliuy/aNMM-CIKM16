package scripts;

import java.util.ArrayList;

import com.FileUtil;

public class generateLambdaMARTRankProbFiles {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int [] NUM_LEAVES = {7, 15, 20, 25};
		double [] MIN_INSTANCE_PERCENTAGE_PER_LEAF = {0.12, 0.25, 0.35};
		double [] FEATURE_SAMPLING = {0.3, 0.5, 1.0};
		double [] LEARNING_RATE = {0.001, 0.01, 0.05,  0.1};
		double [] SUB_SAMPLING = {0.3, 0.5};
		int [] NUM_TREES = {300, 1000, 2000, 3000};
		
		//Try fewer settings/ different settings with RC
//		int [] NUM_LEAVES = {7, 15, 25};
//		double [] MIN_INSTANCE_PERCENTAGE_PER_LEAF = {0.12, 0.25};
//		double [] FEATURE_SAMPLING = {0.5, 0.1};
//		double [] LEARNING_RATE = {0.001,  0.01, 0.1};
//		double [] SUB_SAMPLING = {0.3};
//		int [] NUM_TREES = {1000, 2000};
		
		String rankPropFileFolder =  "pythonCodeCombine/jforests/rank_prop_files_1k/";
		
		for(int num_leaves : NUM_LEAVES){
			for(double min_instance_percentage_per_leaf : MIN_INSTANCE_PERCENTAGE_PER_LEAF){
				for(double feature_sampling : FEATURE_SAMPLING){
					for(double learning_rate : LEARNING_RATE){
						for(double sub_sampling : SUB_SAMPLING){
							for(int num_trees : NUM_TREES){
								writePropFile(rankPropFileFolder, num_leaves, min_instance_percentage_per_leaf, feature_sampling, learning_rate, sub_sampling, num_trees);
							}
						}
					}
				}
			}
		}
	}

	private static void writePropFile(String rankPropFileFolder,
			int num_leaves, double min_instance_percentage_per_leaf,
			double feature_sampling, double learning_rate, double sub_sampling,
			int num_trees) {
		// TODO Auto-generated method stub
		String rankPFile = rankPropFileFolder + num_leaves + "_" + min_instance_percentage_per_leaf + "_" + feature_sampling + "_" + learning_rate + "_" + sub_sampling + "_" + num_trees;
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("trees.num-leaves=" + num_leaves);
		lines.add("trees.min-instance-percentage-per-leaf=" + min_instance_percentage_per_leaf);
		lines.add("trees.feature-sampling=" + feature_sampling);
		lines.add("boosting.learning-rate=" + learning_rate);
		lines.add("boosting.sub-sampling=" + sub_sampling);
		lines.add("boosting.num-trees=" + num_trees);
		lines.add("learning.algorithm=LambdaMART-RegressionTree");
		lines.add("learning.evaluation-metric=NDCG");
		lines.add("params.print-intermediate-valid-measurements=true");
		/*trees.num-leaves={num_leaves}
		trees.min-instance-percentage-per-leaf={min_instance_percentage_per_leaf}
		trees.feature-sampling={feature_sampling}
		boosting.learning-rate={learning_rate:.6f}
		boosting.sub-sampling={sub_sampling}
		boosting.num-trees={num_trees}
		learning.algorithm=LambdaMART-RegressionTree
		learning.evaluation-metric=NDCG

		params.print-intermediate-valid-measurements=true*/
		FileUtil.writeLines(rankPFile, lines);
	}
}
