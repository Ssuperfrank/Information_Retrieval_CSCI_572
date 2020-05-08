import java.io.IOException;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapred.OutputCollector;

public class InvertedIndexBigrams{
	public static class TokenizerMapper
			extends Mapper<Object, Text, Text, Text> {

		private String pattern = "[^a-z]+";
		private Text word = new Text();
		private Text docId =  new Text();

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException
		{
			String [] line  = value.toString().split("\\t");
			docId.set(line[0]);
			String lastWord = "";
			for(int i = 1; i< line.length; i ++){
				StringTokenizer tokenizer = new StringTokenizer(line[i]);
                	while (tokenizer.hasMoreTokens()) {

					String [] words = tokenizer.nextToken().toLowerCase().split(pattern);
                                	for(int j = 0; j < words.length; j ++){
						if(!lastWord.equals("")){
							word.set(lastWord + " " + words[j].trim());
							context.write(word, docId);
						}
						lastWord = words[j].trim();
					}
                	}
			}
		}
	}

	public static class IntSumReducer
			extends Reducer<Text,Text,Text,Text> {

		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			Map<String, Integer> map = new HashMap<String,  Integer>();

			for (Text val : values) {
				if(map.containsKey(val.toString())){
			                map.put(val.toString(),map.get(val.toString()) + 1);
        			}
        			else{
                			map.put(val.toString(),1);
        			}
			}
			StringBuilder sb = new StringBuilder();
			for(Map.Entry entry : map.entrySet()){
				sb.append(entry.getKey() + ":" + entry.getValue() + "\t");
			}
			context.write(key, new Text(sb.toString()));
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "word count");
		job.setJarByClass(InvertedIndexBigrams.class);

		job.setMapperClass(TokenizerMapper.class);
		job.setReducerClass(IntSumReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
