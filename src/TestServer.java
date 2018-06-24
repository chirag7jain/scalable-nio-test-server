import Response.ResponseManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestServer {
    private static final int Port = 8080;

    private static JobServer setup(int port) {
        ResponseManager responseManager;
        MessageVerifier messageVerifier;

        messageVerifier = new MessageVerifier();
        responseManager = new ResponseManager();
        responseManager.register(messageVerifier.getClass().getName(), messageVerifier);

        return new JobServer(port, 128, 1000, responseManager);
    }

    private static class ServerThread implements Runnable {
        private JobServer jobServer;

        private ServerThread(JobServer jobServer) {
            this.jobServer = jobServer;
        }

        @Override
        public void run() {
            this.jobServer.startServer();
        }
    }

    public static void main(String args[]) {
        int givenPort = Port;

        if (args.length > 0) {
            String previousKey = args[0];
            for(int i=1;i<args.length;i++){
                switch (previousKey) {
                    case "-port":
                        givenPort = Integer.parseInt(args[i]);
                        break;
                }
                previousKey = args[i];
            }
        }

        JobServer jobServer;
        Thread thread;

        jobServer = setup(givenPort);
        thread = new Thread(new ServerThread(jobServer));
        thread.start();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))){
            String text;
            while ((text = bufferedReader.readLine()) != null) {
                if (text.equals("STOP")) {
                    break;
                }
            }
        }
        catch (IOException e) {
            System.out.println("Error while reading STDIN");
            e.printStackTrace();
        }
        finally {
            jobServer.stopServer();
            try {
                thread.join();
            }
            catch (InterruptedException e) {
                System.out.println("Error while waiting for thread to exit");
                e.printStackTrace();
            }
        }

        System.out.println("WE ARE DONE");
    }

}
