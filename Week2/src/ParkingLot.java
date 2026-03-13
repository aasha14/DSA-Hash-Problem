import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

// Parking spot status
enum SpotStatus {
    EMPTY, OCCUPIED, DELETED
}

// Parking spot information
class ParkingSpot {
    SpotStatus status = SpotStatus.EMPTY;
    String licensePlate = null;
    LocalDateTime entryTime = null;
    int probes = 0; // number of linear probes to find this spot
}

// Parking lot class
public class ParkingLot {
    private final int capacity;
    private final ParkingSpot[] spots;
    private int totalProbes = 0;
    private int parkedVehicles = 0;

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        this.spots = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) spots[i] = new ParkingSpot();
    }

    // Simple hash function for license plate
    private int hash(String licensePlate) {
        int hash = 0;
        for (char c : licensePlate.toCharArray()) {
            hash = (hash * 31 + c) % capacity;
        }
        return hash;
    }

    // Park a vehicle using linear probing
    public void parkVehicle(String licensePlate) {
        int index = hash(licensePlate);
        int probes = 0;

        while (spots[index].status == SpotStatus.OCCUPIED) {
            index = (index + 1) % capacity;
            probes++;
        }

        spots[index].status = SpotStatus.OCCUPIED;
        spots[index].licensePlate = licensePlate;
        spots[index].entryTime = LocalDateTime.now();
        spots[index].probes = probes;

        totalProbes += probes;
        parkedVehicles++;

        System.out.println("Vehicle " + licensePlate + " assigned spot #" + index + " (" + probes + " probes)");
    }

    // Exit a vehicle
    public void exitVehicle(String licensePlate) {
        for (int i = 0; i < capacity; i++) {
            if (spots[i].status == SpotStatus.OCCUPIED && licensePlate.equals(spots[i].licensePlate)) {
                LocalDateTime exitTime = LocalDateTime.now();
                Duration duration = Duration.between(spots[i].entryTime, exitTime);
                double hours = duration.toMinutes() / 60.0;
                double fee = hours * 5; // $5 per hour

                spots[i].status = SpotStatus.EMPTY;
                spots[i].licensePlate = null;
                spots[i].entryTime = null;
                parkedVehicles--;

                System.out.printf("Vehicle %s exited. Spot #%d freed, Duration: %dh %dm, Fee: $%.2f\n",
                        licensePlate,
                        i,
                        duration.toHours(),
                        duration.toMinutesPart(),
                        fee);
                return;
            }
        }
        System.out.println("Vehicle " + licensePlate + " not found.");
    }

    // Parking statistics
    public void getStatistics() {
        double occupancy = (double) parkedVehicles / capacity * 100;
        double avgProbes = parkedVehicles == 0 ? 0 : (double) totalProbes / parkedVehicles;

        System.out.printf("Occupancy: %.2f%%, Avg Probes: %.2f\n", occupancy, avgProbes);
    }

    // Example main
    public static void main(String[] args) throws InterruptedException {
        ParkingLot lot = new ParkingLot(500);

        lot.parkVehicle("ABC-1234");
        lot.parkVehicle("ABC-1235");
        lot.parkVehicle("XYZ-9999");

        Thread.sleep(2000); // simulate 2 seconds parking

        lot.exitVehicle("ABC-1234");
        lot.getStatistics();
    }
}