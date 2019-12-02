package cn.fy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordCountTopology {
	public static final Log log = LogFactory.getLog(WordCountTopology.class);

	public static class SplitSentence implements IRichBolt {
		private OutputCollector _collector;

		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("word"));
		}

		public Map<String, Object> getComponentConfiguration() {
			return null;
		}

		public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
			_collector = collector;
		}

		public void execute(Tuple input) {
			String sentence = input.getStringByField("word");
			String[] words = sentence.split(" ");
			for (String word : words) {
				this._collector.emit(new Values(word));
			}
		}

		public void cleanup() {
			// TODO Auto-generated method stub

		}
	}

	public static class WordCount extends BaseBasicBolt {
		public static final Log _log = LogFactory.getLog(WordCount.class);
		
		Map<String, Integer> counts = new HashMap<String, Integer>();

		public void execute(Tuple tuple, BasicOutputCollector collector) {
			String word = tuple.getString(0);
			Integer count = counts.get(word);
			if (count == null)
				count = 0;
			count++;
			counts.put(word, count);
			collector.emit(new Values(word, count));
		}

		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("word", "count"));
		}

		@Override
		public void cleanup() {
			// TODO Auto-generated method stub
			_log.info(">>>>>>>>>>WordCount cleanup<<<<<<<<<<");
			super.cleanup();
		}

	}

	public static class WordReport extends BaseBasicBolt {
		public static final Log _log = LogFactory.getLog(WordReport.class);
		
		Map<String, Integer> counts = new HashMap<String, Integer>();

		public void execute(Tuple tuple, BasicOutputCollector collector) {
			String word = tuple.getStringByField("word");
			Integer count = tuple.getIntegerByField("count");
			this.counts.put(word, count);
		}

		public void declareOutputFields(OutputFieldsDeclarer declarer) {

		}

		@Override
		public void cleanup() {
			_log.info("-----------------FINAL COUNTS  START-----------------------");
			List<String> keys = new ArrayList<String>();
			keys.addAll(this.counts.keySet());
			Collections.sort(keys);

			for (String key : keys) {
				System.out.println(key + " : " + this.counts.get(key));
			}

			_log.info("-----------------FINAL COUNTS  END-----------------------");
		}

	}

	public static void main(String[] args) throws Exception {

		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("spout", new RandomSentenceSpout(), 2);

		// ShuffleGrouping：随机选择一个Task来发送。
		builder.setBolt("split", new SplitSentence(), 4).shuffleGrouping("spout");
		// FiledGrouping：根据Tuple中Fields来做一致性hash，相同hash值的Tuple被发送到相同的Task。
		builder.setBolt("count", new WordCount(), 5).fieldsGrouping("split", new Fields("word"));
		// GlobalGrouping：所有的Tuple会被发送到某个Bolt中的id最小的那个Task。
		builder.setBolt("report", new WordReport(), 4).globalGrouping("count");

		Config conf = new Config();
		conf.setDebug(true);

		if (args != null && args.length > 0) {
			conf.setNumWorkers(3);

			StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
		} else {
			conf.setMaxTaskParallelism(3);

			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("word-count", conf, builder.createTopology());

			Thread.sleep(20000);

			cluster.shutdown();
		}
	}
}