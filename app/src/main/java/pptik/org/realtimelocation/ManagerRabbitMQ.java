package pptik.org.realtimelocation;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;

public class ManagerRabbitMQ {

    protected Channel mChannel = null;
    protected Connection mConnection;
    private static final String EXCHANGE_NAME = "sabuga.chat";
    private static final String ACTION_STRING_ACTIVITY = "broadcast_event";


    String userName = Constants.MQ_USERNAME;
    String password = Constants.MQ_PASSWORD;
    String virtualHost = Constants.MQ_VIRTUAL_HOST;
    String serverIp = Constants.MQ_HOSTNAME;
    int port = Constants.MQ_PORT;


    protected boolean running;

    private Context context;

    public ManagerRabbitMQ(Context context) {
        this.context = context;
    }

    public void dispose(){

        running = false;

        try {
            if (mConnection!=null)
                mConnection.close();
            if (mChannel != null)
                mChannel.abort();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void connectToRabbitMQ() {


        if (mChannel != null && mChannel.isOpen()){//already declared
            running = true;
        }

        new AsyncTask<Void,Void,Boolean>(){

            @Override
            protected Boolean doInBackground(Void... voids) {

                try{

                    final ConnectionFactory connectionFactory = new ConnectionFactory();
                    connectionFactory.setUsername(userName);
                    connectionFactory.setPassword(password);
                    connectionFactory.setVirtualHost(virtualHost);
                    connectionFactory.setHost(serverIp);
                    connectionFactory.setPort(port);
                    connectionFactory.setAutomaticRecoveryEnabled(true);

                    mConnection = connectionFactory.newConnection();
                    mChannel = mConnection.createChannel();
                    Log.i("Connect To host", "connected");
                    registerChanelHost();



                    return true;

                } catch (Exception e){
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                running = aBoolean;
            }


        }.execute();
    }


    private void registerChanelHost(){

        try{

            mChannel.exchangeDeclare(EXCHANGE_NAME, "fanout", true);

            final String queueName = mChannel.queueDeclare().getQueue();
            mChannel.queueBind(queueName, EXCHANGE_NAME, "");

            Consumer consumer = new DefaultConsumer(mChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) throws IOException {

                    String message = new String(body, "UTF-8");
                    sendBroadcast(message);


                }
            };

            mChannel.basicConsume(queueName, true, consumer);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendBroadcast(String msg) {
        Intent intent = new Intent(ACTION_STRING_ACTIVITY);
        intent.putExtra("message", msg);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
