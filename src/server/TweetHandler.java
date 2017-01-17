package server;

import java.util.List;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import common.Timeline;
import common.Tweet;
import common.User;

public class TweetHandler implements MessageListener{

	static JMSContext jmsContext;
	private Queue saveQueue;
	private Queue thumbnailQueue;
	
	public TweetHandler() {
		Context initialContext;
		try {
			initialContext = getContext();
			jmsContext = ((ConnectionFactory) initialContext.lookup("java:comp/DefaultJMSConnectionFactory")).createContext();
			
			//lookup tweetQueue and set messageListener
			String tweetQueueName ="tweetQueue";
			Queue tweetQueue = (Queue) initialContext.lookup(tweetQueueName);
			jmsContext.createConsumer(tweetQueue).setMessageListener(this);
			
			//lookup saveQueue
			String saveQueueName ="saveQueue";
			saveQueue = (Queue) initialContext.lookup(saveQueueName);
			
			
			//lookup thumbnailQueue
			String thumbnailQueueName ="thumbnailQueue";
			thumbnailQueue = (Queue) initialContext.lookup(thumbnailQueueName);

		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onMessage(Message msg) {
		//check content of tweet
		try {
			Queue replyToQueue = (Queue)msg.getJMSReplyTo();
			Tweet tweet = msg.getBody(Tweet.class);
			
			if(tweet.getImage()==null){		//only message tweet
				String userID = tweet.getUser();
				User user = Resources.RS.getUserById(userID);
				List<User> followers = user.getFollower();
				for(User u : followers){
					Timeline timeline = u.getMytimeline();
					timeline.addTweet(tweet);
				}
			}else{				//contains image
				jmsContext.createProducer().send(saveQueue, tweet);  //save Image				
				jmsContext.createProducer().send(thumbnailQueue, tweet);	//create thumbnail
			}
			
			jmsContext.createProducer().send(replyToQueue, "message processed");	
			
			
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	
	
	private Context getContext() throws NamingException {
		Properties props = new Properties();
		props.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
		props.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
		props.setProperty("java.naming.provider.url", "iiop://localhost:3700");
		return new InitialContext(props);
	}
	

}
