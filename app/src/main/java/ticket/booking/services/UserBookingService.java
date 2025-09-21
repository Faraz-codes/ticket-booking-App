package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserBookingService {

    private User user;
    private List<User> userList;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String USER_PATH = "/Users/farazjawaid/Desktop/IRCTC/app/src/main/java/ticket/booking/localDB/users.json";

    // Constructors
    public UserBookingService() throws IOException {
        this.userList = loadUsers();
    }

    public UserBookingService(User user) throws IOException {
        this.user = user;
        this.userList = loadUsers();
    }

    // Load users from JSON, create empty file if missing
    private List<User> loadUsers() throws IOException {
        File usersFile = new File(USER_PATH);
        if (!usersFile.exists()) {
            userList = new ArrayList<>();
            saveUsers(); // create empty JSON
            return userList;
        }
        return objectMapper.readValue(usersFile, new TypeReference<List<User>>() {});
    }

    public void saveUsers() throws IOException {
        objectMapper.writeValue(new File(USER_PATH), userList);
    }

    public Optional<User> getUserByName(String name) {
        return userList.stream()
                .filter(u -> u.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    // Login check
    public boolean loginUser() {
        return userList.stream()
                .anyMatch(u -> u.getName().equalsIgnoreCase(user.getName())
                        && UserServiceUtil.checkPassword(user.getPassword(), u.getHashedPassword()));
    }

    // Sign up user
    public boolean signUp(User newUser) {
        try {
            userList.add(newUser);
            saveUsers();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Fetch logged-in user's bookings
    public void fetchBookings() {
        if (user == null) {
            System.out.println("Please login first!");
            return;
        }
        user.printTickets();
    }

    // Cancel a booking
    public boolean cancelBooking(String ticketId) {
        if (user == null) {
            System.out.println("Please login first!");
            return false;
        }
        if (ticketId == null || ticketId.isEmpty()) {
            System.out.println("Ticket ID cannot be empty");
            return false;
        }

        boolean removed = user.getTicketsBooked().removeIf(t -> t.getTicketId().equals(ticketId));
        if (removed) {
            System.out.println("Ticket with ID " + ticketId + " has been cancelled");
        } else {
            System.out.println("No ticket found with ID " + ticketId);
        }
        return removed;
    }

    // Train related methods
    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train) {
        return train.getSeats();
    }

    public boolean bookTrainSeat(Train train, int row, int col) {
        try {
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && col >= 0 && col < seats.get(row).size()) {
                if (seats.get(row).get(col) == 0) {
                    seats.get(row).set(col, 1);
                    train.setSeats(seats);
                    new TrainService().addTrain(train);
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
