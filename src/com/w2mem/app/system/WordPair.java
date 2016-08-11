package com.w2mem.app.system;

public class WordPair {
	public long id;
	public String word;
	public String translation;
	
	public WordPair(String word, String translation, long dictId) {
		this.word = word;
		this.translation = translation;
	}
	
	public WordPair(long id, String word, String translation, long dictId) {
		this.id = id;
		this.word = word;
		this.translation = translation;
	}
	
	@Override
	public String toString(){
		return String.format("%s = %s", word, translation);
	}
}
