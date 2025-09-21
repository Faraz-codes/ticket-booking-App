package ticket.booking;

import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.services.UserBookingService;
import ticket.booking.util.UserServiceUtil;

import java.io.IOException;
import java.util.*;

public class App {

    public String getGreeting() {
        return "Hello from App!";
    }

    public static void main(String[] args) {
        System.out.println("Running Train Booking System");
        Scanner scanner = new Scanner(System.in);
        int option = 0;
        UserBookingService userBookingService;
        Train trainSelectedForBooking = null; // initialize here

        try {
            userBookingService = new UserBookingService();
        } catch (IOException e) {
            System.out.println("Error initializing the service: " + e.getMessage());
            return;
        }

        while (option != 7) {
            System.out.println("\nChoose option:");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel my Booking");
            System.out.println("7. Exit the App");

            // Safe integer input
            if (scanner.hasNextInt()) {
                option = scanner.nextInt();
                scanner.nextLine(); // consume leftover newline
            } else {
                System.out.println("Please enter a valid number between 1-7.");
                scanner.nextLine(); // discard invalid input
                continue;
            }

            switch (option) {
                case 1 -> { // Sign up
                    System.out.println("Enter username to signup:");
                    String nameToSignUp = scanner.nextLine().trim();
                    System.out.println("Enter password to signup:");
                    String passwordToSignUp = scanner.nextLine().trim();
                    User userToSignup = new User(nameToSignUp, passwordToSignUp,
                            UserServiceUtil.hashPassword(passwordToSignUp),
                            new ArrayList<>(), UUID.randomUUID().toString());
                    userBookingService.signUp(userToSignup);
                }

                case 2 -> { // Login
                    System.out.println("Enter username to login:");
                    String nameToLogin = scanner.nextLine().trim();
                    System.out.println("Enter password to login:");
                    String passwordToLogin = scanner.nextLine().trim();
                    User tempUser = new User(nameToLogin, passwordToLogin, null, null, null);

                    try {
                        UserBookingService loginService = new UserBookingService(tempUser);
                        if (loginService.loginUser()) {
                            loginService.getUserByName(nameToLogin).ifPresent(u -> {
                                loginService.setUser(u);
                                System.out.println("Login successful! Welcome back " + u.getName());
                            });
                            userBookingService = loginService; // set main service after login
                        } else {
                            System.out.println("Invalid username or password!");
                        }
                    } catch (IOException e) {
                        System.out.println("Error while logging in: " + e.getMessage());
                    }
                }

                case 3 -> userBookingService.fetchBookings();

                case 4 -> { // Search trains
                    System.out.println("Enter source station:");
                    String source = scanner.nextLine().trim();
                    System.out.println("Enter destination station:");
                    String dest = scanner.nextLine().trim();
                    List<Train> trains = userBookingService.getTrains(source, dest);

                    if (trains.isEmpty()) {
                        System.out.println("No trains found for " + source + " → " + dest);
                        break;
                    }

                    for (int i = 0; i < trains.size(); i++) {
                        Train t = trains.get(i);
                        System.out.println((i + 1) + ". Train ID: " + t.getTrainId());
                        t.getStationTimes().forEach((station, time) ->
                                System.out.println("Station: " + station + ", Time: " + time));
                    }

                    System.out.println("Select a train by number:");
                    int trainIndex = scanner.nextInt() - 1; // list index starts at 0
                    scanner.nextLine(); // consume leftover newline

                    if (trainIndex >= 0 && trainIndex < trains.size()) {
                        trainSelectedForBooking = trains.get(trainIndex);
                        System.out.println("Train selected: " + trainSelectedForBooking.getTrainId());
                    } else {
                        System.out.println("Invalid selection. Please choose a correct train number.");
                    }
                }

                case 5 -> { // Book a seat
                    if (trainSelectedForBooking == null) {
                        System.out.println("Please select a train first (Option 4).");
                        break;
                    }

                    List<List<Integer>> seats = userBookingService.fetchSeats(trainSelectedForBooking);
                    System.out.println("Available seats (0 = free, 1 = booked):");
                    for (int r = 0; r < seats.size(); r++) {
                        for (int c = 0; c < seats.get(r).size(); c++) {
                            System.out.print(seats.get(r).get(c) + " ");
                        }
                        System.out.println();
                    }

                    System.out.println("Enter row number to book:");
                    int row = scanner.nextInt();
                    System.out.println("Enter column number to book:");
                    int col = scanner.nextInt();
                    scanner.nextLine(); // consume leftover newline

                    boolean booked = userBookingService.bookTrainSeat(trainSelectedForBooking, row, col);
                    System.out.println(booked ? "Seat booked successfully!" : "Cannot book this seat.");
                }

                case 6 -> { // Cancel booking
                    System.out.println("Enter Ticket ID to cancel:");
                    String ticketId = scanner.nextLine().trim();
                    boolean canceled = userBookingService.cancelBooking(ticketId);
                    System.out.println(canceled ? "Booking canceled successfully!" : "No ticket found with ID " + ticketId);
                }

                case 7 -> System.out.println("Exiting. Goodbye!");

                default -> System.out.println("Invalid option. Enter 1-7.");
            }
        }

        scanner.close();
    }

}
