import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SingleThreaded {
    public static long startTime = System.currentTimeMillis();

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter the port number."); //port range of 1025-4998
        int port = in.nextInt();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
            // curl http://localhost:7020 OR wget http://localhost:7020

            while (true) {
                //System.out.println("test");
                try (Socket socket = serverSocket.accept()) {
                    System.out.println("New client connected");
                    long processStartTime = System.currentTimeMillis();

                    InputStream input = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    String request = reader.readLine();
                    String response = handleRequest(request);

                    System.out.println(response);

                    OutputStream output = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);
                    writer.write(response);
                    writer.flush();
                    //long processEndTime = System.currentTimeMillis();
                    //System.out.println("Process Request " +request + "time: " + (processEndTime-processStartTime));
                } catch (IOException e) {
                    System.out.println("Client exception: " + e.getMessage());
                    e.printStackTrace();
                }
                // System.out.println("loop again");
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        } catch (SecurityException e) {
            System.out.println("Security exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String handleRequest(String request) {
        if (request == null) {
            return "invalid request";
        }
        switch (request.toLowerCase()) {
            case "date and time":
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            case "uptime":
                return getUpTime();
            case "memory use":
                return getMemoryUse();
            case "netstat":
                return executeCommand("netstat");
            case "current users":
                return executeCommand("who");
            case "running processes":
                return executeCommand("ps -e");
            default:
                return "Invalid request";
        }
    }

    private static String getUpTime() {
        long uptime = System.currentTimeMillis() - startTime;
        return String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(uptime),
                TimeUnit.MILLISECONDS.toSeconds(uptime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(uptime)));
    }

    private static String getMemoryUse() {
        Runtime runtime = Runtime.getRuntime();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        return "Memory usage: " + memory / (1024 * 1024) + " MB";
    }

    private static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.toString();
    }
}