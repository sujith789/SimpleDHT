package edu.buffalo.cse.cse486_586.simpledht;

import android.net.Uri;
import android.provider.BaseColumns;

public class simpledht_pad {
	public static final String AUTHORITY = "edu.buffalo.cse.cse486_586.simpledht.provider";
	
	// This class cannot be instantiated
    private simpledht_pad() {
    }

    /**
     * Notes table contract
     */
    public static final class simpledht_tab implements BaseColumns {

        // This class cannot be instantiated
        private simpledht_tab() {}

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "simpledht_table";
        public static final String COLUMN_1= "provider_key";
        public static final String COLUMN_2= "provider_value";
        /*
         * URI definitions
         */

        /**
         * The scheme part for this provider's URI
         */
      

        
        
        public static final Uri CONTENT_URI =  Uri.parse("content://" + AUTHORITY +"/" +TABLE_NAME);

   

        
    }

}
