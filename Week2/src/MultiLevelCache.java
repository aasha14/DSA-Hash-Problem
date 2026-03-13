import java.util.*;

// Video data placeholder
class VideoData {
    String videoId;
    String content;

    public VideoData(String videoId, String content) {
        this.videoId = videoId;
        this.content = content;
    }
}

// Multi-Level Cache System
public class MultiLevelCache {
    // L1: In-memory cache (10,000), LRU via LinkedHashMap
    private final int L1_CAPACITY = 10000;
    private final LinkedHashMap<String, VideoData> L1Cache = new LinkedHashMap<>(L1_CAPACITY, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
            return size() > L1_CAPACITY;
        }
    };

    // L2: SSD-backed (simulated), capacity 100,000
    private final int L2_CAPACITY = 100000;
    private final LinkedHashMap<String, VideoData> L2Cache = new LinkedHashMap<>(L2_CAPACITY, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
            return size() > L2_CAPACITY;
        }
    };

    // L3: Database (all videos, simulated)
    private final Map<String, VideoData> L3Database = new HashMap<>();

    // Access counts for promotion
    private final Map<String, Integer> accessCountMap = new HashMap<>();
    private final int PROMOTION_THRESHOLD = 3;

    // Hit counters
    private int L1Hits = 0, L2Hits = 0, L3Hits = 0;
    private int totalRequests = 0;

    // Add video to DB
    public void addVideoToDB(VideoData video) {
        L3Database.put(video.videoId, video);
    }

    // Get video with multi-level cache
    public VideoData getVideo(String videoId) {
        totalRequests++;

        // Check L1
        if (L1Cache.containsKey(videoId)) {
            L1Hits++;
            System.out.println("L1 Cache HIT (0.5ms)");
            return L1Cache.get(videoId);
        }

        // Check L2
        if (L2Cache.containsKey(videoId)) {
            L2Hits++;
            System.out.println("L1 Cache MISS");
            System.out.println("L2 Cache HIT (5ms)");

            // Promote to L1 if threshold exceeded
            int count = accessCountMap.getOrDefault(videoId, 0) + 1;
            accessCountMap.put(videoId, count);
            if (count >= PROMOTION_THRESHOLD) {
                L1Cache.put(videoId, L2Cache.get(videoId));
                System.out.println("Promoted to L1");
            }
            return L2Cache.get(videoId);
        }

        // Check L3
        System.out.println("L1 Cache MISS");
        System.out.println("L2 Cache MISS");
        if (L3Database.containsKey(videoId)) {
            L3Hits++;
            System.out.println("L3 Database HIT (150ms)");
            VideoData video = L3Database.get(videoId);
            L2Cache.put(videoId, video);
            accessCountMap.put(videoId, 1);
            return video;
        }

        System.out.println("Video not found in database!");
        return null;
    }

    // Cache statistics
    public void getStatistics() {
        double L1HitRate = totalRequests == 0 ? 0 : (double) L1Hits / totalRequests * 100;
        double L2HitRate = totalRequests == 0 ? 0 : (double) L2Hits / totalRequests * 100;
        double L3HitRate = totalRequests == 0 ? 0 : (double) L3Hits / totalRequests * 100;
        double overallHitRate = L1HitRate + L2HitRate + L3HitRate;

        double avgTime = (L1Hits * 0.5 + L2Hits * 5 + L3Hits * 150) / Math.max(totalRequests, 1);

        System.out.println("\nCache Statistics:");
        System.out.printf("L1: Hit Rate %.2f%%, Avg Time: 0.5ms\n", L1HitRate);
        System.out.printf("L2: Hit Rate %.2f%%, Avg Time: 5ms\n", L2HitRate);
        System.out.printf("L3: Hit Rate %.2f%%, Avg Time: 150ms\n", L3HitRate);
        System.out.printf("Overall: Hit Rate %.2f%%, Avg Time: %.2fms\n", overallHitRate, avgTime);
    }

    // Example usage
    public static void main(String[] args) {
        MultiLevelCache cacheSystem = new MultiLevelCache();

        // Add videos to L3 database
        for (int i = 1; i <= 5; i++) {
            cacheSystem.addVideoToDB(new VideoData("video_" + i, "Content of video " + i));
        }

        // Access some videos
        cacheSystem.getVideo("video_1"); // MISS L1/L2, HIT L3
        cacheSystem.getVideo("video_1"); // MISS L1, HIT L2, access count 2
        cacheSystem.getVideo("video_1"); // Promote to L1
        cacheSystem.getVideo("video_1"); // HIT L1

        cacheSystem.getVideo("video_2"); // L3 HIT
        cacheSystem.getVideo("video_3"); // L3 HIT

        cacheSystem.getStatistics();
    }
}
