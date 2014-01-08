package mainAlg;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class GenStats {
	Map<String, Business> businessMap;
	Map<String, Review> reviewMap;
	Map<String, User> userMap;
	static Map<String, ArrayList<Double>> ubStar = new HashMap<String, ArrayList<Double>> ();

	class ValueComparator implements Comparator<String> {

		Map<String, Integer> base;
		public ValueComparator(Map<String, Integer> base) {
			this.base = base;
		}

		// Note: this comparator imposes orderings that are inconsistent with equals.    
		public int compare(String a, String b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			} // returning 0 would merge keys
		}
	}

	void fillUBStar(HashSet<Review> reviews){
		for (Review review : reviews){
			String keyV = review.b_id;
			if (!ubStar.containsKey(keyV)) {
				ubStar.put(keyV, new ArrayList<Double> ());
			}
			ubStar.get(keyV).add(review.stars);
		}
	}

	public GenStats() throws IOException {
		GenData data = new GenData();
		System.out.println("finish reading data");
		businessMap = GenData.getBusinessMap();
		reviewMap = GenData.getReviewMap();
		userMap = GenData.getUserMap();
		fillUBStar(new HashSet<Review>(reviewMap.values()));
	}

	public void categoryDist() {
		int totalBusinessCnt = 0;
		Map<String, Integer> categoryCntMap = new HashMap<String,Integer>();
		for (Business business:businessMap.values()) {
			//System.out.println("hello");
			for (String category:business.categories) {
				totalBusinessCnt += 1;
				if (!categoryCntMap.containsKey(category)) {
					categoryCntMap.put(category,0);
				}
				categoryCntMap.put(category,categoryCntMap.get(category)+1);
			}
		}
		System.out.println("Number of categories:"+categoryCntMap.size());
		ValueComparator bvc =  new ValueComparator(categoryCntMap);
		TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
		for (String key:categoryCntMap.keySet()) {
			sorted_map.put(key,categoryCntMap.get(key));
		}
		//sorted_map.putAll(categoryCntMap);
		int count = 0;
		DefaultPieDataset pieDataset = new DefaultPieDataset();
		double percentTot = 0.0;
		System.out.println("begin plotting");
		List<String> keys = new ArrayList<String> (sorted_map.keySet());
		List<Integer> values = new ArrayList<Integer>(sorted_map.values());
		//		for (String key:sorted_map.keySet()) {
		//			//System.out.println("hello");
		//			System.out.println(key);
		//			System.out.println(categoryCntMap.get(key));
		//			System.out.println(sorted_map.get(key));
		//			double percent = (double)sorted_map.get(key)/totalBusinessCnt;
		//			percentTot += percent;
		//			pieDataset.setValue(key, percent);
		//			count ++;
		//			if (count == 10) break;
		//		}

		for (int i = 0; i < keys.size(); i++) {
			double percent = (double)values.get(i)/totalBusinessCnt;
			percentTot += percent;
			pieDataset.setValue(keys.get(i), percent);
			count ++;
			if (count == 10) break;
		}
		System.out.println(percentTot);
		if (percentTot != 1) {
			pieDataset.setValue("other",1-percentTot);
		}

		JFreeChart chart = ChartFactory.createPieChart ("Category Distribution", pieDataset, true, true, false);
		try {
			ChartUtilities.saveChartAsJPEG(new File("piechart.jpg"), chart, 500, 300);
		} catch (Exception e) {
			System.out.println("Problem occurred creating chart.");
		}
	}

	public void businessScoreDist() {
		double totalScore = 0;
		int totalBusinessCnt = 0;
		Map<Double, Integer> businessScoreMap = new TreeMap<Double, Integer>();
		Map<Double, Integer> newBusinessScoreMap = new TreeMap<Double, Integer>();
		for (Business business:businessMap.values()) {
			//System.out.println("hello");
			totalBusinessCnt += 1;
			double star = business.stars;
			totalScore += star;
			if (!businessScoreMap.containsKey(star)) {
				businessScoreMap.put(star,0);
			}
			businessScoreMap.put(star,businessScoreMap.get(star)+1);
		}
		System.out.println("Business avg score:" + totalScore/totalBusinessCnt);
		for (Business business:businessMap.values()) {
			totalBusinessCnt += 1;
			double star = roundScore(business.newstars);
			totalScore += star;
			if (!newBusinessScoreMap.containsKey(star)) {
				newBusinessScoreMap.put(star,0);
			}
			newBusinessScoreMap.put(star,newBusinessScoreMap.get(star)+1);
		}
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (Double d:businessScoreMap.keySet()) {
			dataset.setValue((double)businessScoreMap.get(d)/totalBusinessCnt,"Old Score",String.valueOf(d));
			dataset.setValue((double)newBusinessScoreMap.get(d)/totalBusinessCnt,"New Score",String.valueOf(d));
		}
		JFreeChart chart = ChartFactory.createBarChart3D("Business Score Distribution",
				"Business Score", "Percentage", dataset, PlotOrientation.VERTICAL, true, true, false);
		try {
			ChartUtilities.saveChartAsJPEG(new File("barchart2.jpg"), chart, 500, 300);
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
	}

	public double roundScore(double score) {
		double result = Math.floor(score);
		if ((score-result) < 0.25) return result;
		else if ((score-result) < 0.75) return result+0.5;
		else return result+1; 
	}

	public void UserAvgStarDist() {
		int totalUserCnt = 0;
		Map<Double, Integer> UserAvgStarMap = new TreeMap<Double, Integer>();
		for (User user:userMap.values()) {
			//System.out.println("hello");
			double score = roundScore(user.average_stars);
			totalUserCnt += 1;
			if (!UserAvgStarMap.containsKey(score)) {
				UserAvgStarMap.put(score,0);
			}
			UserAvgStarMap.put(score,UserAvgStarMap.get(score)+1);
		}
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (Double d:UserAvgStarMap.keySet()) {
			dataset.setValue((double)UserAvgStarMap.get(d)/totalUserCnt,"Percentage",String.valueOf(d));
		}
		JFreeChart chart = ChartFactory.createBarChart("User Average Score Distribution",
				"Avg Score", "Percentage", dataset, PlotOrientation.VERTICAL, false, true, false);
		try {
			ChartUtilities.saveChartAsJPEG(new File("barchart_user.jpg"), chart, 500, 300);
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
	}

	double calculateAverage(List <Integer> marks) {
		double sum = 0;
		if(!marks.isEmpty()) {
			for (int mark : marks) {
				sum += mark;
			}
			return  (double)sum / marks.size();
		}
		return sum;
	}

	public void scoreCountCorrelate() {
		double totalScore = 0;
		int totalBusinessCnt = 0;
		Map<Double, List<Integer>> businessScoreCountMap = new TreeMap<Double, List<Integer>>();
		for (Business business:businessMap.values()) {
			//System.out.println("hello");
			totalBusinessCnt += 1;
			double star = business.stars;
			totalScore += star;
			if (!businessScoreCountMap.containsKey(star)) {
				businessScoreCountMap.put(star,new ArrayList<Integer> ());
			}
			businessScoreCountMap.get(star).add(business.review_count);
		}
		//		DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		//		for (Double d:businessScoreCountMap.keySet()) {
		//			System.out.println(Collections.max(businessScoreCountMap.get(d)));
		//			System.out.println(businessScoreCountMap.get(d));
		//			dataset.add(businessScoreCountMap.get(d),String.valueOf(d),String.valueOf(d));
		//		}
		//		CategoryAxis xAxis = new CategoryAxis("Business Score");
		//		final NumberAxis yAxis = new NumberAxis("Review Count");
		//		yAxis.setAutoRangeIncludesZero(false);
		//		final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
		//		renderer.setFillBox(false);
		//		renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
		//		final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
		//
		//		final JFreeChart chart = new JFreeChart(
		//				"Review Count vs Business Score Box Plot",
		//				new Font("SansSerif", Font.BOLD, 14),
		//				plot,
		//				true
		//				);
		XYSeries series = new XYSeries("Average Review Count");
		for (Double d:businessScoreCountMap.keySet()) {
			//series.add(d,Collections.max(businessScoreCountMap.get(d)));
			//series2.add(d,Collections.min(businessScoreCountMap.get(d)));
			series.add(d, new Double(calculateAverage(businessScoreCountMap.get(d))));
			//series3.add(d,avg);
			//series3.add(d,avg);
		}
		// Add the series to your data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		//dataset.addSeries(series);
		//dataset.addSeries(series2);
		dataset.addSeries(series);
		// Generate the graph
		JFreeChart chart = ChartFactory.createXYLineChart(
				"Average Review Count vs Business Score Plot",
				"Business Score",
				"Review Count",
				dataset,
				PlotOrientation.VERTICAL,  // Plot Orientation
				true,                      // Show Legend
				true,                      // Use tooltips
				false                      // Configure chart to generate URLs?
				);


		try {
			ChartUtilities.saveChartAsJPEG(new File("avgplot.jpg"), chart, 500, 300);
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
	}

	public void reviewVotesScoreDifference() {
		Map<Double, int[]> reviewUsefulVotesCountMap = new TreeMap<Double, int[]>();
		Map<Double, int[]> reviewFunnyVotesCountMap = new TreeMap<Double, int[]>();
		Map<Double, int[]> reviewCoolVotesCountMap = new TreeMap<Double, int[]>();
		for (Review review:reviewMap.values()) {
			//System.out.println("hello");
			if (businessMap.containsKey(review.b_id)) {
				double scoreDiff = review.stars - businessMap.get(review.b_id).stars;
				if (!reviewUsefulVotesCountMap.containsKey(scoreDiff)) {
					reviewUsefulVotesCountMap.put(scoreDiff,new int[2]);
				}
				int[] newArray = reviewUsefulVotesCountMap.get(scoreDiff);
				newArray[0] += review.votes.get("useful");
				newArray[1] += 1;
				reviewUsefulVotesCountMap.put(scoreDiff,newArray);

				if (!reviewFunnyVotesCountMap.containsKey(scoreDiff)) {
					reviewFunnyVotesCountMap.put(scoreDiff,new int[2]);
				}
				int[] newArray2 = reviewFunnyVotesCountMap.get(scoreDiff);
				newArray2[0] += review.votes.get("funny");
				newArray2[1] += 1;
				reviewFunnyVotesCountMap.put(scoreDiff,newArray2);

				if (!reviewCoolVotesCountMap.containsKey(scoreDiff)) {
					reviewCoolVotesCountMap.put(scoreDiff,new int[2]);
				}
				int[] newArray3 = reviewCoolVotesCountMap.get(scoreDiff);
				newArray3[0] += review.votes.get("cool");
				newArray3[1] += 1;
				reviewCoolVotesCountMap.put(scoreDiff,newArray3);
			} else {
				System.out.println("hello");
			}
		}
		XYSeries series1 = new XYSeries("Useful Votes");
		XYSeries series2 = new XYSeries("Funny Votes");
		XYSeries series3 = new XYSeries("Cool Votes");
		for (Double d:reviewUsefulVotesCountMap.keySet()) {
			int[] arr1 = reviewUsefulVotesCountMap.get(d);
			int[] arr2 = reviewFunnyVotesCountMap.get(d);
			int[] arr3 = reviewCoolVotesCountMap.get(d);
			series1.add(d, new Double((double)arr1[0]/arr1[1]));
			series2.add(d, new Double((double)arr2[0]/arr2[1]));
			series3.add(d, new Double((double)arr3[0]/arr3[1]));
			//series3.add(d,avg);
			//series3.add(d,avg);
		}
		// Add the series to your data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		//dataset.addSeries(series);
		//dataset.addSeries(series2);
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		dataset.addSeries(series3);
		// Generate the graph
		JFreeChart chart = ChartFactory.createXYLineChart(
				"Average votes vs Score difference",
				"Score Difference",
				"Average Votes",
				dataset,
				PlotOrientation.VERTICAL,  // Plot Orientation
				true,                      // Show Legend
				true,                      // Use tooltips
				false                      // Configure chart to generate URLs?
				);
		try {
			ChartUtilities.saveChartAsJPEG(new File("votesplot.jpg"), chart, 500, 300);
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
	}

	public void credibilityUsefulVote() {
		XYSeries series = new XYSeries("credibility");
		int max = -1;
		for (User user:userMap.values()) {
			if (user.votes.get("useful") > max) {
				max = user.votes.get("useful");
			}
		}
		System.out.println(max);
		int count = 0;
		for (User user:userMap.values()) {
			if (user.votes.get("useful") < 500) {
				series.add(user.credibility,user.votes.get("useful"));
			}
			System.out.println();
			count++;
			if (count >= 100) break;
		}
		// Add the series to your data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		//dataset.addSeries(series);
		//dataset.addSeries(series2);
		dataset.addSeries(series);
		// Generate the graph
		JFreeChart chart = ChartFactory.createScatterPlot(
				"Scatter Plot",
				"User Credibility",
				"Useful Votes",
				dataset,
				PlotOrientation.VERTICAL,  // Plot Orientation
				true,                      // Show Legend
				true,                      // Use tooltips
				false                      // Configure chart to generate URLs?
				);


		try {
			ChartUtilities.saveChartAsJPEG(new File("scatterplot.jpg"), chart, 500, 300);
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
		}
	}

	static Comparator<Business> comparator = new Comparator<Business> () {

		@Override
		public int compare(Business b1, Business b2) {
			// TODO Auto-generated method stub
			return -Double.valueOf(Math.abs(b1.stars - b1.newstars)).compareTo(Double.valueOf(Math.abs(b2.stars - b2.newstars)));
		}

	};
	
	public static <T> double getAvg (List<T> list) {
		// T can be Double or Integer
		double sum = 0;
		for (int i = 0; i < list.size(); i++) {
			T elem = list.get(i);
			if (elem instanceof Double) {
				sum += (Double) elem; 
			} else if (elem instanceof Integer) {
				sum += (Integer) elem;
			} 
		}
		return sum/list.size();
	}

	public static void main(String args[]) throws IOException {
		GenStats stats = new GenStats();
		HITS hits = new HITS();
		HashSet<User> users = new HashSet<User>(GenData.getUserMap().values());
		HashSet<Business> businesses = new HashSet<Business>(GenData.getBusinessMap().values());
		HITS.hitsScore(users, businesses);
		List<Business> businessesList = new ArrayList<Business> (businesses);
		Collections.sort(businessesList, comparator);
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("diff.txt"));

		int count  = 0;
		for (Business b : businessesList) {
			//if (b.stars > 2.5 && b.newstars > 2.5) {
				DecimalFormat df = new DecimalFormat("#.#");
				bufferedWriter.write(b.name + "	");
				bufferedWriter.write(b.address + " ");
				bufferedWriter.write(df.format(b.stars) + " ");
				bufferedWriter.write(df.format(b.newstars) + " ");
				bufferedWriter.write(df.format(Math.abs(b.newstars - b.stars)) + " ");
				bufferedWriter.write(Double.toString(getAvg(ubStar.get(b.id))) + " ");
				bufferedWriter.newLine();
				count ++;
			//}
			if (count == 300) break;
		}
//		for (int i = 0; i < 50; i++) {
//			Business cur = businessesList.get(i);
//			DecimalFormat df = new DecimalFormat("#.#");
//			bufferedWriter.write(cur.name + "	");
//			bufferedWriter.write(cur.address + " ");
//			bufferedWriter.write(df.format(cur.stars) + " ");
//			bufferedWriter.write(df.format(cur.newstars) + " ");
//			bufferedWriter.write(df.format(Math.abs(cur.newstars - cur.stars)) + " ");
//			bufferedWriter.write(Double.toString(getAvg(ubStar.get(cur.id))) + " ");
//			bufferedWriter.newLine();
//		}
		bufferedWriter.close();
		//stats.categoryDist();
		//stats.businessScoreDist();
		//stats.UserAvgStarDist();
		//stats.scoreCountCorrelate();
		//stats.credibilityUsefulVote();
		//stats.reviewVotesScoreDifference();
	}

}
