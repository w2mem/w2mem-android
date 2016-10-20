package com.w2mem.app.system;

public class Dictionary {
	public long id;
	public String name;

	public Dictionary(long id, String name) {
		this.id   = id;
		this.name = name;
	}
	
	public long getId() {
		return id;
	}
	
	@Override
	public String toString(){
		return name;	
	}
}
