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
import java.util.HashMap;



import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

public class simpledht_provider extends ContentProvider{
	protected static final int MSG_ID = 0x1337;
	public static Socket socket,socket1;
	public static final int server_port=10000;
	public static String ipAddress = "10.0.2.2";
	static String clientmsg="";
	public int previous_node,successor_node;
	
	ServerSocket serversocket = null;
	hashfun hash1=new hashfun();
	static String hashport;
	static int actualPort;
	static String s_hash,p_hash;
	
	public Integer flag=0;
	public Integer flag2=0;
	public MatrixCursor c = null;
	
	
	// Used for debugging and logging
    private static final String TAG = "SimpleDHTProvider";
    
    public String senderPort ="";

    /**
     * The database that the provider uses as its underlying data store
     */
    private static final String DATABASE_NAME = "simpletab10.db";

    /**
     * The database version
     */
    private static final int DATABASE_VERSION = 2;
    
    private int pl=0;

    /**
     * A projection map used to select columns from the database
     */
    private static HashMap<String, String> tableprojection = new HashMap<String, String>();


    /*
     * Constants used by the Uri matcher to choose an action based on the pattern
     * of the incoming URI
     */
    // The incoming URI matches the Notes URI pattern
    private static final int MESS = 1;



    /**
     * A UriMatcher instance
     */
    private static final UriMatcher sUriMatcher;

    // Handle to a new DatabaseHelper.
    private DatabaseHelper mOpenHelper;


    /**
     * A block that instantiates and sets static objects
     */
    static {

        /*
         * Creates and initializes the URI matcher
         */
        // Create a new instance
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Add a pattern that routes URIs terminated with "notes" to a NOTES operation
        sUriMatcher.addURI(simpledht_pad.AUTHORITY, "simpledht_table", MESS);
        tableprojection.put("provider_key", "provider_key");
        tableprojection.put("provider_value", "provider_value");
     
      
    }

    /**
    *
    * This class helps open, create, and upgrade the database file. Set to package visibility
    * for testing purposes.
    */
   static class DatabaseHelper extends SQLiteOpenHelper {

       DatabaseHelper(Context context) {

           // calls the super constructor, requesting the default cursor factory.
           super(context, DATABASE_NAME, null, DATABASE_VERSION);
       }

       /**
        *
        * Creates the underlying database with table name and column names taken from the
        * NotePad class.
        */
       @Override
       public void onCreate(SQLiteDatabase db) {
           db.execSQL("CREATE TABLE " + simpledht_pad.simpledht_tab.TABLE_NAME + " ("
                   + simpledht_pad.simpledht_tab.COLUMN_1+ " TEXT,"
                   + simpledht_pad.simpledht_tab.COLUMN_2+ " TEXT"
                   + ");");   
           
       }

       
       @Override
       public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

           // Logs that the database is being upgraded
           Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                   + newVersion + ", which will destroy all old data");

           // Kills the table and existing data
           db.execSQL("DROP TABLE IF EXISTS notes");

           // Recreates the database with a new version
           onCreate(db);
       }
   }

   /**
    *
    * Initializes the provider by creating a new DatabaseHelper. onCreate() is called
    * automatically when Android creates the provider in response to a resolver request from a
    * client.
    */
   @Override
   public boolean onCreate() {

       // Creates a new helper object. Note that the database itself isn't opened until
       // something tries to access it, and it's only created if it doesn't already exist.
       mOpenHelper = new DatabaseHelper(getContext());
       
       delete(simpledht_pad.simpledht_tab.CONTENT_URI, null, null);
     
       TelephonyManager tel =
       	    (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
       	String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
       	final int client_port=Integer.parseInt(portStr);
       	 actualPort=client_port;
       	 
       	previous_node=actualPort;
    	successor_node=actualPort;
       	 
       //previous_node=5556;
       //successor_node=5556;
    	
    	s_hash=Integer.toString(successor_node);
    	p_hash=Integer.toString(previous_node);
       	 
       	 try {
				hashport=hash1.genHash(portStr);
				Log.d("Hash_port", hashport);
				s_hash=hash1.genHash(s_hash);
				Log.d("Successor_Hash", s_hash);
				p_hash=hash1.genHash(p_hash);
				Log.d("Predecessor_Hash",p_hash);
			} catch (Exception e1) {
				
				e1.printStackTrace();
			}
       	 
       	
       	Thread connect1= new Thread(new serverthread());
        	connect1.start();
        	
        	
        	
        	
        	
      
        if(actualPort!=5554)
        {	
        	client_fun(5554,actualPort,"JOIN","Q","Q");
        	//successor_node=5554;
        	//previous_node=5554;
        		
        }
        	
        	
        	

       // Assumes that any failures will be reported by a thrown exception.
       return true;
   }

   /**
    * This method is called when a client calls
    * {@link android.content.ContentResolver#query(Uri, String[], String, String[], String)}.
    * Queries the database and returns a cursor containing the results.
    *
    * @return A cursor containing the results of the query. The cursor exists but is empty if
    * the query returns no results or an exception occurs.
    * @throws IllegalArgumentException if the incoming URI pattern is invalid.
    */
   @Override
   public synchronized Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
           String sortOrder) {
     //Log.d("Selection_tag", selection.toString());
	   if(selection==null)
	   {
       // Constructs a new query builder and sets its table name
       SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
       qb.setTables(simpledht_pad.simpledht_tab.TABLE_NAME);

       
       
       /**
        * Choose the projection and adjust the "where" clause based on URI pattern-matching.
        */
       switch (sUriMatcher.match(uri)) {
           // If the incoming URI is for notes, chooses the Notes projection
       case MESS:
               qb.setProjectionMap(tableprojection);
               break;


           default:
               // If the URI doesn't match any of the known patterns, throw an exception.
               throw new IllegalArgumentException("Unknown URI " + uri);
       }


    

       // Opens the database object in "read" mode, since no writes need to be done.
       SQLiteDatabase db = mOpenHelper.getReadableDatabase();

       /*
        * Performs the query. If no problems occur trying to read the database, then a Cursor
        * object is returned; otherwise, the cursor variable contains null. If no records were
        * selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
        */
       Cursor c = qb.query(
           db,            // The database to query
           projection,    // The columns to return from the query
           selection,     // The columns for the where clause
           selectionArgs, // The values for the where clause
           null,          // don't group the rows
           null,          // don't filter by row groups
           null        // The sort order
       );

       // Tells the Cursor what URI to watch, so it knows when its source data changes
       c.setNotificationUri(getContext().getContentResolver(), uri);
       return c;
	   }
	   
	   else{
		   Log.d("Not_null", "not null");
		   
		   if(flag2==1)
	        {
	        	flag2=0;
	        
	   		    
	        }
	        else
	        {
	        return	addquery(String.valueOf(actualPort),"sujith",selection);
	        
	        }
	        

	       // Constructs a new query builder and sets its table name
	       SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
	       qb.setTables(simpledht_pad.simpledht_tab.TABLE_NAME);

	       
	       
	       /**
	        * Choose the projection and adjust the "where" clause based on URI pattern-matching.
	        */
	       switch (sUriMatcher.match(uri)) {
	           // If the incoming URI is for notes, chooses the Notes projection
	       case MESS:
	               qb.setProjectionMap(tableprojection);
	               break;


	           default:
	               // If the URI doesn't match any of the known patterns, throw an exception.
	               throw new IllegalArgumentException("Unknown URI " + uri);
	       }


	    

	       // Opens the database object in "read" mode, since no writes need to be done.
	       SQLiteDatabase db = mOpenHelper.getReadableDatabase();

	       /*
	        * Performs the query. If no problems occur trying to read the database, then a Cursor
	        * object is returned; otherwise, the cursor variable contains null. If no records were
	        * selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
	        */
	       String tempVal = selection;
	       selection = null;
	       Cursor c = qb.query(
	           db,            // The database to query
	           projection,    // The columns to return from the query
	           selection,     // The columns for the where clause
	           selectionArgs, // The values for the where clause
	           null,          // don't group the rows
	           null,          // don't filter by row groups
	           null        // The sort order
	       );

	       // Tells the Cursor what URI to watch, so it knows when its source data changes
	       c.setNotificationUri(getContext().getContentResolver(), uri);
	       Log.d("SEnder_port",senderPort+"  "+selection+"  "+actualPort);
	       //Log.d("Actual Port",actualPort.)
	       
	       
	       
	       if(Integer.valueOf(senderPort)!=actualPort){
	    	   //Log.v("SEnder_port","actual port different from sender port "+c.toString());
	    	   //Cursor cursor=    getContentResolver().query(simpledht_pad.simpledht_tab.CONTENT_URI, null,String.valueOf(i),null,null);
               if (c.moveToFirst()) {
                   do {
                     String temp = "Key "+c.getString(0)+" Value "+c.getString(1);
                     if(tempVal.compareTo(c.getString(1))==0)
                     {		Log.v("SEnder_port","actual port different from sender port "+c.toString());
                    	 client_fun(Integer.valueOf(senderPort),actualPort,"QUERYREPLY",c.getString(0),c.getString(1));
                    	 break;
                     }
                     Log.d("Querying...",temp);
                     
                    // tv.setText(tv.getText().toString()+"\n"+cursor.getString(0)+":"+cursor.getString(1));
                   } while (c.moveToNext());
                 }
               c.close();
               return null;
	    	//Log.d("Sender_port_tag", senderPort);
	       //String id= c.getString(c.getColumnIndex(simpledht_pad.simpledht_tab.COLUMN_1));
	       //String id1= c.getString(c.getColumnIndex(simpledht_pad.simpledht_tab.COLUMN_2));
	       //Log.d("id_tag", id);
	       //Log.d("id1_tag", id1);
	       //String id=c.getString(0);
	       //Log.d("id_tag", id);
	       //String id1=c.getString(1);
	       //client_fun(Integer.valueOf(senderPort),actualPort,"QUERYREPLY",id,id1);
	       
	       
	       }
	       
	       
	       return c;
		      
		   
		   
		   
		   
	   }
       
   }



  

   
    @Override
    public synchronized Uri insert(Uri uri, ContentValues initialValues) {

        // Validates the incoming URI. Only the full provider URI is allowed for inserts.
        if (sUriMatcher.match(uri) != MESS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        String insert_text=initialValues.toString();
        Log.d("Insert_text", insert_text);
        
        String input3 = insert_text.substring(15, insert_text.indexOf(" "));
        String input4 = insert_text.substring(insert_text.indexOf("provider_key=")+13,insert_text.length());
        
        String get=Integer.toString(flag);
        Log.d("flag_tag", get);
        if(flag==1)
        {
        	flag=0;
        
   		    
        }
        else
        {
        	addkey(input3,input4);
        	return null;
        }
        
        
        // A map to hold the new record's values.
        ContentValues values;

        // If the incoming values map is not null, uses it for the new values.
        if (initialValues != null) {
            values = new ContentValues(initialValues);

        } 
        else {
            // Otherwise, create a new value map
            values = new ContentValues();
              }

        // Opens the database object in "write" mode.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Performs the insert and returns the ID of the new note.
        long rowId = db.insert(
           simpledht_pad.simpledht_tab
           .TABLE_NAME,        // The table to insert into.
            null,  // A hack, SQLite sets this column value to null
                                             // if values is empty.
            values                           // A map of column names, and the values to insert
                                             // into the columns.
        );
        System.out.println(rowId+"Insert succeeds");
        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            // Creates a URI with the note ID pattern and the new row ID appended to it.
            Uri noteUri = ContentUris.withAppendedId(simpledht_pad.simpledht_tab.CONTENT_URI, rowId);

            // Notifies observers registered against this provider that the data changed.
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        //addkey(input3,input4);
        
        // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
        throw new SQLException("Failed to insert row into " + uri);
        
        
        
        
        
        
    }

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(simpledht_pad.simpledht_tab.TABLE_NAME, selection, selectionArgs);
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
		
	    
	       
	  void client_fun(final int redirected_port, final int client_port,final String app,final String text1,final String text2)
	    {
	    	Log.d("Redirected_port",Integer.toString(redirected_port));
	    	//client thread
		    new Thread( new Runnable() {
				
				//@Override
				public void run(){
					// TODO Auto-generated method stub
			
				int port=redirected_port*2;
				try {
			         InetAddress serverAddr = InetAddress.getByName(ipAddress);
			        
			         Log.d("Server tag2",Integer.toString(port));
			         socket = new Socket(serverAddr,port);
			         Log.v("Connect_tag","     "+socket);
			         
			         String str;
					 if((text1.equalsIgnoreCase("Q"))&&(text2.equalsIgnoreCase("Q")))
							 {
					  str =client_port+":"+app;
					  Log.d("Multicast_tag", str);
							 }
					 else
					 {
						 str =client_port+":"+app+":"+text1+":"+text2;
						  Log.d("Multicast_tag", str); 
					 }
			         
			         PrintWriter out1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
		             out1.println(str);
		             out1.flush();
		             //socket.close();
			         
			      } 
				catch (UnknownHostException e1) {
			         e1.printStackTrace();
			         Log.v("Unknown Host",e1.toString());
			         
			      } 
				catch (IOException e1) {
			         e1.printStackTrace();
			         Log.v("IOException",e1.toString());
			      }
			      catch(Exception e) {
			    	  e.printStackTrace();
			    	 Log.v("CLientTag",e.toString());
			      }
			      
			 
				
			   }	
		}).start();
	}
	
	
	
	
	
	
	
	
	
	
	
	class serverthread extends Thread {
    	
    	public serverthread()
    	{
    		start();
    	}
	    public void run() {
	    	//sequencer seq=new sequencer();
	            Socket sock = null;
	            try {
	            	if(serversocket==null)
	            	{
	                    serversocket = new ServerSocket(server_port);
	                    Log.v("serversocket_tag","     "+serversocket);
	            	}
	                    //socket binding to the port.
	                   
	            } catch (IOException e) {
	                    e.printStackTrace();
	            }
	            //while (!Thread.currentThread().isInterrupted()) {
	            while(true) {
	                    Message m = new Message();
	                    m.what = MSG_ID;
	                    try {   //if(sock==null)
	                            sock = serversocket.accept();
	                            Log.v("Received_connection"," "+sock);
	                           // Log.v("_tag","     "+socket1);
	                            
	                            BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	                            String st = null;
	                            st = reader.readLine();
	                            int count=1;
	                            clientmsg = st;
	                            Log.d("Message_Tag",clientmsg);
	                            if((clientmsg.contains("JOIN")))
	                            {
	                            	String[] cl = clientmsg.split(":");
	                            	Log.d("J_tag1",cl[0]);
	           	        		    Log.d("J_tag2",cl[1]);
	           	        		    compare(cl[0]);
	           	        		    
	                            }
	                            
	                            if((clientmsg.contains("Predecessor")))
	                            {
	                            	String[] cl = clientmsg.split(":");
	                            	Log.d("P_tag1",cl[0]);
	           	        		    Log.d("P_tag2",cl[1]); 
	           	        		    update_predecessor(cl[0]);
	           	        		    
	                            }
	                            
	                            if((clientmsg.contains("Successor")))
	                            {
	                            	String[] cl = clientmsg.split(":");
	                            	Log.d("S_tag1",cl[0]);
	           	        		    Log.d("S_tag2",cl[1]); 
	           	        		    update_successor(cl[0]);
	           	        		    
	                            }
	                            
	                            
	                            if((clientmsg.contains("Forward")))
	                            {
	                            	String[] cl = clientmsg.split(":");
	                            	Log.d("S_tag1",cl[0]);
	           	        		    Log.d("S_tag2",cl[1]); 
	           	        		    Log.d("S_tag3",cl[2]);
	           	        		    Log.d("S_tag4",cl[3]); 
	           	        		    
	           	        		    addkey(cl[2],cl[3]);
	           	        		    
	                            }
	                            
	                            if((clientmsg.contains("Query")))
	                            {
	                            	final String[] cl = clientmsg.split(":");
	                            	
	                            	Log.d("S_tag1",cl[0]);
	           	        		    Log.d("S_tag2",cl[1]); 
	           	        		    Log.d("S_tag3",cl[2]);
	           	        		    Log.d("S_tag4",cl[3]); 
	           	        		    new Thread(new Runnable() {
										
										@Override
										public void run() {
											// TODO Auto-generated method stub
											  addquery(cl[0],cl[2],cl[3]);	
										}
									}).start();
	           	        		  
	           	        		    
	                            }
	                            
	                            if((clientmsg.contains("QUERYREPLY")))
	                            {
	                            	String[] cl = clientmsg.split(":");
	                            	Log.d("S_tag1",cl[0]);
	           	        		    Log.d("S_tag2",cl[1]); 
	           	        		    Log.d("S_tag3",cl[2]);
	           	        		    Log.d("S_tag4",cl[3]); 
	           	        		  if(pl==0){  
	           	        		 String[] columnNames = {simpledht_pad.simpledht_tab.COLUMN_1,simpledht_pad.simpledht_tab.COLUMN_2};   
	           	        		 c = new MatrixCursor(columnNames)  ;
	           	        		 c.addRow(new Object[] {cl[2],cl[3]});
	           	        		 pl=1;
	           	        		  }
	                            }
	                            
	                    } catch (IOException e) 
	                    		{
	                            e.printStackTrace();
	                            Log.v("Serverhread_tag", e.toString());
	                    		}
	                    catch(Exception e1)
	                    {
	                    	e1.printStackTrace();
                            Log.v("Serverhread_tag", e1.toString());	
	                    }
	            }
	    }
	    
	}//end of class serverthread
    	
	
	//function for comparison
	void compare(String newport)
	{
		int recv_port,hash_port1;
		recv_port=Integer.parseInt(newport);
		try
		{
		String hash_port=hash1.genHash(newport);
		//String hashport1=hash1.genHash(hashport);
		if(((hash_port.compareTo(p_hash)>0)&&(hash_port.compareTo(hashport)<=0))||((hashport.compareTo(p_hash)<0)&&((hash_port.compareTo(p_hash)>0)||(hash_port.compareTo(hashport)<=0))))
		{
		client_fun(recv_port,previous_node,"Predecessor","Q","Q");
		
		Thread.sleep(500);
		client_fun(recv_port,actualPort,"Successor","Q","Q");
		
		Thread.sleep(500);
		client_fun(previous_node,recv_port,"Successor","Q","Q");
		
		
		//client_fun(successor_node,recv_port,"Predecessor","Q","Q");
		
		update_predecessor(newport);
		//setnodes(port);
		}
		
		else if((hashport.compareTo(s_hash)==0)&&(hashport.compareTo(p_hash)==0))
		{
			client_fun(recv_port,actualPort,"Predecessor","Q","Q");
			Thread.sleep(500);
			client_fun(recv_port,actualPort,"Successor","Q","Q");
			update_predecessor(newport);
			update_successor(newport);
			
		}
		else
		{
			client_fun(successor_node,recv_port,"JOIN","Q","Q");
		}
		
		
		}
		catch(Exception e)
		{
			Log.d("Compare_tag",e.toString());
			e.printStackTrace();
		}
		
	}	
	
	
	
	void update_predecessor(String port)
	
	{
		
	int port1;
	port1=Integer.parseInt(port);
	previous_node=port1;
	//successor_node=port1;
	String pnode=Integer.toString(previous_node);
	try {
		p_hash=hash1.genHash(pnode);
	} catch (NoSuchAlgorithmException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	//String snode=Integer.toString(successor_node);
	Log.d("Predecessor_tag", pnode);
	//Log.d("Successor_tag", snode);
	}
	
	void update_successor(String port)
	
	{
	int port1;
	port1=Integer.parseInt(port);
	
	successor_node=port1;
	String snode=Integer.toString(successor_node);
	try {
		s_hash=hash1.genHash(snode);
	} catch (NoSuchAlgorithmException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	System.out.println("");
	Log.d("Successor_tag", snode);
	}
	
	
	
	synchronized void addkey(String text1, String text2)
	{
		String hashkey;
	try
	{	
		hashkey=hash1.genHash(text2);
		Log.d("Hash_current_port", hashport);
		Log.d("Hash_key",hashkey);
		Log.d("Predecesor_hash", p_hash);
		if(((hashkey.compareTo(p_hash)>0)&&(hashkey.compareTo(hashport)<=0))||((hashport.compareTo(p_hash)<0)&&((hashkey.compareTo(p_hash)>0)||(hashkey.compareTo(hashport)<=0))))
		{	Log.d("addkey_tag", "Adding text to "+actualPort);
			ContentValues values = new ContentValues();
   		    flag=1;
            values.put(simpledht_pad.simpledht_tab.COLUMN_1, text2);
            values.put(simpledht_pad.simpledht_tab.COLUMN_2, text1);
            insert(simpledht_pad.simpledht_tab.CONTENT_URI, values);
		}
		else
		{
			client_fun(successor_node,actualPort,"Forward",text1,text2);
			
		}
	}
	catch(Exception e)
	{
		Log.d("Compare_tag",e.toString());
		e.printStackTrace();
	}
	}

	
	synchronized Cursor addquery(String text,String text1, String text2)
	{
		String hashkey;
		senderPort =text;
	try
	{	
		hashkey=hash1.genHash(text2);
		Log.d("Hash_current_port", hashport);
		Log.d("Hash_key",hashkey);
		Log.d("Predecesor_hash", p_hash);
		if(((hashkey.compareTo(p_hash)>0)&&(hashkey.compareTo(hashport)<=0))||((hashport.compareTo(p_hash)<0)&&((hashkey.compareTo(p_hash)>0)||(hashkey.compareTo(hashport)<=0))))
		{	Log.d("addkey_tag", "Adding text to "+actualPort);
			
   		    flag2=1;
   		if(Integer.valueOf(text)==actualPort){
   			String sel = simpledht_pad.simpledht_tab.COLUMN_1+"="+text2;
   			return query(simpledht_pad.simpledht_tab.CONTENT_URI,null, sel, null, null);
   		}
   		else{
   			Log.d("Passing_tag", "Inside pass");
   			return query(simpledht_pad.simpledht_tab.CONTENT_URI,null, text2, null, null);
   			
   		}
		
   		 
		}
		else
		{
			
			Log.d("Inside_else", "Inside cursor");
			client_fun(successor_node,Integer.valueOf(text),"Query",text1,text2);
			while(pl==0){}
			pl=0;
			return c;
			
		}
	}
	catch(Exception e)
	{
		Log.d("Compare_tag",e.toString());
		e.printStackTrace();
	}
	return null;
	}
	
	
}
