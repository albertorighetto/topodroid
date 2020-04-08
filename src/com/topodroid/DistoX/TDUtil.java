/* @file TDUtil.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @grief numerical utilities
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;

import android.util.Log;

// import java.lang.Math;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Calendar;
import java.util.GregorianCalendar;

import java.util.List;
import java.util.Comparator;
import java.util.Collections;

import java.io.File;

import android.media.AudioManager;
import android.media.ToneGenerator;

import android.content.Context;
import android.os.Vibrator;

public class TDUtil
{
  public static final long ZERO = 32768;
  public static final long NEG  = 65536;
  public static final float FV = 24000.0f;
  public static final float FM = 16384.0f; // 2^14
  public static final float FN = 2796f;    // 2^26 / FV

  public static final float DEG2GRAD = 400.0f/360.0f;
  public static final float GRAD2DEG = 360.0f/400.0f;

  public static final int DDMMSS = 0;
  public static final int DEGREE = 1;

  public static final float M2FT = 3.28084f; // meters to feet 
  public static final float FT2M = 0.3048f;
  public static final float IN2M = 0.0254f;
  public static final float YD2M = 0.9144f;

  // DIRS and FILES -------------------------------------------------------------

  public static void deleteFile( File f ) // DistoX-SAF
  {
    if ( f != null && f.exists() ) {
      if ( ! f.delete() ) TDLog.Error("file delete failed " + f.getName() );
    }
  }

  public static void deleteDir( File dir ) // DistoX-SAF
  {
    if ( dir != null && dir.exists() ) {
      File[] files = dir.listFiles();
      if ( files != null ) {
        for ( File file : files ) {
          if (file.isFile()) {
            if ( ! file.delete() ) TDLog.Error("file delete failed " + file.getName() ); 
          }
        }
      }
      if ( ! dir.delete() ) TDLog.Error("dir delete failed " + dir.getName() );
    }
  }

  public static void deleteFile( String pathname ) 
  { 
    deleteFile( new File( pathname ) ); // DistoX-SAF
  }

  public static void deleteDir( String dirname ) 
  { 
    deleteDir( new File( dirname ) ); // DistoX-SAF
  }

  // @pre oldname exists && ! newname exists
  public static void renameFile( String oldname, String newname )
  {
    File f1 = new File( oldname ); // DistoX-SAF
    File f2 = new File( newname );
    if ( f1.exists() && ! f2.exists() ) {
      if ( ! f1.renameTo( f2 ) ) TDLog.Error("file rename: failed " + oldname + " to " + newname );
    } else {
      TDLog.Error("file rename: no-exist " + oldname + " or exist " + newname );
    }
  }

  // @pre oldname exists
  public static void moveFile( String oldname, String newname )
  {
    File f1 = new File( oldname ); // DistoX-SAF
    File f2 = new File( newname );
    if ( f1.exists() ) {
      if ( ! f1.renameTo( f2 ) ) TDLog.Error("file move: failed " + oldname + " to " + newname );
    } else {
      TDLog.Error("file move: no-exist " + oldname );
    }
  }

  public static void makeDir( String pathname )
  {
    File f = new File( pathname ); // DistoX-SAF
    if ( f.exists() ) return;
    if ( ! f.isDirectory() ) {
      if ( ! f.mkdirs() ) TDLog.Error("Mkdir failed " + pathname );
    }
  }

  // STRINGS --------------------------------------------------------------
  
  // concatenate strings using a single-space separator
  // empty strings are skipped
  public static String concat( String[] vals, int k )
  {
    if ( k < vals.length ) {
      StringBuilder sb = new StringBuilder();
      for ( ; k<vals.length; ++k ) if ( vals[k].length() > 0 ) {
        sb.append(vals[k]);
        break;
      }
      for (++k; k < vals.length; ++k) {
        if ( vals[k].length() > 0 ) sb.append(" ").append(vals[k]);
      }
      return sb.toString();
    }
    return "";
  }

  public static String noSpaces( String s )
  {
    return ( s == null )? null 
      : s.trim().replaceAll("\\s+", "_").replaceAll("/", "-").replaceAll("\\*", "+").replaceAll("\\\\", "");
  }

  public static String dropSpaces( String s )
  {
    return ( s == null )? null 
      : s.trim().replaceAll("\\s+", "");
  }

  // sort strings by name (alphabetical order)
  public static void sortStringList( List< String > list )
  {
    if ( list.size() <= 1 ) return;
    Comparator<String> cmp = new Comparator<String>() 
    {
      @Override
      public int compare( String s1, String s2 ) { return s1.compareToIgnoreCase( s2 ); }
    };
    Collections.sort( list, cmp );
  }

  // DATE and TIME -------------------------------------------------------------

  public static String currentDate()
  {
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
    return sdf.format( new Date() );
  }

  public static String currentDateTime()
  {
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd-hh:mm", Locale.US );
    return sdf.format( new Date() );
  }


  public static String getDateString( String format )
  {
    SimpleDateFormat sdf = new SimpleDateFormat( format, Locale.US );
    return sdf.format( new Date() );
  }

  public static int parseDay( String str )
  {
    return 10 * ( str.charAt(0) - '0' ) + ( str.charAt(1) - '0' );
  }
  public static int parseMonth( String str )
  {
    return 10 * ( str.charAt(0) - '0' ) + ( str.charAt(1) - '0' );
  }

  public static float getDatePlg( ) // Polygog style date
  {
    Calendar c = new GregorianCalendar();
    int y = c.get( Calendar.YEAR );
    int m = 1 + c.get( Calendar.MONTH );
    int d = c.get( Calendar.DAY_OF_MONTH );
    return getDatePlg( y, m, d );
  }

  private static final int[] mDaysByMonth = { 0, 31, 59, 90, 120, 151, 181,  212, 243, 273, 304, 324, 365 };
  // m: 1 .. 12
  public static float getDatePlg( int y, int m, int d )
  {
    int days = 36524; // 100 * 365 + 24 = from 1900.01.01 to 1999.12.31
    boolean leap = ( (y%4) == 0 ); 
    while ( y > 2000 ) {
      days += 365;
      if ( (y % 4) == 0 ) ++ days;
      -- y;
    }
    days += mDaysByMonth[ m-1 ] + d + 1; //
    if ( leap && m > 2 ) ++ days;
    // Log.v("DistoX-DATE", "Polygon date " + y + " " + m + " " + d +  " " + days );
    return days;
  }


  public static int dateParseYear( String date )
  {
    try {
      return Integer.parseInt( date.substring(0, 4) );
    } catch ( NumberFormatException e ) { }
    return 2000;
  }

  public static int dateParseMonth( String date )
  {
    int ret = 0;
    if ( date.charAt(5) == '1' ) ret += 10;
    char ch = date.charAt(6);
    if ( ch >= '0' && ch <= '9' ) ret += (ch - '0');
    return (ret > 0)? ret-1 : 0;
  }

  public static int dateParseDay( String date )
  {
    int ret = 0;
    char ch = date.charAt(8);
    if ( ch >= '1' && ch <= '3' ) ret += 10*(ch - '0');
    ch = date.charAt(9);
    if ( ch >= '1' && ch <= '9' ) ret += (ch - '0');
    return Math.max(ret, 0);
  }

  public static String composeDate( int y, int m, int d )
  {
    return String.format(Locale.US, "%04d.%02d.%02d", y, m+1, d );
  }

  public static int year()  { return (new GregorianCalendar()).get( Calendar.YEAR ); }
  public static int month() { return (new GregorianCalendar()).get( Calendar.MONTH ); }
  public static int day()   { return (new GregorianCalendar()).get( Calendar.DAY_OF_MONTH); }

  public static String getAge( long age )
  {
    age /= 60000;
    if ( age < 120 ) return Long.toString(age) + "\'";
    age /= 60;
    if ( age < 24 ) return Long.toString(age) + "h";
    age /= 24;
    if ( age < 60 ) return Long.toString(age) + "d";
    age /= 30;
    if ( age < 24 ) return Long.toString(age) + "m";
    age /= 12;
    return Long.toString(age) + "y";
  }

  // SLOW ----------------------------------------------------

  public static boolean slowDown( int msec ) 
  {
    try {
      Thread.sleep( msec );
    } catch ( InterruptedException e ) { return false; }
    return true;
  }

  public static boolean slowDown( int msec, String msg )
  {
    try {
      Thread.sleep( msec );
    } catch ( InterruptedException e ) {
      TDLog.Error( msg + " " + e.getMessage() );
      return false;
    }
    return true;
  }

  public static boolean yieldDown( int msec ) 
  {
    try {
      Thread.yield();
      Thread.sleep( msec );
    } catch ( InterruptedException e ) { return false; }
    return true;
  }

  // HEPTIC FEEDBACK ------------------------------------------

  public static void ringTheBell( int duration )
  {
    // Log.v("DistoXX", "bell ...");
    // ToneGenerator toneG = new ToneGenerator( AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME );
    ToneGenerator toneG = new ToneGenerator( AudioManager.STREAM_ALARM, TDSetting.mBeepVolume );
    // for ( int i=0; i<2; ++i ) {
      toneG.startTone( ToneGenerator.TONE_PROP_PROMPT, duration ); 
      // TDUtil.slowDown( duration );
    // }
  }

  public static void vibrate( Context ctx, int duration )
  {
    Vibrator vibrator = (Vibrator)ctx.getSystemService( Context.VIBRATOR_SERVICE );
    try {
      vibrator.vibrate(duration);
    } catch ( NullPointerException e ) {
      // TODO
    }
  }

}
