package com.app.signalprotocolimplementation.groupconversation;

public class SenderKeyDistributionModel {
    final String id;
    final String senderKeyDistributionMessage;

    public SenderKeyDistributionModel(String name, String senderKeyDistributionMessage) {
        this.id = name;
        this.senderKeyDistributionMessage = senderKeyDistributionMessage;
    }
}