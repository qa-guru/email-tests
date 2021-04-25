package utils;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmailUtils {
    private static Logger logger = LoggerFactory.getLogger(EmailUtils.class);

    static String inboxHost = "imap.gmail.com";
    static String outboxHost = "smtp.gmail.com";

    public static List<String> waitForMail(String user, String password, String text, int minutes) {
        for (int i = 0; i < minutes; i++) {
            logger.info("waitForMail i: " + i);
            List<String> messages = getMail(user, password, text);
            if (messages.size() > 0) {
                return messages;
            }
            sleepMail(20000);
        }
        return new ArrayList<>();
    }

    public static List<String> getMail(String user, String password, String expectedText) {
        try {
            Properties properties = new Properties();
            properties.put("mail.store.protocol", "imaps");

            Session emailSession = Session.getDefaultInstance(properties);

            Store store = emailSession.getStore("imaps");
            store.connect(inboxHost, user, password);

            //create the folder object and open it
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            // retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.getMessages();

            List<String> messagesList = new ArrayList<>();
            for(Message message: messages) {
                String messageText = getTextFromMessage(message);
                if(messageText.contains(expectedText))
                    messagesList.add(messageText);
            }


            emailFolder.close(false);
            store.close();

            return messagesList;

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            logger.info("getMail MessagingException error:\n" + e);
            e.printStackTrace();
        } catch (Exception e) {
            logger.info("getMail Exception error:\n" + e);
            e.printStackTrace();
        }
        return null;
    }

    public static String getTextFromMessage(Message message) {
        String result = "";

        try {
            if (message.isMimeType("multipart/*")) {
                MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                result = getTextFromMimeMultipart(mimeMultipart);
            } else {
                result = message.getContent().toString();
            }
        } catch (MessagingException e) {
            logger.info("getTextFromMessage MessagingException error:\n" + e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.info("getTextFromMessage IOException error:\n" + e);
            e.printStackTrace();
        }

        result = result.replaceAll("\n", "")
                .replaceAll("[\\s]+", " ");
//        logger.info("message: " + result);
        return result;
    }

    private static String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break;
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }

    public static void sendMailWithoutAuth(String from, String to, String subject, String text) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");

        Session session = Session.getDefaultInstance(props);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(text, "UTF-8"); // as "text/plain"
            message.setSentDate(new Date());
            Transport.send(message);

            logger.info("Message successfully sent!\n" +
                    "Subject: " + subject + "\n" +
                    "Message: " + text);
        } catch (MessagingException e) {
            logger.info("sendMailWithoutAuth error:\n" + e);
            throw new RuntimeException(e);
        }
    }

    public static void sendMail(String user, String password, String to, String subject, String text) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", outboxHost);
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(text);
            Transport.send(message);

            logger.info("Message successfully sent!\n" +
                    "Subject: " + subject + "\n" +
                    "Message: " + text);

        } catch (MessagingException e) {
            logger.info("sendMail error:\n" + e);
            throw new RuntimeException(e);
        }

    }

    private static void sleepMail(int i) {
        try {
            sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void verifyMailHasMessage(String user, String password, String text) {
        List<String> messages = waitForMail(user, password, text, 10);

        logger.info("Message found: " + messages.size());
        assertEquals(messages.size(), 1, "Проверка, что найдено одно сообщение с данным текстом в течение 10 минут");
    }
}
