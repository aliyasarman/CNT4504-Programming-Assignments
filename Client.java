import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Client {
    public static final String[] commands = {
            "date and time",
            "uptime",
            "memory use",
            "netstat",
            "current users",
            "running processes"
    };

    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter the Network Address");
        String networkAddress = in.nextLine();
        System.out.println("Enter the port number.");
        int port = in.nextInt();
        int numberOfRequests = 0;

        while (true) {
            System.out.println("Please select on of the following options:\n" +
                    "1: Date and Time\n" +
                    "2: Uptime\n" +
                    "3: Memory Use\n" +
                    "4: Netstat\n" +
                    "5: Current Users\n" +
                    "6: Running Processes\n" +
                    "0: Exit program");
            int selection = in.nextInt();
            if (selection < 0 || selection > 6) {
                System.out.println("Invalid input: Please try again.");
                continue;
            }
            if (selection == 0) {
                break;
            }
            System.out.println("How many client requests will be generated? Choose between 1, 5, 10, 15, 20 25, and 100");
            numberOfRequests = in.nextInt();
            List<Integer> acceptedVals = Arrays.asList(1, 5, 10, 15, 20, 25, 100);
            if (!acceptedVals.contains(numberOfRequests)) {
                System.out.println("Invalid amount of client requests. Choose between 1, 5, 10, 15, 20, 25, and 100");
                continue;
            }

            ExecutorService executor = Executors.newFixedThreadPool(numberOfRequests);
            CompletionService<Long> completionService = new ExecutorCompletionService<>(executor);
            long startTime = System.nanoTime();
            for (int i = 0; i < numberOfRequests; i++) {
                completionService.submit(() -> {
                    // System.out.println("About to get response");
                    try (Socket socket = new Socket(networkAddress, port)) {
                        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        writer.println(commands[selection-1]);
                        StringBuilder sb = new StringBuilder();
                        String response;
                        while ((response = reader.readLine()) != null) {
                            sb.append(response).append("\n");
                        }
                      System.out.println(sb);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    long endTime = System.nanoTime();
                    return endTime - startTime;
                });
                // System.out.println("Submitting " + i);
            }

            executor.shutdown();
            //executor.awaitTermination(5000,TimeUnit.MILLISECONDS);

            try {
                long totalTurnAroundTime = 0;
                for (int i = 0; i < numberOfRequests; i++) {
                    Future<Long> future = completionService.take();
                    long turnAroundTime = future.get();
                    totalTurnAroundTime += turnAroundTime;
                    System.out.println("Request " + (i + 1) + " turn-around time: " + TimeUnit.NANOSECONDS.toMillis(turnAroundTime) + " ms");
                }
                System.out.println();
                System.out.println("Total turn-around time: " + TimeUnit.NANOSECONDS.toMillis(totalTurnAroundTime) + " ms");
                System.out.println("Average turn-around time: " + (TimeUnit.NANOSECONDS.toMillis(totalTurnAroundTime / numberOfRequests) + " ms"));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

        }
    }
}
