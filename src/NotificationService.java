import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



    public class NotificationService {
        private static NotificationService instance;
        private Map<Integer, List<Notification>> notifications = new HashMap<>();

        private NotificationService() {}

        public static NotificationService getInstance() {
            if (instance == null) {
                instance = new NotificationService();
            }
            return instance;
        }

        public void sendNotification(int userId, String message) {
            System.out.println("Sending notification to user " + userId + ": " + message);
            List<Notification> userNotifications = notifications.computeIfAbsent(userId, k -> new ArrayList<>());
            userNotifications.add(new Notification(userId, message));
        }




        public List<Notification> getUnreadNotifications(int userId) {
            return notifications.getOrDefault(userId, new ArrayList<>())
                    .stream()
                    .filter(notification -> !notification.isRead())
                    .collect(Collectors.toList());
        }
        public void markNotificationAsRead(int notificationId) {
            for (List<Notification> userNotifications : notifications.values()) {
                for (Notification notification : userNotifications) {
                    if (notification.getId() == notificationId) {
                        notification.markAsRead();
                        return;
                    }
                }
            }
        }



    }


