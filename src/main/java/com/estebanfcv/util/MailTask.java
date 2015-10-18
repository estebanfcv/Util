package com.estebanfcv.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import org.apache.commons.lang.StringUtils;

public class MailTask implements Runnable, Cloneable {

    private List<MailTask> lista;
    private InternetAddress from;
    private InternetAddress[] to;
    private String subject;
    private String text;
    private String mailServer;
    private String password = "";
    private String user = "";
    private String tls = "0";
    private int port;
    private ByteArrayDataSource attachment;
    private List<ByteArrayDataSource> listaAttachment;
    private String toString;
    private String[] datos;
    private String cc;

    /**
     *
     * @param idEmpresa
     * @param to
     * @param cc
     * @param subject
     * @param text
     * @param datosCorreo
     * @param attachment
     * @throws AddressException
     * @throws UnsupportedEncodingException
     */
    public MailTask(String idEmpresa, String to, String cc, String subject, String text, String[] datosCorreo,
            ByteArrayDataSource attachment) throws AddressException, UnsupportedEncodingException {
        this.user = datosCorreo[0];
        this.password = datosCorreo[1];
        inicializaTask(to, cc, subject, text, datosCorreo, attachment);
    }

    @Override
    public MailTask clone() {
        MailTask clon = null;
        try {
            clon = (MailTask) super.clone();
        } catch (Exception e) {
            System.out.println("No se puede duplicar");
        }
        return clon;
    }

    public void agregarALista(String idEmpresa, String to, String cc, String subject, String text,
            String[] datosCorreo, ByteArrayDataSource attachment
    ) throws AddressException, UnsupportedEncodingException {
        this.user = datosCorreo[0];
        this.password = datosCorreo[1];
        this.toString = to;
        this.datos = datosCorreo;
        this.subject = subject;
        this.text = text;
        this.cc = cc;
        this.attachment = attachment;
        lista.add(this.clone());
    }

    public MailTask() {
        lista = new ArrayList<>();
    }

    private void inicializaTask(String to,
            String cc,
            String subject,
            String text,
            String[] datosCorreo,
            ByteArrayDataSource attachment) throws IllegalArgumentException, NullPointerException, AddressException, UnsupportedEncodingException {
        if (to == null) {
            throw new NullPointerException("to es nulo");
        }
        if (to.length() == 0) {
            throw new IllegalArgumentException("to esta vacio");
        }
        if (subject == null) {
            throw new NullPointerException("subject es nulo");
        }
        if (subject.length() == 0) {
            throw new IllegalArgumentException("subject esta vacio");
        }
        if (text == null) {
            throw new NullPointerException("text es nulo");
        }
        if (text.length() == 0) {
            throw new IllegalArgumentException("text esta vacio");
        }
        if (datosCorreo[0] == null) {
            throw new NullPointerException("from es nulo");
        }
        if (datosCorreo[0].length() == 0) {
            throw new IllegalArgumentException("from esta vacio");
        }
        if (datosCorreo[2] == null) {
            throw new NullPointerException("mail server es nulo");
        }
        if (datosCorreo[2].length() == 0) {
            throw new IllegalArgumentException("mail server esta vacio");
        }
        if (!StringUtils.isBlank(datosCorreo[1])) {
            this.password = datosCorreo[1];
        }
        if (!StringUtils.isBlank(datosCorreo[4])) {
            this.tls = datosCorreo[4];
        }
        this.from = new InternetAddress(datosCorreo[0]);
        if (cc != null) {
            this.to = new InternetAddress[]{new InternetAddress(to), new InternetAddress(cc)};
        } else {
            StringTokenizer token = new StringTokenizer(to, ",");
            int conta = 0;
            this.to = new InternetAddress[token.countTokens()];
            while (token.hasMoreTokens()) {
                this.to[conta++] = new InternetAddress(token.nextToken());
            }
        }
        this.subject = subject;
        this.text = text;
        this.mailServer = datosCorreo[2];
        this.port = Integer.parseInt(datosCorreo[3]);
        this.attachment = attachment;
    }

    @Override
    public void run() {
        if (lista == null) {
            if (attachment == null) {
                enviaCorreo();
            } else {
                enviaCorreoConAttachment();
            }
        } else {
            for (MailTask mailTask : lista) {
                try {
                    inicializaTask(mailTask.toString, mailTask.cc, mailTask.subject, mailTask.text, datos, mailTask.attachment);
                    if (mailTask.attachment == null) {
                        enviaCorreo();
                    } else {
                        enviaCorreoConAttachment();
                    }
                    Thread.sleep(120000);
                    System.out.println("correo enviado a " + mailTask.toString + " con copia a " + mailTask.cc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void enviaCorreoConAttachment() {
        Transport transport = null;
        try {
            // create a message
            MimeMessage message = new MimeMessage(getSession());
            message.setFrom(from);
            message.setRecipients(Message.RecipientType.TO, to);
            message.setSubject(subject, "UTF-8");
            // create and fill the first message part
            MimeBodyPart cuerpo = new MimeBodyPart();
            cuerpo.setContent(text, "text/html");
            // create the second message part
            MimeBodyPart archivo = new MimeBodyPart();
            // attach the file to the message
            archivo.setDataHandler(new DataHandler(attachment));
            archivo.setFileName(attachment.getName());
            // create the Multipart and add its parts to it
            Multipart multiPart = new MimeMultipart();
            multiPart.addBodyPart(cuerpo);
            multiPart.addBodyPart(archivo);
            // add the Multipart to the message
            message.setContent(multiPart);
            // set the Date: header
            message.setSentDate(new Date());
            message.saveChanges();
            // send the message
            transport = getSession().getTransport("smtp");
            transport.send(message);
        } catch (MessagingException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (transport != null) {
                    transport.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void enviaCorreo() {
        Transport transport = null;
        try {
            MimeMessage message = new MimeMessage(getSession());
            message.setFrom(from);
            message.setRecipients(Message.RecipientType.TO, to);
            message.setSubject(subject, "UTF-8");
            message.setContent(text, "text/html");
            message.saveChanges();
            transport = getSession().getTransport("smtp");
            transport.send(message);
//            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (transport != null) {
                    transport.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Session getSession() {
        Authenticator authenticator = new Authenticator();
        Properties properties = new Properties();
        properties.put("mail.smtp.submitter", authenticator.getPasswordAuthentication().getUserName());
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", mailServer);
        properties.put("mail.smtp.port", port);
//        properties.put("mail.debug", "true");
        properties.put("mail.smtp.EnableSSL.enable", "true");
        properties.setProperty("mail.smtp.ssl.trust", "smtpserver");
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
        properties.setProperty("mail.smtp.socketFactory.port", "465");
        if (null == tls || "0".equals(tls)) {
            properties.put("mail.smtp.starttls.enable", "false");
        } else {
            properties.put("mail.smtp.starttls.enable", "true");
        }
        Session session = Session.getInstance(properties, authenticator);
        return session;
    }

    private class Authenticator extends javax.mail.Authenticator {

        private PasswordAuthentication authentication;

        public Authenticator() {
            authentication = new PasswordAuthentication(user, password);
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return authentication;
        }
    }

    public List<MailTask> getLista() {
        return lista;
    }
}
