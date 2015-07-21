package edu.buffalo.cse.cse486_586.simpledht;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SimpleDHTActivity extends Activity {
/** Called when the activity is first created. */
	
	Button test,dump,join;
	TextView tv;
	EditText text01,text02;
	String clientmsg="";
	ListView msglist;
	public String ipAddress = "10.0.2.2";
	public Socket socket,socket1,socket2,socket3,socket4;
	public int redirected_port;
	public static final int server_port=10000;
	ServerSocket serversocket = null;
	Socket client_seq_socket,clientSocket;
	ServerSocket sequence_socket;
	static int sequence_id;
	public Thread connect1;
	private int actualPort;
	public String hashport;
	hashfun hash1=new hashfun();
	simpledht_provider simple=new simpledht_provider();
	
	protected static final int MSG_ID = 0x1337;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        test=(Button) findViewById(R.id.button1);
        
        dump=(Button) findViewById(R.id.button2);
        join= (Button) findViewById(R.id.button3);
        tv=(TextView) findViewById(R.id.TextView01);
        text01= (EditText) findViewById(R.id.editText1);
        text01.setText("Active-nodes: ");
        
       
       
        
        TelephonyManager tel =
        	    (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        	String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        	final int client_port=Integer.parseInt(portStr);
        	 actualPort=client_port;
        	 
        	 
        	
         	
         	/*if(client_port!=5554)
         	{
        	client_fun(client_port);
        	Log.v("Client_port","     "+client_port);
        	multicast_fun(client_port,false);
         	}*/
        
	               
	               
                  
	        
	        
	        
	        //start of test.setOnClickListener
        		test.setOnClickListener(new OnClickListener() {
        		
        		public void onClick(View v) {
        			try
        			{
        		//getContentResolver().delete(simpledht_pad.simpledht_tab.CONTENT_URI, null, null);
	        	 for(int i=0;i<10;i++)
	        		 
	        	 	{	
	        		 //String val=Integer.toString(i);
	        	 	 //String hash_val=hash1.genHash(val);
	        		 String st=i+":Test"+i;
	        		 String[] cl = st.split(":");
	        		 ContentValues values = new ContentValues();
	        		 Log.d("Test_tag1",cl[0]);
	        		 Log.d("Test_tag2",cl[1]);   
                     values.put(simpledht_pad.simpledht_tab.COLUMN_1, cl[0]);
                     values.put(simpledht_pad.simpledht_tab.COLUMN_2, cl[1]);
                     getContentResolver().insert(simpledht_pad.simpledht_tab.CONTENT_URI, values);
                     Thread.sleep(1000);
	        		 }
	        	 
	        	 
	        	 
	        	 
	        	 
        			}
	        		 catch(Exception e)
	        		 {
	        			 e.printStackTrace();
	        			 Log.v("Test_button_tag",e.toString());
	        		 }
	        	 
	         	}
	         
        		});//End of set onclick listener
    
    	
        		join.setOnClickListener(new OnClickListener() {
                	public void onClick(View v) {	
                		try
                		{
                		for(int i=0;i<10;i++)
       	        		 
    	        	 	{	
    	        	 
                    String st=Integer.toString(i);
                  Cursor cursor=    getContentResolver().query(simpledht_pad.simpledht_tab.CONTENT_URI, null,String.valueOf(i),null,null);
                      if (cursor.moveToFirst()) {
                          do {
                            String temp = "Key "+cursor.getString(0)+" Value "+cursor.getString(1);
                            Log.d("Integer_tag", st);
                            if (st.compareTo(cursor.getString(1))==0)
                            {
                              Log.v("Querying... in cursor",temp);
                            tv.setText(tv.getText().toString()+"\n"+cursor.getString(0)+":"+cursor.getString(1));
                            }
                          } while (cursor.moveToNext());
                        }
                      cursor.close();
                      Thread.sleep(1000);
                      
    	        		 }
                		
                	}
                		catch(Exception e)
   	        		 {
   	        			 e.printStackTrace();
   	        			 Log.v("Test_button_tag",e.toString());
   	        		 }
                	}
        		
        		});
        	
        	
        	//start of dump.setOnClickListener
        	dump.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		tv.setText("");
        		Cursor cursor = getContentResolver().query(simpledht_pad.simpledht_tab.CONTENT_URI,null,null,null,null);
                if (cursor.moveToFirst()) {
                    do {
                      String temp = "Key "+cursor.getString(0)+" Value "+cursor.getString(1);
                      
                      Log.v("Querying...",temp);
                      tv.setText(tv.getText().toString()+"\n"+cursor.getString(0)+":"+cursor.getString(1));
                    } while (cursor.moveToNext());
                  }
                cursor.close();
         
        	}
        	});//End of set onclick listener

	               
	               
	             
    }//End of void onCreate
    
   
   
        
        //start of handler
        Handler myUpdateHandler = new Handler() {
            public void handleMessage(Message message) {
                    switch (message.what) {
                    case MSG_ID:
                    	//EditText text2= (EditText) findViewById(R.id.editText2);
                    	EditText text1= (EditText) findViewById(R.id.editText1);
                    		text1.append(clientmsg);
                            //text2.setText(clientmsg);
                            //String[] cl = clientmsg.split(":");
                            //ContentValues values = new ContentValues();
                    
                            //values.put(multicast_pad.multicast_tab.COLUMN_1, cl[1]);
                           // values.put(multicast_pad.multicast_tab.COLUMN_2, clientmsg);
                            //getContentResolver().insert(multicast_pad.multicast_tab.CONTENT_URI, values);
                            //text2.append(clientmsg);
                            break;
                    default:
                            break;
                    }
                    super.handleMessage(message);
            }
       };
        //end of handler
        
        
        
 
    
}