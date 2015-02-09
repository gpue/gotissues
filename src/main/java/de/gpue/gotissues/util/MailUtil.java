package de.gpue.gotissues.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MailUtil {

	private static final Log log = LogFactory.getLog(MailUtil.class);

	public static void sendHTMLMail(String from, String to, String subject,
			String text) throws AddressException, MessagingException {
		log.info("mail from " + from + " to " + to + " with subject '"
				+ subject + "':\n" + text);

		// Assuming you are sending email from localhost
		String host = "localhost";

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", host);

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);

		// Create a default MimeMessage object.
		MimeMessage message = new MimeMessage(session);

		// Set From: header field of the header.
		message.setFrom(new InternetAddress(from));

		message.setSubject(subject);

		// Set To: header field of the header.
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

		message.setContent(text, "text/html; charset=utf-8");

		// Send message
		Transport.send(message);
	}
}
