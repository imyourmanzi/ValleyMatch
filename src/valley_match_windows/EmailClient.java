package valley_match_windows;

import java.util.ArrayList;
import java.util.Properties;
import java.io.UnsupportedEncodingException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/*
 * Valley Match Services
 * EmailClient
 * (c) 2016 Matthew R. Manzi
 * !!!!WARNING: 'FROM' ACCOUNT NEEDS TO ALLOW LESS SECURE APPS TO SIGN ON!!!!
 * 
 * Class to allow local programs to send emails to
 * send emails from Valley Match
 * @author matteomanzi
 * @version 1.0 --- Feb 5, 2016
 * @version 1.0.1 --- Feb 6, 2016 -- Revised email messages sent to entrants
 * 									 with top three matches
 * 
 */
public class EmailClient {

	// INSTANCE DATA \\
	private String to;
	private final String FROM = "slataa@garnetvalley.org";	
	private final String USERNAME = "slataa@garnetvalley.org";
	private final String PASSWORD = "****";					
	private final String HOST = "smtp.gmail.com";
	private boolean isSetUp; 
	private Properties props;
	private Session session;
	private Transport transport;
	private Message msg;
	private InternetAddress fromAddress;
	
	
	// CONSTRUCTORS \\
	
	public EmailClient() {
		this.isSetUp = false;
		this.props = System.getProperties();
	}
	
	public EmailClient(String to) {
		this.to = to;
		this.isSetUp = false;
		this.props = System.getProperties();
		this.setUpEmailClient();
	}
	
	
	// GETTERS & SETTERS \\
	
	public String getTo() {
		return to;
	}
	
	public void setTo(String to) {
		this.to = to;
	}
	
	public boolean isSetUp() {
		return isSetUp;
	}
	
	
	// EMAIL CLIENT SUPPORT \\
	
	public boolean setUpEmailClient() {
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", HOST);
		props.put("mail.smtp.user", USERNAME);
		props.put("mail.smtp.password", PASSWORD);
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.auth", "true");
		
		session = Session.getDefaultInstance(props);
		
		try {
			transport = session.getTransport("smtp");
			transport.connect(HOST, FROM, PASSWORD);
			
			 fromAddress = new InternetAddress(FROM, "GVHS Robotics - Valley Match");
			
			isSetUp = true;
		} catch (NoSuchProviderException nspe) {
			nspe.printStackTrace();
			
			isSetUp = false;
		} catch (MessagingException me) {
			me.printStackTrace();
			
			isSetUp = false;
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			
			isSetUp = false;
		}
		
		return isSetUp;
	}
	
	public boolean sendEmailWithTopEntrantsTo(String matcheeName, ArrayList<Entrant> males, ArrayList<Entrant> females) throws MessagingException {
		
		if (isSetUp) {			
			String results = matcheeName + ",\nYour top compatibility results are...\n\nMales:\n";
			for (Entrant male: males) {
				results += (male.toString() + "\n");
			}
			results += "\nFemales:\n";
			for (Entrant female: females) {
				results += (female.toString() + "\n");
			}		
			results += "\n\nThank you for your support.  Happy Matching!";
			
			msg = new MimeMessage(session);
			msg.setFrom(fromAddress);
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			msg.setSubject("Your Valley Match Results Are In!");								// ***Probably needs to be revised***
			msg.setText(results);
			
			transport.sendMessage(msg, msg.getAllRecipients());
			
			return true;
		} else {
			return false;			
		}
		
	}
	
	public boolean sendCustomEmail(String subject, String message) throws MessagingException {
		
		if (isSetUp) {
			msg = new MimeMessage(session);
			msg.setFrom(fromAddress);
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			msg.setSubject(subject);
			msg.setText(message);
			
			transport.sendMessage(msg, msg.getAllRecipients());
			
			return true;
		} else {
			return false;
		}
		
	}
	
	public void close() throws MessagingException {
		transport.close();
		props.clear();
	}
		
} // End class
	
