import java.util.*;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

// Transaction class using LocalTime
class Transaction {
    int id;
    double amount;
    String merchant;
    String account;
    LocalTime time;

    public Transaction(int id, double amount, String merchant, String account, String timeStr) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        this.time = LocalTime.parse(timeStr, formatter);
    }

    @Override
    public String toString() {
        return "id:" + id + ", amount:" + amount + ", merchant:" + merchant + ", account:" + account;
    }
}

public class TransactionAnalyzer {

    // Classic Two-Sum: O(n)
    public static List<int[]> findTwoSum(List<Transaction> transactions, double target) {
        Map<Double, Transaction> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();

        for (Transaction t : transactions) {
            double complement = target - t.amount;
            if (map.containsKey(complement)) {
                result.add(new int[]{map.get(complement).id, t.id});
            }
            map.put(t.amount, t);
        }
        return result;
    }

    // Two-Sum within a time window (minutes)
    public static List<int[]> findTwoSumTimeWindow(List<Transaction> transactions, double target, int minutesWindow) {
        Map<Double, List<Transaction>> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();

        for (Transaction t : transactions) {
            double complement = target - t.amount;
            if (map.containsKey(complement)) {
                for (Transaction candidate : map.get(complement)) {
                    long diff = Math.abs(Duration.between(candidate.time, t.time).toMinutes());
                    if (diff <= minutesWindow) {
                        result.add(new int[]{candidate.id, t.id});
                    }
                }
            }
            map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
        }
        return result;
    }

    // K-Sum using recursion + backtracking
    public static List<List<Integer>> findKSum(List<Transaction> transactions, int k, double target) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(transactions, 0, k, target, new ArrayList<>(), result);
        return result;
    }

    private static void backtrack(List<Transaction> transactions, int start, int k, double target,
                                  List<Integer> path, List<List<Integer>> result) {
        if (k == 0 && Math.abs(target) < 0.0001) { // floating point safety
            result.add(new ArrayList<>(path));
            return;
        }
        if (k == 0) return;

        for (int i = start; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            path.add(t.id);
            backtrack(transactions, i + 1, k - 1, target - t.amount, path, result);
            path.remove(path.size() - 1);
        }
    }

    // Duplicate detection: same amount + merchant, different accounts
    public static Map<String, List<String>> detectDuplicates(List<Transaction> transactions) {
        Map<String, List<String>> map = new HashMap<>(); // key: amount|merchant, value: accounts

        for (Transaction t : transactions) {
            String key = t.amount + "|" + t.merchant;
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(t.account);
        }

        Map<String, List<String>> duplicates = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            Set<String> uniqueAccounts = new HashSet<>(entry.getValue());
            if (uniqueAccounts.size() > 1) {
                duplicates.put(entry.getKey(), new ArrayList<>(uniqueAccounts));
            }
        }
        return duplicates;
    }

    public static void main(String[] args) {
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1, 500, "Store A", "acc1", "10:00"),
                new Transaction(2, 300, "Store B", "acc2", "10:15"),
                new Transaction(3, 200, "Store C", "acc3", "10:30"),
                new Transaction(4, 500, "Store A", "acc2", "10:45")
        );

        System.out.println("Two-Sum for 500:");
        System.out.println(findTwoSum(transactions, 500));

        System.out.println("\nTwo-Sum within 30 minutes for 500:");
        System.out.println(findTwoSumTimeWindow(transactions, 500, 30));

        System.out.println("\n3-Sum for 1000:");
        System.out.println(findKSum(transactions, 3, 1000));

        System.out.println("\nDuplicate detection:");
        System.out.println(detectDuplicates(transactions));
    }
}