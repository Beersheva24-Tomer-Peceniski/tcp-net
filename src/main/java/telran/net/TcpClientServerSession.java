package telran.net;

import java.net.*;

import org.json.JSONObject;

import telran.net.exceptions.TooManyFailuresException;

import java.io.*;

public class TcpClientServerSession implements Runnable {
    Protocol protocol;
    Socket socket;
    int responseCounter;

    public TcpClientServerSession(Protocol protocol, Socket socket) {
        this.protocol = protocol;
        this.socket = socket;
    }

    @Override
    public void run() {
        definingSoTimeout(socket);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintStream writer = new PrintStream(socket.getOutputStream())) {
            String request = null;
            while ((request = reader.readLine()) != null) {
                String response = protocol.getResponseWithJSON(request);
                writer.println(response);
                responseCounter(response);
            }
            socket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void definingSoTimeout(Socket socket) {
        try {
            socket.setSoTimeout(10000);
        } catch (SocketException e) {
            System.out.println(e);
        }
    }

    private void responseCounter(String response) throws SocketTimeoutException {
        JSONObject jsonObj = new JSONObject(response);
        String responseCodeString = jsonObj.getString(TcpConfigurationProperties.RESPONSE_CODE_FIELD);
        ResponseCode responseCode = ResponseCode.valueOf(responseCodeString);
        if (responseCode == ResponseCode.OK) {
            responseCounter = 0;
        } else if (responseCounter == 4) {
            throw new TooManyFailuresException();
        } else {
            responseCounter++;
        }
    }

}
