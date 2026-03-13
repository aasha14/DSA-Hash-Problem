import java.util.*;

// Trie node for prefix matching
class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfQuery = false;
    String query = null; // store full query at end node
}

// Autocomplete system
public class AutocompleteSystem {
    private final TrieNode root = new TrieNode();
    private final Map<String, Integer> frequencyMap = new HashMap<>();

    // Insert query into Trie
    private void insert(String query) {
        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEndOfQuery = true;
        node.query = query;
    }

    // Update frequency of a query (increment or add)
    public void updateFrequency(String query) {
        frequencyMap.put(query, frequencyMap.getOrDefault(query, 0) + 1);
        insert(query);
    }

    // Get top K suggestions for a prefix
    public List<String> search(String prefix, int k) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            node = node.children.get(c);
            if (node == null) return Collections.emptyList();
        }

        PriorityQueue<Map.Entry<String, Integer>> minHeap = new PriorityQueue<>(
                Comparator.comparingInt(Map.Entry::getValue)
        );

        collectAllQueries(node, minHeap, k);

        List<String> result = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            result.add(0, minHeap.poll().getKey()); // reverse order to get highest frequency first
        }
        return result;
    }

    // DFS to collect queries under this node into min-heap
    private void collectAllQueries(TrieNode node, PriorityQueue<Map.Entry<String, Integer>> heap, int k) {
        if (node.isEndOfQuery && node.query != null) {
            Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>(node.query, frequencyMap.get(node.query));
            if (heap.size() < k) {
                heap.offer(entry);
            } else if (entry.getValue() > heap.peek().getValue()) {
                heap.poll();
                heap.offer(entry);
            }
        }
        for (TrieNode child : node.children.values()) {
            collectAllQueries(child, heap, k);
        }
    }

    public static void main(String[] args) {
        AutocompleteSystem auto = new AutocompleteSystem();

        // Add queries with frequencies
        auto.updateFrequency("java tutorial");
        auto.updateFrequency("javascript tutorial");
        auto.updateFrequency("java download");
        auto.updateFrequency("java tutorial"); // frequency increases

        // Search for prefix "jav"
        List<String> suggestions = auto.search("jav", 10);
        System.out.println("Suggestions for 'jav': " + suggestions);

        // Show current frequencies
        System.out.println("Frequency Map: " + auto.frequencyMap);
    }
}