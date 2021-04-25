package tests;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.mail.Message;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.EmailUtils.*;
import static utils.RegexpUtils.getUrlsFromMessage;

public class EmailTests {
    String senderEmailLogin = "";
    String senderEmailPassword = "";
    String receiverEmailLogin = "";
    String receiverEmailPassword = "";


    @Test
    void sendMailTest() {
        Faker faker = new Faker();

        String url = faker.internet().url();
        String subject = faker.name().firstName();
        String message = faker.name().firstName() + " " + url + " " + faker.name().lastName();

        sendMail(senderEmailLogin, senderEmailPassword, "aanher@gmail.com", subject, message);
        // no assert here
    }

    @Test
    @Disabled("Not working and bad practice")
    void sendMailWithoutAuthTest() {
        Faker faker = new Faker();

        String url = faker.internet().url();
        String subject = faker.name().firstName();
        String message = faker.name().firstName() + " " + url + " " + faker.name().lastName();

        sendMailWithoutAuth("admin@gmail.com", "aanher@gmail.com", subject, message);
        // no assert here
    }

    @Test
    void receiveMailTest() {
        Faker faker = new Faker();

        String url = faker.internet().url();
        String subject = faker.name().firstName();
        String message = faker.name().firstName() + " " + url + " " + faker.name().lastName();

        sendMail(senderEmailLogin, senderEmailPassword, receiverEmailLogin + "@gmail.com", subject, message);

        List<String> receivedMessages = waitForMail(receiverEmailLogin, receiverEmailPassword, message, 3);
        assertTrue(receivedMessages.size() > 0);
    }

    @Test
    void getUrlFromReceivedMessagesTest() {
        Faker faker = new Faker();

        String url = "https://" + faker.internet().url();
        String subject = faker.name().firstName();
        String message = faker.name().firstName() + " " + url + " " + faker.name().lastName();

        sendMail(senderEmailLogin, senderEmailPassword, senderEmailLogin + "@gmail.com", subject, message);

        List<String> receivedMessages = waitForMail(senderEmailLogin, senderEmailPassword, message, 3);
//
//        sendMail(senderEmailLogin, senderEmailPassword, receiverEmailLogin + "@gmail.com", subject, message);
//
//        List<Message> receivedMessages = waitForMail(receiverEmailLogin, receiverEmailPassword, message, 3);
        assertTrue(receivedMessages.size() > 0);

        List<String> urls = getUrlsFromMessage(receivedMessages.get(0));
        System.out.println(urls.get(0));

        assertEquals(url, urls.get(0));
    }
}
