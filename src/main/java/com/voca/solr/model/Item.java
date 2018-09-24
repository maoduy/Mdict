package com.voca.solr.model;

import org.apache.solr.client.solrj.beans.Field;

public class Item {

	@Field
	private String key;

	@Field
	private String meaning;
	
	@Field
	private String type;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getMeaning() {
		return meaning;
	}

	public void setMeaning(String meaning) {
		this.meaning = meaning;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
