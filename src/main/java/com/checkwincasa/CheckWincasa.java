package com.checkwincasa;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.azure.communication.email.*;
import com.azure.communication.email.models.*;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;

public class CheckWincasa {

    @FunctionName("CheckWincasa")
    public String run (
            @TimerTrigger(name = "dailyPulseTrigger", schedule = "0 30 8 * * 1-5") String timer, //To trigger at 8:30 AM UTC every weekday
            ExecutionContext context) {

        String res = "CheckWincasa: before execution";

        try {

            Document doc = null;
            doc = Jsoup.connect("https://264.hci-is24.ch/public/hci/list?s=2&t=1&l=7481&r=0&pa=Beliebig&wl=264").get();
            res = doc.select("h1").text().split(" ")[0];

        } catch (Exception e) {
            res = "CheckWincasa: Exception caught: " + e.toString();
        }

        if (!"0".equals(res)) {
            String connectionString = "endpoint=https://rs-az-comm-service.switzerland.communication.azure.com/;accesskey=XXX";
            EmailClient emailClient = new EmailClientBuilder().connectionString(connectionString).buildClient();

            EmailAddress toAddress = new EmailAddress("lewap02@gmail.com");

            EmailMessage emailMessage = new EmailMessage()
                .setSenderAddress("DoNotReply@b93b0176-893c-41da-893d-b1042b3f9162.azurecomm.net")
                .setToRecipients(toAddress)
                .setSubject("CheckWincasa")
                .setBodyPlainText("Result: " + ((res == null || res.replaceAll("\\s","").length() == 0)?"empty":res));

            SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(emailMessage, null);
            PollResponse<EmailSendResult> result = poller.waitForCompletion();
        }

        return res;

    }

}
