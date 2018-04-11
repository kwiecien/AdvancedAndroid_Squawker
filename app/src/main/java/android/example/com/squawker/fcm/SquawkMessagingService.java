package android.example.com.squawker.fcm;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class SquawkMessagingService extends FirebaseMessagingService {

    private static final String KEY_AUTHOR = "author";
    private static final String KEY_AUTHOR_KEY = "authorKey";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_DATE = "date";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        if (data != null && !data.isEmpty()) {
            sendNotification(data);
            insertSquawk(data);
        }
    }

    private void insertSquawk(final Map<String, String> data) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues message = new ContentValues();
                message.put(SquawkContract.COLUMN_AUTHOR, data.get(KEY_AUTHOR));
                message.put(SquawkContract.COLUMN_AUTHOR_KEY, data.get(KEY_AUTHOR_KEY));
                message.put(SquawkContract.COLUMN_MESSAGE, data.get(KEY_MESSAGE));
                message.put(SquawkContract.COLUMN_DATE, data.get(KEY_DATE));
                getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI, message);
                return null;
            }
        }.execute();
    }

    private void sendNotification(Map<String, String> data) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(String.format(getString(R.string.notification_message), data.get(KEY_AUTHOR)))
                .setContentText(data.get(KEY_MESSAGE).substring(0, 30) + "\u2026")
                .setSmallIcon(R.drawable.ic_duck)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        NotificationManagerCompat.from(this).notify(123, builder.build());
    }
}
