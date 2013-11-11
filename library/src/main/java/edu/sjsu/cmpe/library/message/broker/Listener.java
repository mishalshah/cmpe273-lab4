package edu.sjsu.cmpe.library.message.broker;

import java.net.MalformedURLException;
import java.net.URL;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import org.fusesource.stomp.jms.message.StompJmsMessage;

import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Book.Status;
import edu.sjsu.cmpe.library.repository.BookRepository;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;

public class Listener {

	 public static void pubSubListener(LibraryServiceConfiguration configuration, BookRepositoryInterface bookrepository) throws JMSException, MalformedURLException {

//			String user = env("APOLLO_USER", "admin");
//			String password = env("APOLLO_PASSWORD", "password");
//			String host = env("APOLLO_HOST", "54.215.210.214");
//			int port = Integer.parseInt(env("APOLLO_PORT", "61613"));
		 
			String user = env("APOLLO_USER", configuration.getApolloUser());
			String password = env("APOLLO_PASSWORD", configuration.getApolloPassword());
			String host = env("APOLLO_HOST", configuration.getApolloHost());
			int port = Integer.parseInt(env("APOLLO_PORT", configuration.getApolloPort()));
			String[] args = new String[]{};
//			String destination = arg(args, 0, "/topic/31944.book.*");
			String destination = arg(args,0,configuration.getStompTopicName());
			StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
			factory.setBrokerURI("tcp://" + host + ":" + port);

			Connection connection = factory.createConnection(user, password);
			connection.start();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination dest = new StompJmsDestination(destination);

			MessageConsumer consumer = session.createConsumer(dest);
			System.currentTimeMillis();
			System.out.println("Waiting for messages...");
			while(true) {
			    Message msg = consumer.receive();
			    if( msg instanceof  TextMessage ) {
				String body = ((TextMessage) msg).getText();
				if( "SHUTDOWN".equals(body)) {
				    break;
				}
				System.out.println("Received message = " + body);
				
				 String[] partString = body.split("\\:");
				 long isbn = Long.parseLong(partString[0]);
				 
				 for(String incr :  partString)
				 System.out.println(incr);
				 // It will update book object
	//			BookRepository bookrepositry = new BookRepository();
				 
				Book book = bookrepository.getBookByISBN(isbn);
				if(book != null)
				{
					book.setStatus(Status.available);
					System.out.println("status of book is set to available");
				}
				else
				{
					book = new Book();
					book.setIsbn(isbn);
					book.setTitle(partString[1]);
					book.setCategory(partString[2]);
					book.setCoverimage(new URL(partString[3]+":"+partString[4]));
					book.setStatus(Status.available);
					bookrepository.addNewBook(book);
					System.out.println("a new book is added in hashmap");
				}

			    } else if (msg instanceof StompJmsMessage) {
				StompJmsMessage smsg = ((StompJmsMessage) msg);
				String body = smsg.getFrame().contentAsString();
				if ("SHUTDOWN".equals(body)) {
				    break;
				}
				System.out.println("Received message = " + body);
				
				 String[] partString = body.split("\\:");
				 long isbn = Long.parseLong(partString[0]);
				 
				 for(String incr :  partString)
				 System.out.println(incr);
				 // It will update book object
			//	BookRepositoryInterface bookrepositry = new BookRepository();
				 
				Book book = bookrepository.getBookByISBN(isbn);
				if(book != null)
				{
					book.setStatus(Status.available);
					System.out.println("status of book is set to available");
				}
				else
				{
					book = new Book();
					book.setIsbn(isbn);
					book.setTitle(partString[1]);
					book.setCategory(partString[2]);
					book.setCoverimage(new URL(partString[3]));
					book.setStatus(Status.available);
					bookrepository.saveBook(book);
					System.out.println("a new book is added in hashmap");
				}

			    } else {
				System.out.println("Unexpected message type: "+msg.getClass());
			    }
			}
			connection.close();
		    }

		    private static String env(String key, String defaultValue) {
			String rc = System.getenv(key);
			if( rc== null ) {
			    return defaultValue;
			}
			return rc;
		    }

		    private static String arg(String []args, int index, String defaultValue) {
			if( index < args.length ) {
			    return args[index];
			} else {
			    return defaultValue;
			}
		    }
}
