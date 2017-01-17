package common;

import java.io.Serializable;

public class Tweet implements Serializable {

	@Override
	public String toString() {
		return "Tweet [user=" + user + ", text=" + text + "]";
	}

	private static final long serialVersionUID = 1L;
	
	private final String user;
	private final byte[] image;
	private final String text;

	public Tweet(String user, byte[] image, String text) {
		this.user = user;
		this.image = image;
		this.text = text;
	}

	public String getUser() {
		return user;
	}

	public byte[] getImage() {
		return image;
	}

	public String getText() {
		return text;
	}
}
