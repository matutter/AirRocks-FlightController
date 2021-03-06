package com.lestherreynoso.airrocksflightcontroller;

import android.os.AsyncTask;
import android.os.Handler;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
* Created by Kpable on 12/23/2014.
*/
public class Server {
    public static final int SEND_PORT = 5001;
    DatagramSocket recvDatagramSocket;
    DatagramSocket sendDatagramSocket;
    Handler updateUIHandler;
    Thread receiveThread;
    String sendMsg = "";
    MessageHandler messageHandler = new MessageHandler();
    public static final int RECEIVE_PORT = 5000;
    InetAddress ipAddress;
    Boolean serverStarted = false;


    public void start(String ipadstring) {
        updateUIHandler = new Handler();
        this.receiveThread = new Thread(new ReceiveRunnable());
        receiveThread.start();

//        if (ipadstring.isEmpty()){        //doesnt get here at all if ip not set
//            ipadstring = "192.168.42.1";
//        }
//        else{
//            ipadstring = "192.168.0.4";
//        }
        try {
            ipAddress = InetAddress.getByName(ipadstring);
        } catch (UnknownHostException e) {
            MainActivity.updateUI(R.id.debugTextView, "Failed to connect to " + ipadstring, "TextView", "append");
            e.printStackTrace();
        }
        serverStarted = true;
    }

    public void stop(){
        receiveThread.interrupt();
        serverStarted = false;
    }

    public void send(String message) {
        sendMsg = message;//.replace("\"","\\\"");
        if (serverStarted){
            new SendMessage().execute(sendMsg);
        }else{
            MainActivity.updateUI(R.id.debugTV, "Server is not started.\n Unable to send: " + message, "TextView", "append");
//            MainActivity.debugTV.append("Server is not started.\n Unable to send: "+message+"\n");
        }

    }

    class ReceiveRunnable implements  Runnable {
        @Override
        public void run() {
            byte[] receivedData = new byte[1024];
            DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
            try {
                recvDatagramSocket = new DatagramSocket(RECEIVE_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (!Thread.currentThread().isInterrupted()){
                try{
                    receivedPacket.setData(new byte[1024]);
                    recvDatagramSocket.receive(receivedPacket);

                    String recievedStringMessage = new String(receivedPacket.getData());
                    recievedStringMessage = recievedStringMessage.trim();
                    messageHandler.read(recievedStringMessage);

                    MainActivity.updateUI(R.id.debugTV, "Recieved: "+ recievedStringMessage, "TextView", "append");
//                    MainActivity.debugTV.append("Recieved: "+ recievedStringMessage);

                }catch (IOException e){
                    e.printStackTrace();
                }

            }
            recvDatagramSocket.close();
        }
    }

    public  class SendMessage extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... params) {
            if(!params[0].isEmpty()){
                byte[] sendData = params[0].getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, SEND_PORT);
                if (ipAddress != null) {
                    try {
                        sendDatagramSocket = new DatagramSocket(SEND_PORT);
                        sendDatagramSocket.send(sendPacket);
                        String sentStringMessage = params[0];
                        sentStringMessage = sentStringMessage.trim();

                        MainActivity.updateUI(R.id.debugTV, "Sent: " + sentStringMessage, "TextView", "append");
//                        MainActivity.debugTV.append("Sent: " + sentStringMessage);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally{
                    sendDatagramSocket.close();
                    }
                }
            }
            return null;
        }
    }
}
