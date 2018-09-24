package com.voca.solr.query;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class SearchingSample {

	public static void main(String[] args) {
		SolrClient client = new HttpSolrClient.Builder()
			.withBaseSolrUrl("http://localhost:8983/solr/mdict")
			.build();

		SolrQuery query = createSolrQuery();
		QueryResponse response = null;

		try {
			response = client.query(query);
			System.out.println("status : " + response.getStatus());
			System.out.println("QTime : " + response.getQTime());
			System.out.println("numFound : " + response.getResults().getNumFound());
			SolrDocumentList list = response.getResults();
			for (SolrDocument doc : list) {
				System.out.println("------------" + doc.getFieldValue("key"));
				System.out.println("++++++++++++: " + doc.getFieldValue("meaning"));
			}
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
	}

	private static SolrQuery createSolrQuery() {
		SolrQuery query = new SolrQuery("meaning:*fuck*");
		query.setFields("id", "meaning", "key");
		query.setRows(10);

		return query;
	}
}
