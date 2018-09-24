package com.voca.solr.indexing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import com.voca.dict.StarDictParser;
import com.voca.dict.WordPosition;
import com.voca.solr.model.Item;

public class VocaIndexing {

	private static final String VI_EN = "VI_EN";
	private static final String EN_VI = "EN_VI";

	public static void main(String[] args) throws IOException, SolrServerException {
		StarDictParser starDictParser = new StarDictParser().initStarDictParser();
		SolrClient enClient = new HttpSolrClient.Builder().withBaseSolrUrl("http://localhost:8983/solr/en_vi").build();
		int counter = 0;
		enClient.deleteByQuery("*");
		enClient.commit();
		
		for (Map.Entry<String, WordPosition> en : starDictParser.getWords().entrySet()) {
			if (!en.getKey().toLowerCase().equals("make")) {
				//System.out.println("======== SKIPPING " + en.getKey());
				//continue;
			}
			String meaning = starDictParser.getWordExplanation(en.getValue().getStartPos(), en.getValue().getLength());
			
			if (meaning.contains("người bịa đặt")) {
				System.out.println("======== PROCESSING FOR " + en.getKey());
				BufferedReader bufferedReader = new BufferedReader(new StringReader(meaning));
				String line = null;
				while ((line = bufferedReader.readLine()) != null) {
					if (line.contains("người bịa đặt")) {
						System.out.println(line);
					}
				}
			}
			
			//processWord(enClient, meaning, en);

			/*Pattern pattern = Pattern.compile("=(.*?)\\n");
			Matcher matcher = pattern.matcher(meaning);
			while (matcher.find()) {
				String st = matcher.group(1);
				if (st.indexOf("+") >= 0) {
					String enSt = st.substring(0, st.indexOf("+"));
					String viSt = st.substring(st.indexOf("+") + 1).trim();
					
					Item viItem = new Item();
					viItem.setKey(viSt);
					viItem.setMeaning(enSt);
					viItem.setType(VI_EN);
					
					Item enItem = new Item();
					enItem.setKey(enSt);
					enItem.setMeaning(viSt);
					enItem.setType(EN_VI);
					
					//enClient.addBean(viItem);
					//enClient.addBean(enItem);
					
					if (counter++==500) {
						//enClient.commit();
					}
				}
			}*/
		}
	}
	
	private static void processWord(SolrClient enClient, String meaning, Map.Entry<String, WordPosition> en) throws IOException, SolrServerException {
		BufferedReader bufferedReader = new BufferedReader(new StringReader(meaning));
		String line = null;
		String word = null;
		String minusLine = null;
		boolean isJournalize = false;
		List<String> journalizeMeanings = new ArrayList<>();
		Map<String, String> map = new HashMap<>();
		while ((line = bufferedReader.readLine()) != null) {
			if (line.startsWith("*")) {
				word = en.getKey();
				isJournalize = false;
			}
			if (line.startsWith("!")) {
				word = line.substring(1);
				isJournalize = true;
				map.put(word, "");
			}
			if (line.startsWith("-")) {
				minusLine = line.substring(1).trim();
				String[] subMeanings = minusLine.split(",");
				for (String subMeaning: subMeanings) {
					Item viItem = new Item();
					viItem.setKey(subMeaning.trim());
					viItem.setMeaning(word);
					viItem.setType(VI_EN);
					enClient.addBean(viItem);
				}
				
			}
			
			if (line.startsWith("=")) {
				if (line.indexOf("+") > 0) {
					String enSt = line.substring(1, line.indexOf("+")).trim();
					String viSt = line.substring(line.indexOf("+") + 1).trim();
					
					Item viItem = new Item();
					viItem.setKey(viSt);
					viItem.setMeaning(enSt);
					viItem.setType(VI_EN);
					
					Item enItem = new Item();
					enItem.setKey(enSt);
					enItem.setMeaning(viSt);
					enItem.setType(EN_VI);
					
					enClient.addBean(viItem);
					enClient.addBean(enItem);
				}
			}
			
			if (isJournalize) {
				String mapValue = map.get(word);
				mapValue += minusLine;
				map.put(word, mapValue);
			}
		}
		
		for (Map.Entry<String, String> item: map.entrySet()) {
			Item journalizeItem = new Item();
			journalizeItem.setKey(item.getKey());
			journalizeItem.setMeaning(item.getValue());
			journalizeItem.setType(EN_VI);
			enClient.addBean(journalizeItem);
		}
		
		enClient.commit();
	}

}
