package com.appspot.hachiko_schedule.util;

import android.content.Context;
import android.os.AsyncTask;
import com.appspot.hachiko_schedule.prefs.GoogleAuthPreferences;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64EncoderStream;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.util.Properties;

public class GmailSendHelper {
    private static final int RESPONSE_CODE_AUTH_SUCCESS = 235;
    private static final int SUBMISSION_PORT = 587;
    private static final String GMAIL_SMTP_SERVER = "smtp.gmail.com";
    private final GoogleAuthPreferences googleAuthPreferences;
    private Session session;

    public GmailSendHelper(Context context) {
        googleAuthPreferences = new GoogleAuthPreferences(context);
    }

    private SMTPTransport connectToSmtp(String host, int port)
            throws Exception {
        String userEmail = googleAuthPreferences.getAccountName();

        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.sasl.enable", "false");
        session = Session.getInstance(props);

        SMTPTransport transport = new SMTPTransport(session, /* URL Name */ null);
        transport.connect(host, port, userEmail, /* Password, OAuth認証なのでNull */ null);

        byte[] response = BASE64EncoderStream.encode(
                String.format("user=%s\1auth=Bearer %s\1\1",
                        userEmail, googleAuthPreferences.getToken()).getBytes()
        );

        transport.issueCommand("AUTH XOAUTH2 " + new String(response), RESPONSE_CODE_AUTH_SUCCESS);
        return transport;
    }

    public synchronized void sendHtmlMail(String subject, String body, String recipients) {
        try {
            SMTPTransport smtpTransport = connectToSmtp(GMAIL_SMTP_SERVER, SUBMISSION_PORT);

            MimeMessage message = new MimeMessage(session);
            DataHandler handler = new DataHandler(
                    new ByteArrayDataSource(body.getBytes(), "text/html"));
            message.setSender(new InternetAddress(googleAuthPreferences.getAccountName()));
            message.setSubject(subject);
            message.setDataHandler(handler);
            if (recipients.indexOf(',') > 0) {
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            } else {
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
            }
            smtpTransport.sendMessage(message, message.getAllRecipients());
        } catch (Exception e) {
            // TODO: proper error handling
            HachikoLogger.error("Send mail error ", e);
        }
    }

    public synchronized void sendHtmlMailAsync(final String subject, final String body,
                                               final String recipients) {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                sendHtmlMail(subject, body, recipients);
                return "";
            }
        };
        task.execute();
    }
}